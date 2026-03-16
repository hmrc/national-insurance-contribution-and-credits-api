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

package uk.gov.hmrc.app.benefitEligibility.repository

import cats.data.NonEmptyList
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  DateOfBirth,
  EndTaxYear,
  NpsNormalizedError,
  StartTaxYear,
  TaxWindow,
  TaxYear
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.{
  Class1ContributionAndCredits,
  Class2ContributionAndCredits,
  Class2NIContributionAmount,
  Class2Or3EarningsFactor,
  ContributionCategoryLetter,
  EmployerName,
  NiContributionsAndCreditsSuccessResponse,
  NumberOfCreditsAndContributions,
  PrimaryContribution,
  PrimaryPaidEarnings,
  TotalGraduatedPensionUnits
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  CreditSource,
  LatePaymentPeriod,
  NiContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.NpsStandardErrorResponse400
import uk.gov.hmrc.app.benefitEligibility.repository.ContributionAndCreditsPaging.fromContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.service.ContributionCreditPagingResult

import java.time.LocalDate

class ContributionsAndCreditsPagingSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  "ContributionAndCreditsPaging" - {
    ".apply" - {
      "Should successfully create ContributionAndCreditsPaging" in {
        val taxWindow = NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2030)))

        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))

        val result = ContributionAndCreditsPaging.apply(taxWindow, dob)
        result.dateOfBirth shouldBe dob
        result.niContributionAndCreditsTaxWindows shouldBe taxWindow
        result.apiName shouldBe NiContributionAndCredits
      }
    }
    ".fromContributionAndCredits" - {
      "Should return ContributionAndCreditsPaging if successful ContributionCreditPagingResult has more than one tax window" in {
        val taxWindow = NonEmptyList.of(
          TaxWindow(StartTaxYear(2015), EndTaxYear(2020)),
          TaxWindow(StartTaxYear(2021), EndTaxYear(2030))
        )
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
          Some(
            List(
              Class1ContributionAndCredits(
                taxYear = Some(TaxYear(2022)),
                numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                contributionCategory = Some(ContributionCategory.None),
                contributionCreditType = Some(NiContributionCreditType.C1),
                primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                creditSource = Some(CreditSource.NotKnown),
                employerName = Some(EmployerName("ipOpMs")),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          ),
          Some(
            List(
              Class2ContributionAndCredits(
                taxYear = Some(TaxYear(2022)),
                numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                contributionCreditType = Some(NiContributionCreditType.C1),
                class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
                class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          )
        )
        val paginationSource = ContributionAndCreditsPaging(taxWindow, dob)

        val creditsAndContributionsPagingResult: ContributionCreditPagingResult =
          ContributionCreditPagingResult(
            Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
            Some(paginationSource)
          )

        val result = fromContributionAndCredits(creditsAndContributionsPagingResult)
        result shouldBe Some(
          ContributionAndCreditsPaging(NonEmptyList.of(TaxWindow(StartTaxYear(2021), EndTaxYear(2030))), dob)
        )
      }
      "Should return None if successful ContributionCreditPagingResult has one tax window" in {
        val taxWindow = NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)))
        val dob       = DateOfBirth(LocalDate.parse("2025-10-10"))
        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
          Some(
            List(
              Class1ContributionAndCredits(
                taxYear = Some(TaxYear(2022)),
                numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                contributionCategory = Some(ContributionCategory.None),
                contributionCreditType = Some(NiContributionCreditType.C1),
                primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                creditSource = Some(CreditSource.NotKnown),
                employerName = Some(EmployerName("ipOpMs")),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          ),
          Some(
            List(
              Class2ContributionAndCredits(
                taxYear = Some(TaxYear(2022)),
                numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                contributionCreditType = Some(NiContributionCreditType.C1),
                class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
                class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          )
        )
        val paginationSource = ContributionAndCreditsPaging(taxWindow, dob)

        val creditsAndContributionsPagingResult: ContributionCreditPagingResult =
          ContributionCreditPagingResult(
            Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
            Some(paginationSource)
          )

        val result = fromContributionAndCredits(creditsAndContributionsPagingResult)
        result shouldBe None
      }
      "Should return None if Failure ContributionCreditPagingResult passed in" in {
        val errorResponse =
          """{
            |  "origin": "HIP",
            |  "response": {
            |    "failures": [
            |      {
            |        "reason": "reason_1",
            |        "code": "400.1"
            |      },
            |      {
            |        "reason": "reason_2",
            |        "code": "400.2"
            |      }
            |    ]
            |  }
            |}""".stripMargin
        val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
        val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

        val failureResult = FailureResult(
          NiContributionAndCredits,
          ErrorReport(NpsNormalizedError.BadRequest, Some(response))
        )

        val creditsAndContributionsPagingResult: ContributionCreditPagingResult =
          ContributionCreditPagingResult(Some(failureResult), None)

        val result = fromContributionAndCredits(creditsAndContributionsPagingResult)
        result shouldBe None
      }
    }
  }

}
