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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.enums.{
  ContributionCreditType,
  CreditSourceType
}

class IndividualStatePensionInformationResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val individualStatePensionInformationSuccessResponse = IndividualStatePensionInformationSuccessResponse(
    identifier = Identifier("AA000001A"),
    numberOfQualifyingYears = Some(NumberOfQualifyingYears(35)),
    nonQualifyingYears = Some(NonQualifyingYears(5)),
    yearsToFinalRelevantYear = Some(YearsToFinalRelevantYear(3)),
    nonQualifyingYearsPayable = Some(NonQualifyingYearsPayable(2)),
    pre1975CCCount = Some(Pre1975CCCount(156)),
    dateOfEntry = Some(DateOfEntry("1975-04-06")),
    contributionsByTaxYear = Some(
      List(
        ContributionsByTaxYear(
          taxYear = Some(TaxYear(2022)),
          qualifyingTaxYear = Some(QualifyingTaxYear(true)),
          payableAccepted = Some(PayableAccepted(false)),
          amountNeeded = Some(AmountNeeded(BigDecimal("1250.50"))),
          classThreePayable = Some(ClassThreePayable(BigDecimal("824.20"))),
          classThreePayableBy = Some(ClassThreePayableBy("2028-04-05")),
          classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2030-04-05")),
          classTwoPayable = Some(ClassTwoPayable(BigDecimal("164.25"))),
          classTwoPayableBy = Some(ClassTwoPayableBy("2028-01-31")),
          classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2030-01-31")),
          classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(12)),
          totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("3456.78"))),
          niEarnings = Some(NiEarnings(BigDecimal("45000.00"))),
          coClassOnePaid = Some(CoClassOnePaid(BigDecimal("1234.56"))),
          totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("52000.00"))),
          niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
          niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
          underInvestigationFlag = Some(UnderInvestigationFlag(true)),
          primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("48500.75"))),
          otherCredits = Some(
            List(
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class1Credit),
                creditSourceType = Some(CreditSourceType.JsaTapeInput),
                contributionCreditCount = Some(ContributionCreditCount(15))
              ),
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class3Credit),
                creditSourceType = Some(CreditSourceType.CarersCredit),
                contributionCreditCount = Some(ContributionCreditCount(52))
              ),
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class2NormalRate),
                creditSourceType = Some(CreditSourceType.ChildBenefit),
                contributionCreditCount = Some(ContributionCreditCount(-5))
              )
            )
          )
        ),
        ContributionsByTaxYear(
          taxYear = Some(TaxYear(2023)),
          qualifyingTaxYear = Some(QualifyingTaxYear(false)),
          payableAccepted = Some(PayableAccepted(true)),
          amountNeeded = Some(AmountNeeded(BigDecimal("2100.75"))),
          classThreePayable = Some(ClassThreePayable(BigDecimal("876.80"))),
          classThreePayableBy = Some(ClassThreePayableBy("2029-04-05")),
          classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2031-04-05")),
          classTwoPayable = Some(ClassTwoPayable(BigDecimal("175.60"))),
          classTwoPayableBy = Some(ClassTwoPayableBy("2029-01-31")),
          classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2031-01-31")),
          classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(35)),
          totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("2987.45"))),
          niEarnings = Some(NiEarnings(BigDecimal("38500.25"))),
          coClassOnePaid = Some(CoClassOnePaid(BigDecimal("987.65"))),
          totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("41250.80"))),
          niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(42)),
          niEarningsVoluntary = Some(NiEarningsVoluntary(15)),
          underInvestigationFlag = Some(UnderInvestigationFlag(false)),
          primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("39875.90"))),
          otherCredits = Some(
            List(
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class1Credit),
                creditSourceType = Some(CreditSourceType.UniversalCredit),
                contributionCreditCount = Some(ContributionCreditCount(26))
              ),
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class2VoluntaryDevelopmentWorkerRateA),
                creditSourceType = Some(CreditSourceType.StatutoryMaternityPayCredit),
                contributionCreditCount = Some(ContributionCreditCount(12))
              ),
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class3RateC),
                creditSourceType = Some(CreditSourceType.ModSpouseCivilPartnersCredits),
                contributionCreditCount = Some(ContributionCreditCount(39))
              ),
              OtherCredits(
                contributionCreditType = Some(ContributionCreditType.Class1EmployeeOnly),
                creditSourceType = Some(CreditSourceType.SharedParentalPay),
                contributionCreditCount = Some(ContributionCreditCount(-3))
              )
            )
          )
        )
      )
    )
  )

  val hipFailureResponse400 = HipFailureResponse400(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val standardErrorResponse400 = StandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = ErrorResponse400(
      failures = List(
        ErrorResourceObj400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
        ErrorResourceObj400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
      )
    )
  )

  val individualStatePensionInformationErrorResponse403 =
    IndividualStatePensionInformationErrorResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

  val individualStatePensionInformationErrorResponse503 = IndividualStatePensionInformationErrorResponse503(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val underTest = new IndividualStatePensionInformationResponseMapper

  "IndividualStatePensionInformationResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {
        underTest.toApiResult(individualStatePensionInformationSuccessResponse) shouldBe
          DownstreamSuccessResponse(ApiName.IndividualStatePension, individualStatePensionInformationSuccessResponse)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (HipFailureResponse400)" in {
        underTest.toApiResult(hipFailureResponse400) shouldBe
          DownstreamErrorReport(ApiName.IndividualStatePension, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (StandardErrorResponse400)" in {

        underTest.toApiResult(standardErrorResponse400) shouldBe
          DownstreamErrorReport(ApiName.IndividualStatePension, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 403" in {
        underTest.toApiResult(individualStatePensionInformationErrorResponse403) shouldBe
          DownstreamErrorReport(ApiName.IndividualStatePension, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a DownstreamErrorReport result when when handling a 503" in {
        underTest.toApiResult(individualStatePensionInformationErrorResponse503) shouldBe
          DownstreamErrorReport(ApiName.IndividualStatePension, NpsNormalizedError.ServiceUnavailable)
      }
    }
  }

}
