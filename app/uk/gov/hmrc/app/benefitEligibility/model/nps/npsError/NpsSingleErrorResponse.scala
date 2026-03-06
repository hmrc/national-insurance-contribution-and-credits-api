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

package uk.gov.hmrc.app.benefitEligibility.model.nps.npsError

import play.api.libs.json.{Format, Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsErrorReason

final case class NpsErrorCode(value: String) extends AnyVal

object NpsErrorCode {
  implicit val reads: Format[NpsErrorCode] = Json.valueFormat[NpsErrorCode]
}

case class NpsSingleErrorResponse(reason: NpsErrorReason, code: NpsErrorCode) extends NpsError

object NpsSingleErrorResponse {
  implicit val npsErrorResponseV1Reads: Reads[NpsSingleErrorResponse] = Json.reads[NpsSingleErrorResponse]
}
