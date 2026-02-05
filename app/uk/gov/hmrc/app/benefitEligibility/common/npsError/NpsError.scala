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
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorReason
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponse

sealed trait NpsError extends NpsApiResponse

sealed trait NpsErrorResponse400 extends NpsError

case class NpsSingleErrorResponse(reason: NpsErrorReason, code: NpsErrorCode) extends NpsError

object NpsSingleErrorResponse {
  implicit val npsErrorResponseV1Reads: Reads[NpsSingleErrorResponse] = Json.reads[NpsSingleErrorResponse]
}

case class NpsMultiErrorResponse(failures: Option[List[NpsSingleErrorResponse]]) extends NpsError

object NpsMultiErrorResponse {
  implicit val npsErrorResponseV2Reads: Reads[NpsMultiErrorResponse] = Json.reads[NpsMultiErrorResponse]
}

case class NpsErrorResponseHipOrigin(
    origin: HipOrigin,
    response: HipFailureResponse
) extends NpsErrorResponse400

object NpsErrorResponseHipOrigin {

  implicit val npsErrorResponseHipOriginReads: Reads[NpsErrorResponseHipOrigin] =
    Json.reads[NpsErrorResponseHipOrigin]

}

case class NpsStandardErrorResponse400(origin: HipOrigin, response: NpsMultiErrorResponse) extends NpsErrorResponse400

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

case class NpsErrorResponse422Special(
    failures: Option[List[NpsSingleErrorResponse]],
    askUser: Option[Boolean],
    fixRequired: Option[Boolean],
    workItemRaised: Option[Boolean]
) extends NpsError

object NpsErrorResponse422Special {

  implicit val npsErrorResponse422SpecialReads: Reads[NpsErrorResponse422Special] =
    Json.reads[NpsErrorResponse422Special]

}
