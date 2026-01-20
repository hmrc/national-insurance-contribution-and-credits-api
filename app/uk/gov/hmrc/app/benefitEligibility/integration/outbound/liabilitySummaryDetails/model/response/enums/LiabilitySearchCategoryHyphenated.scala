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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class LiabilitySearchCategoryHyphenated(override val entryName: String) extends EnumEntry

object LiabilitySearchCategoryHyphenated
    extends Enum[LiabilitySearchCategoryHyphenated]
    with PlayJsonEnum[LiabilitySearchCategoryHyphenated] {
  val values: immutable.IndexedSeq[LiabilitySearchCategoryHyphenated] = findValues

  case object Abroad                    extends LiabilitySearchCategoryHyphenated("ABROAD")
  case object AbsoluteWaiver            extends LiabilitySearchCategoryHyphenated("ABSOLUTE-WAIVER")
  case object AllLiabilities            extends LiabilitySearchCategoryHyphenated("ALL-LIABILITIES")
  case object ApprovedTraining          extends LiabilitySearchCategoryHyphenated("APPROVED-TRAINING")
  case object CaS2p                     extends LiabilitySearchCategoryHyphenated("CA-S2P")
  case object Cl2EcBilateralRegs        extends LiabilitySearchCategoryHyphenated("CL2-EC_BILATERAL-REGS")
  case object Class2ConvertedFromNirs1  extends LiabilitySearchCategoryHyphenated("CLASS-2-CONVERTED-FROM-NIRS1")
  case object Class2InSa                extends LiabilitySearchCategoryHyphenated("CLASS-2-IN-SA")
  case object Class2LiabilityUk         extends LiabilitySearchCategoryHyphenated("CLASS-2-LIABILITY-UK")
  case object Class2MaternityAllowance  extends LiabilitySearchCategoryHyphenated("CLASS-2-MATERNITY-ALLOWANCE")
  case object Class2VoluntaryUk         extends LiabilitySearchCategoryHyphenated("CLASS-2-VOLUNTARY-UK")
  case object Class3Uk                  extends LiabilitySearchCategoryHyphenated("CLASS-3-UK")
  case object Class3BillingOverride     extends LiabilitySearchCategoryHyphenated("CLASS-3-BILLING-OVERRIDE")
  case object Class3ConvertedFromNirs1  extends LiabilitySearchCategoryHyphenated("CLASS-3-CONVERTED-FROM-NIRS1")
  case object CodedOut                  extends LiabilitySearchCategoryHyphenated("CODED-OUT")
  case object ContinuingClass1Liability extends LiabilitySearchCategoryHyphenated("CONTINUING-CLASS-1-LIABILITY")
  case object ContinuingClass2Liability extends LiabilitySearchCategoryHyphenated("CONTINUING-CLASS-2-LIABILITY")

  case object ContributorInitiatedNonLiabilityOverride
      extends LiabilitySearchCategoryHyphenated("CONTRIBUTOR-INITIATED-NON-LIABILITY-OVERRIDE")

  case object CriminalProceedings       extends LiabilitySearchCategoryHyphenated("CRIMINAL-PROCEEDINGS")
  case object CrsRo                     extends LiabilitySearchCategoryHyphenated("CRS_RO")
  case object CarerOfAaCaaDlaRecipient  extends LiabilitySearchCategoryHyphenated("Carer-of-AA_CAA_DLA-recipient")
  case object CarerSupportPayment       extends LiabilitySearchCategoryHyphenated("Carer-Support-Payment")
  case object CarersAllowance           extends LiabilitySearchCategoryHyphenated("Carers-Allowance")
  case object CarersCredit              extends LiabilitySearchCategoryHyphenated("Carers-Credit")
  case object ChildBenefit              extends LiabilitySearchCategoryHyphenated("Child-Benefit")
  case object Class2BillingOverride     extends LiabilitySearchCategoryHyphenated("Class-2-Billing-Override")
  case object DefermentType1            extends LiabilitySearchCategoryHyphenated("DEFERMENT-TYPE-1")
  case object DefermentType2            extends LiabilitySearchCategoryHyphenated("DEFERMENT-TYPE-2")
  case object DefermentType3            extends LiabilitySearchCategoryHyphenated("DEFERMENT-TYPE-3")
  case object DefermentType4            extends LiabilitySearchCategoryHyphenated("DEFERMENT-TYPE-4")
  case object DptcGBEmployed            extends LiabilitySearchCategoryHyphenated("DPTC-GB-EMPLOYED")
  case object DptcGBSelfEmployed        extends LiabilitySearchCategoryHyphenated("DPTC-GB-SELF-EMPLOYED")
  case object DptcNiEmployed            extends LiabilitySearchCategoryHyphenated("DPTC-NI-EMPLOYED")
  case object DptcNiSelfEmployed        extends LiabilitySearchCategoryHyphenated("DPTC-NI-SELF-EMPLOYED")
  case object Dummy101                  extends LiabilitySearchCategoryHyphenated("DUMMY-101")
  case object Dummy102                  extends LiabilitySearchCategoryHyphenated("DUMMY-102")
  case object DwaGbEmployed             extends LiabilitySearchCategoryHyphenated("DWA-GB-EMPLOYED")
  case object DwaGbSelfEmployed         extends LiabilitySearchCategoryHyphenated("DWA-GB-SELF-EMPLOYED")
  case object DwaNiEmployed             extends LiabilitySearchCategoryHyphenated("DWA-NI-EMPLOYED")
  case object DwaNiSelfEmployed         extends LiabilitySearchCategoryHyphenated("DWA-NI-SELF-EMPLOYED")
  case object DisabledTaxCreditEmployed extends LiabilitySearchCategoryHyphenated("Disabled-Tax-Credit-Employed")

  case object DisabledTaxCreditSelfEmployed
      extends LiabilitySearchCategoryHyphenated("Disabled-Tax-Credit-Self-Employed")

  case object EsaS2p            extends LiabilitySearchCategoryHyphenated("ESA-S2P")
  case object FullTimeEducation extends LiabilitySearchCategoryHyphenated("FULL-TIME-EDUCATION")
  case object FamcGb            extends LiabilitySearchCategoryHyphenated("FamC-GB")
  case object FamcNi            extends LiabilitySearchCategoryHyphenated("FamC-NI")

  case object ForeignEarningsQualificationS2p
      extends LiabilitySearchCategoryHyphenated("Foreign-Earnings-Qualification-S2P")

  case object FosterCarer extends LiabilitySearchCategoryHyphenated("Foster-Carer")

  case object HigherRateProvisionOverride extends LiabilitySearchCategoryHyphenated("HIGHER-RATE-PROVISION-OVERRIDE")

  case object Hmf extends LiabilitySearchCategoryHyphenated("HMF")

  case object HomeResponsibilityProtectionAaCaa
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-AA_CAA")

  case object HomeResponsibilityProtectionCbcAuto
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-CBC-AUTO")

  case object HomeResponsibilityProtectionCbcClerical
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-CBC-CLERICAL")

  case object HomeResponsibilityProtectionDla
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-DLA")

  case object HomeResponsibilityProtectionFosterCarers
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-FOSTER-CARERS")

  case object HomeResponsibilityProtectionIS
      extends LiabilitySearchCategoryHyphenated("HOME-RESPONSIBILITY-PROTECTION-I_S")

  case object IbS2p                    extends LiabilitySearchCategoryHyphenated("IB-S2P")
  case object InsolvencyClass1         extends LiabilitySearchCategoryHyphenated("INSOLVENCY-CLASS-1")
  case object InsolvencyClass2         extends LiabilitySearchCategoryHyphenated("INSOLVENCY-CLASS-2")
  case object IrrecoverableArrears     extends LiabilitySearchCategoryHyphenated("IRRECOVERABLE-ARREARS")
  case object IsleOfMan                extends LiabilitySearchCategoryHyphenated("ISLE-OF-MAN")
  case object LcwLcwra                 extends LiabilitySearchCategoryHyphenated("LCW_LCWRA")
  case object LcwLcwraOverride         extends LiabilitySearchCategoryHyphenated("LCW_LCWRA-Override")
  case object LongTermNonEmployment    extends LiabilitySearchCategoryHyphenated("LONG-TERM-NON-EMPLOYMENT")
  case object LongTermScaleRateLtsr    extends LiabilitySearchCategoryHyphenated("LONG-TERM-SCALE-RATE-LTSR")
  case object LostCard                 extends LiabilitySearchCategoryHyphenated("LOST-CARD")
  case object MarinersVoluntaryClass2  extends LiabilitySearchCategoryHyphenated("MARINERS-VOLUNTARY-CLASS-2")
  case object MarriedWomansReducedRate extends LiabilitySearchCategoryHyphenated("MARRIED-WOMANS-REDUCED-RATE")
  case object NegligentEmployee        extends LiabilitySearchCategoryHyphenated("NEGLIGENT-EMPLOYEE")
  case object NotApplicable            extends LiabilitySearchCategoryHyphenated("NOT-APPLICABLE")

  case object PaymentOfArrearsByAdministrativeOrder
      extends LiabilitySearchCategoryHyphenated("PAYMENT-OF-ARREARS-BY-ADMINISTRATIVE-ORDER")

  case object PaymentOfArrearsByInstalments
      extends LiabilitySearchCategoryHyphenated("PAYMENT-OF-ARREARS-BY-INSTALMENTS")

  case object PeriodsOfEsaAuto        extends LiabilitySearchCategoryHyphenated("PERIODS-OF-ESA-AUTO")
  case object PeriodsOfEsaClerical    extends LiabilitySearchCategoryHyphenated("PERIODS-OF-ESA-CLERICAL")
  case object PeriodsOfIncapacityAuto extends LiabilitySearchCategoryHyphenated("PERIODS-OF-INCAPACITY-AUTO")

  case object PeriodsOfIncapacityClerical extends LiabilitySearchCategoryHyphenated("PERIODS-OF-INCAPACITY-CLERICAL")

  case object ProvisionalWaiver extends LiabilitySearchCategoryHyphenated("PROVISIONAL-WAIVER")

  case object PseudoCivilRecoverySection extends LiabilitySearchCategoryHyphenated("PSEUDO-CIVIL-RECOVERY-SECTION")

  case object PseudoDebtManagementSection extends LiabilitySearchCategoryHyphenated("PSEUDO-DEBT-MANAGEMENT-SECTION")

  case object PseudoInsolvencySection extends LiabilitySearchCategoryHyphenated("PSEUDO-INSOLVENCY-SECTION")

  case object PseudoSubjectToNormalCollectionAction
      extends LiabilitySearchCategoryHyphenated("PSEUDO-SUBJECT-TO-NORMAL-COLLECTION-ACTION")

  case object ReducedBenefitDirectiveRbd extends LiabilitySearchCategoryHyphenated("REDUCED-BENEFIT-DIRECTIVE-RBD")

  case object SentToIdms         extends LiabilitySearchCategoryHyphenated("SENT-TO-IDMS")
  case object Sharefisherman     extends LiabilitySearchCategoryHyphenated("SHAREFISHERMAN")
  case object SharefishermanInSa extends LiabilitySearchCategoryHyphenated("SHAREFISHERMAN-IN-SA")

  case object SharefishermanUnemploymentBenefit
      extends LiabilitySearchCategoryHyphenated("SHAREFISHERMAN-UNEMPLOYMENT-BENEFIT")

  case object Sickness               extends LiabilitySearchCategoryHyphenated("SICKNESS")
  case object SmallEarningsException extends LiabilitySearchCategoryHyphenated("SMALL-EARNINGS-EXCEPTION")
  case object SptLpt                 extends LiabilitySearchCategoryHyphenated("SPT-LPT")
  case object SptLptEc               extends LiabilitySearchCategoryHyphenated("SPT-LPT-EC")

  case object SystemInitiatedNonLiabilityOverride
      extends LiabilitySearchCategoryHyphenated("SYSTEM-INITIATED-NON-LIABILITY-OVERRIDE")

  case object SpecifiedAdultCarer       extends LiabilitySearchCategoryHyphenated("Specified-Adult-Carer")
  case object TransferredChbLiability   extends LiabilitySearchCategoryHyphenated("Transferred-ChB-Liability")
  case object UnderSmallProfitThreshold extends LiabilitySearchCategoryHyphenated("UNDER-SMALL-PROFIT-THRESHOLD")
  case object UnemployabilitySupplement extends LiabilitySearchCategoryHyphenated("UNEMPLOYABILITY-SUPPLEMENT")
  case object UniversalCredit           extends LiabilitySearchCategoryHyphenated("Universal-Credit")
  case object UniversalCreditS2p        extends LiabilitySearchCategoryHyphenated("Universal-Credit-S2P")
  case object VoluntaryClass2Abroad     extends LiabilitySearchCategoryHyphenated("VOLUNTARY-CLASS-2-ABROAD")

  case object VoluntaryClass2AbroadByEmployersSchedule
      extends LiabilitySearchCategoryHyphenated("VOLUNTARY-CLASS-2-ABROAD-BY-EMPLOYERS-SCHEDULE")

  case object VoluntaryClass3Abroad extends LiabilitySearchCategoryHyphenated("VOLUNTARY-CLASS-3-ABROAD")

  case object VoluntaryClass3AbroadByEmployersSchedule
      extends LiabilitySearchCategoryHyphenated("VOLUNTARY-CLASS-3-ABROAD-BY-EMPLOYERS-SCHEDULE")

  case object VolunteerDevelopmentWorker extends LiabilitySearchCategoryHyphenated("VOLUNTEER-DEVELOPMENT-WORKER")
  case object WftcGb                     extends LiabilitySearchCategoryHyphenated("WFTC-GB")
  case object WftcNi                     extends LiabilitySearchCategoryHyphenated("WFTC-NI")
  case object WorkingTaxCreditEmployed   extends LiabilitySearchCategoryHyphenated("Working-Tax-Credit-Employed")

  case object WorkingTaxCreditSelfEmployed extends LiabilitySearchCategoryHyphenated("Working-Tax-Credit-Self-Employed")

}
