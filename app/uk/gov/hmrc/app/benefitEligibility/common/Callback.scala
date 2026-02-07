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

package uk.gov.hmrc.app.benefitEligibility.common

import play.api.libs.json.{Format, Json}

case class CallbackUrl(value: String) extends AnyVal

object CallbackUrl {
  implicit val callbackUrlFormat: Format[CallbackUrl] = Json.valueFormat[CallbackUrl]
}

case class Callback(
    callbackURL: Option[CallbackUrl]
)

object Callback {
  implicit val callbackFormat: Format[Callback] = Json.format[Callback]
}
