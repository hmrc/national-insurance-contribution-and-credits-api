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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CursorId, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult

final case class BenefitEligibilityInfoSuccessResponseBsp private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
    marriageDetailsResult: FilteredMarriageDetails,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseBsp {

  implicit val benefitEligibilityInfoResponseBspWrites: Writes[BenefitEligibilityInfoSuccessResponseBsp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseBsp]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
      marriageDetailsResult: FilteredMarriageDetails,
      nextCursor: Option[CursorId]
  ) = new BenefitEligibilityInfoSuccessResponseBsp(
    BSP,
    nationalInsuranceNumber,
    niContributionsAndCreditsResult,
    marriageDetailsResult,
    nextCursor
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultBSP
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseBsp] =

    (result.marriageDetailsResult, result.contributionCreditResult) match {
      case (
            NpsApiResult.SuccessResult(_, marriageDetailsSuccessResponse),
            NpsApiResult.SuccessResult(_, contributionsAndCreditsSuccessResponse)
          ) =>
        Right(
          BenefitEligibilityInfoSuccessResponseBsp(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult =
              ContributionsAndCreditsResponse.from(contributionsAndCreditsSuccessResponse),
            marriageDetailsResult = FilteredMarriageDetails.from(marriageDetailsSuccessResponse),
            nextCursor = result.batchId.map(CursorId.from)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}
