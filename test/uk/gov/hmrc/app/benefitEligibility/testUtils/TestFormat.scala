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

package uk.gov.hmrc.app.benefitEligibility.testUtils

import play.api.libs.json.{Json, OWrites, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.{MaternityAllowanceSortType, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.*

import java.time.LocalDate

object TestFormat {

  implicit val npsErrorResponse422SpecialWrites: Writes[NpsErrorResponse422Special] =
    Json.writes[NpsErrorResponse422Special]

  implicit val npsSingleErrorResponseWrites: Writes[NpsSingleErrorResponse] = Json.writes[NpsSingleErrorResponse]
  implicit val npsMultiErrorResponseWrites: OWrites[NpsMultiErrorResponse]  = Json.writes[NpsMultiErrorResponse]

  implicit val npsStandardErrorResponse400Writes: Writes[NpsStandardErrorResponse400] =
    Json.writes[NpsStandardErrorResponse400]

  implicit val hipFailureResponseWrites: Writes[HipFailureResponse] = Json.writes[HipFailureResponse]

  implicit val npsErrorResponseHipOriginResponseWrites: Writes[NpsErrorResponseHipOrigin] =
    Json.writes[NpsErrorResponseHipOrigin]

  implicit val contributionCreditWrites: Writes[ContributionsAndCreditsRequestParams] =
    Json.writes[ContributionsAndCreditsRequestParams]

  implicit val liabilitiesWrites: Writes[LiabilitiesRequestParams] = Json.writes[LiabilitiesRequestParams]

  implicit val maternityAllowanceSortTypeWrites: Writes[MaternityAllowanceSortType] =
    Json.writes[MaternityAllowanceSortType]

  implicit val class2MaReceiptsWrites: Writes[Class2MaReceiptsRequestParams] =
    Json.writes[Class2MaReceiptsRequestParams]

  implicit val marriageDetailsWrites: Writes[MarriageDetailsRequestParams] = Json.writes[MarriageDetailsRequestParams]

  implicit val longTermBenefitCalculationWrites: Writes[LongTermBenefitCalculationRequestParams] =
    Json.writes[LongTermBenefitCalculationRequestParams]

  implicit val esaEligibilityCheckDataRequestWrites: Writes[ESAEligibilityCheckDataRequest] =
    Json.writes[ESAEligibilityCheckDataRequest]

  implicit val maEligibilityCheckDataRequestWrites: Writes[MAEligibilityCheckDataRequest] =
    Json.writes[MAEligibilityCheckDataRequest]

  implicit val jsaEligibilityCheckDataRequestWrites: Writes[JSAEligibilityCheckDataRequest] =
    Json.writes[JSAEligibilityCheckDataRequest]

  implicit val bspEligibilityCheckDataRequestWrites: Writes[BSPEligibilityCheckDataRequest] =
    Json.writes[BSPEligibilityCheckDataRequest]

  implicit val gypEligibilityCheckDataRequestWrites: Writes[GYSPEligibilityCheckDataRequest] =
    Json.writes[GYSPEligibilityCheckDataRequest]

}
