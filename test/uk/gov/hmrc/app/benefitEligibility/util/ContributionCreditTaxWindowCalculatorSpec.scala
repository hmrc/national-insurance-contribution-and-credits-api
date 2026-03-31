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

package uk.gov.hmrc.app.benefitEligibility.util

import cats.data.NonEmptyList
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  ContributionCreditTaxWindowCalculatorError,
  EndTaxYear,
  StartTaxYear,
  TaxWindow
}

class ContributionCreditTaxWindowCalculatorSpec extends AnyFreeSpec with Matchers {

  "ContributionCreditTaxWindowCalculator" - {

    ".createWindows" - {

      "should return empty list when start year is greater than end year" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2025),
          EndTaxYear(2020)
        )
        result shouldBe Left(ContributionCreditTaxWindowCalculatorError("Invalid tax year range"))
      }

      "should create single window when start year equals end year" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2020)
        )
        result shouldBe Right(NonEmptyList.one(TaxWindow(StartTaxYear(2020), EndTaxYear(2020))))
      }

      "should create single window when range is less than 6 tax years" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2024)
        )
        result shouldBe Right(NonEmptyList.one(TaxWindow(StartTaxYear(2020), EndTaxYear(2024))))
      }

      "should create single window when range is exactly 6 tax years" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2025)
        )
        result shouldBe Right(
          NonEmptyList.one(
            TaxWindow(StartTaxYear(2020), EndTaxYear(2025))
          )
        )
      }

      "should create multiple windows when range is greater than 6 tax years" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2030)
        )
        result shouldBe Right(
          NonEmptyList.of(
            TaxWindow(StartTaxYear(2020), EndTaxYear(2025)),
            TaxWindow(StartTaxYear(2026), EndTaxYear(2030))
          )
        )
      }

      "should create multiple windows for exactly 12 tax years" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2031)
        )
        result shouldBe Right(
          NonEmptyList.of(
            TaxWindow(StartTaxYear(2020), EndTaxYear(2025)),
            TaxWindow(StartTaxYear(2026), EndTaxYear(2031))
          )
        )
      }

      "should create three windows for 13 tax years range" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2033)
        )
        result shouldBe Right(
          NonEmptyList.of(
            TaxWindow(StartTaxYear(2020), EndTaxYear(2025)),
            TaxWindow(StartTaxYear(2026), EndTaxYear(2031)),
            TaxWindow(StartTaxYear(2032), EndTaxYear(2033))
          )
        )
      }

      "should create three windows for exactly 18 tax years" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2020),
          EndTaxYear(2037)
        )
        result shouldBe Right(
          NonEmptyList.of(
            TaxWindow(StartTaxYear(2020), EndTaxYear(2025)),
            TaxWindow(StartTaxYear(2026), EndTaxYear(2031)),
            TaxWindow(StartTaxYear(2032), EndTaxYear(2037))
          )
        )
      }

      "should handle large ranges correctly" in {
        val result = ContributionCreditTaxWindowCalculator.createTaxWindows(
          StartTaxYear(2000),
          EndTaxYear(2025)
        )
        result shouldBe Right(
          NonEmptyList.of(
            TaxWindow(StartTaxYear(2000), EndTaxYear(2005)),
            TaxWindow(StartTaxYear(2006), EndTaxYear(2011)),
            TaxWindow(StartTaxYear(2012), EndTaxYear(2017)),
            TaxWindow(StartTaxYear(2018), EndTaxYear(2023)),
            TaxWindow(StartTaxYear(2024), EndTaxYear(2025))
          )
        )
      }
    }
  }

}
