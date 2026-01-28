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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class EnumLiabtp(override val entryName: String) extends EnumEntry

object EnumLiabtp extends Enum[EnumLiabtp] with PlayJsonEnum[EnumLiabtp] {
  val values: immutable.IndexedSeq[EnumLiabtp] = findValues

  case object Abroad                    extends EnumLiabtp("ABROAD")
  case object AbsoluteWaiver            extends EnumLiabtp("ABSOLUTE WAIVER")
  case object AllLiabilities            extends EnumLiabtp("ALL LIABILITIES")
  case object ApprovedTraining          extends EnumLiabtp("APPROVED TRAINING")
  case object CaS2p                     extends EnumLiabtp("CA (S2P)")
  case object Cl2EcBilateralRegs        extends EnumLiabtp("CL2 EC/BILATERAL REGS")
  case object Class2ConvertedFromNirs1  extends EnumLiabtp("CLASS 2 CONVERTED FROM NIRS1")
  case object Class2insa                extends EnumLiabtp("CLASS 2 IN SA")
  case object Class2LiabilityUk         extends EnumLiabtp("CLASS 2 LIABILITY (UK)")
  case object Class2MaternityAllowance  extends EnumLiabtp("CLASS 2 MATERNITY ALLOWANCE")
  case object Class2VoluntaryUk         extends EnumLiabtp("CLASS 2 VOLUNTARY UK")
  case object Class3Uk                  extends EnumLiabtp("CLASS 3 (UK)")
  case object Class3BillingOverride     extends EnumLiabtp("CLASS 3 BILLING OVERRIDE")
  case object Class3ConvertedFromNirs1  extends EnumLiabtp("CLASS 3 CONVERTED FROM NIRS1")
  case object CodedOut                  extends EnumLiabtp("CODED OUT")
  case object ContinuingClass1Liability extends EnumLiabtp("CONTINUING CLASS 1 LIABILITY")
  case object ContinuingClass2Liability extends EnumLiabtp("CONTINUING CLASS 2 LIABILITY")

  case object ContributorInitiatedNonLiabilityOverride
      extends EnumLiabtp("CONTRIBUTOR INITIATED NON-LIABILITY OVERRIDE")

  case object CriminalProceedings                 extends EnumLiabtp("CRIMINAL PROCEEDINGS")
  case object CrsRo                               extends EnumLiabtp("CRS/RO")
  case object CarerOfAaCaaDlaRecipient            extends EnumLiabtp("Carer of AA/CAA/DLA recipient")
  case object CarerSupportPayment                 extends EnumLiabtp("Carer Support Payment")
  case object CarersAllowance                     extends EnumLiabtp("Carer's Allowance")
  case object CarersCredit                        extends EnumLiabtp("Carer's Credit")
  case object ChildBenefit                        extends EnumLiabtp("Child Benefit")
  case object Class2BillingOverride               extends EnumLiabtp("Class 2 Billing Override")
  case object DefermentType1                      extends EnumLiabtp("DEFERMENT TYPE 1")
  case object DefermentType2                      extends EnumLiabtp("DEFERMENT TYPE 2")
  case object DefermentType3                      extends EnumLiabtp("DEFERMENT TYPE 3")
  case object DefermentType4                      extends EnumLiabtp("DEFERMENT TYPE 4")
  case object DptcGbEmployed                      extends EnumLiabtp("DPTC (GB - EMPLOYED)")
  case object DptcGbSelfEmployed                  extends EnumLiabtp("DPTC (GB - SELF EMPLOYED)")
  case object DptcNiEmployed                      extends EnumLiabtp("DPTC (NI - EMPLOYED)")
  case object DptcNiSelfEmployed                  extends EnumLiabtp("DPTC (NI - SELF EMPLOYED)")
  case object Dummy101                            extends EnumLiabtp("DUMMY 101")
  case object Dummy102                            extends EnumLiabtp("DUMMY 102")
  case object DwaGbEmployed                       extends EnumLiabtp("DWA (GB EMPLOYED)")
  case object DwaGbSelfEmployed                   extends EnumLiabtp("DWA (GB SELF - EMPLOYED)")
  case object DwaNiEmployed                       extends EnumLiabtp("DWA (NI EMPLOYED)")
  case object DwaNiSelfEmployed                   extends EnumLiabtp("DWA (NI SELF -EMPLOYED)")
  case object DisabledTaxCreditEmployed           extends EnumLiabtp("Disabled Tax Credit (Employed)")
  case object DisabledTaxCreditSelfEmployed       extends EnumLiabtp("Disabled Tax Credit (Self-Employed)")
  case object EsaS2p                              extends EnumLiabtp("ESA (S2P)")
  case object FullTimeEducation                   extends EnumLiabtp("FULL TIME EDUCATION")
  case object FamCGb                              extends EnumLiabtp("FamC (GB)")
  case object FamCNi                              extends EnumLiabtp("FamC (NI)")
  case object ForeignEarningsQualificationS2p     extends EnumLiabtp("Foreign Earnings Qualification (S2P)")
  case object FosterCarer                         extends EnumLiabtp("Foster Carer")
  case object HigherRateProvisionOverride         extends EnumLiabtp("HIGHER RATE PROVISION OVERRIDE")
  case object Hmf                                 extends EnumLiabtp("HMF")
  case object HomeResponsibilityProtectionAaCaa   extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (AA/CAA)")
  case object HomeResponsibilityProtectionCbcAuto extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (CBC AUTO)")

  case object HomeResponsibilityProtectionCbcClerical
      extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (CBC CLERICAL)")

  case object HomeResponsibilityProtectionDla extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (DLA)")

  case object HomeResponsibilityProtectionFosterCarers
      extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (FOSTER CARERS)")

  case object HomeResponsibilityProtectionIs        extends EnumLiabtp("HOME RESPONSIBILITY PROTECTION (I/S)")
  case object IbS2p                                 extends EnumLiabtp("IB (S2P)")
  case object InsolvencyClass1                      extends EnumLiabtp("INSOLVENCY (CLASS 1)")
  case object InsolvencyClass2                      extends EnumLiabtp("INSOLVENCY (CLASS 2)")
  case object IrrecoverableArrears                  extends EnumLiabtp("IRRECOVERABLE ARREARS")
  case object IsleOfMan                             extends EnumLiabtp("ISLE OF MAN")
  case object LcwLcwra                              extends EnumLiabtp("LCW/LCWRA")
  case object LcwLcwraOverride                      extends EnumLiabtp("LCW/LCWRA Override")
  case object LongTermNonEmployment                 extends EnumLiabtp("LONG TERM NON EMPLOYMENT")
  case object LongTermScaleRateLtsr                 extends EnumLiabtp("LONG TERM SCALE RATE (LTSR)")
  case object LostCard                              extends EnumLiabtp("LOST CARD")
  case object MarinersVoluntaryClass2               extends EnumLiabtp("MARINER'S VOLUNTARY CLASS 2")
  case object MarriedWomansReducedRate              extends EnumLiabtp("MARRIED WOMAN'S REDUCED RATE")
  case object NegligentEmployee                     extends EnumLiabtp("NEGLIGENT EMPLOYEE")
  case object NotApplicable                         extends EnumLiabtp("NOT APPLICABLE")
  case object PaymentOfArrearsByAdministrativeOrder extends EnumLiabtp("PAYMENT OF ARREARS BY ADMINISTRATIVE ORDER")
  case object PaymentOfArrearsByInstalments         extends EnumLiabtp("PAYMENT OF ARREARS BY INSTALMENTS")
  case object PeriodsOfEsaAuto                      extends EnumLiabtp("PERIODS OF ESA (AUTO)")
  case object PeriodsOfEsaClerical                  extends EnumLiabtp("PERIODS OF ESA (CLERICAL)")
  case object PeriodsOfIncapacityAuto               extends EnumLiabtp("PERIODS OF INCAPACITY (AUTO)")
  case object PeriodsOfIncapacityClerical           extends EnumLiabtp("PERIODS OF INCAPACITY (CLERICAL)")
  case object ProvisionalWaiver                     extends EnumLiabtp("PROVISIONAL WAIVER")
  case object PseudoCivilRecoverySection            extends EnumLiabtp("PSEUDO CIVIL RECOVERY SECTION")
  case object PseudoDebtManagementSection           extends EnumLiabtp("PSEUDO DEBT MANAGEMENT SECTION")
  case object PseudoInsolvencySection               extends EnumLiabtp("PSEUDO INSOLVENCY SECTION")
  case object PseudoSubjectToNormalCollectionAction extends EnumLiabtp("PSEUDO SUBJECT TO NORMAL COLLECTION ACTION")
  case object ReducedBenefitDirectiveRbd            extends EnumLiabtp("REDUCED BENEFIT DIRECTIVE (RBD)")
  case object SentToIdms                            extends EnumLiabtp("SENT TO IDMS")
  case object Sharefisherman                        extends EnumLiabtp("SHAREFISHERMAN")
  case object SharefishermanInSa                    extends EnumLiabtp("SHAREFISHERMAN IN SA")
  case object SharefishermanUnemploymentBenefit     extends EnumLiabtp("SHAREFISHERMAN UNEMPLOYMENT BENEFIT")
  case object Sickness                              extends EnumLiabtp("SICKNESS")
  case object SmallEarningsException                extends EnumLiabtp("SMALL EARNINGS EXCEPTION")
  case object SptLpt                                extends EnumLiabtp("SPT-LPT")
  case object SptLptEc                              extends EnumLiabtp("SPT-LPT EC")
  case object SystemInitiatedNonLiabilityOverride   extends EnumLiabtp("SYSTEM INITIATED NON-LIABILITY OVERRIDE")
  case object SpecifiedAdultCarer                   extends EnumLiabtp("Specified Adult Carer")
  case object TransferredChbLiability               extends EnumLiabtp("Transferred ChB Liability")
  case object UnderSmallProfitThreshold             extends EnumLiabtp("UNDER SMALL PROFIT THRESHOLD")
  case object UnemployabilitySupplement             extends EnumLiabtp("UNEMPLOYABILITY SUPPLEMENT")
  case object UniversalCredit                       extends EnumLiabtp("Universal Credit")
  case object UniversalCreditS2p                    extends EnumLiabtp("Universal Credit (S2P)")
  case object VoluntaryClass2Abroad                 extends EnumLiabtp("VOLUNTARY CLASS 2 ABROAD")

  case object VoluntaryClass2AbroadbyEmployersSchedule
      extends EnumLiabtp("VOLUNTARY CLASS 2 ABROAD BY EMPLOYERS SCHEDULE")

  case object VoluntaryClass3Abroad extends EnumLiabtp("VOLUNTARY CLASS 3 ABROAD")

  case object VoluntaryClass3AbroadbyEmployersSchedule
      extends EnumLiabtp("VOLUNTARY CLASS 3 ABROAD BY EMPLOYERS SCHEDULE")

  case object VolunteerDevelopmentWorker            extends EnumLiabtp("VOLUNTEER DEVELOPMENT WORKER")
  case object WftcGb                                extends EnumLiabtp("WFTC (GB)")
  case object WftcNi                                extends EnumLiabtp("WFTC (NI)")
  case object WorkingTaxCreditEmployed              extends EnumLiabtp("Working Tax Credit (Employed)")
  case object WorkingTaxCreditSelfEmployed          extends EnumLiabtp("Working Tax Credit (Self-Employed)")
  case object ReplacementCreditsForParentsAndCarers extends EnumLiabtp("Replacement Credits for Parents and Carers")

}
