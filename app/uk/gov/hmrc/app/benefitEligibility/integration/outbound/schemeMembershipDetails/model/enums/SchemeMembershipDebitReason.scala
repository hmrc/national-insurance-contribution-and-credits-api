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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class SchemeMembershipDebitReason(override val entryName: String) extends EnumEntry

object SchemeMembershipDebitReason
    extends Enum[SchemeMembershipDebitReason]
    with PlayJsonEnum[SchemeMembershipDebitReason] {
  val values: immutable.IndexedSeq[SchemeMembershipDebitReason] = findValues

  case object AppShpAppCancelledFromOutset extends SchemeMembershipDebitReason("APP/SHP APP CANCELLED FROM OUTSET")
  case object AutoGmpIncorrect             extends SchemeMembershipDebitReason("AUTO GMP INCORRECT")
  case object BeneficiaryRemoved           extends SchemeMembershipDebitReason("BENEFICIARY REMOVED")

  case object CancellationOfChangeOfRpaBuyOut
      extends SchemeMembershipDebitReason("CANCELLATION OF CHANGE OF RPA/BUY OUT")

  case object Cancelled63DocketEntry         extends SchemeMembershipDebitReason("CANCELLED 63 DOCKET ENTRY")
  case object CepReplacingGmp                extends SchemeMembershipDebitReason("CEP REPLACING GMP")
  case object CepReplacingTransfer           extends SchemeMembershipDebitReason("CEP REPLACING TRANSFER")
  case object ChangeOfContributionsEarnings  extends SchemeMembershipDebitReason("CHANGE OF CONTRIBUTIONS/EARNINGS")
  case object ChangeOfDateOfBirth            extends SchemeMembershipDebitReason("CHANGE OF DATE OF BIRTH")
  case object ChangeOfRevaluationRate        extends SchemeMembershipDebitReason("CHANGE OF REVALUATION RATE")
  case object ChangeRpaReplacingTransfer     extends SchemeMembershipDebitReason("CHANGE RPA REPLACING TRANSFER")
  case object CodReplacingGmp                extends SchemeMembershipDebitReason("COD REPLACING GMP")
  case object CodGmpEnforcedInError          extends SchemeMembershipDebitReason("COD/GMP ENFORCED IN ERROR")
  case object CodGmpReplacingCep             extends SchemeMembershipDebitReason("COD/GMP REPLACING CEP")
  case object ConvertedFromNirs1EntryDebited extends SchemeMembershipDebitReason("CONVERTED FROM NIRS1 ENTRY DEBITED")
  case object DataIntegrityConversionError   extends SchemeMembershipDebitReason("DATA INTEGRITY CONVERSION ERROR")
  case object DebitGmpConversion             extends SchemeMembershipDebitReason("DEBIT GMP CONVERSION")

  case object DebitOfIncorrectChangeOfRpaBuyOut
      extends SchemeMembershipDebitReason("DEBIT OF INCORRECT CHANGE OF RPA/BUY OUT")

  case object GmpReplacingCod         extends SchemeMembershipDebitReason("GMP REPLACING COD")
  case object GmpCodReplacingTransfer extends SchemeMembershipDebitReason("GMP/COD REPLACING TRANSFER")

  case object IncorrectEconSconOnNoticeOfTermination
      extends SchemeMembershipDebitReason("INCORRECT ECON/SCON ON NOTICE OF TERMINATION")

  case object NonSequentialTransfer extends SchemeMembershipDebitReason("NON-SEQUENTIAL TRANSFER")
  case object NotApplicable         extends SchemeMembershipDebitReason("NOT APPLICABLE")

  case object NoticeOfTerminationSentInErrorCancelled
      extends SchemeMembershipDebitReason("NOTICE OF TERMINATION SENT IN ERROR/CANCELLED")

  case object OtherReason       extends SchemeMembershipDebitReason("OTHER REASON")
  case object PeriodIncorrect   extends SchemeMembershipDebitReason("PERIOD INCORRECT")
  case object PpDebitAndReEntry extends SchemeMembershipDebitReason("PP DEBIT AND RE-ENTRY")

  case object PseudoTransferReplacingPreviousNoticeOfTermination
      extends SchemeMembershipDebitReason("PSEUDO TRANSFER REPLACING PREVIOUS NOTICE OF TERMINATION")

  case object SchemeCessationCase12PercentReplacedByS148AndViceVersa
      extends SchemeMembershipDebitReason("SCHEME CESSATION CASE 12% REPLACED BY S148 AND VICE VERSA")

  case object SplitRightsReplacingPreviousNoticeOfTermination
      extends SchemeMembershipDebitReason("SPLIT RIGHTS REPLACING PREVIOUS NOTICE OF TERMINATION")

  case object TerminationNoticeReceivedFromSchemeReplacingEnforcedCodGmp
      extends SchemeMembershipDebitReason("TERMINATION NOTICE RECEIVED FROM SCHEME REPLACING ENFORCED COD/GMP")

  case object TerminationNoticeToBeInputUsingControlledEarnings
      extends SchemeMembershipDebitReason("TERMINATION NOTICE TO BE INPUT USING CONTROLLED EARNINGS")

  case object TerminationOnWrongAccount  extends SchemeMembershipDebitReason("TERMINATION ON WRONG ACCOUNT")
  case object TransferReplacingCep       extends SchemeMembershipDebitReason("TRANSFER REPLACING CEP")
  case object TransferReplacingChangeRpa extends SchemeMembershipDebitReason("TRANSFER REPLACING CHANGE RPA")
  case object TransferReplacingGmp       extends SchemeMembershipDebitReason("TRANSFER REPLACING GMP")

  case object TransferRpaCodDebitedToProcessEarlierTransfer
      extends SchemeMembershipDebitReason("TRANSFER/RPA/COD DEBITED TO PROCESS EARLIER TRANSFER")

  case object TwoOrMoreGmpsToOneAscn extends SchemeMembershipDebitReason("TWO OR MORE GMPS TO ONE ASCN")
}
