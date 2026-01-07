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

import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, OverallResultStatus, OverallResultSummary}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  Class2MaReceiptsResult,
  ContributionCreditResult,
  LiabilityResult,
  MarriageDetailsResult
}
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.aggregators.{
  AggregationServiceBSP,
  AggregationServiceGYSP,
  AggregationServiceMA
}

sealed trait EligibilityCheckDataResult {
  def benefitType: BenefitType
  def allResults: List[NpsApiResult]

  def overallResultStatus: OverallResultStatus =
    if (allResults.forall(_.status == Success)) OverallResultStatus.Success
    else if (allResults.forall(_.status == Failure)) OverallResultStatus.Failure
    else OverallResultStatus.Partial

  def resultSummary: OverallResultSummary = OverallResultSummary(
    totalCalls = allResults.size,
    successful = allResults.count(_.status == Success),
    failed = allResults.count(_.status == Failure)
  )

}

object EligibilityCheckDataResult {

  implicit val aggregator: ResultAggregator[EligibilityCheckDataResult] = {
    case result: EligibilityCheckDataResultMA   => AggregationServiceMA.aggregator.aggregate(result)
    case result: EligibilityCheckDataResultESA  => ???
    case result: EligibilityCheckDataResultJSA  => ???
    case result: EligibilityCheckDataResultGYSP => AggregationServiceGYSP.aggregator.aggregate(result)
    case result: EligibilityCheckDataResultBSP  => AggregationServiceBSP.aggregator.aggregate(result)
  }

  case class EligibilityCheckDataResultMA(
      class2MaReceiptsResult: Class2MaReceiptsResult,
      liabilityResult: LiabilityResult,
      contributionCreditResult: ContributionCreditResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType       = BenefitType.MA
    val allResults: List[NpsApiResult] = List(class2MaReceiptsResult, liabilityResult, contributionCreditResult)

  }

  case class EligibilityCheckDataResultESA() extends EligibilityCheckDataResult {
    val benefitType: BenefitType                = BenefitType.ESA
    override def allResults: List[NpsApiResult] = ???
  }

  case class EligibilityCheckDataResultJSA() extends EligibilityCheckDataResult {
    val benefitType: BenefitType                = BenefitType.JSA
    override def allResults: List[NpsApiResult] = ???
  }

  case class EligibilityCheckDataResultGYSP(
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType                = BenefitType.GYSP
    override def allResults: List[NpsApiResult] = List(marriageDetailsResult)
  }

  case class EligibilityCheckDataResultBSP(
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType                = BenefitType.BSP
    override def allResults: List[NpsApiResult] = List(marriageDetailsResult)
  }

}
