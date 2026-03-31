/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits.catsSyntaxTuple2Parallel
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.connectors.{MarriageDetailsConnector, NiContributionsAndCreditsConnector}
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.MarriageDetails
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  ContributionCreditTaxWindowCalculatorError,
  CorrelationId,
  DataRetrievalServiceError,
  PaginationType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.BSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.{
  BspPageTask,
  ContributionAndCreditsPaging,
  PageTaskId,
  PaginationCursor,
  PaginationSource
}
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.app.benefitEligibility.util.{ContributionCreditTaxWindowCalculator, CurrentTimeSource}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BereavementSupportPaymentDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    marriageDetailsConnector: MarriageDetailsConnector,
    paginationService: PaginationService,
    uuidGenerator: UuidGenerator,
    currentTimeSource: CurrentTimeSource
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: BSPEligibilityCheckDataRequest
  )(
      implicit hc: HeaderCarrier,
      correlationId: CorrelationId
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultBSP] = {

    val maybeTaxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
      eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
      eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
    )

    maybeTaxWindows match {
      case Left(error) => EitherT.leftT[Future, EligibilityCheckDataResultBSP](error)
      case Right(taxWindows) =>
        (
          niContributionsAndCreditsConnector.fetchContributionsAndCredits(
            eligibilityCheckDataRequest.benefitType,
            NiContributionsAndCreditsRequest(
              eligibilityCheckDataRequest.nationalInsuranceNumber,
              eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth,
              taxWindows.head.startTaxYear,
              taxWindows.head.endTaxYear
            )
          ),
          marriageDetailsConnector.fetchMarriageDetails(
            eligibilityCheckDataRequest.nationalInsuranceNumber
          )
        ).parTupled
          .flatMap { case (contributionsAndCreditResult, marriageDetailsResult) =>
            val result = EligibilityCheckDataResultBSP(
              contributionsAndCreditResult,
              marriageDetailsResult,
              None
            )

            val shouldPage =
              if (result.allResults.exists(_.isFailure)) false
              else {
                marriageDetailsResult.getSuccess.get.marriageDetails._links.isDefined || taxWindows.length > 1
              }

            if (shouldPage) {
              val marriageDetailsPaginate: Option[PaginationSource] = marriageDetailsResult.getSuccess.flatMap(
                _.marriageDetails._links.flatMap(_.self.href).map(url => PaginationSource(MarriageDetails, url.value))
              )

              val niContributionsCreditsPaginate = taxWindows.toList.safeTailNel.map { remainingWindows =>
                ContributionAndCreditsPaging(
                  remainingWindows,
                  eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth
                )
              }

              paginationService
                .addTask(
                  BspPageTask(
                    correlationId,
                    PageTaskId(uuidGenerator.generate),
                    marriageDetailsPaging = marriageDetailsPaginate,
                    contributionAndCreditsPaging = niContributionsCreditsPaginate,
                    eligibilityCheckDataRequest.nationalInsuranceNumber,
                    currentTimeSource.instantNow()
                  )
                )
                .map(id =>
                  result.copy(nextCursor = Some(PaginationCursor(PaginationType.BspPagination, PageTaskId(id))))
                )
            } else {
              EitherT
                .rightT[Future, BenefitEligibilityError](result)
            }
          }
    }
  }.leftMap {
    case error @ DataRetrievalServiceError(_) => error
    case error                                => DataRetrievalServiceError(List(error))
  }

}
