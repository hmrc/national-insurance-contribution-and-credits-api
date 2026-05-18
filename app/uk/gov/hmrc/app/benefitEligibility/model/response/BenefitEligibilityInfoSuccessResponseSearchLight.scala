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
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CursorId, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultSearchLight
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult

final case class BenefitEligibilityInfoSuccessResponseSearchLight(
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseSearchLight {

  implicit val benefitEligibilityInfoResponseBspSearchlightWrites
      : Writes[BenefitEligibilityInfoSuccessResponseSearchLight] =
    Json.writes[BenefitEligibilityInfoSuccessResponseSearchLight]

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultSearchLight
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseSearchLight] =

    result.contributionCreditResult match {
      case (
            NpsApiResult.SuccessResult(_, contributionsAndCreditsSuccessResponse)
          ) =>
        Right(
          BenefitEligibilityInfoSuccessResponseSearchLight(
            result.benefitType,
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult =
              ContributionsAndCreditsResponse.from(contributionsAndCreditsSuccessResponse),
            result.nextCursor.map(CursorId.from)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}
