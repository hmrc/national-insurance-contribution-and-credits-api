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

import play.api.libs.functional.syntax._
import com.google.inject.Inject
import play.api.libs.json._

case class NIResponse(niContribution: NIContribution,
                      niCredit: NICredit)

@Inject
object NIResponse {
  implicit val reads: Reads[NIResponse] = (
    (__ \ "niContribution").read[NIContribution] and
      (__ \ "niCredit").read[NICredit]
  ) (NIResponse.apply _)

  implicit val writes: Writes[NIResponse] = new Writes[NIResponse] {
    override def writes(data: NIResponse): JsValue = {
      Json.obj(
        "niContribution" -> Json.toJson(data.niContribution),
        "niCredit" -> Json.toJson(data.niCredit)
      )
    }
  }
}
