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
import cats.implicits.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.connectors.{
  Class2MAReceiptsConnector,
  LiabilitySummaryDetailsConnector,
  NiContributionsAndCreditsConnector
}
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  DataRetrievalServiceError,
  PaginationType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource

import java.time.{Instant, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}

class MaternityAllowanceDataRetrievalService @Inject() (
    class2MAReceiptsConnector: Class2MAReceiptsConnector,
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    liabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector,
    paginationService: PaginationService,
    uuidGenerator: UuidGenerator,
    currentTimeSource: CurrentTimeSource
)(
    implicit ec: ExecutionContext
) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: MAEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultMA] =
    (
      class2MAReceiptsConnector.fetchClass2MAReceipts(
        eligibilityCheckDataRequest.benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber
      ),
      niContributionsAndCreditsConnector.fetchContributionsAndCredits(
        eligibilityCheckDataRequest.benefitType,
        NiContributionsAndCreditsRequest(
          eligibilityCheckDataRequest.nationalInsuranceNumber,
          eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth,
          eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
          eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
        )
      ),
      eligibilityCheckDataRequest.liabilities.searchCategories.map { searchCategory =>
        liabilitySummaryDetailsConnector.fetchLiabilitySummaryDetails(
          eligibilityCheckDataRequest.benefitType,
          eligibilityCheckDataRequest.nationalInsuranceNumber,
          searchCategory,
          eligibilityCheckDataRequest.liabilities.earliestLiabilityStartDate,
          eligibilityCheckDataRequest.liabilities.liabilityStart,
          eligibilityCheckDataRequest.liabilities.liabilityEnd
        )
      }.sequence
    ).parTupled
      .flatMap { case (class2MaReceiptsResult, contributionsAndCreditResult, liabilityResult) =>

        val result = EligibilityCheckDataResultMA(
          class2MaReceiptsResult,
          liabilityResult,
          contributionsAndCreditResult,
          None
        )

        val shouldPaginate: Boolean =
          if (result.allResults.exists(_.isFailure))
            false
          else
            liabilityResult.forall(_.getSuccess.get.callback.isDefined)

        if (shouldPaginate) {
          val liabilityPages = liabilityResult.flatMap {
            case NpsApiResult.FailureResult(apiName, result) => None
            case NpsApiResult.SuccessResult(apiName, result) =>
              Some(PaginationSource(apiName, result.callback.flatMap(_.callbackURL.map(_.value))))
          }
          paginationService
            .addTask(
              MaPageTask(PageTaskId(uuidGenerator.generate), liabilityPages, currentTimeSource.instantNow())
            )
            .map(id => result.copy(nextCursor = Some(PaginationCursor(PaginationType.MA, PageTaskId(id)))))
        } else EitherT.rightT[Future, BenefitEligibilityError](result)
      }
      .leftMap { error =>
        // TODO ADD LOGGING
        DataRetrievalServiceError()
      }

}
