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

package uk.gov.hmrc.app.benefitEligibility.common.npsError

import play.api.libs.json.{Format, JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.common.Reason
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponse

sealed trait NpsError extends NpsApiResponse

sealed trait NpsErrorResponse400 extends NpsError

case class NpsErrorResponseHipOrigin(
    origin: HipOrigin,
    response: HipFailureResponse
) extends NpsErrorResponse400

object NpsErrorResponseHipOrigin {

  implicit val npsErrorResponseHipOriginReads: Reads[NpsErrorResponseHipOrigin] =
    Json.reads[NpsErrorResponseHipOrigin]

}

case class ErrorResourceObj400(
    reason: Reason,
    code: NpsErrorCode400
)

object ErrorResourceObj400 {
  implicit val errorResourceObj400Reads: Reads[ErrorResourceObj400] = Json.reads[ErrorResourceObj400]
}

case class ErrorResponse400(failures: List[ErrorResourceObj400])

object ErrorResponse400 {
  implicit val errorResponse400Reads: Reads[ErrorResponse400] = Json.reads[ErrorResponse400]
}

case class NpsStandardErrorResponse400(origin: HipOrigin, response: ErrorResponse400) extends NpsErrorResponse400

object NpsStandardErrorResponse400 {

  implicit val standardErrorResponse400Reads: Reads[NpsStandardErrorResponse400] =
    Json.reads[NpsStandardErrorResponse400]

}

object NpsErrorResponse400 {

  implicit val npsErrorResponse400Reads: Reads[NpsErrorResponse400] =
    Reads[NpsErrorResponse400] { resp =>
      NpsStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
        case JsSuccess(value, path) => JsSuccess(value, path)
        case JsError(errors) =>
          NpsErrorResponseHipOrigin.npsErrorResponseHipOriginReads.reads(resp) match {
            case JsSuccess(value, path) => JsSuccess(value, path)
            case JsError(errors)        => JsError(errors)
          }
      }
    }

}

case class NpsErrorResponse403(
    code: NpsErrorCode403,
    reason: NpsErrorReason403
) extends NpsError

object NpsErrorResponse403 {

  implicit val npsErrorResponse4033Reads: Format[NpsErrorResponse403] =
    Json.format[NpsErrorResponse403]

}

case class NpsErrorResponse404(code: NpsErrorCode, reason: Reason) extends NpsError

object NpsErrorResponse404 {
  implicit val npsErrorResponse404Reads: Reads[NpsErrorResponse404] = Json.reads[NpsErrorResponse404]
}

case class NpsError422(code: NpsErrorCode, reason: Reason)

object NpsError422 {

  implicit val NpsError422Reads: Reads[NpsError422] =
    Json.reads[NpsError422]

}

case class NpsErrorResponse422(failures: List[NpsError422]) extends NpsError

object NpsErrorResponse422 {

  implicit val NpsErrorResponse422Reads: Reads[NpsErrorResponse422] =
    Json.reads[NpsErrorResponse422]

}

case class NpsErrorResponse500(
    origin: HipOrigin,
    response: HipFailureResponse
) extends NpsError

object NpsErrorResponse500 {
  implicit val npsErrorResponse500Reads: Reads[NpsErrorResponse500] = Json.reads[NpsErrorResponse500]
}

case class NpsErrorResponse503(
    origin: HipOrigin,
    response: HipFailureResponse
) extends NpsError

object NpsErrorResponse503 {
  implicit val npsErrorResponse500Reads: Reads[NpsErrorResponse500] = Json.reads[NpsErrorResponse500]
}
