/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.app.benefitEligibility.service

import cats.data.{EitherT, NonEmptyList}
import cats.instances.future.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.connectors.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  CorrelationId,
  DataRetrievalServiceError,
  PaginationType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultSearchLight
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.SearchlightEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.{
  ContributionAndCreditsPaging,
  PageTaskId,
  PaginationCursor,
  SearchLightPageTask
}
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.app.benefitEligibility.util.{ContributionCreditTaxWindowCalculator, CurrentTimeSource}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SearchlightDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    paginationService: PaginationService,
    uuidGenerator: UuidGenerator,
    currentTimeSource: CurrentTimeSource
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: SearchlightEligibilityCheckDataRequest
  )(
      implicit hc: HeaderCarrier,
      correlationId: CorrelationId
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultSearchLight] = {

    val maybeTaxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
      eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
      eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
    )

    maybeTaxWindows match {
      case Left(error) => EitherT.leftT[Future, EligibilityCheckDataResultSearchLight](error)
      case Right(taxWindows) =>

        niContributionsAndCreditsConnector
          .fetchContributionsAndCredits(
            eligibilityCheckDataRequest.benefitType,
            NiContributionsAndCreditsRequest(
              eligibilityCheckDataRequest.nationalInsuranceNumber,
              eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth,
              taxWindows.head.startTaxYear,
              taxWindows.head.endTaxYear
            ),
            Some(eligibilityCheckDataRequest.system)
          )
          .flatMap { contributionCreditResult =>
            val result = EligibilityCheckDataResultSearchLight(
              eligibilityCheckDataRequest.benefitType,
              contributionCreditResult,
              None
            )

            val shouldPage = if (contributionCreditResult.isSuccess) taxWindows.length > 1 else false

            (PaginationType.from(eligibilityCheckDataRequest.benefitType), shouldPage) match {
              case (Some(paginationType), true) =>

                val niContributionsCreditsPaginate = taxWindows.toList.safeTailNel.map { remainingWindows =>
                  ContributionAndCreditsPaging(
                    remainingWindows,
                    eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth
                  )
                }

                paginationService
                  .addTask(
                    SearchLightPageTask(
                      correlationId,
                      PageTaskId(uuidGenerator.generate),
                      paginationType,
                      contributionAndCreditsPaging = niContributionsCreditsPaginate,
                      eligibilityCheckDataRequest.nationalInsuranceNumber,
                      currentTimeSource.instantNow()
                    )
                  )
                  .map { id =>
                    EligibilityCheckDataResultSearchLight(
                      benefitType = result.benefitType,
                      contributionCreditResult = result.contributionCreditResult,
                      nextCursor = Some(PaginationCursor(paginationType, PageTaskId(id)))
                    )
                  }

              case _ =>
                EitherT
                  .rightT[Future, BenefitEligibilityError](result)
            }
          }
    }

  }
    .leftMap {
      case error @ DataRetrievalServiceError(_) => error
      case error                                => DataRetrievalServiceError(List(error))
    }

}
