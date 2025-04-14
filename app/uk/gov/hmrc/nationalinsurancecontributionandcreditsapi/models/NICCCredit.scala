/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NICCCredit(
    taxYear: Option[Int],
    numberOfWeeks: Option[Int],
    niContributionType: Option[String],
    totalEarningsFactor: Option[BigDecimal],
    totalPrimaryContribution: Option[BigDecimal],
    contributionStatus: Option[String]
) {

  def this(niClass2: NICCClass2) =
    this(
      niClass2.taxYear,
      niClass2.noOfCreditsAndConts,
      niClass2.contributionCreditType,
      niClass2.class2Or3EarningsFactor,
      niClass2.class2NicAmount,
      niClass2.class2Or3CreditStatus
    )

}

object NICCCredit {
  implicit val format: OFormat[NICCCredit] = Json.format[NICCCredit]
}
