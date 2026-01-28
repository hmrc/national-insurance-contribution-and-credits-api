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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.{
  OfficeDetails,
  OfficeLocationDecode,
  OfficeLocationValue
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.EnumOffidtp

import java.time.LocalDate

class BenefitCalculationDetailsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val benefitCalculationDetailsSuccessResponse = BenefitCalculationDetailsSuccessResponse(
    statePensionAgeBefore2010TaxYear = Some(StatePensionAgeBefore2010TaxYear(true)),
    statePensionAgeAfter2016TaxYear = Some(StatePensionAgeAfter2016TaxYear(true)),
    benefitCalculationDetailsList = Some(
      List(
        BenefitCalculationDetailsList(
          additionalPensionAmountPre1997 = Some(AdditionalPensionAmountPre1997(10.56)),
          additionalPensionAmountPost1997 = Some(AdditionalPensionAmountPost1997(10.56)),
          pre97AgeRelatedAdditionalPension = Some(Pre97AgeRelatedAdditionalPension(10.56)),
          post97AgeRelatedAdditionalPension = Some(Post97AgeRelatedAdditionalPension(10.56)),
          basicPensionIncrementsCashValue = Some(BasicPensionIncrementsCashValue(10.56)),
          additionalPensionIncrementsCashValue = Some(AdditionalPensionIncrementsCashValue(10.56)),
          graduatedRetirementBenefitCashValue = Some(GraduatedRetirementBenefitCashValue(10.56)),
          totalGuaranteedMinimumPension = Some(TotalGuaranteedMinimumPension(10.56)),
          totalNonGuaranteedMinimumPension = Some(TotalNonGuaranteedMinimumPension(10.56)),
          longTermBenefitsIncrementalCashValue = Some(LongTermBenefitsIncrementalCashValue(10.56)),
          greatBritainPaymentAmount = Some(GreatBritainPaymentAmount(10.56)),
          dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
          notionalPost1997AdditionalPension = Some(NotionalPost1997AdditionalPension(10.56)),
          notionalPre1997AdditionalPension = Some(NotionalPre1997AdditionalPension(10.56)),
          inheritableNotionalAdditionalPensionIncrements = Some(InheritableNotionalAdditionalPensionIncrements(10.56)),
          conditionOneSatisfied = Some(ConditionOneSatisfied("H")),
          reasonForFormIssue = Some(ReasonForFormIssue("REQUESTED BENEFIT CALCULATION")),
          longTermBenefitsCategoryACashValue = Some(LongTermBenefitsCategoryACashValue(10.56)),
          longTermBenefitsCategoryBLCashValue = Some(LongTermBenefitsCategoryBLCashValue(10.56)),
          longTermBenefitsUnitValue = Some(LongTermBenefitsUnitValue(10.56)),
          additionalNotionalPensionAmountPost2002 = Some(AdditionalNotionalPensionAmountPost2002(10.56)),
          additionalPensionAmountPost2002 = Some(AdditionalPensionAmountPost2002(10.56)),
          additionalNotionalPensionIncrementsInheritedPost2002 =
            Some(AdditionalNotionalPensionIncrementsInheritedPost2002(10.56)),
          additionalPensionIncrementsInheritedPost2002 = Some(AdditionalPensionIncrementsInheritedPost2002(10.56)),
          post02AgeRelatedAdditionalPension = Some(Post02AgeRelatedAdditionalPension(10.56)),
          pre1975ShortTermBenefits = Some(Pre1975ShortTermBenefits(2)),
          survivingSpouseAge = Some(SurvivingSpouseAge(45)),
          operativeBenefitStartDate = Some(OperativeBenefitStartDate(LocalDate.parse("2022-06-27"))),
          sicknessBenefitStatusForReports = Some(SicknessBenefitStatusForReports("Y")),
          benefitCalculationDetail = Some(
            BenefitCalculationDetail(
              nationalInsuranceNumber = Identifier("AA123456"),
              benefitType = LongTermBenefitType.All,
              associatedCalculationSequenceNumber = AssociatedCalculationSequenceNumber(86),
              calculationStatus = Some(CalculationStatus.Definitive),
              substitutionMethod1 = Some(SubstitutionMethod1(235)),
              substitutionMethod2 = Some(SubstitutionMethod2(235)),
              calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27"))),
              guaranteedMinimumPensionContractedOutDeductionsPre1988 =
                Some(GuaranteedMinimumPensionContractedOutDeductionsPre1988(10.56)),
              guaranteedMinimumPensionContractedOutDeductionsPost1988 =
                Some(GuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
              contractedOutDeductionsPre1988 = Some(ContractedOutDeductionsPre1988(10.56)),
              contractedOutDeductionsPost1988 = Some(ContractedOutDeductionsPost1988(10.56)),
              additionalPensionPercentage = Some(AdditionalPensionPercentage(10.56)),
              basicPensionPercentage = Some(BasicPensionPercentage(86)),
              survivorsBenefitAgeRelatedPensionPercentage = Some(SurvivorsBenefitAgeRelatedPensionPercentage(10.56)),
              additionalAgeRelatedPensionPercentage = Some(AdditionalAgeRelatedPensionPercentage(10.56)),
              inheritedBasicPensionPercentage = Some(InheritedBasicPensionPercentage(10.56)),
              inheritedAdditionalPensionPercentage = Some(InheritedAdditionalPensionPercentage(10.56)),
              inheritedGraduatedPensionPercentage = Some(InheritedGraduatedPensionPercentage(10.56)),
              inheritedGraduatedBenefit = Some(InheritedGraduatedBenefit(10.56)),
              calculationSource = Some(CalculationSource.ApComponentSuspectAprilMayCalc),
              payday = Some(Payday.Friday),
              dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
              husbandDateOfDeath = Some(HusbandDateOfDeath(LocalDate.parse("2022-06-27"))),
              additionalPost1997PensionPercentage = Some(AdditionalPost1997PensionPercentage(10.56)),
              additionalPost1997AgeRelatedPensionPercentage =
                Some(AdditionalPost1997AgeRelatedPensionPercentage(10.56)),
              additionalPensionNotionalPercentage = Some(AdditionalPensionNotionalPercentage(10.56)),
              additionalPost1997PensionNotionalPercentage = Some(AdditionalPost1997PensionNotionalPercentage(10.56)),
              inheritedAdditionalPensionNotionalPercentage = Some(InheritedAdditionalPensionNotionalPercentage(10.56)),
              inheritableAdditionalPensionPercentage = Some(InheritableAdditionalPensionPercentage(90)),
              additionalPost2002PensionNotionalPercentage = Some(AdditionalPost2002PensionNotionalPercentage(10.56)),
              additionalPost2002PensionPercentage = Some(AdditionalPost2002PensionPercentage(10.56)),
              inheritedAdditionalPost2002PensionNotionalPercentage =
                Some(InheritedAdditionalPost2002PensionNotionalPercentage(10.56)),
              inheritedAdditionalPost2002PensionPercentage = Some(InheritedAdditionalPost2002PensionPercentage(10.56)),
              additionalPost2002AgeRelatedPensionPercentage =
                Some(AdditionalPost2002AgeRelatedPensionPercentage(10.56)),
              singleContributionConditionRulesApply = Some(SingleContributionConditionRulesApply(true)),
              officeDetails = Some(
                OfficeDetails(
                  officeLocationDecode = Some(OfficeLocationDecode(1)),
                  officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                  officeIdentifier = Some(EnumOffidtp.None)
                )
              ),
              newStatePensionCalculationDetails = Some(
                NewStatePensionCalculationDetails(
                  netAdditionalPensionPre1997 = Some(NetAdditionalPensionPre1997(10.56)),
                  oldRulesStatePensionEntitlement = Some(OldRulesStatePensionEntitlement(10.56)),
                  netRulesAmount = Some(NetRulesAmount(10.56)),
                  derivedRebateAmount = Some(DerivedRebateAmount(10.56)),
                  initialStatePensionAmount = Some(InitialStatePensionAmount(10.56)),
                  protectedPayment2016 = Some(ProtectedPayment2016(10.56)),
                  minimumQualifyingPeriodMet = Some(MinimumQualifyingPeriodMet(true)),
                  qualifyingYearsAfter2016 = Some(QualifyingYearsAfter2016(3)),
                  newStatePensionQualifyingYears = Some(NewStatePensionQualifyingYears(20)),
                  newStatePensionRequisiteYears = Some(NewStatePensionRequisiteYears(35)),
                  newStatePensionEntitlement = Some(NewStatePensionEntitlement(10.56)),
                  protectedPayment = Some(ProtectedPayment(10.56)),
                  pensionSharingOrderContractedOutEmploymentsGroup =
                    Some(PensionSharingOrderContractedOutEmploymentsGroup(true)),
                  pensionSharingOrderStateEarningsRelatedPensionScheme =
                    Some(PensionSharingOrderStateEarningsRelatedPensionScheme(true)),
                  considerReducedRateElection = Some(ConsiderReducedRateElection(true)),
                  weeklyBudgetingLoanAmount = Some(WeeklyBudgetingLoanAmount(10.56)),
                  calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27")))
                )
              )
            )
          )
        )
      )
    )
  )

  val hipFailureResponse = NpsErrorResponseHipOrigin(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse503 = NpsErrorResponse503(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse500 = NpsErrorResponse500(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val standardErrorResponse400 = NpsStandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = ErrorResponse400(
      failures = List(
        ErrorResourceObj400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
        ErrorResourceObj400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
      )
    )
  )

  val npsErrorResponse403 =
    NpsErrorResponse403(NpsErrorCode403.NpsErrorCode403_2, NpsErrorReason403.Forbidden)

  val npsErrorResponse422 =
    NpsErrorResponse422(
      List(
        NpsError422(NpsErrorCode("some code 1"), Reason("some reason 2")),
        NpsError422(NpsErrorCode("some code 2"), Reason("some reason 2"))
      )
    )

  val underTest = new BenefitCalculationDetailsResponseMapper

  "BenefitCalculationDetailsResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {
        underTest.toApiResult(benefitCalculationDetailsSuccessResponse) shouldBe
          SuccessResult(ApiName.BenefitCalculationDetails, benefitCalculationDetailsSuccessResponse)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (HipFailureResponse)" in {
        underTest.toApiResult(hipFailureResponse) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.BadRequest, Some(hipFailureResponse))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (StandardErrorResponse400)" in {

        underTest.toApiResult(standardErrorResponse400) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.BadRequest, Some(standardErrorResponse400))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 403" in {
        underTest.toApiResult(npsErrorResponse403) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.AccessForbidden, Some(npsErrorResponse403))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 422" in {
        underTest.toApiResult(npsErrorResponse422) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(npsErrorResponse422))
          )
      }

      "should successfully return a DownstreamErrorReport result when when handling a 500" in {
        underTest.toApiResult(npsErrorResponse500) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.InternalServerError, Some(npsErrorResponse500))
          )
      }

      "should successfully return a DownstreamErrorReport result when when handling a 503" in {
        underTest.toApiResult(npsErrorResponse503) shouldBe
          FailureResult(
            ApiName.BenefitCalculationDetails,
            ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(npsErrorResponse503))
          )
      }

    }
  }

}
