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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class CreditSourceType(override val entryName: String) extends EnumEntry

object CreditSourceType extends Enum[CreditSourceType] with PlayJsonEnum[CreditSourceType] {
  val values: immutable.IndexedSeq[CreditSourceType] = findValues

  case object NotKnown extends CreditSourceType("NOT KNOWN")

  case object AdpCentreLivingston extends CreditSourceType("ADP CENTRE LIVINGSTON")

  case object DssLocalOffices extends CreditSourceType("DSS LOCAL OFFICES")

  case object EmploymentServices extends CreditSourceType("EMPLOYMENT SERVICES")

  case object DirectDebit extends CreditSourceType("DIRECT DEBIT")

  case object CarersAllowance extends CreditSourceType("CARER'S ALLOWANCE (CA)")

  case object HncipOrSda extends CreditSourceType("HNCIP OR SDA")

  case object NorthernIrelandSicknessCredits extends CreditSourceType("NORTHERN IRELAND SICKNESS CREDITS")

  case object JuryService extends CreditSourceType("JURY SERVICE")

  case object Nubs2 extends CreditSourceType("NUBS 2")

  case object Nubs2AlternativeSbConditionMet extends CreditSourceType("NUBS 2 [ALTERNATIVE SB CONDITION MET]")

  case object DwaUnitBlackpoolEmployedEarners extends CreditSourceType("DWA UNIT (BLACKPOOL) EMPLOYED EARNERS")

  case object DwaUnitBlackpoolSelfEmployedEarners extends CreditSourceType("DWA UNIT (BLACKPOOL) SELF EMPLOYED EARNERS")

  case object DwaUnitBelfastEmployedEarners extends CreditSourceType("DWA UNIT (BELFAST) EMPLOYED EARNERS")

  case object DwaUnitBelfastSelfEmployedEarners extends CreditSourceType("DWA UNIT (BELFAST) SELF EMPLOYED EARNERS")

  case object IsleOfMan extends CreditSourceType("ISLE OF MAN")

  case object AutocreditsInternallyCalculated extends CreditSourceType("AUTOCREDITS - INTERNALLY CALCULATED")

  case object AutocreditsExternallyInput extends CreditSourceType("AUTOCREDITS - EXTERNALLY INPUT")

  case object JuvenileCreditsInternal extends CreditSourceType("JUVENILE CREDITS (INTERNAL)")

  case object WidowsCreditsInternal extends CreditSourceType("WIDOWS CREDITS (INTERNAL)")

  case object ApprovedTrainingCreditsInternal extends CreditSourceType("APPROVED TRAINING CREDITS (INTERNAL)")

  case object UnemployabilitySupplementCreditsInternal
      extends CreditSourceType("UNEMPLOYABILITY SUPPLEMENT CREDITS (INTERNAL)")

  case object PscsIncap extends CreditSourceType("PSCS INCAP")

  case object LongTermScaleRate extends CreditSourceType("LONG TERM SCALE RATE")

  case object AdpReading extends CreditSourceType("ADP READING")

  case object NotApplicable extends CreditSourceType("NOT APPLICABLE")

  case object QuarterlyBilling extends CreditSourceType("QUARTERLY BILLING")

  case object JsaTapeInput extends CreditSourceType("JSA TAPE INPUT")

  case object JsaTapeInputAlternativeSbConditionMet
      extends CreditSourceType("JSA TAPE INPUT (ALTERNATIVE SB CONDITION MET)")

  case object JsaPaperInput extends CreditSourceType("JSA PAPER INPUT")

  case object FamcNi extends CreditSourceType("FAMC-NI")

  case object FamcGb extends CreditSourceType("FAMC-GB")

  case object CentralAward extends CreditSourceType("CENTRAL AWARD")

  case object AdpNorthernIreland extends CreditSourceType("ADP NORTHERN IRELAND")

  case object FamilyCreditGb extends CreditSourceType("FAMILY CREDIT (GB)")

  case object FamilyCreditNi extends CreditSourceType("FAMILY CREDIT (NI)")

  case object Incapacity extends CreditSourceType("INCAPACITY")

  case object DptcGbEmp extends CreditSourceType("DPTC (GB) EMP")

  case object DptcGbSEmp extends CreditSourceType("DPTC (GB) S/EMP")

  case object DptcNiEmp extends CreditSourceType("DPTC (NI) EMP")

  case object DptcNiSEmp extends CreditSourceType("DPTC (NI) S/EMP")

  case object WftcGb extends CreditSourceType("WFTC (GB)")

  case object WftcNi extends CreditSourceType("WFTC (NI)")

  case object StatutoryMaternityPayCredit extends CreditSourceType("STATUTORY MATERNITY PAY CREDIT")

  case object StatutoryAdoptionPayCredit extends CreditSourceType("STATUTORY ADOPTION PAY CREDIT")

  case object DisabledTaxCreditEmployed extends CreditSourceType("DISABLED TAX CREDIT (EMPLOYED)")

  case object DisabledTaxCreditSelfEmployed extends CreditSourceType("DISABLED TAX CREDIT (SELF-EMPLOYED)")

  case object WorkingTaxCreditEmployed extends CreditSourceType("WORKING TAX CREDIT (EMPLOYED)")

  case object WorkingTaxCreditSelfEmployed extends CreditSourceType("WORKING TAX CREDIT (SELF-EMPLOYED)")

  case object EmploymentAndSupport extends CreditSourceType("EMPLOYMENT AND SUPPORT")

  case object HrpConversion extends CreditSourceType("HRP CONVERSION")

  case object ChildBenefit extends CreditSourceType("CHILD BENEFIT")

  case object CarersCredit extends CreditSourceType("CARER'S CREDIT")

  case object FosterCarer extends CreditSourceType("FOSTER CARER")

  case object ModSpouseCivilPartnersCredits extends CreditSourceType("MOD SPOUSE/CIVIL PARTNER'S CREDITS")

  case object AdditionalStatutoryPaternityPay extends CreditSourceType("ADDITTIONAL STATUTORY PATERNITY PAY")

  case object SpecifiedAdultCarer extends CreditSourceType("SPECIFIED ADULT CARER")

  case object UniversalCredit extends CreditSourceType("UNIVERSAL CREDIT")

  case object SharedParentalPay extends CreditSourceType("SHARED PARENTAL PAY")

  case object Post75ServiceSpousesCredit extends CreditSourceType("POST 75 SERVICE SPOUSES CREDIT")

  case object StatutoryParentalBereavementPay extends CreditSourceType("STATUTORY PARENTAL BEREAVEMENT PAY")
}
