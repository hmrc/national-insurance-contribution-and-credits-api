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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound.request

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.OccurrenceNumber
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.LiabilitySearchCategoryHyphenated

import java.time.LocalDate

object EligibilityCheckDataRequest {

  implicit val reads: Reads[EligibilityCheckDataRequest] = Reads { json =>
    (json \ "type").validate[BenefitType].flatMap {
      case BenefitType.MA   => json.validate[MAEligibilityCheckDataRequest]
      case BenefitType.ESA  => json.validate[ESAEligibilityCheckDataRequest]
      case BenefitType.JSA  => json.validate[JSAEligibilityCheckDataRequest]
      case BenefitType.GYSP => json.validate[GYSPEligibilityCheckDataRequest]
      case BenefitType.BSP  => json.validate[BSPEligibilityCheckDataRequest]
    }
  }

}

sealed trait EligibilityCheckDataRequest {
  def `type`: BenefitType
}

case class MAEligibilityCheckDataRequest(
    nationalInsuranceNumber: String, // contribution credit api
    dateOfBirth: DateOfBirth,        // contribution credit api
    startTaxYear: StartTaxYear,      // contribution credit api
    endTaxYear: EndTaxYear,          // contribution credit api
    identifier: Identifier,          // maybe trn or nino (required by both the liability and ma receipt api)
    liabilitySearchCategoryHyphenated: LiabilitySearchCategoryHyphenated, // liability api
    liabilityOccurrenceNumber: Option[OccurrenceNumber],                  // liability api
    liabilityType: Option[LiabilitySearchCategoryHyphenated],             // liability api
    earliestLiabilityStartDate: Option[LocalDate],                        // liability api
    liabilityStart: Option[LocalDate],                                    // liability api
    liabilityEnd: Option[LocalDate],                                      // liability api
    archived: Option[Boolean],                                            // ma receipts api
    receiptDate: Option[ReceiptDate],                                     // ma receipts api
    sortBy: Option[MaternityAllowanceSortType]                            // ma receipts api
) extends EligibilityCheckDataRequest {
  val `type`: BenefitType = MA
}

object MAEligibilityCheckDataRequest {
  implicit val reads: Reads[MAEligibilityCheckDataRequest]   = Json.reads[MAEligibilityCheckDataRequest]
  implicit val writes: Writes[MAEligibilityCheckDataRequest] = Json.writes[MAEligibilityCheckDataRequest]
}

case class ESAEligibilityCheckDataRequest(
    nationalInsuranceNumber: String, // contribution credit api
    dateOfBirth: LocalDate,          // contribution credit api
    startTaxYear: Int,               // contribution credit api
    endTaxYear: Int                  // contribution credit api
) extends EligibilityCheckDataRequest {
  val `type`: BenefitType = ESA
}

object ESAEligibilityCheckDataRequest {
  implicit val reads: Reads[ESAEligibilityCheckDataRequest]   = Json.reads[ESAEligibilityCheckDataRequest]
  implicit val writes: Writes[ESAEligibilityCheckDataRequest] = Json.writes[ESAEligibilityCheckDataRequest]
}

case class JSAEligibilityCheckDataRequest(
    nationalInsuranceNumber: String, // contribution credit api
    dateOfBirth: LocalDate,          // contribution credit api
    startTaxYear: Int,               // contribution credit api
    endTaxYear: Int                  // contribution credit api
) extends EligibilityCheckDataRequest {
  val `type`: BenefitType = JSA
}

object JSAEligibilityCheckDataRequest {
  implicit val reads: Reads[JSAEligibilityCheckDataRequest]   = Json.reads[JSAEligibilityCheckDataRequest]
  implicit val writes: Writes[JSAEligibilityCheckDataRequest] = Json.writes[JSAEligibilityCheckDataRequest]
}

case class GYSPEligibilityCheckDataRequest(
    nationalInsuranceNumber: String, // contribution credit api +  scheme-membership-details
    identifier: Identifier, // (maybe trn or nino) Marriage Details API + benefit calculation API + benefit calculation notes API + benefit-scheme-details API + State Pension - Get Qualifying Years and Contributions API
    dateOfBirth: LocalDate,                              // contribution credit api
    startTaxYear: Int,                                   // contribution credit api
    endTaxYear: Int,                                     // contribution credit api
    benefitType: LongTermBenefitType,                    // benefit calculation API + benefit calculation notes API
    associatedCalculationSequenceNumber: Int,            // benefit calculation API + benefit calculation notes API
    schemeContractedOutNumber: Int,                      // benefit-scheme-details API
    searchStartYear: Option[Int],                        // Marriage Details API
    searchEndYear: Option[Int],                          // Marriage Details API
    latest: Option[Boolean],                             // Marriage Details API
    sequence: Option[Int],                               // Marriage Details API
    pensionProcessingArea: Option[String],               // benefit calculation API,
    schemeMembershipSequenceNumber: Option[Int],         // scheme-membership-details API
    schemeMembershipTransferSequenceNumber: Option[Int], // scheme-membership-details API
    schemeMembershipOccurrenceNumber: Option[Int]        // scheme-membership-details API
) extends EligibilityCheckDataRequest {
  val `type`: BenefitType = GYSP
}

object GYSPEligibilityCheckDataRequest {
  implicit val reads: Reads[GYSPEligibilityCheckDataRequest]   = Json.reads[GYSPEligibilityCheckDataRequest]
  implicit val writes: Writes[GYSPEligibilityCheckDataRequest] = Json.writes[GYSPEligibilityCheckDataRequest]
}

case class BSPEligibilityCheckDataRequest(
    nationalInsuranceNumber: String, // contribution credit api
    dateOfBirth: LocalDate,          // contribution credit api
    startTaxYear: Int,               // contribution credit api
    endTaxYear: Int,                 // contribution credit api
    identifier: Identifier,          // (maybe trn or nino) Marriage Details API
    searchStartYear: Option[Int],    // Marriage Details API
    searchEndYear: Option[Int],      // Marriage Details API
    latest: Option[Boolean],         // Marriage Details API
    sequence: Option[Int]            // Marriage Details API
) extends EligibilityCheckDataRequest {
  val `type`: BenefitType = BSP
}

object BSPEligibilityCheckDataRequest {
  implicit val reads: Reads[BSPEligibilityCheckDataRequest]   = Json.reads[BSPEligibilityCheckDataRequest]
  implicit val writes: Writes[BSPEligibilityCheckDataRequest] = Json.writes[BSPEligibilityCheckDataRequest]
}
