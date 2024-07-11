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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.TaxIdentifier

case class NICCNino(nino: String) extends TaxIdentifier {
  require(NICCNino.isValid(nino), s"$nino is not a valid nino.")

  override def toString = nino

  def value = nino
}

object NICCNino extends (String => NICCNino) {
  implicit val format: OFormat[NICCNino] = Json.format[NICCNino]

  // New regex pattern
  private val validNinoFormat = "[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\\d{2} ?\\d{2} ?\\d{2} ?[A-D]?"

  private val invalidPrefixes = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")

  private def hasValidPrefix(nino: String) = !invalidPrefixes.exists(nino.startsWith)

  def isValid(nino: String) = nino != null && hasValidPrefix(nino) && nino.matches(validNinoFormat)
}