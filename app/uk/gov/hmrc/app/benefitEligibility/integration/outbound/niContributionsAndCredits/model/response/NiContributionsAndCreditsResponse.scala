/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.catsSyntaxTuple2Semigroupal
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.{ErrorCode400, ErrorCode422, Reason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult

import java.time.LocalDate
import scala.collection.immutable

sealed trait NiContributionsAndCreditsResponse extends NpsApiResponse

object NiContributionsAndCreditsError {

  case class NiContributionsAndCredits400(reason: Reason, code: ErrorCode400)

  object NiContributionsAndCredits400 {

    implicit val npsErrorResponse400Reads: Reads[NiContributionsAndCredits400] =
      Json.reads[NiContributionsAndCredits400]

  }

  case class NiContributionsAndCreditsResponse400(failures: List[NiContributionsAndCredits400])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse400 {

    implicit val npsFailureResponse400Reads: Reads[NiContributionsAndCreditsResponse400] =
      Json.reads[NiContributionsAndCreditsResponse400]

  }

  sealed abstract class ErrorCode403(override val entryName: String) extends EnumEntry

  object ErrorCode403 extends Enum[ErrorCode403] with PlayJsonEnum[ErrorCode403] {
    val values: immutable.IndexedSeq[ErrorCode403] = findValues
    case object ErrorCode403_2 extends ErrorCode403("403.2")
  }

  sealed abstract class ErrorReason403(override val entryName: String) extends EnumEntry

  object ErrorReason403 extends Enum[ErrorReason403] with PlayJsonEnum[ErrorReason403] {
    val values: immutable.IndexedSeq[ErrorReason403] = findValues
    case object Forbidden extends ErrorReason403("Forbidden")
  }

  case class NiContributionsAndCreditsResponse403(reason: ErrorReason403, code: ErrorCode403)
      extends NiContributionsAndCreditsResponse
      with NpsApiResponse

  object NiContributionsAndCreditsResponse403 {

    implicit val npsFailureResponse403Reads: Reads[NiContributionsAndCreditsResponse403] =
      Json.reads[NiContributionsAndCreditsResponse403]

  }

  case class NiContributionsAndCredits422(reason: Reason, code: ErrorCode422)

  object NiContributionsAndCredits422 {

    implicit val NpsErrorResponse422Reads: Reads[NiContributionsAndCredits422] =
      Json.reads[NiContributionsAndCredits422]

  }

  case class NiContributionsAndCreditsResponse422(failures: List[NiContributionsAndCredits422])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse422 {

    implicit val npsFailureResponse422Reads: Reads[NiContributionsAndCreditsResponse422] =
      Json.reads[NiContributionsAndCreditsResponse422]

  }

}

object NiContributionsAndCreditsSuccess {

  case class PrimaryContribution(value: BigDecimal) extends AnyVal

  object PrimaryContribution {
    implicit val primaryContributionReads: Reads[PrimaryContribution] = Json.valueReads[PrimaryContribution]
  }

  case class PrimaryPaidEarnings(value: BigDecimal) extends AnyVal

  object PrimaryPaidEarnings {

    implicit val primaryPaidEarningsReads: Reads[PrimaryPaidEarnings] =
      Json.valueReads[PrimaryPaidEarnings]

  }

  case class ReceivablePeriodStartDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodStartDate {

    implicit val receivablePeriodStartDateReads: Format[ReceivablePeriodStartDate] =
      Json.valueFormat[ReceivablePeriodStartDate]

  }

  case class ReceivablePeriodEndDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodEndDate {

    implicit val receivablePeriodEndDateReads: Format[ReceivablePeriodEndDate] =
      Json.valueFormat[ReceivablePeriodEndDate]

  }

  case class LiabilityStartDate(value: LocalDate) extends AnyVal

  object LiabilityStartDate {
    implicit val liabilityStartDateReads: Format[LiabilityStartDate] = Json.valueFormat[LiabilityStartDate]
  }

  case class LiabilityEndDate(value: LocalDate) extends AnyVal

  object LiabilityEndDate {
    implicit val liabilityEndDateReads: Format[LiabilityEndDate] = Json.valueFormat[LiabilityEndDate]
  }

  case class BillScheduleNumber(value: Int) extends AnyVal

  object BillScheduleNumber {
    implicit val billScheduleNumberReads: Format[BillScheduleNumber] = Json.valueFormat[BillScheduleNumber]
  }

  final case class ContributionCategoryLetter(value: String) extends AnyVal

  object ContributionCategoryLetter {

    implicit val contributionCategoryLetterWrites: Reads[ContributionCategoryLetter] =
      Json.valueReads[ContributionCategoryLetter]

  }

  final case class EmployerName(value: String) extends AnyVal

  object EmployerName {

    implicit val employerNameFormat: Format[EmployerName] =
      Json.valueFormat[EmployerName]

  }

  sealed abstract class ContributionCategory(override val entryName: String) extends EnumEntry

  object ContributionCategory extends Enum[ContributionCategory] with PlayJsonEnum[ContributionCategory] {
    val values: immutable.IndexedSeq[ContributionCategory] = findValues

    case object None             extends ContributionCategory("(NONE)")
    case object Class1A          extends ContributionCategory("CLASS 1A - PAYE EMPLOYER ONLY CONTRIBUTIONS")
    case object ConvertedAccount extends ContributionCategory("CONVERTED ACCOUNT - CONTRACTED OUT")

    case object MarinerRebateReducedContractedOut
        extends ContributionCategory("MARINER FOREIGN GOING REBATE ( REDUCED CONTRACTED-OUT )")

    case object MarinerRebateStandardContractedOut
        extends ContributionCategory("MARINER FOREIGN GOING REBATE ( STANDARD CONTRACTED-OUT )")

    case object MarinerRebateEquivalentReduced
        extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (REDUCED)")

    case object MarinerRebateEquivalentSecondaryOnly
        extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (SECONDARY ONLY)")

    case object MarinerRebateEquivalentStandard
        extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (STANDARD)")

    case object MarinerRedundancyFundAndForeignRebateReducedContractedOut
        extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE ( REDUCED C-OUT)")

    case object MarinerRedundancyFundAndForeignRebateStandardContractedOut
        extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE ( STANDARD C-OUT)")

    case object MarinerRedundancyFundAndForeignRebateEquivalentSecondaryOnly
        extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE EQUIV.(SECONDARY ONLY)")

    case object MarinerRedundancyFundAndForeignRebateEquivalentStd
        extends ContributionCategory("MARINER REDUNDANCY FUND & FRGN GNG REBATE EQUIV.(STD)")

    case object MarinerRedundancyFundAndForeignRebateEquivalentRdcd
        extends ContributionCategory("MARINER REDUNDANCY FUND & FRGN GOING REBATE EQUIV.(RDCD)")

    case object MarinerRedundancyFundRebateReducedContractedOut
        extends ContributionCategory("MARINER REDUNDANCY FUND REBATE ( REDUCED CONTRACTED-OUT )")

    case object MarinerRedundancyFundRebateStandardContractedOut
        extends ContributionCategory("MARINER REDUNDANCY FUND REBATE ( STANDARD CONTRACTED-OUT )")

    case object MarinerRedundancyFundRebateEquivalentRdcd
        extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIV. (RDCD)")

    case object MarinerRedundancyFundRebateEquivalentSecondaryOnly
        extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIVALENT (SECONDARY ONLY)")

    case object MarinerRedundancyFundRebateEquivalentStandard
        extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIVALENT (STANDARD)")

    case object MarriedWomanReducedRateElection extends ContributionCategory("MARRIED WOMAN'S REDUCED RATE ELECTION")
    case object NoLiability                     extends ContributionCategory("NO LIABILITY")
    case object OfficeHolders                   extends ContributionCategory("OFFICE HOLDERS")
    case object ReducedRate                     extends ContributionCategory("REDUCED RATE")
    case object ReducedRateContractedOut        extends ContributionCategory("REDUCED RATE CONTRACTED-OUT")
    case object StandardRate                    extends ContributionCategory("STANDARD RATE")
    case object StandardRateContractedOut       extends ContributionCategory("STANDARD RATE CONTRACTED-OUT")
    case object Unallocated                     extends ContributionCategory("UNALLOCATED")
    case object ZeroRate                        extends ContributionCategory("ZERO RATE")

  }

  sealed abstract class ContributionCreditType(override val entryName: String) extends EnumEntry

  object ContributionCreditType extends Enum[ContributionCreditType] with PlayJsonEnum[ContributionCreditType] {
    val values: immutable.IndexedSeq[ContributionCreditType] = findValues

    case object C1   extends ContributionCreditType("C1")
    case object Hmf  extends ContributionCreditType("HMF")
    case object Mar  extends ContributionCreditType("MAR")
    case object Cs   extends ContributionCreditType("CS")
    case object Eon  extends ContributionCreditType("EON")
    case object Cr1  extends ContributionCreditType("CR1")
    case object C2   extends ContributionCreditType("C2")
    case object C2w  extends ContributionCreditType("C2W")
    case object Sf   extends ContributionCreditType("SF")
    case object TwoA extends ContributionCreditType("2A")
    case object TwoB extends ContributionCreditType("2B")
    case object TwoC extends ContributionCreditType("2C")
    case object TwoD extends ContributionCreditType("2D")
    case object Sfa  extends ContributionCreditType("SFA")
    case object Sfb  extends ContributionCreditType("SFB")
    case object Sfc  extends ContributionCreditType("SFC")
    case object Sfd  extends ContributionCreditType("SFD")
    case object Vda  extends ContributionCreditType("VDA")
    case object Vdb  extends ContributionCreditType("VDB")
    case object Vdc  extends ContributionCreditType("VDC")
    case object Vdd  extends ContributionCreditType("VDD")
    case object TwoN extends ContributionCreditType("2N")
  }

  sealed abstract class Class1ContributionStatus(override val entryName: String) extends EnumEntry

  object Class1ContributionStatus extends Enum[Class1ContributionStatus] with PlayJsonEnum[Class1ContributionStatus] {
    val values: immutable.IndexedSeq[Class1ContributionStatus] = findValues
    case object ComplianceAndYieldIncomplete extends Class1ContributionStatus("COMPLIANCE & YIELD INCOMPLETE")
    case object Current                      extends Class1ContributionStatus("CURRENT")
    case object HistoricAmend                extends Class1ContributionStatus("HISTORIC AMENDED")
    case object HistoricCancelled            extends Class1ContributionStatus("HISTORIC CANCELLED")
    case object InvalidNoRcf                 extends Class1ContributionStatus("INVALID - NO RCF")
    case object InvalidRcf                   extends Class1ContributionStatus("INVALID - RCF")
    case object InvalidCompatibilityCheck    extends Class1ContributionStatus("INVALID COMPATIBILITY CHECK")
    case object NotKnownOrNotApplicable      extends Class1ContributionStatus("NOT KNOWN / NOT APPLICABLE")
    case object Potential                    extends Class1ContributionStatus("POTENTIAL")
    case object Valid                        extends Class1ContributionStatus("VALID")
    case object ValidRcf                     extends Class1ContributionStatus("VALID RCF")
  }

  sealed abstract class LatePaymentPeriod(override val entryName: String) extends EnumEntry

  object LatePaymentPeriod extends Enum[LatePaymentPeriod] with PlayJsonEnum[LatePaymentPeriod] {
    val values: immutable.IndexedSeq[LatePaymentPeriod] = findValues

    case object L extends LatePaymentPeriod("L")

    case object Lx extends LatePaymentPeriod("LX")

    case object Zy extends LatePaymentPeriod("ZY")

    case object Zz extends Class1ContributionStatus("ZZ")

  }

  sealed abstract class CreditSource(override val entryName: String) extends EnumEntry

  object CreditSource extends Enum[CreditSource] with PlayJsonEnum[CreditSource] {
    val values: immutable.IndexedSeq[CreditSource] = findValues

    case object NotKnown extends CreditSource("NOT KNOWN")

    case object AdpCentreLivingston extends CreditSource("ADP CENTRE LIVINGSTON")

    case object DssLocalOffices extends CreditSource("DSS LOCAL OFFICES")

    case object EmploymentServices extends CreditSource("EMPLOYMENT SERVICES")

    case object DirectDebit extends CreditSource("DIRECT DEBIT")

    case object CarersAllowanceCa extends CreditSource("CARER'S ALLOWANCE (CA)")

    case object HncipOrSda extends CreditSource("HNCIP OR SDA")

    case object NorthernIrelandSicknessCredits extends CreditSource("NORTHERN IRELAND SICKNESS CREDITS")

    case object JuryService extends CreditSource("JURY SERVICE")

    case object Nubs2 extends CreditSource("NUBS 2")

    case object Nubs2AlternativeSbConditionMet extends CreditSource("NUBS 2 [ALTERNATIVE SB CONDITION MET]")

    case object DwaUnitBlackpoolEmployedEarners extends CreditSource("DWA UNIT (BLACKPOOL) EMPLOYED EARNERS")

    case object DwaUnitBlackpoolSelfEmployedEarners extends CreditSource("DWA UNIT (BLACKPOOL) SELF EMPLOYED EARNERS")

    case object DwaUnitBelfastEmployedEarners extends CreditSource("DWA UNIT (BELFAST) EMPLOYED EARNERS")

    case object DwaUnitBelfastSelfEmployedEarners extends CreditSource("DWA UNIT (BELFAST) SELF EMPLOYED EARNERS")

    case object IsleOfMan extends CreditSource("ISLE OF MAN")

    case object AutoCreditsInternallyCalculated extends CreditSource("AUTOCREDITS - INTERNALLY CALCULATED")

    case object AutoCreditsExternallyInput extends CreditSource("AUTOCREDITS - EXTERNALLY INPUT")

    case object JuvenileCreditsInternal extends CreditSource("JUVENILE CREDITS (INTERNAL)")

    case object WidowsCreditsInternal extends CreditSource("WIDOWS CREDITS   (INTERNAL)")

    case object ApprovedTrainingCreditsInternal extends CreditSource("APPROVED TRAINING CREDITS (INTERNAL)")

    case object UnemployabilitySupplementCreditsInternal
        extends CreditSource("UNEMPLOYABILITY SUPPLEMENT CREDITS (INTERNAL)")

    case object PscsIncap extends CreditSource("PSCS INCAP")

    case object LongTermScaleRate extends CreditSource("LONG TERM SCALE RATE")

    case object AdpReading extends CreditSource("ADP READING")

    case object NotApplicable extends CreditSource("NOT APPLICABLE")

    case object QuarterlyBilling extends CreditSource("QUARTERLY BILLING")

    case object JsaTapeInput extends CreditSource("JSA TAPE INPUT")

    case object JsaTapeInputAlternativeSbConditionMet
        extends CreditSource("JSA TAPE INPUT (ALTERNATIVE SB CONDITION MET)")

    case object JsaPaperInput extends CreditSource("JSA PAPER INPUT")

    case object FamcNi extends CreditSource("FAMC-NI")

    case object FamcGb extends CreditSource("FAMC-GB")

    case object CentralAward extends CreditSource("CENTRAL AWARD")

    case object AdpNorthernIreland extends CreditSource("ADP NORTHERN IRELAND")

    case object FamilyCreditGb extends CreditSource("FAMILY CREDIT (GB)")

    case object FamilyCreditNi extends CreditSource("FAMILY CREDIT (NI)")

    case object Incapacity extends CreditSource("INCAPACITY")

    case object DptcGbEmp extends CreditSource("DPTC (GB) EMP")

    case object DptcGbSEmp extends CreditSource("DPTC (GB) S/EMP")

    case object DptcNiEmp extends CreditSource("DPTC (NI) EMP")

    case object DptcNiSEmp extends CreditSource("DPTC (NI) S/EMP")

    case object WftcGb extends CreditSource("WFTC (GB)")

    case object WftcNi extends CreditSource("WFTC (NI)")

    case object StatutoryMaternityPayCredit extends CreditSource("STATUTORY MATERNITY PAY CREDIT")

    case object StatutoryAdoptionPayCredit extends CreditSource("STATUTORY ADOPTION PAY CREDIT")

    case object DisabledTaxCreditEmployed extends CreditSource("Disabled Tax Credit (Employed)")

    case object DisabledTaxCreditSelfEmployed extends CreditSource("Disabled Tax Credit (Self-employed)")

    case object WorkingTaxCreditEmployed extends CreditSource("Working Tax Credit (Employed)")

    case object WorkingTaxCreditSelfEmployed extends CreditSource("Working Tax Credit (Self-employed)")

    case object EmploymentAndSupport extends CreditSource("EMPLOYMENT AND SUPPORT")

    case object HrpConversion extends CreditSource("HRP CONVERSION")

    case object ChildBenefit extends CreditSource("CHILD BENEFIT")

    case object CarersCredit extends CreditSource("CARER'S CREDIT")

    case object FosterCarer extends CreditSource("FOSTER CARER")

    case object MoDSpouseCivilPartnerCredits extends CreditSource("MoD Spouse/Civil Partner's Credits")

    case object AdditionalStatutoryPaternityPay extends CreditSource("ADDITIONAL STATUTORY PATERNITY PAY")

    case object SpecifiedAdultCarer extends CreditSource("SPECIFIED ADULT CARER")

    case object UniversalCredit extends CreditSource("UNIVERSAL CREDIT")

    case object SharedParentalPay extends CreditSource("SHARED PARENTAL PAY")

    case object Post75ServiceSpousesCredit extends CreditSource("Post 75 Service Spouses Credit")

    case object StatutoryParentalBereavementPay extends CreditSource("Statutory Parental Bereavement Pay")

    case object CarerSupportPaymentCsp extends CreditSource("CARER SUPPORT PAYMENT (CSP)")

    case object StatutoryNeonatalCarePay extends CreditSource("Statutory Neonatal Care Pay")

    case object ReplacementCreditsForParentsAndCarers extends CreditSource("REPLACEMENT CREDITS FOR PARENTS AND CARERS")

  }

  case class TaxYear(value: Int) extends AnyVal

  object TaxYear {
    implicit val weeksPaidReads: Format[TaxYear] = Json.valueFormat[TaxYear]
  }

  case class NumberOfCreditsAndConts(value: Int) extends AnyVal

  object NumberOfCreditsAndConts {

    implicit val numberOfCreditsAndContsFormat: Format[NumberOfCreditsAndConts] =
      Json.valueFormat[NumberOfCreditsAndConts]

  }

  case class Class2NIContributionAmount(value: BigDecimal) extends AnyVal

  object Class2NIContributionAmount {

    implicit val numberOfCreditsAndContsFormat: Format[Class2NIContributionAmount] =
      Json.valueFormat[Class2NIContributionAmount]

  }

  case class Class2Or3EarningsFactor(value: BigDecimal) extends AnyVal

  object Class2Or3EarningsFactor {

    implicit val class2Or3EarningsFactorFormat: Format[Class2Or3EarningsFactor] =
      Json.valueFormat[Class2Or3EarningsFactor]

  }

  sealed abstract class Class2Or3CreditStatus(override val entryName: String) extends EnumEntry

  object Class2Or3CreditStatus extends Enum[Class2Or3CreditStatus] with PlayJsonEnum[Class2Or3CreditStatus] {
    val values: immutable.IndexedSeq[Class2Or3CreditStatus] = findValues

    case object NotKnowNotApplicable      extends Class2Or3CreditStatus("NOT KNOWN/NOT APPLICABLE")
    case object Valid                     extends Class2Or3CreditStatus("VALID")
    case object Invalid                   extends Class2Or3CreditStatus("INVALID")
    case object Potential                 extends Class2Or3CreditStatus("POTENTIAL")
    case object InvalidCompatibilityCheck extends Class2Or3CreditStatus("INVALID COMPATIBILITY CHECK")

  }

  case class NicClass1(
      taxYear: Option[TaxYear],
      contributionCategoryLetter: Option[ContributionCategoryLetter],
      contributionCategory: Option[ContributionCategory],
      contributionCreditType: Option[ContributionCreditType],
      primaryContribution: Option[PrimaryContribution],
      class1ContributionStatus: Option[Class1ContributionStatus],
      primaryPaidEarnings: Option[PrimaryPaidEarnings],
      creditSource: Option[CreditSource],
      employerName: Option[EmployerName],
      latePaymentPeriod: Option[LatePaymentPeriod]
  )

  object NicClass1 {
    implicit val nicClass1Reads: Reads[NicClass1] = Json.reads[NicClass1]
  }

  case class NicClass2(
      taxYear: Option[TaxYear],
      noOfCreditsAndConts: Option[NumberOfCreditsAndConts],
      contributionCreditType: Option[ContributionCreditType],
      class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
      class2NIContributionAmount: Option[Class2NIContributionAmount],
      class2Or3CreditStatus: Option[Class2Or3CreditStatus],
      creditSource: Option[CreditSource],
      latePaymentPeriod: Option[LatePaymentPeriod]
  )

  object NicClass2 {
    implicit val nicClass2Reads: Reads[NicClass2] = Json.reads[NicClass2]
  }

  case class NiContributionsAndCreditsSuccessResponse(
      niClass1: List[NicClass1],
      niClass2: List[NicClass2]
  ) extends NiContributionsAndCreditsResponse
      with NpsSuccessfulApiResponse

  object NiContributionsAndCreditsSuccessResponse {

    implicit val getNiContributionsAndCreditsReads: Reads[NiContributionsAndCreditsSuccessResponse] =
      Json.reads[NiContributionsAndCreditsSuccessResponse]

  }

}
