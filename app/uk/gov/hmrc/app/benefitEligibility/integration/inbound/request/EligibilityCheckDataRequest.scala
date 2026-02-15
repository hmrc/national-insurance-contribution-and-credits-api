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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated

import java.time.LocalDate

object EligibilityCheckDataRequest {

  implicit val reads: Reads[EligibilityCheckDataRequest] = Reads { json =>
    (json \ "benefitType").validate[BenefitType].flatMap {
      case BenefitType.MA   => json.validate[MAEligibilityCheckDataRequest]
      case BenefitType.ESA  => json.validate[ESAEligibilityCheckDataRequest]
      case BenefitType.JSA  => json.validate[JSAEligibilityCheckDataRequest]
      case BenefitType.GYSP => json.validate[GYSPEligibilityCheckDataRequest]
      case BenefitType.BSP  => json.validate[BSPEligibilityCheckDataRequest]
    }
  }

}

sealed trait EligibilityCheckDataRequest {
  def benefitType: BenefitType
  def nationalInsuranceNumber: Identifier
}

case class LiabilitiesRequestParams(
    searchCategory: LiabilitySearchCategoryHyphenated,              // liability api
    liabilityOccurrenceNumber: Option[LiabilitiesOccurrenceNumber], // liability api
    liabilityType: Option[LiabilitySearchCategoryHyphenated],       // liability api
    earliestLiabilityStartDate: Option[LocalDate],                  // liability api
    liabilityStart: Option[LocalDate],                              // liability api
    liabilityEnd: Option[LocalDate]                                 // liability api
)

object LiabilitiesRequestParams {

  implicit val liabilitiesReads: Reads[LiabilitiesRequestParams] =
    Json.reads[LiabilitiesRequestParams]

}

case class Class2MaReceiptsRequestParams(
    archived: Option[Boolean],                 // ma receipts api
    receiptDate: Option[ReceiptDate],          // ma receipts api
    sortBy: Option[MaternityAllowanceSortType] // ma receipts api
)

object Class2MaReceiptsRequestParams {

  implicit val class2MaReceiptsReads: Reads[Class2MaReceiptsRequestParams] =
    Json.reads[Class2MaReceiptsRequestParams]

}

final case class MAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams, // no callback
    liabilities: LiabilitiesRequestParams,                           // has callbackUrl
    class2MaReceipts: Class2MaReceiptsRequestParams                  // no callback
) extends EligibilityCheckDataRequest

object MAEligibilityCheckDataRequest {

  def apply(
      nationalInsuranceNumber: Identifier,
      contributionsAndCredits: ContributionsAndCreditsRequestParams,
      liabilities: LiabilitiesRequestParams,
      class2MaReceipts: Class2MaReceiptsRequestParams
  ) = new MAEligibilityCheckDataRequest(
    MA,
    nationalInsuranceNumber,
    contributionsAndCredits,
    liabilities,
    class2MaReceipts
  )

  implicit val maEligibilityCheckDataRequestReads: Reads[MAEligibilityCheckDataRequest] =
    Json.reads[MAEligibilityCheckDataRequest]

}

final case class ESAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object ESAEligibilityCheckDataRequest {

  implicit val esaEligibilityCheckDataRequestReads: Reads[ESAEligibilityCheckDataRequest] =
    Json.reads[ESAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new ESAEligibilityCheckDataRequest(ESA, nationalInsuranceNumber, niContributionsAndCredits)

}

final case class JSAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object JSAEligibilityCheckDataRequest {

  implicit val jsaEligibilityCheckDataRequestReads: Reads[JSAEligibilityCheckDataRequest] =
    Json.reads[JSAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new JSAEligibilityCheckDataRequest(JSA, nationalInsuranceNumber, niContributionsAndCredits)

}

case class ContributionsAndCreditsRequestParams(
    dateOfBirth: DateOfBirth,   // contribution credit api
    startTaxYear: StartTaxYear, // contribution credit api
    endTaxYear: EndTaxYear      // contribution credit api
)

object ContributionsAndCreditsRequestParams {

  implicit val contributionsAndCreditsReads: Reads[ContributionsAndCreditsRequestParams] =
    Json.reads[ContributionsAndCreditsRequestParams]

}

case class LongTermBenefitCalculationRequestParams(
    longTermBenefitType: Option[LongTermBenefitType],    // benefit calculation API + benefit calculation notes API
    pensionProcessingArea: Option[PensionProcessingArea] // benefit calculation API
)

object LongTermBenefitCalculationRequestParams {

  implicit val BenefitCalculation: Reads[LongTermBenefitCalculationRequestParams] =
    Json.reads[LongTermBenefitCalculationRequestParams]

}

case class MarriageDetailsRequestParams(
    searchStartYear: Option[StartYear], // Marriage Details API
    latest: Option[FilterLatest]        // Marriage Details API
)

object MarriageDetailsRequestParams {

  implicit val MarriageDetails: Reads[MarriageDetailsRequestParams] =
    Json.reads[MarriageDetailsRequestParams]

}

final case class GYSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier, // contribution credit api
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    longTermBenefitCalculation: LongTermBenefitCalculationRequestParams,
    marriageDetails: Option[MarriageDetailsRequestParams]
) extends EligibilityCheckDataRequest

object GYSPEligibilityCheckDataRequest {

  implicit val gyspEligibilityCheckDataRequestReads: Reads[GYSPEligibilityCheckDataRequest] =
    Json.reads[GYSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier, // contribution credit api
      niContributionsAndCredits: ContributionsAndCreditsRequestParams,
      longTermBenefitCalculation: LongTermBenefitCalculationRequestParams,
      marriageDetails: Option[MarriageDetailsRequestParams]
  ) = new GYSPEligibilityCheckDataRequest(
    GYSP,
    nationalInsuranceNumber,
    niContributionsAndCredits,
    longTermBenefitCalculation,
    marriageDetails
  )

}

final case class BSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier, // contribution credit api
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    marriageDetails: Option[MarriageDetailsRequestParams]
) extends EligibilityCheckDataRequest

object BSPEligibilityCheckDataRequest {

  implicit val bspEligibilityCheckDataRequestReads: Reads[BSPEligibilityCheckDataRequest] =
    Json.reads[BSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier, // contribution credit api
      niContributionsAndCredits: ContributionsAndCreditsRequestParams,
      marriageDetails: Option[MarriageDetailsRequestParams]
  ) = new BSPEligibilityCheckDataRequest(BSP, nationalInsuranceNumber, niContributionsAndCredits, marriageDetails)

}
