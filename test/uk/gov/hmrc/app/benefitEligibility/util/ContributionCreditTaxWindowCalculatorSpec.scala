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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import ContributionCreditTaxWindowCalculator.*
import uk.gov.hmrc.app.benefitEligibility.common.{EndTaxYear, StartTaxYear}

class ContributionCreditTaxWindowCalculatorSpec extends AnyFreeSpec with Matchers {

  "ContributionCreditTaxWindowCalculator" - {

    ".createWindows" - {

      "should return empty list when start year equals end year" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2020)
        )
        result shouldBe List.empty
      }

      "should return empty list when start year is greater than end year" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2025),
          EndTaxYear(2020)
        )
        result shouldBe List.empty
      }

      "should create single window when range is less than 6 years" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2024)
        )
        result shouldBe List(TaxWindow(StartTaxYear(2020), EndTaxYear(2024)))
      }

      "should create single window when range is exactly 6 years" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2026)
        )
        result shouldBe List(TaxWindow(StartTaxYear(2020), EndTaxYear(2026)))
      }

      "should create multiple windows when range is greater than 6 years" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2030)
        )
        result shouldBe List(
          TaxWindow(StartTaxYear(2020), EndTaxYear(2026)),
          TaxWindow(StartTaxYear(2026), EndTaxYear(2030))
        )
      }

      "should create multiple windows for exactly 12 years" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2032)
        )
        result shouldBe List(
          TaxWindow(StartTaxYear(2020), EndTaxYear(2026)),
          TaxWindow(StartTaxYear(2026), EndTaxYear(2032))
        )
      }

      "should create three windows for 13 years range" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2033)
        )
        result shouldBe List(
          TaxWindow(StartTaxYear(2020), EndTaxYear(2026)),
          TaxWindow(StartTaxYear(2026), EndTaxYear(2032)),
          TaxWindow(StartTaxYear(2032), EndTaxYear(2033))
        )
      }

      "should create three windows for exactly 18 years" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2020),
          EndTaxYear(2038)
        )
        result shouldBe List(
          TaxWindow(StartTaxYear(2020), EndTaxYear(2026)),
          TaxWindow(StartTaxYear(2026), EndTaxYear(2032)),
          TaxWindow(StartTaxYear(2032), EndTaxYear(2038))
        )
      }

      "should handle large ranges correctly" in {
        val result = ContributionCreditTaxWindowCalculator.createWindows(
          StartTaxYear(2000),
          EndTaxYear(2025)
        )
        result shouldBe List(
          TaxWindow(StartTaxYear(2000), EndTaxYear(2006)),
          TaxWindow(StartTaxYear(2006), EndTaxYear(2012)),
          TaxWindow(StartTaxYear(2012), EndTaxYear(2018)),
          TaxWindow(StartTaxYear(2018), EndTaxYear(2024)),
          TaxWindow(StartTaxYear(2024), EndTaxYear(2025))
        )
      }
    }
  }

}
