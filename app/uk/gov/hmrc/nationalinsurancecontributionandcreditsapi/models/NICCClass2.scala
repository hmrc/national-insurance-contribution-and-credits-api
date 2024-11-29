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

case class NICCClass2(taxYear: Option[Int],
                      noOfCreditsAndConts: Option[Int],
                      contributionCreditType: Option[String],
                      class2Or3EarningsFactor: Option[BigDecimal],
                      class2NicAmount: Option[BigDecimal],
                      class2Or3CreditStatus: Option[String])

object NICCClass2{
  implicit val format: OFormat[NICCClass2] = Json.format[NICCClass2]
}
