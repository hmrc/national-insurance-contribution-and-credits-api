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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.{ErrorCode400, ErrorCode422, Identifier, Reason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate

sealed trait LiabilitySummaryDetailsResponse extends NpsApiResponse

object LiabilitySummaryDetailsError {

  // region Error400

  case class LiabilitySummaryDetailsError400(reason: Reason, code: ErrorCode400)

  object LiabilitySummaryDetailsError400 {

    implicit val npsErrorResponse400Reads: Reads[LiabilitySummaryDetailsError400] =
      Json.reads[LiabilitySummaryDetailsError400]

  }

  case class LiabilitySummaryDetailsErrorResponse400(failures: Option[List[LiabilitySummaryDetailsError400]])
      extends LiabilitySummaryDetailsResponse

  object LiabilitySummaryDetailsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[LiabilitySummaryDetailsErrorResponse400] =
      Json.reads[LiabilitySummaryDetailsErrorResponse400]

  }

  // endregion Error400

  // region Error403

  case class LiabilitySummaryDetailsErrorResponse403(reason: ErrorReason403, code: ErrorCode403)
      extends LiabilitySummaryDetailsResponse
      with NpsApiResponse

  object LiabilitySummaryDetailsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[LiabilitySummaryDetailsErrorResponse403] =
      Json.reads[LiabilitySummaryDetailsErrorResponse403]

  }

  // endregion Error403

  // region Error422

  case class LiabilitySummaryDetailsError422(reason: Reason, code: ErrorCode422)

  object LiabilitySummaryDetailsError422 {

    implicit val NpsErrorResponse422Reads: Reads[LiabilitySummaryDetailsError422] =
      Json.reads[LiabilitySummaryDetailsError422]

  }

  case class LiabilitySummaryDetailsErrorResponse422(
      failures: Option[List[LiabilitySummaryDetailsError422]],
      askUser: Option[Boolean],
      fixRequired: Option[Boolean],
      workItemRaised: Option[Boolean]
  ) extends LiabilitySummaryDetailsResponse

  object LiabilitySummaryDetailsErrorResponse422 {

    implicit val npsFailureResponse422Reads: Reads[LiabilitySummaryDetailsErrorResponse422] =
      Json.reads[LiabilitySummaryDetailsErrorResponse422]

  }

  // endregion Error422

}

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
      startDateStatus: Option[EnumLtpsdttp],
      endDateStatus: Option[EnumLtpedttp],
      startDate: StartDate,
      endDate: Option[EndDate],
      country: Option[EnumCountry],
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

  // region Liability Employment Details List

  case class EmploymentStatusForLiability(value: String) extends AnyVal

  object EmploymentStatusForLiability {

    implicit val employmentStatusForLiabilityFormats: Format[EmploymentStatusForLiability] =
      Json.valueFormat[EmploymentStatusForLiability]

  }

  case class LiabilityDetails(
      identifier: Identifier,
      `type`: EnumLiabtp,
      occurrenceNumber: OccurrenceNumber,
      startDateStatus: Option[EnumLtpsdttp],
      endDateStatus: Option[EnumLtpedttp],
      startDate: StartDate,
      endDate: Option[EndDate],
      country: Option[EnumCountry],
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

  case class LiabilityEmploymentDetailsList(
      employmentStatusForLiability: EmploymentStatusForLiability,
      liabilityDetails: List[LiabilityDetails]
  )

  object LiabilityEmploymentDetailsList {

    implicit val liabilityEmploymentDetailsListFormats: Format[LiabilityEmploymentDetailsList] =
      Json.format[LiabilityEmploymentDetailsList]

  }

  // endregion Liability Employment Details List

  // region Callback

  case class Callback(value: String) extends AnyVal

  object Callback {
    implicit val callbackReads: Format[Callback] = Json.valueFormat[Callback]
  }

  // endregion Callback

  // region Success Response

  case class LiabilitySummaryDetailsSuccessResponse(
      liabilityDetailsList: Option[List[LiabilityDetailsList]],
      liabilityEmploymentDetailsList: Option[List[LiabilityEmploymentDetailsList]],
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
