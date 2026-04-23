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

package uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class CreditSource(override val entryName: String) extends EnumEntry

object CreditSource extends Enum[CreditSource] with PlayJsonEnum[CreditSource] {
  val values: immutable.IndexedSeq[CreditSource] = findValues

  case object AdditionalStatutoryPaternityPay     extends CreditSource("ADDITIONAL STATUTORY PATERNITY PAY")
  case object AdpCentreLivingston                 extends CreditSource("ADP CENTRE LIVINGSTON")
  case object AdpNorthernIreland                  extends CreditSource("ADP NORTHERN IRELAND")
  case object AdpReading                          extends CreditSource("ADP READING")
  case object ApprovedTrainingCreditsInternal     extends CreditSource("APPROVED TRAINING CREDITS (INTERNAL)")
  case object AutoCreditsExternallyInput          extends CreditSource("AUTOCREDITS - EXTERNALLY INPUT")
  case object AutoCreditsInternallyCalculated     extends CreditSource("AUTOCREDITS - INTERNALLY CALCULATED")
  case object CarersAllowanceCa                   extends CreditSource("CARER'S ALLOWANCE (CA)")
  case object CarersCredit                        extends CreditSource("CARER'S CREDIT")
  case object CentralAward                        extends CreditSource("CENTRAL AWARD")
  case object ChildBenefit                        extends CreditSource("CHILD BENEFIT")
  case object DirectDebit                         extends CreditSource("DIRECT DEBIT")
  case object DptcGbEmp                           extends CreditSource("DPTC (GB) EMP")
  case object DptcGbsEmp                          extends CreditSource("DPTC (GB) S/EMP")
  case object DptcNiEmp                           extends CreditSource("DPTC (NI) EMP")
  case object DptcNisEmp                          extends CreditSource("DPTC (NI) S/EMP")
  case object DssLocalOffices                     extends CreditSource("DSS LOCAL OFFICES")
  case object DwaUnitBelfastEmployedEarners       extends CreditSource("DWA UNIT (BELFAST) EMPLOYED EARNERS")
  case object DwaUnitBelfastSelfEmployedEarners   extends CreditSource("DWA UNIT (BELFAST) SELF EMPLOYED EARNERS")
  case object DwaUnitBlackpoolEmployedEarners     extends CreditSource("DWA UNIT (BLACKPOOL) EMPLOYED EARNERS")
  case object DwaUnitBlackpoolSelfEmployedEarners extends CreditSource("DWA UNIT (BLACKPOOL) SELF EMPLOYED EARNERS")
  case object DisabledTaxCreditEmployed           extends CreditSource("Disabled Tax Credit (Employed)")
  case object DisabledTaxCreditSelfEmployed       extends CreditSource("Disabled Tax Credit (Self-employed)")
  case object EmploymentAndSupport                extends CreditSource("EMPLOYMENT AND SUPPORT")
  case object EmploymentServices                  extends CreditSource("EMPLOYMENT SERVICES")
  case object FamcGb                              extends CreditSource("FAMC-GB")
  case object FamcNi                              extends CreditSource("FAMC-NI")
  case object FamilyCreditGb                      extends CreditSource("FAMILY CREDIT (GB)")
  case object FamilyCreditNi                      extends CreditSource("FAMILY CREDIT (NI)")
  case object FosterCarer                         extends CreditSource("FOSTER CARER")
  case object HncipOrSda                          extends CreditSource("HNCIP OR SDA")
  case object HrpConversion                       extends CreditSource("HRP CONVERSION")
  case object Incapacity                          extends CreditSource("INCAPACITY")
  case object IsleOfMan                           extends CreditSource("ISLE OF MAN")
  case object JsaPaperInput                       extends CreditSource("JSA PAPER INPUT")
  case object JsaTapeInput                        extends CreditSource("JSA TAPE INPUT")

  case object JsaTapeInputAlternativeSbConditionMet
      extends CreditSource("JSA TAPE INPUT (ALTERNATIVE SB CONDITION MET)")

  case object JuryService                     extends CreditSource("JURY SERVICE")
  case object JuvenileCreditsInternal         extends CreditSource("JUVENILE CREDITS (INTERNAL)")
  case object LongTermScaleRate               extends CreditSource("LONG TERM SCALE RATE")
  case object ModSpouseCivilPartnersCredits   extends CreditSource("MoD Spouse/Civil Partner's Credits")
  case object NorthernIrelandSicknessCredits  extends CreditSource("NORTHERN IRELAND SICKNESS CREDITS")
  case object NotApplicable                   extends CreditSource("NOT APPLICABLE")
  case object NotKnown                        extends CreditSource("NOT KNOWN")
  case object Nubs2                           extends CreditSource("NUBS 2")
  case object Nubs2AlternativeSbConditionMet  extends CreditSource("NUBS 2 [ALTERNATIVE SB CONDITION MET]")
  case object PscsIncap                       extends CreditSource("PSCS INCAP")
  case object Post75ServiceSpousesCredit      extends CreditSource("Post 75 Service Spouses Credit")
  case object QuarterlyBilling                extends CreditSource("QUARTERLY BILLING")
  case object SharedParentalPay               extends CreditSource("SHARED PARENTAL PAY")
  case object SpecifiedAdultCarer             extends CreditSource("SPECIFIED ADULT CARER")
  case object StatutoryAdoptionPayCredit      extends CreditSource("STATUTORY ADOPTION PAY CREDIT")
  case object StatutoryMaternityPayCredit     extends CreditSource("STATUTORY MATERNITY PAY CREDIT")
  case object StatutoryParentalBereavementPay extends CreditSource("Statutory Parental Bereavement Pay")

  case object UnemployabilitySupplementCreditsInternal
      extends CreditSource("UNEMPLOYABILITY SUPPLEMENT CREDITS (INTERNAL)")

  case object UniversalCredit              extends CreditSource("UNIVERSAL CREDIT")
  case object WftcGb                       extends CreditSource("WFTC (GB)")
  case object WftcNi                       extends CreditSource("WFTC (NI)")
  case object WidowsCreditsInternal        extends CreditSource("WIDOWS CREDITS (INTERNAL)")
  case object WorkingTaxCreditEmployed     extends CreditSource("Working Tax Credit (Employed)")
  case object WorkingTaxCreditSelfEmployed extends CreditSource("Working Tax Credit (Self-employed)")
  case object CarerSupportPaymentCsp       extends CreditSource("CARER SUPPORT PAYMENT (CSP)")
  case object StatutoryNeonatalCarePay     extends CreditSource("Statutory Neonatal Care Pay")
}
