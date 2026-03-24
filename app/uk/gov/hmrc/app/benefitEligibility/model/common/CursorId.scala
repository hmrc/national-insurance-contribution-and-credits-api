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

package uk.gov.hmrc.app.benefitEligibility.model.common

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor

import java.util.Base64

case class CursorId(value: String) extends AnyVal

object CursorId {
  implicit val format: Format[CursorId] = Json.valueFormat[CursorId]

  def from(paginationCursor: PaginationCursor): CursorId = {
    val byteArray = Base64.getEncoder.encode(Json.toJson(paginationCursor).toString.replaceAll("\\s", "").getBytes)
    CursorId(new String(byteArray))
  }

}
