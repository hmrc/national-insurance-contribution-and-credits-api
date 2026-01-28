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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.NotFound
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, OverallResultStatus, OverallResultSummary}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.FailureResult
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.AggregatedData
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.aggregators.{
  AggregationServiceBsp,
  AggregationServiceGysp,
  AggregationServiceMa
}

sealed trait EligibilityCheckDataResult {
  def benefitType: BenefitType
  def allResults: List[ApiResult]

  def overallResultStatus: OverallResultStatus =
    if (allResults.forall(_.isSuccess)) OverallResultStatus.Success
    else if (allResults.forall(_.isFailure)) OverallResultStatus.Failure
    else OverallResultStatus.Partial

  def resultSummary: OverallResultSummary = OverallResultSummary(
    totalCalls = allResults.size,
    successful = allResults.count(_.isSuccess),
    failed = allResults.count(_.isFailure)
  )

}

object EligibilityCheckDataResult {

  implicit val aggregator: ResultAggregator[EligibilityCheckDataResult, AggregatedData] = {
    case result: EligibilityCheckDataResultMA => AggregationServiceMa.aggregator.aggregate(result)
    case _: EligibilityCheckDataResultESA =>
      AggregationServiceMa.aggregator.aggregate(
        EligibilityCheckDataResultMA(
          FailureResult(Class2MAReceipts, NotFound),
          FailureResult(Class2MAReceipts, NotFound),
          List()
        )
      )
    case _: EligibilityCheckDataResultJSA =>
      AggregationServiceMa.aggregator.aggregate(
        EligibilityCheckDataResultMA(
          FailureResult(Class2MAReceipts, NotFound),
          FailureResult(Class2MAReceipts, NotFound),
          List()
        )
      )
    case result: EligibilityCheckDataResultGYSP => AggregationServiceGysp.aggregator.aggregate(result)
    case result: EligibilityCheckDataResultBSP  => AggregationServiceBsp.aggregator.aggregate(result)
  }

  case class EligibilityCheckDataResultMA(
      class2MaReceiptsResult: Class2MaReceiptsResult,
      liabilityResult: LiabilityResult,
      contributionCreditResult: List[ContributionCreditResult]
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType = BenefitType.MA

    val allResults: List[ApiResult] =
      List(class2MaReceiptsResult, liabilityResult) ++ contributionCreditResult

  }

  case class EligibilityCheckDataResultESA() extends EligibilityCheckDataResult {
    val benefitType: BenefitType             = BenefitType.ESA
    override def allResults: List[ApiResult] = ???
  }

  case class EligibilityCheckDataResultJSA() extends EligibilityCheckDataResult {
    val benefitType: BenefitType             = BenefitType.JSA
    override def allResults: List[ApiResult] = ???
  }

  case class EligibilityCheckDataResultGYSP(
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType             = BenefitType.GYSP
    override def allResults: List[ApiResult] = List(marriageDetailsResult)
  }

  case class EligibilityCheckDataResultBSP(
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType             = BenefitType.BSP
    override def allResults: List[ApiResult] = List(marriageDetailsResult)
  }

}
