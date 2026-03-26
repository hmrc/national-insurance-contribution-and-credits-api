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
  Class2NIContributionAmount,
  Class2Or3EarningsFactor,
  Class2or3ContributionAndCredits,
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
      "should successfully create ContributionAndCreditsPaging" in {
        val taxWindow = NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2030)))

        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))

        val result = ContributionAndCreditsPaging.apply(taxWindow, dob)
        result.dateOfBirth shouldBe dob
        result.niContributionAndCreditsTaxWindows shouldBe taxWindow
        result.apiName shouldBe NiContributionAndCredits
      }
    }
    ".tail" - {
      "should return an updated ContributionAndCreditsPaging with the tail of the windows on the initial ContributionAndCreditsPaging object" in {
        val taxWindow = NonEmptyList.of(
          TaxWindow(StartTaxYear(2015), EndTaxYear(2020)),
          TaxWindow(StartTaxYear(2021), EndTaxYear(2030))
        )
        val dob              = DateOfBirth(LocalDate.parse("2025-10-10"))
        val paginationSource = ContributionAndCreditsPaging(taxWindow, dob)

        val result = paginationSource.tail

        result shouldBe
          Some(ContributionAndCreditsPaging(NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2030))), dob))
      }
      "should return None if ContributionAndCreditsPaging has only one tax window" in {
        val taxWindow        = NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)))
        val dob              = DateOfBirth(LocalDate.parse("2025-10-10"))
        val paginationSource = ContributionAndCreditsPaging(taxWindow, dob)

        val result = paginationSource.tail

        result shouldBe None
      }
    }
  }

}
