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

package uk.gov.hmrc.app.benefitEligibility.common

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ApiResult, EligibilityCheckDataResult}

case class OverallResultSummary(totalCalls: Int, successful: Int, failed: Int)

object OverallResultSummary {
  implicit val writes: Writes[OverallResultSummary] = Json.writes[OverallResultSummary]
}

case class BenefitEligibilityDataFetchErrorReport(
    overallResultStatus: OverallResultStatus,
    benefitType: BenefitType,
    overallResultSummary: OverallResultSummary,
    downStreams: List[ApiResult]
)

object BenefitEligibilityDataFetchErrorReport {

  def from(
      eligibilityCheckDataResult: EligibilityCheckDataResult
  ): BenefitEligibilityDataFetchErrorReport =

    if (eligibilityCheckDataResult.overallResultStatus == OverallResultStatus.Failure) {
      BenefitEligibilityDataFetchErrorReport(
        overallResultStatus = OverallResultStatus.Failure,
        benefitType = eligibilityCheckDataResult.benefitType,
        overallResultSummary = eligibilityCheckDataResult.resultSummary,
        downStreams = eligibilityCheckDataResult.allResults.filter(_.isFailure)
      )
    } else {
      BenefitEligibilityDataFetchErrorReport(
        overallResultStatus = OverallResultStatus.Partial,
        benefitType = eligibilityCheckDataResult.benefitType,
        overallResultSummary = eligibilityCheckDataResult.resultSummary,
        downStreams = eligibilityCheckDataResult.allResults.filter(_.isSuccess) ++
          eligibilityCheckDataResult.allResults.filter(_.isFailure)
      )
    }

}
