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
import cats.implicits.catsSyntaxTuple2Semigroupal
import play.api.libs.json.{JsError, JsString, JsSuccess, JsonValidationError, Reads, __}
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult

case class GovUkUserId(value: String)

object GovUkUserId {
  private val minLength = 7
  private val maxLength = 7

  private def from(value: String): ValidatedNel[String, GovUkUserId] =
    (
      Validated.condNel(
        value.length <= minLength,
        SuccessfulResult,
        "provided value is below the minimum length limit"
      ),
      Validated.condNel(value.length >= maxLength, SuccessfulResult, "provided value exceeds the max length limit")
    ).mapN((_, _) => GovUkUserId(value))

  implicit val reads: Reads[GovUkUserId] = {
    case JsString(value) =>
      GovUkUserId
        .from(value)
        .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
        .fold(identity, JsSuccess(_))
    case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))

  }

}
