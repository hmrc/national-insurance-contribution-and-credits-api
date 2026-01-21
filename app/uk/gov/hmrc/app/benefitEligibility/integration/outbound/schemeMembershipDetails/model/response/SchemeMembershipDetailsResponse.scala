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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response

import enumeratum.EnumEntry.*
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait SchemeMembershipDetailsResponse extends NpsApiResponse

object SchemeMembershipDetailsError {

  // region Error400

  case class SchemeMembershipDetailsError400(reason: Reason, code: NpsErrorCode400) extends NpsApiResponse

  object SchemeMembershipDetailsError400 {

    implicit val npsErrorResponse400Reads: Reads[SchemeMembershipDetailsError400] =
      Json.reads[SchemeMembershipDetailsError400]

  }

  case class SchemeMembershipDetailsErrorResponse400(failures: List[SchemeMembershipDetailsError400])
      extends SchemeMembershipDetailsResponse

  object SchemeMembershipDetailsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[SchemeMembershipDetailsErrorResponse400] =
      Json.reads[SchemeMembershipDetailsErrorResponse400]

  }

  case class SchemeMembershipDetailsErrorResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends SchemeMembershipDetailsResponse
      with NpsApiResponse

  object SchemeMembershipDetailsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[SchemeMembershipDetailsErrorResponse403] =
      Json.reads[SchemeMembershipDetailsErrorResponse403]

  }

  case class SchemeMembershipDetailsError422(reason: Reason, code: ErrorCode422)

  object SchemeMembershipDetailsError422 {

    implicit val NpsErrorResponse422Reads: Reads[SchemeMembershipDetailsError422] =
      Json.reads[SchemeMembershipDetailsError422]

  }

  case class SchemeMembershipDetailsErrorResponse422(failures: List[SchemeMembershipDetailsError422])
      extends SchemeMembershipDetailsResponse

  object SchemeMembershipDetailsErrorResponse422 {

    implicit val npsFailureResponse422Reads: Reads[SchemeMembershipDetailsErrorResponse422] =
      Json.reads[SchemeMembershipDetailsErrorResponse422]

  }

}

object SchemeMembershipDetailsSuccess {
  case class SchemeMembershipSequenceNumber(value: Int) extends AnyVal

  object SchemeMembershipSequenceNumber {
    implicit val reads: Format[SchemeMembershipSequenceNumber] = Json.valueFormat[SchemeMembershipSequenceNumber]
  }

  case class SchemeMembershipTransferSequenceNumber(value: Int) extends AnyVal

  object SchemeMembershipTransferSequenceNumber {

    implicit val reads: Format[SchemeMembershipTransferSequenceNumber] =
      Json.valueFormat[SchemeMembershipTransferSequenceNumber]

  }

  case class SchemeMembershipOccurrenceNumber(value: Int) extends AnyVal

  object SchemeMembershipOccurrenceNumber {
    implicit val reads: Format[SchemeMembershipOccurrenceNumber] = Json.valueFormat[SchemeMembershipOccurrenceNumber]
  }

  case class ContractedOutEmployerIdentifier(value: Int) extends AnyVal

  object ContractedOutEmployerIdentifier {
    implicit val reads: Format[ContractedOutEmployerIdentifier] = Json.valueFormat[ContractedOutEmployerIdentifier]
  }

  case class TerminationMicrofilmNumber(value: Int) extends AnyVal

  object TerminationMicrofilmNumber {
    implicit val reads: Format[TerminationMicrofilmNumber] = Json.valueFormat[TerminationMicrofilmNumber]
  }

  case class EmployeesReference(value: String) extends AnyVal

  object EmployeesReference {
    implicit val reads: Format[EmployeesReference] = Json.valueFormat[EmployeesReference]
  }

  case class EmployersContractedOutNumberDetails(value: String) extends AnyVal

  object EmployersContractedOutNumberDetails {

    implicit val reads: Format[EmployersContractedOutNumberDetails] =
      Json.valueFormat[EmployersContractedOutNumberDetails]

  }

  case class DebitVoucherMicrofilmNumber(value: Int) extends AnyVal

  object DebitVoucherMicrofilmNumber {
    implicit val reads: Format[DebitVoucherMicrofilmNumber] = Json.valueFormat[DebitVoucherMicrofilmNumber]
  }

  case class CreationMicrofilmNumber(value: Int) extends AnyVal

  object CreationMicrofilmNumber {
    implicit val reads: Format[CreationMicrofilmNumber] = Json.valueFormat[CreationMicrofilmNumber]
  }

  case class CallbackURL(value: String) extends AnyVal

  object CallbackURL {
    implicit val reads: Format[CallbackURL] = Json.valueFormat[CallbackURL]
  }

  case class ErrorResourceObj400(
      reason: String,
      code: String
  )

  object ErrorResourceObj400 {
    implicit val reads: Reads[ErrorResourceObj400] = Json.reads[ErrorResourceObj400]
  }

  case class ErrorResponse400(
      failures: List[ErrorResourceObj400]
  )

  object ErrorResponse400 {
    implicit val reads: Reads[ErrorResponse400] = Json.reads[ErrorResponse400]
  }

  case class ErrorResourceObj403Forbidden(
      reason: String,
      code: String
  )

  object ErrorResourceObj403Forbidden {
    implicit val reads: Reads[ErrorResourceObj403Forbidden] = Json.reads[ErrorResourceObj403Forbidden]
  }

  case class ErrorResourceObj403UserNotAuthorised(
      reason: String,
      code: String
  )

  object ErrorResourceObj403UserNotAuthorised {
    implicit val reads: Reads[ErrorResourceObj403UserNotAuthorised] = Json.reads[ErrorResourceObj403UserNotAuthorised]
  }

  case class ErrorResourceObj422(
      reason: String,
      code: String
  )

  object ErrorResourceObj422 {
    implicit val reads: Reads[ErrorResourceObj422] = Json.reads[ErrorResourceObj422]
  }

  case class ErrorResponse422(
      failures: List[ErrorResourceObj422]
  )

  object ErrorResponse422 {
    implicit val reads: Reads[ErrorResponse422] = Json.reads[ErrorResponse422]
  }

  // Main data case classes
  case class Callback(
      callbackURL: Option[CallbackURL]
  )

  object Callback {
    implicit val reads: Reads[Callback] = Json.reads[Callback]
  }

  case class SchemeMembershipStartDate(value: LocalDate) extends AnyVal

  case object SchemeMembershipStartDate {
    implicit val reads: Format[SchemeMembershipStartDate] = Json.valueFormat[SchemeMembershipStartDate]
  }

  case class SchemeMembershipEndDate(value: LocalDate) extends AnyVal

  case object SchemeMembershipEndDate {
    implicit val reads: Format[SchemeMembershipEndDate] = Json.valueFormat[SchemeMembershipEndDate]
  }

  case class TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(value: BigDecimal) extends AnyVal

  case object TotalLinkedGuaranteedMinimumPensionContractedOutDeductions {

    implicit val totalLinkedGuaranteedMinimumPensionContractedOutDeductionsFormat
        : Format[TotalLinkedGuaranteedMinimumPensionContractedOutDeductions] =
      Json.valueFormat[TotalLinkedGuaranteedMinimumPensionContractedOutDeductions]

  }

  case class AccruedPensionContractedOutDeductionsValue(value: BigDecimal) extends AnyVal

  case object AccruedPensionContractedOutDeductionsValue {

    implicit val reads: Format[AccruedPensionContractedOutDeductionsValue] =
      Json.valueFormat[AccruedPensionContractedOutDeductionsValue]

  }

  case class TechnicalAmount(value: BigDecimal) extends AnyVal

  object TechnicalAmount {
    implicit val reads: Format[TechnicalAmount] = Json.valueFormat[TechnicalAmount]
  }

  case class RevaluationApplied(value: Boolean) extends AnyVal

  object RevaluationApplied {
    implicit val reads: Format[RevaluationApplied] = Json.valueFormat[RevaluationApplied]
  }

  case class InhibitSchemeProcessing(value: Boolean) extends AnyVal

  object InhibitSchemeProcessing {
    implicit val reads: Format[InhibitSchemeProcessing] = Json.valueFormat[InhibitSchemeProcessing]
  }

  case class GuaranteedMinimumPensionConversionApplied(value: Boolean) extends AnyVal

  object GuaranteedMinimumPensionConversionApplied {

    implicit val guaranteedMinimumPensionConversionAppliedFormat: Format[GuaranteedMinimumPensionConversionApplied] =
      Json.valueFormat[GuaranteedMinimumPensionConversionApplied]

  }

  // LocalDate wrapper types
  case class TransferPremiumElectionDate(value: LocalDate) extends AnyVal

  object TransferPremiumElectionDate {
    implicit val reads: Format[TransferPremiumElectionDate] = Json.valueFormat[TransferPremiumElectionDate]
  }

  case class ExtensionDate(value: LocalDate) extends AnyVal

  object ExtensionDate {
    implicit val reads: Format[ExtensionDate] = Json.valueFormat[ExtensionDate]
  }

  case class TransferTakeUpDate(value: LocalDate) extends AnyVal

  object TransferTakeUpDate {
    implicit val reads: Format[TransferTakeUpDate] = Json.valueFormat[TransferTakeUpDate]
  }

  case class ProtectedRightsStartDate(value: LocalDate) extends AnyVal

  object ProtectedRightsStartDate {
    implicit val reads: Format[ProtectedRightsStartDate] = Json.valueFormat[ProtectedRightsStartDate]
  }

  case class TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(value: BigDecimal) extends AnyVal

  object TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988 {

    implicit val totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988Format
        : Format[TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988] =
      Json.valueFormat[TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988]

  }

  case class AccruedPensionContractedOutDeductionsValuePost1988(value: BigDecimal) extends AnyVal

  object AccruedPensionContractedOutDeductionsValuePost1988 {

    implicit val reads: Format[AccruedPensionContractedOutDeductionsValuePost1988] =
      Json.valueFormat[AccruedPensionContractedOutDeductionsValuePost1988]

  }

  case class FinalYearEarnings(value: BigDecimal) extends AnyVal

  object FinalYearEarnings {
    implicit val reads: Format[FinalYearEarnings] = Json.valueFormat[FinalYearEarnings]
  }

  case class PenultimateYearEarnings(value: BigDecimal) extends AnyVal

  object PenultimateYearEarnings {
    implicit val reads: Format[PenultimateYearEarnings] = Json.valueFormat[PenultimateYearEarnings]
  }

  case class RetrospectiveEarnings(value: BigDecimal) extends AnyVal

  object RetrospectiveEarnings {
    implicit val reads: Format[RetrospectiveEarnings] = Json.valueFormat[RetrospectiveEarnings]
  }

  case class StateEarningsRelatedPensionsSchemeNonRestorationValue(value: BigDecimal) extends AnyVal

  object StateEarningsRelatedPensionsSchemeNonRestorationValue {

    implicit val reads: Format[StateEarningsRelatedPensionsSchemeNonRestorationValue] =
      Json.valueFormat[StateEarningsRelatedPensionsSchemeNonRestorationValue]

  }

  case class StateEarningsRelatedPensionsSchemeValuePost1988(value: BigDecimal) extends AnyVal

  object StateEarningsRelatedPensionsSchemeValuePost1988 {

    implicit val reads: Format[StateEarningsRelatedPensionsSchemeValuePost1988] =
      Json.valueFormat[StateEarningsRelatedPensionsSchemeValuePost1988]

  }

  case class GuaranteedMinimumPensionContractedOutDeductionsRevalued(value: BigDecimal) extends AnyVal

  object GuaranteedMinimumPensionContractedOutDeductionsRevalued {

    implicit val reads: Format[GuaranteedMinimumPensionContractedOutDeductionsRevalued] =
      Json.valueFormat[GuaranteedMinimumPensionContractedOutDeductionsRevalued]

  }

  case class ClericallyControlledTotal(value: BigDecimal) extends AnyVal

  object ClericallyControlledTotal {
    implicit val reads: Format[ClericallyControlledTotal] = Json.valueFormat[ClericallyControlledTotal]
  }

  case class ClericallyControlledTotalPost1988(value: BigDecimal) extends AnyVal

  object ClericallyControlledTotalPost1988 {
    implicit val reads: Format[ClericallyControlledTotalPost1988] = Json.valueFormat[ClericallyControlledTotalPost1988]
  }

  case class CertifiedAmount(value: BigDecimal) extends AnyVal

  object CertifiedAmount {
    implicit val reads: Format[CertifiedAmount] = Json.valueFormat[CertifiedAmount]
  }

  case class MinimumFundTransferAmount(value: BigDecimal) extends AnyVal

  object MinimumFundTransferAmount {
    implicit val reads: Format[MinimumFundTransferAmount] = Json.valueFormat[MinimumFundTransferAmount]
  }

  case class ActualTransferValue(value: BigDecimal) extends AnyVal

  object ActualTransferValue {
    implicit val reads: Format[ActualTransferValue] = Json.valueFormat[ActualTransferValue]
  }

  // String wrapper types for the remaining String fields
  case class SchemeCreatingContractedOutNumberDetails(value: String) extends AnyVal

  object SchemeCreatingContractedOutNumberDetails {

    implicit val reads: Format[SchemeCreatingContractedOutNumberDetails] =
      Json.valueFormat[SchemeCreatingContractedOutNumberDetails]

  }

  case class SchemeTerminatingContractedOutNumberDetails(value: String) extends AnyVal

  object SchemeTerminatingContractedOutNumberDetails {

    implicit val reads: Format[SchemeTerminatingContractedOutNumberDetails] =
      Json.valueFormat[SchemeTerminatingContractedOutNumberDetails]

  }

  case class ImportingAppropriateSchemeNumberDetails(value: String) extends AnyVal

  object ImportingAppropriateSchemeNumberDetails {

    implicit val reads: Format[ImportingAppropriateSchemeNumberDetails] =
      Json.valueFormat[ImportingAppropriateSchemeNumberDetails]

  }

  case class ApparentUnnotifiedTerminationDestinationDetails(value: String) extends AnyVal

  object ApparentUnnotifiedTerminationDestinationDetails {

    implicit val reads: Format[ApparentUnnotifiedTerminationDestinationDetails] =
      Json.valueFormat[ApparentUnnotifiedTerminationDestinationDetails]

  }

  // Updated case class with all wrapped types
  case class SchemeMembershipDetails(
      nationalInsuranceNumber: Identifier,
      schemeMembershipSequenceNumber: SchemeMembershipSequenceNumber,
      schemeMembershipOccurrenceNumber: SchemeMembershipOccurrenceNumber,
      schemeMembershipStartDate: Option[SchemeMembershipStartDate],
      contractedOutEmployerIdentifier: Option[ContractedOutEmployerIdentifier],
      schemeMembershipEndDate: Option[SchemeMembershipEndDate],
      methodOfPreservationType: Option[MethodOfPreservation],
      totalLinkedGuaranteedMinimumPensionContractedOutDeductions: Option[
        TotalLinkedGuaranteedMinimumPensionContractedOutDeductions
      ],
      accruedPensionContractedOutDeductionsValue: Option[AccruedPensionContractedOutDeductionsValue],
      totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988: Option[
        TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988
      ],
      accruedPensionContractedOutDeductionsValuePost1988: Option[AccruedPensionContractedOutDeductionsValuePost1988],
      revaluationRate: Option[RevaluationRate],
      guaranteedMinimumPensionReconciliationStatus: Option[GuaranteedMinimumPensionReconciliationStatus],
      employeesReference: Option[EmployeesReference],
      finalYearEarnings: Option[FinalYearEarnings],
      penultimateYearEarnings: Option[PenultimateYearEarnings],
      retrospectiveEarnings: Option[RetrospectiveEarnings],
      furtherPaymentsConfirmation: Option[FurtherPaymentsConfirmation],
      survivorStatus: Option[SurvivorStatus],
      transferPremiumElectionDate: Option[TransferPremiumElectionDate],
      revaluationApplied: Option[RevaluationApplied],
      stateEarningsRelatedPensionsSchemeNonRestorationValue: Option[
        StateEarningsRelatedPensionsSchemeNonRestorationValue
      ],
      stateEarningsRelatedPensionsSchemeValuePost1988: Option[StateEarningsRelatedPensionsSchemeValuePost1988],
      apparentUnnotifiedTerminationStatus: Option[ApparentUnnotifiedTerminationStatus],
      terminationMicrofilmNumber: Option[TerminationMicrofilmNumber],
      debitVoucherMicrofilmNumber: Option[DebitVoucherMicrofilmNumber],
      creationMicrofilmNumber: Option[CreationMicrofilmNumber],
      inhibitSchemeProcessing: Option[InhibitSchemeProcessing],
      extensionDate: Option[ExtensionDate],
      guaranteedMinimumPensionContractedOutDeductionsRevalued: Option[
        GuaranteedMinimumPensionContractedOutDeductionsRevalued
      ],
      clericalCalculationInvolved: Option[Clercalc],
      clericallyControlledTotal: Option[ClericallyControlledTotal],
      clericallyControlledTotalPost1988: Option[ClericallyControlledTotalPost1988],
      certifiedAmount: Option[CertifiedAmount],
      enforcementStatus: Option[Enfcment],
      stateSchemePremiumDeemed: Option[SspDeem],
      transferTakeUpDate: Option[TransferTakeUpDate],
      schemeMembershipTransferSequenceNumber: Option[SchemeMembershipTransferSequenceNumber],
      contributionCategoryFinalYear: Option[ContCatLetter],
      contributionCategoryPenultimateYear: Option[ContCatLetter],
      contributionCategoryRetrospectiveYear: Option[ContCatLetter],
      protectedRightsStartDate: Option[ProtectedRightsStartDate],
      schemeMembershipDebitReason: Option[SchemeMembershipDebitReason],
      technicalAmount: Option[TechnicalAmount],
      minimumFundTransferAmount: Option[MinimumFundTransferAmount],
      actualTransferValue: Option[ActualTransferValue],
      schemeSuspensionType: Option[SchemeSuspensionType],
      guaranteedMinimumPensionConversionApplied: Option[GuaranteedMinimumPensionConversionApplied],
      employersContractedOutNumberDetails: Option[EmployersContractedOutNumberDetails],
      schemeCreatingContractedOutNumberDetails: Option[SchemeCreatingContractedOutNumberDetails],
      schemeTerminatingContractedOutNumberDetails: Option[SchemeTerminatingContractedOutNumberDetails],
      importingAppropriateSchemeNumberDetails: Option[ImportingAppropriateSchemeNumberDetails],
      apparentUnnotifiedTerminationDestinationDetails: Option[ApparentUnnotifiedTerminationDestinationDetails]
  )

  object SchemeMembershipDetails {
    implicit val reads: Reads[SchemeMembershipDetails] = Json.reads[SchemeMembershipDetails]
  }

  case class SchemeMembershipDetailsSummary(
      stakeholderPensionSchemeType: StakeholderPensionSchemeType,
      schemeMembershipDetails: SchemeMembershipDetails
  )

  object SchemeMembershipDetailsSummary {
    implicit val reads: Reads[SchemeMembershipDetailsSummary] = Json.reads[SchemeMembershipDetailsSummary]
  }

  case class SchemeMembershipDetailsSuccessResponse(
      schemeMembershipDetailsSummaryList: Option[List[SchemeMembershipDetailsSummary]],
      callback: Option[Callback]
  ) extends SchemeMembershipDetailsResponse
      with NpsSuccessfulApiResponse

  object SchemeMembershipDetailsSuccessResponse {

    implicit val reads: Reads[SchemeMembershipDetailsSuccessResponse] =
      Json.reads[SchemeMembershipDetailsSuccessResponse]

  }

}
