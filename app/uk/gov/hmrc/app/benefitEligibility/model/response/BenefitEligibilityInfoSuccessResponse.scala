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

import play.api.libs.json.Writes

trait BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponse {

  implicit val benefitEligibilityInfoSuccessResponseWrites: Writes[BenefitEligibilityInfoSuccessResponse] = Writes {
    case r: BenefitEligibilityInfoSuccessResponseMa =>
      BenefitEligibilityInfoSuccessResponseMa.benefitEligibilityInfoResponseMaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseBsp =>
      BenefitEligibilityInfoSuccessResponseBsp.benefitEligibilityInfoResponseBspWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseEsa =>
      BenefitEligibilityInfoSuccessResponseEsa.benefitEligibilityInfoResponseEsaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseJsa =>
      BenefitEligibilityInfoSuccessResponseJsa.benefitEligibilityInfoResponseJsaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseGysp =>
      BenefitEligibilityInfoSuccessResponseGysp.benefitEligibilityInfoResponseGyspWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseSearchLight =>
      BenefitEligibilityInfoSuccessResponseSearchLight.benefitEligibilityInfoResponseBspSearchlightWrites.writes(r)
  }

}
