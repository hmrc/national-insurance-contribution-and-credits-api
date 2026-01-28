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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model

import play.api.libs.json.{Format, Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.common.Reason
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsSuccessfulApiResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.*

import scala.collection.immutable

sealed trait BenefitSchemeDetailsResponse

object BenefitSchemeDetailsSuccess {

  case class SchemeContractedOutNumberDetails(value: String) extends AnyVal

  object SchemeContractedOutNumberDetails {

    implicit val schemeContractedOutNumberDetailsReads: Format[SchemeContractedOutNumberDetails] =
      Json.valueFormat[SchemeContractedOutNumberDetails]

  }

  case class MagneticTapeNumber(value: Int) extends AnyVal

  object MagneticTapeNumber {
    implicit val magneticTapeNumberFormat: Format[MagneticTapeNumber] = Json.valueFormat[MagneticTapeNumber]
  }

  case class BenefitSchemeName(value: String) extends AnyVal

  object BenefitSchemeName {
    implicit val schemeNameFormat: Format[BenefitSchemeName] = Json.valueFormat[BenefitSchemeName]
  }

  case class SchemeStartDate(value: String) extends AnyVal

  object SchemeStartDate {
    implicit val schemeStartDateFormat: Format[SchemeStartDate] = Json.valueFormat[SchemeStartDate]
  }

  case class SchemeCessationDate(value: String) extends AnyVal

  object SchemeCessationDate {
    implicit val schemeCessationDateFormat: Format[SchemeCessationDate] = Json.valueFormat[SchemeCessationDate]
  }

  case class ContractedOutDeductionExtinguishedDate(value: String) extends AnyVal

  object ContractedOutDeductionExtinguishedDate {

    implicit val contractedOutDeductionExtinguishedDateReads: Format[ContractedOutDeductionExtinguishedDate] =
      Json.valueFormat[ContractedOutDeductionExtinguishedDate]

  }

  case class PaymentSuspensionDate(value: String) extends AnyVal

  object PaymentSuspensionDate {
    implicit val paymentSuspensionDateFormat: Format[PaymentSuspensionDate] = Json.valueFormat[PaymentSuspensionDate]
  }

  case class RecoveriesSuspendedDate(value: String) extends AnyVal

  object RecoveriesSuspendedDate {

    implicit val recoveriesSuspendedDateFormat: Format[RecoveriesSuspendedDate] =
      Json.valueFormat[RecoveriesSuspendedDate]

  }

  case class PaymentRestartDate(value: String) extends AnyVal

  object PaymentRestartDate {
    implicit val paymentRestartDateFormat: Format[PaymentRestartDate] = Json.valueFormat[PaymentRestartDate]
  }

  case class RecoveriesRestartedDate(value: String) extends AnyVal

  object RecoveriesRestartedDate {

    implicit val recoveriesRestartedDateFormat: Format[RecoveriesRestartedDate] =
      Json.valueFormat[RecoveriesRestartedDate]

  }

  case class AccruedGMPLiabilityServiceDate(value: String) extends AnyVal

  object AccruedGMPLiabilityServiceDate {

    implicit val accruedGMPLiabilityServiceDateReads: Format[AccruedGMPLiabilityServiceDate] =
      Json.valueFormat[AccruedGMPLiabilityServiceDate]

  }

  case class CertificateCancellationDate(value: String) extends AnyVal

  object CertificateCancellationDate {

    implicit val certificateCancellationDateReads: Format[CertificateCancellationDate] =
      Json.valueFormat[CertificateCancellationDate]

  }

  case class SuspendedDate(value: String) extends AnyVal

  object SuspendedDate {
    implicit val suspendedDateFormat: Format[SuspendedDate] = Json.valueFormat[SuspendedDate]
  }

  case class IsleOfManInterest(value: Boolean) extends AnyVal

  object IsleOfManInterest {
    implicit val isleOfManInterestFormat: Format[IsleOfManInterest] = Json.valueFormat[IsleOfManInterest]
  }

  case class SchemeWindingUp(value: Boolean) extends AnyVal

  object SchemeWindingUp {
    implicit val schemeWindingUpFormat: Format[SchemeWindingUp] = Json.valueFormat[SchemeWindingUp]
  }

  case class RevaluationRateSequenceNumber(value: Int) extends AnyVal

  object RevaluationRateSequenceNumber {

    implicit val revaluationRateSequenceNumberReads: Format[RevaluationRateSequenceNumber] =
      Json.valueFormat[RevaluationRateSequenceNumber]

  }

  case class DateFormallyCertified(value: String) extends AnyVal

  object DateFormallyCertified {
    implicit val dateFormallyCertifiedFormat: Format[DateFormallyCertified] = Json.valueFormat[DateFormallyCertified]
  }

  case class PrivatePensionSchemeSanctionDate(value: String) extends AnyVal

  object PrivatePensionSchemeSanctionDate {

    implicit val privatePensionSchemeSanctionDateReads: Format[PrivatePensionSchemeSanctionDate] =
      Json.valueFormat[PrivatePensionSchemeSanctionDate]

  }

  case class CurrentOptimisticLock(value: Int) extends AnyVal

  object CurrentOptimisticLock {
    implicit val currentOptimisticLockFormat: Format[CurrentOptimisticLock] = Json.valueFormat[CurrentOptimisticLock]
  }

  case class SchemeConversionDate(value: String) extends AnyVal

  object SchemeConversionDate {
    implicit val schemeConversionDateFormat: Format[SchemeConversionDate] = Json.valueFormat[SchemeConversionDate]
  }

  case class ReconciliationDate(value: String) extends AnyVal

  object ReconciliationDate {
    implicit val reconciliationDateFormat: Format[ReconciliationDate] = Json.valueFormat[ReconciliationDate]
  }

  case class SchemeAddressSequenceNumber(value: Int) extends AnyVal

  object SchemeAddressSequenceNumber {

    implicit val schemeAddressSequenceNumberReads: Format[SchemeAddressSequenceNumber] =
      Json.valueFormat[SchemeAddressSequenceNumber]

  }

  case class SchemeAddressStartDate(value: String) extends AnyVal

  object SchemeAddressStartDate {
    implicit val schemeAddressStartDateFormat: Format[SchemeAddressStartDate] = Json.valueFormat[SchemeAddressStartDate]
  }

  case class SchemeAddressEndDate(value: String) extends AnyVal

  object SchemeAddressEndDate {
    implicit val schemeAddressEndDateFormat: Format[SchemeAddressEndDate] = Json.valueFormat[SchemeAddressEndDate]
  }

  case class SchemeTelephoneNumber(value: String) extends AnyVal

  object SchemeTelephoneNumber {
    implicit val schemeTelephoneNumberFormat: Format[SchemeTelephoneNumber] = Json.valueFormat[SchemeTelephoneNumber]
  }

  case class SchemeAddressLine1(value: String) extends AnyVal

  object SchemeAddressLine1 {
    implicit val schemeAddressLine1Format: Format[SchemeAddressLine1] = Json.valueFormat[SchemeAddressLine1]
  }

  case class SchemeAddressLine2(value: String) extends AnyVal

  object SchemeAddressLine2 {
    implicit val schemeAddressLine2Format: Format[SchemeAddressLine2] = Json.valueFormat[SchemeAddressLine2]
  }

  case class SchemeAddressLocality(value: String) extends AnyVal

  object SchemeAddressLocality {
    implicit val schemeAddressLocalityFormat: Format[SchemeAddressLocality] = Json.valueFormat[SchemeAddressLocality]
  }

  case class SchemeAddressPostalTown(value: String) extends AnyVal

  object SchemeAddressPostalTown {

    implicit val schemeAddressPostalTownFormat: Format[SchemeAddressPostalTown] =
      Json.valueFormat[SchemeAddressPostalTown]

  }

  case class SchemePostcode(value: String) extends AnyVal

  object SchemePostcode {
    implicit val schemePostcodeFormat: Format[SchemePostcode] = Json.valueFormat[SchemePostcode]
  }

  case class BenefitSchemeAddressDetails(
      schemeAddressLine1: Option[SchemeAddressLine1],
      schemeAddressLine2: Option[SchemeAddressLine2],
      schemeAddressLocality: Option[SchemeAddressLocality],
      schemeAddressPostalTown: Option[SchemeAddressPostalTown],
      schemePostcode: Option[SchemePostcode]
  )

  object BenefitSchemeAddressDetails {

    implicit val benefitSchemeAddressDetailsReads: Reads[BenefitSchemeAddressDetails] =
      Json.reads[BenefitSchemeAddressDetails]

  }

  case class SchemeAddressDetails(
      schemeAddressSequenceNumber: SchemeAddressSequenceNumber,
      schemeContractedOutNumberDetails: SchemeContractedOutNumberDetails,
      schemeAddressType: Option[SchemeAddressType],
      schemeAddressStartDate: Option[SchemeAddressStartDate],
      schemeAddressEndDate: Option[SchemeAddressEndDate],
      country: Option[Country],
      areaDiallingCode: Option[AreaDiallingCode],
      schemeTelephoneNumber: Option[SchemeTelephoneNumber],
      benefitSchemeAddressDetails: Option[BenefitSchemeAddressDetails]
  )

  object SchemeAddressDetails {
    implicit val schemeAddressDetailsReads: Reads[SchemeAddressDetails] = Json.reads[SchemeAddressDetails]
  }

  case class BenefitSchemeDetails(
      currentOptimisticLock: CurrentOptimisticLock,
      schemeContractedOutNumberDetails: SchemeContractedOutNumberDetails,
      schemeInhibitionStatus: SchemeInhibitionStatus,
      magneticTapeNumber: Option[MagneticTapeNumber],
      schemeName: Option[BenefitSchemeName],
      schemeStartDate: Option[SchemeStartDate],
      schemeCessationDate: Option[SchemeCessationDate],
      contractedOutDeductionExtinguishedDate: Option[ContractedOutDeductionExtinguishedDate],
      paymentSuspensionDate: Option[PaymentSuspensionDate],
      recoveriesSuspendedDate: Option[RecoveriesSuspendedDate],
      paymentRestartDate: Option[PaymentRestartDate],
      recoveriesRestartedDate: Option[RecoveriesRestartedDate],
      schemeNature: Option[SchemeNature],
      benefitSchemeInstitution: Option[BenefitSchemeInstitutionType],
      accruedGMPLiabilityServiceDate: Option[AccruedGMPLiabilityServiceDate],
      rerouteToSchemeCessation: Option[RerouteToSchemeCessation],
      statementInhibitor: Option[StatementInhibitor],
      certificateCancellationDate: Option[CertificateCancellationDate],
      suspendedDate: Option[SuspendedDate],
      isleOfManInterest: Option[IsleOfManInterest],
      schemeWindingUp: Option[SchemeWindingUp],
      revaluationRateSequenceNumber: Option[RevaluationRateSequenceNumber],
      benefitSchemeStatus: Option[BenefitSchemeStatus],
      dateFormallyCertified: Option[DateFormallyCertified],
      privatePensionSchemeSanctionDate: Option[PrivatePensionSchemeSanctionDate],
      schemeConversionDate: Option[SchemeConversionDate],
      reconciliationDate: Option[ReconciliationDate]
  )

  object BenefitSchemeDetails {
    implicit val benefitSchemeDetailsReads: Reads[BenefitSchemeDetails] = Json.reads[BenefitSchemeDetails]
  }

  case class BenefitSchemeDetailsSuccessResponse(
      benefitSchemeDetails: BenefitSchemeDetails,
      schemeAddressDetailsList: List[SchemeAddressDetails]
  ) extends BenefitSchemeDetailsResponse
      with NpsSuccessfulApiResponse

  object BenefitSchemeDetailsSuccessResponse {

    implicit val benefitSchemeDetailsSuccessResponseReads: Reads[BenefitSchemeDetailsSuccessResponse] =
      Json.reads[BenefitSchemeDetailsSuccessResponse]

  }

}

object BenefitSchemeDetailsError {

  case class ErrorResourceObj400(
      reason: Reason,
      code: NpsErrorCode
  )

  object ErrorResourceObj400 {
    implicit val errorResourceObj400Reads: Reads[ErrorResourceObj400] = Json.reads[ErrorResourceObj400]
  }

  case class ErrorResponse400(
      failures: List[ErrorResourceObj400]
  )

  object ErrorResponse400 {
    implicit val errorResponse400Reads: Reads[ErrorResponse400] = Json.reads[ErrorResponse400]
  }

  case class ErrorResourceObj403Forbidden(
      reason: Reason,
      code: NpsErrorCode
  )

  object ErrorResourceObj403Forbidden {

    implicit val errorResourceObj403ForbiddenReads: Reads[ErrorResourceObj403Forbidden] =
      Json.reads[ErrorResourceObj403Forbidden]

  }

  case class ErrorResourceObj403UserNotAuthorised(
      reason: Reason,
      code: NpsErrorCode
  )

  object ErrorResourceObj403UserNotAuthorised {

    implicit val errorResourceObj403UserNotAuthorisedReads: Reads[ErrorResourceObj403UserNotAuthorised] =
      Json.reads[ErrorResourceObj403UserNotAuthorised]

  }

  case class ErrorResourceObj422(
      reason: Reason,
      code: NpsErrorCode
  )

  object ErrorResourceObj422 {
    implicit val errorResourceObj422Reads: Reads[ErrorResourceObj422] = Json.reads[ErrorResourceObj422]
  }

  case class ErrorResponse422(
      failures: List[ErrorResourceObj422]
  )

  object ErrorResponse422 {
    implicit val errorResponse422Reads: Reads[ErrorResponse422] = Json.reads[ErrorResponse422]
  }

}
