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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.ESA
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultESA
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult

final case class BenefitEligibilityInfoSuccessResponseEsa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseEsa {

  implicit val benefitEligibilityInfoResponseEsaWrites: Writes[BenefitEligibilityInfoSuccessResponseEsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseEsa]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: ContributionsAndCreditsResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseEsa(
      ESA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultESA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseEsa] =

    result.contributionCreditResult match {
      case NpsApiResult.SuccessResult(_, niContributionsAndCreditsSuccessResponse) =>
        Right(
          BenefitEligibilityInfoSuccessResponseEsa(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult =
              ContributionsAndCreditsResponse.from(niContributionsAndCreditsSuccessResponse)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}
