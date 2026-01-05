/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.common

import cats.data.{Validated, ValidatedNel}
import play.api.libs.json._

/** Represents either a national insurance number (NINO) or Temporary Reference Number (TRN). */
case class Identifier(value: String)

object Identifier {

  private val pattern =
    """^(((?:[ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|[KT][A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y])[0-9]{6}[A-D]?)|([0-9]{2}[A-Z]{1}[0-9]{5}))$""".r

  private def from(value: String): ValidatedNel[String, Identifier] =
    Validated.condNel(pattern.matches(value), Identifier(value), "invalid identifier")

  implicit val reads: Reads[Identifier] = {
    case JsString(value) =>
      Identifier
        .from(value)
        .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
        .fold(identity, JsSuccess(_))
    case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
  }

  implicit val identifierWrites: Writes[Identifier] = Json.writes[Identifier]

}
