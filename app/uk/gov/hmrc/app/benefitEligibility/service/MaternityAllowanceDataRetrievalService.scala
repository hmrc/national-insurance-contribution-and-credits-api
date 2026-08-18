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
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.app.benefitEligibility.connectors.{
  LiabilitySummaryDetailsConnector,
  NiContributionsAndCreditsConnector
}
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  CorrelationId,
  DataRetrievalServiceError
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.app.benefitEligibility.model.request.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class MaternityAllowanceDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    liabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector,
    batchService: BatchService,
    uuidGenerator: UuidGenerator,
    currentTimeSource: CurrentTimeSource
)(
    implicit ec: ExecutionContext
) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: MAEligibilityCheckDataRequest
  )(
      implicit hc: HeaderCarrier,
      correlationId: CorrelationId
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultMA] =
    (
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
      .flatMap { case (contributionsAndCreditResult, liabilityResult) =>

        val result = EligibilityCheckDataResultMA(
          liabilityResult,
          contributionsAndCreditResult,
          None
        )

        val shouldBatch: Boolean =
          if (result.allResults.exists(_.isFailure))
            false
          else
            liabilityResult.exists(_.getSuccess.get.callback.isDefined)

        if (shouldBatch) {
          val liabilityBatchs = liabilityResult.flatMap {
            case NpsApiResult.FailureResult(apiName, result) => None
            case NpsApiResult.SuccessResult(apiName, result) =>
              result.callback.flatMap(_.callbackURL.map(_.value)).map(url => BatchSource(apiName, url))
          }

          val batch = MaBatch(
            liabilityBatchs,
            eligibilityCheckDataRequest.nationalInsuranceNumber
          )
          batchService
            .addTask(
              BatchDocument(
                correlationId,
                BatchId(uuidGenerator.generate),
                Json.toJson(batch).as[JsObject],
                currentTimeSource.instantNow()
              )
            )
            .map(id => result.copy(batchId = Some(BatchId(id))))
        } else EitherT.rightT[Future, BenefitEligibilityError](result)
      }
      .leftMap {
        case error @ DataRetrievalServiceError(_) => error
        case error                                => DataRetrievalServiceError(List(error))
      }

}
