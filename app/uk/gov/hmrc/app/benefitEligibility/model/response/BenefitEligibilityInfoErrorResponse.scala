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

package uk.gov.hmrc.app.benefitEligibility.model.response

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult

case class OverallResultSummary(totalCalls: Int, successful: Int, failed: Int)

object OverallResultSummary {
  implicit val writes: Writes[OverallResultSummary] = Json.writes[OverallResultSummary]

  def from(allResults: List[ApiResult]): OverallResultSummary = OverallResultSummary(
    totalCalls = allResults.size,
    successful = allResults.count(_.isSuccess),
    failed = allResults.count(_.isFailure)
  )

}

case class BenefitEligibilityInfoErrorResponse(
    status: OverallResultStatus,
    nationalInsuranceNumber: Identifier,
    benefitType: BenefitType,
    summary: OverallResultSummary
)

object BenefitEligibilityInfoErrorResponse {

  implicit val benefitEligibilityInfoErrorResponseWrites: Writes[BenefitEligibilityInfoErrorResponse] =
    Json.writes[BenefitEligibilityInfoErrorResponse]

  private def from(
      benefitType: BenefitType,
      nationalInsuranceNumber: Identifier,
      allResults: List[ApiResult]
  ): BenefitEligibilityInfoErrorResponse =

    BenefitEligibilityInfoErrorResponse(
      status = OverallResultStatus.fromApiResults(allResults),
      nationalInsuranceNumber = nationalInsuranceNumber,
      benefitType = benefitType,
      summary = OverallResultSummary.from(allResults)
    )

  def from(
      nationalInsuranceNumber: Identifier,
      eligibilityCheckDataResult: EligibilityCheckDataResult
  ): BenefitEligibilityInfoErrorResponse =

    BenefitEligibilityInfoErrorResponse.from(
      eligibilityCheckDataResult.benefitType,
      nationalInsuranceNumber,
      eligibilityCheckDataResult.allResults
    )

  def from(
      paginationResult: PaginationResult
  ): BenefitEligibilityInfoErrorResponse =

    BenefitEligibilityInfoErrorResponse.from(
      BenefitType.from(paginationResult.paginationType),
      paginationResult.nationalInsuranceNumber,
      paginationResult.allResults
    )

}
