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

package uk.gov.hmrc.bereavementsupportpaymentapi.models.errors

import play.api.libs.json.{JsValue, Json, Writes}

case class Errors(errors: Seq[Error])

object Errors {
  def apply(error: Error): Errors = Errors(Seq(error))

  implicit val writes: Writes[Errors] = new Writes[Errors] {
    override def writes(data: Errors): JsValue = {
      if (data.errors.size > 1) {
        Json.obj("errors" -> Json.toJson(data.errors))
      } else {
        Json.toJson(data.errors.head)
      }
    }
  }
}
