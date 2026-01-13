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

  private final case class NormalizedTaxYear(start: Int, end: Int)

  final case class TaxWindow(startTaxYear: StartTaxYear, endTaxYear: EndTaxYear)

  def createTaxWindows(startTaxYear: StartTaxYear, endTaxYear: EndTaxYear): List[TaxWindow] = {

    val normalizedTaxYears = (startTaxYear.value to endTaxYear.value).map(year => NormalizedTaxYear(year, year + 1))

    normalizedTaxYears.zipWithIndex
      .foldLeft(List(List.empty[NormalizedTaxYear])) { case (windows, (year, index)) =>
        if (index > 0 && index % 6 == 0) {
          List(year) :: windows
        } else {
          (year :: windows.head) :: windows.tail
        }
      }
      .map(_.reverse)
      .reverse
      .collect {
        case group if group.nonEmpty =>
          TaxWindow(StartTaxYear(group.map(_.start).min), EndTaxYear(group.map(_.start).max))
      }

  }

}
