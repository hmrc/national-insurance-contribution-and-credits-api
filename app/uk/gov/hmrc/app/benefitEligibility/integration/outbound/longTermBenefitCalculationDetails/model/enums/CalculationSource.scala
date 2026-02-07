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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class CalculationSource(override val entryName: String) extends EnumEntry

object CalculationSource extends Enum[CalculationSource] with PlayJsonEnum[CalculationSource] {
  val values: immutable.IndexedSeq[CalculationSource] = findValues

  case object ApComponentSuspectAprilMayCalc    extends CalculationSource("AP COMPONENT SUSPECT (APRIL - MAY CALC)")
  case object AwardReconciled                   extends CalculationSource("AWARD RECONCILED")
  case object AwardReconciliationRevisedUpwards extends CalculationSource("AWARD RECONCILIATION - REVISED UPWARDS")
  case object InterimWbCalculation              extends CalculationSource("INTERIM WB CALCULATION")
  case object IvbUprating                       extends CalculationSource("IVB UPRATING")
  case object NotKnown                          extends CalculationSource("NOT KNOWN")

  case object ReconciliationFailedAwardRevisedDownwards
      extends CalculationSource("RECONCILIATION FAILED - AWARD REVISED DOWNWARDS")

  case object RequestedBenefitCalculation     extends CalculationSource("REQUESTED BENEFIT CALCULATION")
  case object RpCalculationClaimantReachedDpa extends CalculationSource("RP CALCULATION - CLAIMANT REACHED DPA")
  case object RpCalculationClaimantReachedMpa extends CalculationSource("RP CALCULATION - CLAIMANT REACHED MPA")

  case object RpWbBbCalculationFedbackDetailsReceived
      extends CalculationSource("RP/WB/BB CALCULATION - FEDBACK DETAILS RECEIVED")

  case object SchemeReconciliationServiceAccount extends CalculationSource("SCHEME RECONCILIATION SERVICE ACCOUNT")

  case object SrbIssuedChangeToFlatRateRecord1948
      extends CalculationSource("SRB ISSUED - CHANGE TO FLAT RATE RECORD (<1948)")

  case object SrbIssuedChangeToGrbRecord extends CalculationSource("SRB ISSUED - CHANGE TO GRB RECORD")

  case object SrbIssuedContributionCreditChangeInFryTaxYear
      extends CalculationSource("SRB ISSUED - CONTRIBUTION/CREDIT CHANGE IN FRY TAX YEAR")

  case object SrbIssuedContributionCreditChangeInPreFryTaxYear
      extends CalculationSource("SRB ISSUED - CONTRIBUTION/CREDIT CHANGE IN PRE-FRY TAX YEAR")

  case object SrbIssuedDateOfBirthChangeWidow extends CalculationSource("SRB ISSUED - DATE OF BIRTH CHANGE (WIDOW)")
  case object SrbIssuedHrpInvolvement         extends CalculationSource("SRB ISSUED - HRP INVOLVEMENT")

  case object SrbIssuedPersonalDetailsChangeAccountHolder
      extends CalculationSource("SRB ISSUED - PERSONAL DETAILS CHANGE (ACCOUNT HOLDER)")

  case object SrbIssuedRetrospectivePpOptionReceived
      extends CalculationSource("SRB ISSUED - RETROSPECTIVE PP OPTION RECEIVED")

  case object SrbIssuedS2pEntitlementChange extends CalculationSource("SRB ISSUED - S2P ENTITLEMENT CHANGE")
  case object SrbIssuedSspReceived          extends CalculationSource("SRB ISSUED - SSP RECEIVED")

  case object SrbIssuedSubstitutionInheritanceDetailsRevised
      extends CalculationSource("SRB ISSUED - SUBSTITUTION/INHERITANCE DETAILS REVISED")

  case object SrbIssuedTerminationDebitReceived extends CalculationSource("SRB ISSUED - TERMINATION DEBIT RECEIVED")

}
