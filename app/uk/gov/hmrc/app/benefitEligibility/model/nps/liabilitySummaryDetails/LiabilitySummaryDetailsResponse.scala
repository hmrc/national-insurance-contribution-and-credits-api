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

package uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.common.{Callback, Country, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.{NpsApiResponse, NpsSuccessfulApiResponse}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.{
  EnumAtcredfg,
  EnumHrpIndicator,
  EnumLcheadtp,
  EnumLcruletp,
  EnumLiabtp,
  EnumLtpedttp,
  EnumLtpsdttp,
  EnumOffidtp
}

import java.time.LocalDate

sealed trait LiabilitySummaryDetailsResponse extends NpsApiResponse

object LiabilitySummaryDetailsSuccess {

  // region Liability Details List
  case class OccurrenceNumber(value: Int) extends AnyVal

  object OccurrenceNumber {
    implicit val occurrenceNumberFormats: Format[OccurrenceNumber] = Json.valueFormat[OccurrenceNumber]
  }

  case class StartDate(value: LocalDate) extends AnyVal

  object StartDate {
    implicit val startDateFormats: Format[StartDate] = Json.valueFormat[StartDate]
  }

  case class EndDate(value: LocalDate) extends AnyVal

  object EndDate {
    implicit val endDateFormats: Format[EndDate] = Json.valueFormat[EndDate]
  }

  case class CasepaperReferenceNumber(value: String) extends AnyVal

  object CasepaperReferenceNumber {

    implicit val casepaperReferenceNumberFormats: Format[CasepaperReferenceNumber] =
      Json.valueFormat[CasepaperReferenceNumber]

  }

  case class HomeResponsibilitiesProtectionBenefitReference(value: String) extends AnyVal

  object HomeResponsibilitiesProtectionBenefitReference {

    implicit val homeResponsibilitiesProtectionBenefitReferenceFormats
        : Format[HomeResponsibilitiesProtectionBenefitReference] =
      Json.valueFormat[HomeResponsibilitiesProtectionBenefitReference]

  }

  case class HomeResponsibilitiesProtectionRate(value: BigDecimal) extends AnyVal

  object HomeResponsibilitiesProtectionRate {

    implicit val homeResponsibilitiesProtectionRateFormats: Format[HomeResponsibilitiesProtectionRate] =
      Json.valueFormat[HomeResponsibilitiesProtectionRate]

  }

  case class HomeResponsibilityProtectionCalculationYear(value: Int) extends AnyVal

  object HomeResponsibilityProtectionCalculationYear {

    implicit val homeResponsibilityProtectionCalculationYearFormats
        : Format[HomeResponsibilityProtectionCalculationYear] =
      Json.valueFormat[HomeResponsibilityProtectionCalculationYear]

  }

  case class AwardAmount(value: BigDecimal) extends AnyVal

  object AwardAmount {
    implicit val awardAmountFormats: Format[AwardAmount] = Json.valueFormat[AwardAmount]
  }

  case class ResourceGroupIdentifier(value: Int) extends AnyVal

  object ResourceGroupIdentifier {

    implicit val resourceGroupIdentifierFormats: Format[ResourceGroupIdentifier] =
      Json.valueFormat[ResourceGroupIdentifier]

  }

  case class OfficeLocationDecode(value: Int) extends AnyVal

  object OfficeLocationDecode {

    implicit val officeLocationDecodeFormats: Format[OfficeLocationDecode] = Json.valueFormat[OfficeLocationDecode]
  }

  case class OfficeLocationValue(value: String) extends AnyVal

  object OfficeLocationValue {
    implicit val officeLocationValueFormats: Format[OfficeLocationValue] = Json.valueFormat[OfficeLocationValue]
  }

  case class OfficeDetails(
      officeLocationDecode: Option[OfficeLocationDecode],
      officeLocationValue: Option[OfficeLocationValue],
      officeIdentifier: Option[EnumOffidtp]
  )

  object OfficeDetails {
    implicit val officeDetailsFormats: Format[OfficeDetails] = Json.format[OfficeDetails]
  }

  case class LiabilityDetailsList(
      identifier: Identifier,
      `type`: EnumLiabtp,
      occurrenceNumber: OccurrenceNumber,
      startDate: StartDate,
      startDateStatus: Option[EnumLtpsdttp],
      endDateStatus: Option[EnumLtpedttp],
      endDate: Option[EndDate],
      country: Option[Country],
      trainingCreditApprovalStatus: Option[EnumAtcredfg],
      casepaperReferenceNumber: Option[CasepaperReferenceNumber],
      homeResponsibilitiesProtectionBenefitReference: Option[HomeResponsibilitiesProtectionBenefitReference],
      homeResponsibilitiesProtectionRate: Option[HomeResponsibilitiesProtectionRate],
      lostCardNotificationReason: Option[EnumLcheadtp],
      lostCardRulingReason: Option[EnumLcruletp],
      homeResponsibilityProtectionCalculationYear: Option[HomeResponsibilityProtectionCalculationYear],
      awardAmount: Option[AwardAmount],
      resourceGroupIdentifier: Option[ResourceGroupIdentifier],
      homeResponsibilitiesProtectionIndicator: Option[EnumHrpIndicator],
      officeDetails: Option[OfficeDetails]
  )

  object LiabilityDetailsList {
    implicit val liabilityDetailsListFormats: Format[LiabilityDetailsList] = Json.format[LiabilityDetailsList]
  }

  // endregion Liability Details List

  case class LiabilityDetails(
      identifier: Identifier,
      `type`: EnumLiabtp,
      occurrenceNumber: OccurrenceNumber,
      startDateStatus: Option[EnumLtpsdttp],
      endDateStatus: Option[EnumLtpedttp],
      startDate: StartDate,
      endDate: Option[EndDate],
      country: Option[Country],
      trainingCreditApprovalStatus: Option[EnumAtcredfg],
      casepaperReferenceNumber: Option[CasepaperReferenceNumber],
      homeResponsibilitiesProtectionBenefitReference: Option[HomeResponsibilitiesProtectionBenefitReference],
      homeResponsibilitiesProtectionRate: Option[HomeResponsibilitiesProtectionRate],
      lostCardNotificationReason: Option[EnumLcheadtp],
      lostCardRulingReason: Option[EnumLcruletp],
      homeResponsibilityProtectionCalculationYear: Option[HomeResponsibilityProtectionCalculationYear],
      awardAmount: Option[AwardAmount],
      resourceGroupIdentifier: Option[ResourceGroupIdentifier],
      officeDetails: Option[OfficeDetails]
  )

  object LiabilityDetails {
    implicit val liabilityDetailsFormats: Format[LiabilityDetails] = Json.format[LiabilityDetails]
  }

  // region Success Response

  case class LiabilitySummaryDetailsSuccessResponse(
      liabilityDetailsList: Option[List[LiabilityDetailsList]],
      callback: Option[Callback]
  ) extends LiabilitySummaryDetailsResponse
      with NpsSuccessfulApiResponse

  object LiabilitySummaryDetailsSuccessResponse {

    implicit val liabilitySummaryDetailsSuccessResponseReads: Reads[LiabilitySummaryDetailsSuccessResponse] =
      Json.reads[LiabilitySummaryDetailsSuccessResponse]

    implicit val liabilitySummaryDetailsSuccessResponseWrites: Writes[LiabilitySummaryDetailsSuccessResponse] =
      Json.writes[LiabilitySummaryDetailsSuccessResponse]

  }

  // endregion Success Response

}
