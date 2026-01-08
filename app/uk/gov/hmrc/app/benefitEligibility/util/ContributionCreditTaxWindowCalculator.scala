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

import uk.gov.hmrc.app.benefitEligibility.common.{EndTaxYear, StartTaxYear}

import scala.annotation.tailrec

object ContributionCreditTaxWindowCalculator {

  case class TaxWindow(startTaxYear: StartTaxYear, endTaxYear: EndTaxYear)

  def createWindows(startTaxYear: StartTaxYear, endTaxYear: EndTaxYear): List[TaxWindow] = {
    @tailrec
    def loop(start: StartTaxYear, windows: List[TaxWindow]): List[TaxWindow] = {

      val maybeNewStart = StartTaxYear(start.value + 6)

      if (start.value >= endTaxYear.value) windows
      else if (endTaxYear.value - start.value <= 6) {
        (TaxWindow(start, endTaxYear) +: windows).reverse
      } else
        loop(maybeNewStart, TaxWindow(StartTaxYear(start.value), EndTaxYear(maybeNewStart.value)) +: windows)
    }

    loop(start = startTaxYear, windows = List.empty)
  }

}
