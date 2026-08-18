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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CursorId, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultMA

final case class BenefitEligibilityInfoSuccessResponseMa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    liabilitySummaryDetailsResult: List[FilteredLiabilitySummaryDetails],
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseMa {

  implicit val benefitEligibilityInfoResponseMaWrites: Writes[BenefitEligibilityInfoSuccessResponseMa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseMa]

  def apply(
      nationalInsuranceNumber: Identifier,
      liabilitySummaryDetailsResult: List[FilteredLiabilitySummaryDetails],
      niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
      nextCursor: Option[CursorId]
  ) = new BenefitEligibilityInfoSuccessResponseMa(
    MA,
    nationalInsuranceNumber,
    liabilitySummaryDetailsResult,
    niContributionsAndCreditsResult,
    nextCursor
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultMA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseMa] =

    if (result.allResults.exists(_.isFailure)) {
      Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    } else {
      Right(
        BenefitEligibilityInfoSuccessResponseMa(
          nationalInsuranceNumber = nationalInsuranceNumber,
          niContributionsAndCreditsResult =
            ContributionsAndCreditsResponse.from(result.contributionCreditResult.getSuccess.get),
          liabilitySummaryDetailsResult =
            result.liabilityResult.map(r => FilteredLiabilitySummaryDetails.from(r.getSuccess.get)),
          nextCursor = result.batchId.map(CursorId.from)
        )
      )
    }

}
