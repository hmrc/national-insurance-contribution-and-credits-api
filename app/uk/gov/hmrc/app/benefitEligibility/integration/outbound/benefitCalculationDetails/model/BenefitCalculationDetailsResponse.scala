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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.OfficeDetails
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate

sealed trait BenefitCalculationDetailsResponse extends NpsApiResponse

object BenefitCalculationDetailsError {

  // region Error400

  sealed trait BenefitCalculationDetailsErrorResponse400 extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsErrorResponse400 {

    implicit val longTermBenefitNotesErrorResponse400Reads: Reads[BenefitCalculationDetailsErrorResponse400] =
      Reads[BenefitCalculationDetailsErrorResponse400] { resp =>
        BenefitCalculationDetailsStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
          case JsSuccess(value, path) => JsSuccess(value, path)
          case JsError(errors) =>
            BenefitCalculationDetailsHipFailureResponse400.hipFailureResponse400Reads.reads(resp) match {
              case JsSuccess(value, path) => JsSuccess(value, path)
              case JsError(errors)        => JsError(errors)
            }
        }
      }

  }

  case class BenefitCalculationDetailsHipFailureResponse400(origin: HipOrigin, response: HipFailureResponse)
      extends BenefitCalculationDetailsErrorResponse400

  object BenefitCalculationDetailsHipFailureResponse400 {

    implicit val hipFailureResponse400Reads: Reads[BenefitCalculationDetailsHipFailureResponse400] =
      Json.reads[BenefitCalculationDetailsHipFailureResponse400]

  }

  case class BenefitCalculationDetailsErrorItem400(reason: NpsErrorReason, code: NpsErrorCode400)

  object BenefitCalculationDetailsErrorItem400 {

    implicit val longTermBenefitNotesErrorItem400Reads: Reads[BenefitCalculationDetailsErrorItem400] =
      Json.reads[BenefitCalculationDetailsErrorItem400]

  }

  case class BenefitCalculationDetailsError400(failures: List[BenefitCalculationDetailsErrorItem400])

  object BenefitCalculationDetailsError400 {

    implicit val longTermBenefitNotesError400Reads: Reads[BenefitCalculationDetailsError400] =
      Json.reads[BenefitCalculationDetailsError400]

  }

  case class BenefitCalculationDetailsStandardErrorResponse400(
      origin: HipOrigin,
      response: BenefitCalculationDetailsError400
  ) extends BenefitCalculationDetailsErrorResponse400

  object BenefitCalculationDetailsStandardErrorResponse400 {

    implicit val standardErrorResponse400Reads: Reads[BenefitCalculationDetailsStandardErrorResponse400] =
      Json.reads[BenefitCalculationDetailsStandardErrorResponse400]

  }

  // endregion Error400

  // region Error403

  case class BenefitCalculationDetailsErrorResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsErrorResponse403 {

    implicit val longTermBenefitNotesErrorResponse403Reads: Reads[BenefitCalculationDetailsErrorResponse403] =
      Json.reads[BenefitCalculationDetailsErrorResponse403]

  }

  // endregion Error403

  // region Error404

  case class BenefitCalculationDetailsErrorResponse404(code: NpsErrorCode404, reason: NpsErrorReason404)
      extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsErrorResponse404 {

    implicit val longTermBenefitNotesErrorResponse404Reads: Reads[BenefitCalculationDetailsErrorResponse404] =
      Json.reads[BenefitCalculationDetailsErrorResponse404]

  }

  // endregion Error404

  // region Error422

  case class BenefitCalculationDetailsError422(reason: NpsErrorReason, code: ErrorCode422)

  object BenefitCalculationDetailsError422 {

    implicit val longTermBenefitNotesError422Reads: Reads[BenefitCalculationDetailsError422] =
      Json.reads[BenefitCalculationDetailsError422]

  }

  case class BenefitCalculationDetailsErrorResponse422(failures: Option[List[BenefitCalculationDetailsError422]])
      extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsErrorResponse422 {

    implicit val longTermBenefitNotesErrorResponse422Reads: Reads[BenefitCalculationDetailsErrorResponse422] =
      Json.reads[BenefitCalculationDetailsErrorResponse422]

  }

  // endregion Error422

  // region Error500

  case class BenefitCalculationDetailsHipFailureResponse500(origin: HipOrigin, response: HipFailureResponse)
      extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsHipFailureResponse500 {

    implicit val hipFailureResponse500Reads: Reads[BenefitCalculationDetailsHipFailureResponse500] =
      Json.reads[BenefitCalculationDetailsHipFailureResponse500]

  }

  // endregion Error500

  // region Error503

  case class BenefitCalculationDetailsHipFailureResponse503(origin: HipOrigin, response: HipFailureResponse)
      extends BenefitCalculationDetailsResponse

  object BenefitCalculationDetailsHipFailureResponse503 {

    implicit val hipFailureResponse503Reads: Reads[BenefitCalculationDetailsHipFailureResponse503] =
      Json.reads[BenefitCalculationDetailsHipFailureResponse503]

  }

  // endregion Error503

}

object BenefitCalculationDetailsSuccess {

  // region NewStatePensionCalculationDetails

  case class NetAdditionalPensionPre1997(value: BigDecimal) extends AnyVal

  object NetAdditionalPensionPre1997 {

    implicit val netAdditionalPensionPre1997Format: Format[NetAdditionalPensionPre1997] =
      Json.valueFormat[NetAdditionalPensionPre1997]

  }

  case class OldRulesStatePensionEntitlement(value: BigDecimal) extends AnyVal

  object OldRulesStatePensionEntitlement {

    implicit val oldRulesStatePensionEntitlementFormat: Format[OldRulesStatePensionEntitlement] =
      Json.valueFormat[OldRulesStatePensionEntitlement]

  }

  case class NetRulesAmount(value: BigDecimal) extends AnyVal

  object NetRulesAmount {
    implicit val netRulesAmountFormat: Format[NetRulesAmount] = Json.valueFormat[NetRulesAmount]
  }

  case class DerivedRebateAmount(value: BigDecimal) extends AnyVal

  object DerivedRebateAmount {
    implicit val derivedRebateAmountFormat: Format[DerivedRebateAmount] = Json.valueFormat[DerivedRebateAmount]
  }

  case class InitialStatePensionAmount(value: BigDecimal) extends AnyVal

  object InitialStatePensionAmount {

    implicit val initialStatePensionAmountFormat: Format[InitialStatePensionAmount] =
      Json.valueFormat[InitialStatePensionAmount]

  }

  case class ProtectedPayment2016(value: BigDecimal) extends AnyVal

  object ProtectedPayment2016 {
    implicit val protectedPayment2016Format: Format[ProtectedPayment2016] = Json.valueFormat[ProtectedPayment2016]
  }

  case class MinimumQualifyingPeriodMet(value: Boolean) extends AnyVal

  object MinimumQualifyingPeriodMet {

    implicit val minimumQualifyingPeriodMetFormat: Format[MinimumQualifyingPeriodMet] =
      Json.valueFormat[MinimumQualifyingPeriodMet]

  }

  case class QualifyingYearsAfter2016(value: Int) extends AnyVal

  object QualifyingYearsAfter2016 {

    implicit val qualifyingYearsAfter2016Format: Format[QualifyingYearsAfter2016] =
      Json.valueFormat[QualifyingYearsAfter2016]

  }

  case class NewStatePensionQualifyingYears(value: Int) extends AnyVal

  object NewStatePensionQualifyingYears {

    implicit val newStatePensionQualifyingYearsFormat: Format[NewStatePensionQualifyingYears] =
      Json.valueFormat[NewStatePensionQualifyingYears]

  }

  case class NewStatePensionRequisiteYears(value: Int) extends AnyVal

  object NewStatePensionRequisiteYears {

    implicit val newStatePensionRequisiteYearsFormat: Format[NewStatePensionRequisiteYears] =
      Json.valueFormat[NewStatePensionRequisiteYears]

  }

  case class NewStatePensionEntitlement(value: BigDecimal) extends AnyVal

  object NewStatePensionEntitlement {

    implicit val newStatePensionEntitlementFormat: Format[NewStatePensionEntitlement] =
      Json.valueFormat[NewStatePensionEntitlement]

  }

  case class ProtectedPayment(value: BigDecimal) extends AnyVal

  object ProtectedPayment {
    implicit val protectedPaymentFormat: Format[ProtectedPayment] = Json.valueFormat[ProtectedPayment]
  }

  case class PensionSharingOrderContractedOutEmploymentsGroup(value: Boolean) extends AnyVal

  object PensionSharingOrderContractedOutEmploymentsGroup {

    implicit val pensionSharingOrderContractedOutEmploymentsGroupFormat
        : Format[PensionSharingOrderContractedOutEmploymentsGroup] =
      Json.valueFormat[PensionSharingOrderContractedOutEmploymentsGroup]

  }

  case class PensionSharingOrderStateEarningsRelatedPensionScheme(value: Boolean) extends AnyVal

  object PensionSharingOrderStateEarningsRelatedPensionScheme {

    implicit val pensionSharingOrderStateEarningsRelatedPensionSchemeFormat
        : Format[PensionSharingOrderStateEarningsRelatedPensionScheme] =
      Json.valueFormat[PensionSharingOrderStateEarningsRelatedPensionScheme]

  }

  case class ConsiderReducedRateElection(value: Boolean) extends AnyVal

  object ConsiderReducedRateElection {

    implicit val considerReducedRateElectionFormat: Format[ConsiderReducedRateElection] =
      Json.valueFormat[ConsiderReducedRateElection]

  }

  case class WeeklyBudgetingLoanAmount(value: BigDecimal) extends AnyVal

  object WeeklyBudgetingLoanAmount {

    implicit val weeklyBudgetingLoanAmountFormat: Format[WeeklyBudgetingLoanAmount] =
      Json.valueFormat[WeeklyBudgetingLoanAmount]

  }

  case class CalculationDate(value: LocalDate) extends AnyVal

  object CalculationDate {
    implicit val calculationDateFormat: Format[CalculationDate] = Json.valueFormat[CalculationDate]
  }

  case class NewStatePensionCalculationDetails(
      netAdditionalPensionPre1997: Option[NetAdditionalPensionPre1997],
      oldRulesStatePensionEntitlement: Option[OldRulesStatePensionEntitlement],
      netRulesAmount: Option[NetRulesAmount],
      derivedRebateAmount: Option[DerivedRebateAmount],
      initialStatePensionAmount: Option[InitialStatePensionAmount],
      protectedPayment2016: Option[ProtectedPayment2016],
      minimumQualifyingPeriodMet: Option[MinimumQualifyingPeriodMet],
      qualifyingYearsAfter2016: Option[QualifyingYearsAfter2016],
      newStatePensionQualifyingYears: Option[NewStatePensionQualifyingYears],
      newStatePensionRequisiteYears: Option[NewStatePensionRequisiteYears],
      newStatePensionEntitlement: Option[NewStatePensionEntitlement],
      protectedPayment: Option[ProtectedPayment],
      pensionSharingOrderContractedOutEmploymentsGroup: Option[PensionSharingOrderContractedOutEmploymentsGroup],
      pensionSharingOrderStateEarningsRelatedPensionScheme: Option[
        PensionSharingOrderStateEarningsRelatedPensionScheme
      ],
      considerReducedRateElection: Option[ConsiderReducedRateElection],
      weeklyBudgetingLoanAmount: Option[WeeklyBudgetingLoanAmount],
      calculationDate: Option[CalculationDate]
  )

  object NewStatePensionCalculationDetails {

    implicit val newStatePensionCalculationDetailsFormat: Format[NewStatePensionCalculationDetails] =
      Json.format[NewStatePensionCalculationDetails]

  }

  // endregion NewStatePensionCalculationDetails

  // region BenefitCalculationDetail

  case class SubstitutionMethod1(value: Int) extends AnyVal

  object SubstitutionMethod1 {
    implicit val substitutionMethod1Format: Format[SubstitutionMethod1] = Json.valueFormat[SubstitutionMethod1]
  }

  case class SubstitutionMethod2(value: Int) extends AnyVal

  object SubstitutionMethod2 {
    implicit val substitutionMethod2Format: Format[SubstitutionMethod2] = Json.valueFormat[SubstitutionMethod2]
  }

  case class GuaranteedMinimumPensionContractedOutDeductionsPre1988(value: BigDecimal) extends AnyVal

  object GuaranteedMinimumPensionContractedOutDeductionsPre1988 {

    implicit val guaranteedMinimumPensionContractedOutDeductionsPre1988Format
        : Format[GuaranteedMinimumPensionContractedOutDeductionsPre1988] =
      Json.valueFormat[GuaranteedMinimumPensionContractedOutDeductionsPre1988]

  }

  case class GuaranteedMinimumPensionContractedOutDeductionsPost1988(value: BigDecimal) extends AnyVal

  object GuaranteedMinimumPensionContractedOutDeductionsPost1988 {

    implicit val GuaranteedMinimumPensionContractedOutDeductionsPost1988Format
        : Format[GuaranteedMinimumPensionContractedOutDeductionsPost1988] =
      Json.valueFormat[GuaranteedMinimumPensionContractedOutDeductionsPost1988]

  }

  case class ContractedOutDeductionsPre1988(value: BigDecimal) extends AnyVal

  object ContractedOutDeductionsPre1988 {

    implicit val contractedOutDeductionsPre1988Format: Format[ContractedOutDeductionsPre1988] =
      Json.valueFormat[ContractedOutDeductionsPre1988]

  }

  case class ContractedOutDeductionsPost1988(value: BigDecimal) extends AnyVal

  object ContractedOutDeductionsPost1988 {

    implicit val ContractedOutDeductionsPost1988Format: Format[ContractedOutDeductionsPost1988] =
      Json.valueFormat[ContractedOutDeductionsPost1988]

  }

  case class AdditionalPensionPercentage(value: Float) extends AnyVal

  object AdditionalPensionPercentage {

    implicit val additionalPensionPercentageFormat: Format[AdditionalPensionPercentage] =
      Json.valueFormat[AdditionalPensionPercentage]

  }

  case class BasicPensionPercentage(value: Int) extends AnyVal

  object BasicPensionPercentage {
    implicit val basicPensionPercentageFormat: Format[BasicPensionPercentage] = Json.valueFormat[BasicPensionPercentage]
  }

  case class SurvivorsBenefitAgeRelatedPensionPercentage(value: Float) extends AnyVal

  object SurvivorsBenefitAgeRelatedPensionPercentage {

    implicit val survivorsBenefitAgeRelatedPensionPercentageFormat
        : Format[SurvivorsBenefitAgeRelatedPensionPercentage] =
      Json.valueFormat[SurvivorsBenefitAgeRelatedPensionPercentage]

  }

  case class AdditionalAgeRelatedPensionPercentage(value: Float) extends AnyVal

  object AdditionalAgeRelatedPensionPercentage {

    implicit val additionalAgeRelatedPensionPercentageFormat: Format[AdditionalAgeRelatedPensionPercentage] =
      Json.valueFormat[AdditionalAgeRelatedPensionPercentage]

  }

  case class InheritedBasicPensionPercentage(value: Float) extends AnyVal

  object InheritedBasicPensionPercentage {

    implicit val inheritedBasicPensionPercentageFormat: Format[InheritedBasicPensionPercentage] =
      Json.valueFormat[InheritedBasicPensionPercentage]

  }

  case class InheritedAdditionalPensionPercentage(value: Float) extends AnyVal

  object InheritedAdditionalPensionPercentage {

    implicit val inheritedAdditionalPensionPercentageFormat: Format[InheritedAdditionalPensionPercentage] =
      Json.valueFormat[InheritedAdditionalPensionPercentage]

  }

  case class InheritedGraduatedPensionPercentage(value: Float) extends AnyVal

  object InheritedGraduatedPensionPercentage {

    implicit val inheritedGraduatedPensionPercentageFormat: Format[InheritedGraduatedPensionPercentage] =
      Json.valueFormat[InheritedGraduatedPensionPercentage]

  }

  case class InheritedGraduatedBenefit(value: BigDecimal) extends AnyVal

  object InheritedGraduatedBenefit {

    implicit val inheritedGraduatedBenefitFormat: Format[InheritedGraduatedBenefit] =
      Json.valueFormat[InheritedGraduatedBenefit]

  }

  case class HusbandDateOfDeath(value: LocalDate) extends AnyVal

  object HusbandDateOfDeath {
    implicit val husbandDateOfDeathFormat: Format[HusbandDateOfDeath] = Json.valueFormat[HusbandDateOfDeath]
  }

  case class AdditionalPost1997PensionPercentage(value: Float) extends AnyVal

  object AdditionalPost1997PensionPercentage {

    implicit val additionalPost1997PensionPercentageFormat: Format[AdditionalPost1997PensionPercentage] =
      Json.valueFormat[AdditionalPost1997PensionPercentage]

  }

  case class AdditionalPost1997AgeRelatedPensionPercentage(value: Float) extends AnyVal

  object AdditionalPost1997AgeRelatedPensionPercentage {

    implicit val additionalPost1997AgeRelatedPensionPercentageFormat
        : Format[AdditionalPost1997AgeRelatedPensionPercentage] =
      Json.valueFormat[AdditionalPost1997AgeRelatedPensionPercentage]

  }

  case class AdditionalPensionNotionalPercentage(value: Float) extends AnyVal

  object AdditionalPensionNotionalPercentage {

    implicit val additionalPensionNotionalPercentageFormat: Format[AdditionalPensionNotionalPercentage] =
      Json.valueFormat[AdditionalPensionNotionalPercentage]

  }

  case class AdditionalPost1997PensionNotionalPercentage(value: Float) extends AnyVal

  object AdditionalPost1997PensionNotionalPercentage {

    implicit val additionalPost1997PensionNotionalPercentageFormat
        : Format[AdditionalPost1997PensionNotionalPercentage] =
      Json.valueFormat[AdditionalPost1997PensionNotionalPercentage]

  }

  case class InheritedAdditionalPensionNotionalPercentage(value: Float) extends AnyVal

  object InheritedAdditionalPensionNotionalPercentage {

    implicit val inheritedAdditionalPensionNotionalPercentageFormat
        : Format[InheritedAdditionalPensionNotionalPercentage] =
      Json.valueFormat[InheritedAdditionalPensionNotionalPercentage]

  }

  case class InheritableAdditionalPensionPercentage(value: Int) extends AnyVal

  object InheritableAdditionalPensionPercentage {

    implicit val inheritableAdditionalPensionPercentageFormat: Format[InheritableAdditionalPensionPercentage] =
      Json.valueFormat[InheritableAdditionalPensionPercentage]

  }

  case class AdditionalPost2002PensionNotionalPercentage(value: Float) extends AnyVal

  object AdditionalPost2002PensionNotionalPercentage {

    implicit val additionalPost2002PensionNotionalPercentageFormat
        : Format[AdditionalPost2002PensionNotionalPercentage] =
      Json.valueFormat[AdditionalPost2002PensionNotionalPercentage]

  }

  case class AdditionalPost2002PensionPercentage(value: Float) extends AnyVal

  object AdditionalPost2002PensionPercentage {

    implicit val additionalPost2002PensionPercentageFormat: Format[AdditionalPost2002PensionPercentage] =
      Json.valueFormat[AdditionalPost2002PensionPercentage]

  }

  case class InheritedAdditionalPost2002PensionNotionalPercentage(value: Float) extends AnyVal

  object InheritedAdditionalPost2002PensionNotionalPercentage {

    implicit val inheritedAdditionalPost2002PensionNotionalPercentageFormat
        : Format[InheritedAdditionalPost2002PensionNotionalPercentage] =
      Json.valueFormat[InheritedAdditionalPost2002PensionNotionalPercentage]

  }

  case class InheritedAdditionalPost2002PensionPercentage(value: Float) extends AnyVal

  object InheritedAdditionalPost2002PensionPercentage {

    implicit val InheritedAdditionalPost2002PensionPercentageFormat
        : Format[InheritedAdditionalPost2002PensionPercentage] =
      Json.valueFormat[InheritedAdditionalPost2002PensionPercentage]

  }

  case class AdditionalPost2002AgeRelatedPensionPercentage(value: Float) extends AnyVal

  object AdditionalPost2002AgeRelatedPensionPercentage {

    implicit val additionalPost2002AgeRelatedPensionPercentageFormat
        : Format[AdditionalPost2002AgeRelatedPensionPercentage] =
      Json.valueFormat[AdditionalPost2002AgeRelatedPensionPercentage]

  }

  case class SingleContributionConditionRulesApply(value: Boolean) extends AnyVal

  object SingleContributionConditionRulesApply {

    implicit val singleContributionConditionRulesApplyFormat: Format[SingleContributionConditionRulesApply] =
      Json.valueFormat[SingleContributionConditionRulesApply]

  }

  case class BenefitCalculationDetail(
      nationalInsuranceNumber: Identifier,
      benefitType: LongTermBenefitType,
      associatedCalculationSequenceNumber: AssociatedCalculationSequenceNumber,
      calculationStatus: Option[CalculationStatus],
      substitutionMethod1: Option[SubstitutionMethod1],
      substitutionMethod2: Option[SubstitutionMethod2],
      calculationDate: Option[CalculationDate],
      guaranteedMinimumPensionContractedOutDeductionsPre1988: Option[
        GuaranteedMinimumPensionContractedOutDeductionsPre1988
      ],
      guaranteedMinimumPensionContractedOutDeductionsPost1988: Option[
        GuaranteedMinimumPensionContractedOutDeductionsPost1988
      ],
      contractedOutDeductionsPre1988: Option[ContractedOutDeductionsPre1988],
      contractedOutDeductionsPost1988: Option[ContractedOutDeductionsPost1988],
      additionalPensionPercentage: Option[AdditionalPensionPercentage],
      basicPensionPercentage: Option[BasicPensionPercentage],
      survivorsBenefitAgeRelatedPensionPercentage: Option[SurvivorsBenefitAgeRelatedPensionPercentage],
      additionalAgeRelatedPensionPercentage: Option[AdditionalAgeRelatedPensionPercentage],
      inheritedBasicPensionPercentage: Option[InheritedBasicPensionPercentage],
      inheritedAdditionalPensionPercentage: Option[InheritedAdditionalPensionPercentage],
      inheritedGraduatedPensionPercentage: Option[InheritedGraduatedPensionPercentage],
      inheritedGraduatedBenefit: Option[InheritedGraduatedBenefit],
      calculationSource: Option[CalculationSource],
      payday: Option[Payday],
      dateOfBirth: Option[DateOfBirth],
      husbandDateOfDeath: Option[HusbandDateOfDeath],
      additionalPost1997PensionPercentage: Option[AdditionalPost1997PensionPercentage],
      additionalPost1997AgeRelatedPensionPercentage: Option[AdditionalPost1997AgeRelatedPensionPercentage],
      additionalPensionNotionalPercentage: Option[AdditionalPensionNotionalPercentage],
      additionalPost1997PensionNotionalPercentage: Option[AdditionalPost1997PensionNotionalPercentage],
      inheritedAdditionalPensionNotionalPercentage: Option[InheritedAdditionalPensionNotionalPercentage],
      inheritableAdditionalPensionPercentage: Option[InheritableAdditionalPensionPercentage],
      additionalPost2002PensionNotionalPercentage: Option[AdditionalPost2002PensionNotionalPercentage],
      additionalPost2002PensionPercentage: Option[AdditionalPost2002PensionPercentage],
      inheritedAdditionalPost2002PensionNotionalPercentage: Option[
        InheritedAdditionalPost2002PensionNotionalPercentage
      ],
      inheritedAdditionalPost2002PensionPercentage: Option[InheritedAdditionalPost2002PensionPercentage],
      additionalPost2002AgeRelatedPensionPercentage: Option[AdditionalPost2002AgeRelatedPensionPercentage],
      singleContributionConditionRulesApply: Option[SingleContributionConditionRulesApply],
      officeDetails: Option[OfficeDetails],
      newStatePensionCalculationDetails: Option[NewStatePensionCalculationDetails]
  )

  object BenefitCalculationDetail {

    implicit val benefitCalculationDetailFormat: Format[BenefitCalculationDetail] =
      Json.format[BenefitCalculationDetail]

  }

  // endregion BenefitCalculationDetail

  // region BenefitCalculationDetailsList

  case class AdditionalPensionAmountPre1997(value: BigDecimal) extends AnyVal

  object AdditionalPensionAmountPre1997 {

    implicit val additionalPensionAmountPre1997Format: Format[AdditionalPensionAmountPre1997] =
      Json.valueFormat[AdditionalPensionAmountPre1997]

  }

  case class AdditionalPensionAmountPost1997(value: BigDecimal) extends AnyVal

  object AdditionalPensionAmountPost1997 {

    implicit val additionalPensionAmountPost1997Format: Format[AdditionalPensionAmountPost1997] =
      Json.valueFormat[AdditionalPensionAmountPost1997]

  }

  case class Pre97AgeRelatedAdditionalPension(value: BigDecimal) extends AnyVal

  object Pre97AgeRelatedAdditionalPension {

    implicit val pre97AgeRelatedAdditionalPensionFormat: Format[Pre97AgeRelatedAdditionalPension] =
      Json.valueFormat[Pre97AgeRelatedAdditionalPension]

  }

  case class Post97AgeRelatedAdditionalPension(value: BigDecimal) extends AnyVal

  object Post97AgeRelatedAdditionalPension {

    implicit val post97AgeRelatedAdditionalPensionFormat: Format[Post97AgeRelatedAdditionalPension] =
      Json.valueFormat[Post97AgeRelatedAdditionalPension]

  }

  case class BasicPensionIncrementsCashValue(value: BigDecimal) extends AnyVal

  object BasicPensionIncrementsCashValue {

    implicit val basicPensionIncrementsCashValueFormat: Format[BasicPensionIncrementsCashValue] =
      Json.valueFormat[BasicPensionIncrementsCashValue]

  }

  case class AdditionalPensionIncrementsCashValue(value: BigDecimal) extends AnyVal

  object AdditionalPensionIncrementsCashValue {

    implicit val additionalPensionIncrementsCashValueFormat: Format[AdditionalPensionIncrementsCashValue] =
      Json.valueFormat[AdditionalPensionIncrementsCashValue]

  }

  case class GraduatedRetirementBenefitCashValue(value: BigDecimal) extends AnyVal

  object GraduatedRetirementBenefitCashValue {

    implicit val graduatedRetirementBenefitCashValueFormat: Format[GraduatedRetirementBenefitCashValue] =
      Json.valueFormat[GraduatedRetirementBenefitCashValue]

  }

  case class TotalGuaranteedMinimumPension(value: BigDecimal) extends AnyVal

  object TotalGuaranteedMinimumPension {

    implicit val totalGuaranteedMinimumPensionFormat: Format[TotalGuaranteedMinimumPension] =
      Json.valueFormat[TotalGuaranteedMinimumPension]

  }

  case class TotalNonGuaranteedMinimumPension(value: BigDecimal) extends AnyVal

  object TotalNonGuaranteedMinimumPension {

    implicit val totalNonGuaranteedMinimumPensionFormat: Format[TotalNonGuaranteedMinimumPension] =
      Json.valueFormat[TotalNonGuaranteedMinimumPension]

  }

  case class LongTermBenefitsIncrementalCashValue(value: BigDecimal) extends AnyVal

  object LongTermBenefitsIncrementalCashValue {

    implicit val longTermBenefitsIncrementalCashValueFormat: Format[LongTermBenefitsIncrementalCashValue] =
      Json.valueFormat[LongTermBenefitsIncrementalCashValue]

  }

  case class GreatBritainPaymentAmount(value: BigDecimal) extends AnyVal

  object GreatBritainPaymentAmount {

    implicit val greatBritainPaymentAmountFormat: Format[GreatBritainPaymentAmount] =
      Json.valueFormat[GreatBritainPaymentAmount]

  }

  case class NotionalPost1997AdditionalPension(value: BigDecimal) extends AnyVal

  object NotionalPost1997AdditionalPension {

    implicit val notionalPost1997AdditionalPensionFormat: Format[NotionalPost1997AdditionalPension] =
      Json.valueFormat[NotionalPost1997AdditionalPension]

  }

  case class NotionalPre1997AdditionalPension(value: BigDecimal) extends AnyVal

  object NotionalPre1997AdditionalPension {

    implicit val notionalPre1997AdditionalPensionFormat: Format[NotionalPre1997AdditionalPension] =
      Json.valueFormat[NotionalPre1997AdditionalPension]

  }

  case class InheritableNotionalAdditionalPensionIncrements(value: BigDecimal) extends AnyVal

  object InheritableNotionalAdditionalPensionIncrements {

    implicit val inheritableNotionalAdditionalPensionIncrementsFormat
        : Format[InheritableNotionalAdditionalPensionIncrements] =
      Json.valueFormat[InheritableNotionalAdditionalPensionIncrements]

  }

  case class ConditionOneSatisfied(value: String) extends AnyVal

  object ConditionOneSatisfied {
    implicit val conditionOneSatisfiedFormat: Format[ConditionOneSatisfied] = Json.valueFormat[ConditionOneSatisfied]
  }

  case class ReasonForFormIssue(value: String) extends AnyVal

  object ReasonForFormIssue {
    implicit val reasonForFormIssueFormat: Format[ReasonForFormIssue] = Json.valueFormat[ReasonForFormIssue]
  }

  case class LongTermBenefitsCategoryACashValue(value: BigDecimal) extends AnyVal

  object LongTermBenefitsCategoryACashValue {

    implicit val longTermBenefitsCategoryACashValueFormat: Format[LongTermBenefitsCategoryACashValue] =
      Json.valueFormat[LongTermBenefitsCategoryACashValue]

  }

  case class LongTermBenefitsCategoryBLCashValue(value: BigDecimal) extends AnyVal

  object LongTermBenefitsCategoryBLCashValue {

    implicit val longTermBenefitsCategoryBLCashValueFormat: Format[LongTermBenefitsCategoryBLCashValue] =
      Json.valueFormat[LongTermBenefitsCategoryBLCashValue]

  }

  case class LongTermBenefitsUnitValue(value: BigDecimal) extends AnyVal

  object LongTermBenefitsUnitValue {

    implicit val longTermBenefitsUnitValueFormat: Format[LongTermBenefitsUnitValue] =
      Json.valueFormat[LongTermBenefitsUnitValue]

  }

  case class AdditionalNotionalPensionAmountPost2002(value: BigDecimal) extends AnyVal

  object AdditionalNotionalPensionAmountPost2002 {

    implicit val additionalNotionalPensionAmountPost2002Format: Format[AdditionalNotionalPensionAmountPost2002] =
      Json.valueFormat[AdditionalNotionalPensionAmountPost2002]

  }

  case class AdditionalPensionAmountPost2002(value: BigDecimal) extends AnyVal

  object AdditionalPensionAmountPost2002 {

    implicit val additionalPensionAmountPost2002Format: Format[AdditionalPensionAmountPost2002] =
      Json.valueFormat[AdditionalPensionAmountPost2002]

  }

  case class AdditionalNotionalPensionIncrementsInheritedPost2002(value: BigDecimal) extends AnyVal

  object AdditionalNotionalPensionIncrementsInheritedPost2002 {

    implicit val additionalNotionalPensionIncrementsInheritedPost2002Format
        : Format[AdditionalNotionalPensionIncrementsInheritedPost2002] =
      Json.valueFormat[AdditionalNotionalPensionIncrementsInheritedPost2002]

  }

  case class AdditionalPensionIncrementsInheritedPost2002(value: BigDecimal) extends AnyVal

  object AdditionalPensionIncrementsInheritedPost2002 {

    implicit val additionalPensionIncrementsInheritedPost2002Format
        : Format[AdditionalPensionIncrementsInheritedPost2002] =
      Json.valueFormat[AdditionalPensionIncrementsInheritedPost2002]

  }

  case class Post02AgeRelatedAdditionalPension(value: BigDecimal) extends AnyVal

  object Post02AgeRelatedAdditionalPension {

    implicit val post02AgeRelatedAdditionalPensionFormat: Format[Post02AgeRelatedAdditionalPension] =
      Json.valueFormat[Post02AgeRelatedAdditionalPension]

  }

  case class Pre1975ShortTermBenefits(value: Int) extends AnyVal

  object Pre1975ShortTermBenefits {

    implicit val pre1975ShortTermBenefitsFormat: Format[Pre1975ShortTermBenefits] =
      Json.valueFormat[Pre1975ShortTermBenefits]

  }

  case class SurvivingSpouseAge(value: Int) extends AnyVal

  object SurvivingSpouseAge {
    implicit val survivingSpouseAgeFormat: Format[SurvivingSpouseAge] = Json.valueFormat[SurvivingSpouseAge]
  }

  case class OperativeBenefitStartDate(value: LocalDate) extends AnyVal

  object OperativeBenefitStartDate {

    implicit val operativeBenefitStartDateFormat: Format[OperativeBenefitStartDate] =
      Json.valueFormat[OperativeBenefitStartDate]

  }

  case class SicknessBenefitStatusForReports(value: String) extends AnyVal

  object SicknessBenefitStatusForReports {

    implicit val sicknessBenefitStatusForReportsFormat: Format[SicknessBenefitStatusForReports] =
      Json.valueFormat[SicknessBenefitStatusForReports]

  }

  case class BenefitCalculationDetailsList(
      additionalPensionAmountPre1997: Option[AdditionalPensionAmountPre1997],
      additionalPensionAmountPost1997: Option[AdditionalPensionAmountPost1997],
      pre97AgeRelatedAdditionalPension: Option[Pre97AgeRelatedAdditionalPension],
      post97AgeRelatedAdditionalPension: Option[Post97AgeRelatedAdditionalPension],
      basicPensionIncrementsCashValue: Option[BasicPensionIncrementsCashValue],
      additionalPensionIncrementsCashValue: Option[AdditionalPensionIncrementsCashValue],
      graduatedRetirementBenefitCashValue: Option[GraduatedRetirementBenefitCashValue],
      totalGuaranteedMinimumPension: Option[TotalGuaranteedMinimumPension],
      totalNonGuaranteedMinimumPension: Option[TotalNonGuaranteedMinimumPension],
      longTermBenefitsIncrementalCashValue: Option[LongTermBenefitsIncrementalCashValue],
      greatBritainPaymentAmount: Option[GreatBritainPaymentAmount],
      dateOfBirth: Option[DateOfBirth],
      notionalPost1997AdditionalPension: Option[NotionalPost1997AdditionalPension],
      notionalPre1997AdditionalPension: Option[NotionalPre1997AdditionalPension],
      inheritableNotionalAdditionalPensionIncrements: Option[InheritableNotionalAdditionalPensionIncrements],
      conditionOneSatisfied: Option[ConditionOneSatisfied],
      reasonForFormIssue: Option[ReasonForFormIssue],
      longTermBenefitsCategoryACashValue: Option[LongTermBenefitsCategoryACashValue],
      longTermBenefitsCategoryBLCashValue: Option[LongTermBenefitsCategoryBLCashValue],
      longTermBenefitsUnitValue: Option[LongTermBenefitsUnitValue],
      additionalPensionAmountPost2002: Option[AdditionalPensionAmountPost2002],
      additionalNotionalPensionAmountPost2002: Option[AdditionalNotionalPensionAmountPost2002],
      additionalNotionalPensionIncrementsInheritedPost2002: Option[
        AdditionalNotionalPensionIncrementsInheritedPost2002
      ],
      additionalPensionIncrementsInheritedPost2002: Option[AdditionalPensionIncrementsInheritedPost2002],
      post02AgeRelatedAdditionalPension: Option[Post02AgeRelatedAdditionalPension],
      pre1975ShortTermBenefits: Option[Pre1975ShortTermBenefits],
      survivingSpouseAge: Option[SurvivingSpouseAge],
      operativeBenefitStartDate: Option[OperativeBenefitStartDate],
      sicknessBenefitStatusForReports: Option[SicknessBenefitStatusForReports],
      benefitCalculationDetail: Option[BenefitCalculationDetail]
  )

  object BenefitCalculationDetailsList {

    implicit val benefitCalculationDetailsListFormat: Format[BenefitCalculationDetailsList] =
      Json.format[BenefitCalculationDetailsList]

  }

  // endregion BenefitCalculationDetailsList

  // region SuccessResponse

  case class StatePensionAgeBefore2010TaxYear(value: Boolean) extends AnyVal

  object StatePensionAgeBefore2010TaxYear {

    implicit val statePensionAgeBefore2010TaxYearFormat: Format[StatePensionAgeBefore2010TaxYear] =
      Json.valueFormat[StatePensionAgeBefore2010TaxYear]

  }

  case class StatePensionAgeAfter2016TaxYear(value: Boolean) extends AnyVal

  object StatePensionAgeAfter2016TaxYear {

    implicit val statePensionAgeAfter2016TaxYearFormat: Format[StatePensionAgeAfter2016TaxYear] =
      Json.valueFormat[StatePensionAgeAfter2016TaxYear]

  }

  case class BenefitCalculationDetailsSuccessResponse(
      statePensionAgeBefore2010TaxYear: Option[StatePensionAgeBefore2010TaxYear],
      statePensionAgeAfter2016TaxYear: Option[StatePensionAgeAfter2016TaxYear],
      benefitCalculationDetailsList: Option[List[BenefitCalculationDetailsList]]
  ) extends BenefitCalculationDetailsResponse
      with NpsSuccessfulApiResponse

  object BenefitCalculationDetailsSuccessResponse {

    implicit val benefitCalculationDetailsSuccessResponseFormat: Format[BenefitCalculationDetailsSuccessResponse] =
      Json.format[BenefitCalculationDetailsSuccessResponse]

  }

  // endregion SuccessResponse

}
