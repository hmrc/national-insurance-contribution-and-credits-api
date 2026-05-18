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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.JSA
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultJSA
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult

final case class BenefitEligibilityInfoSuccessResponseJsa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseJsa {

  implicit val benefitEligibilityInfoResponseJsaWrites: Writes[BenefitEligibilityInfoSuccessResponseJsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseJsa]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: ContributionsAndCreditsResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseJsa(
      JSA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultJSA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseJsa] =

    result.contributionCreditResult match {
      case NpsApiResult.SuccessResult(_, niContributionsAndCreditsSuccessResponse) =>
        Right(
          BenefitEligibilityInfoSuccessResponseJsa(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult =
              ContributionsAndCreditsResponse.from(niContributionsAndCreditsSuccessResponse)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}
