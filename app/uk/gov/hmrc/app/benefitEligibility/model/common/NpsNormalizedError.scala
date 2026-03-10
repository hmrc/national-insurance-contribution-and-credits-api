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

package uk.gov.hmrc.app.benefitEligibility.model.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{JsNumber, JsObject, JsString, Writes}

import scala.collection.immutable

sealed abstract class NpsNormalizedError(val code: String, val message: String, val downstreamStatus: Int)
    extends EnumEntry

object NpsNormalizedError extends Enum[NpsNormalizedError] with PlayJsonEnum[NpsNormalizedError] {

  val values: immutable.IndexedSeq[NpsNormalizedError] = findValues

  implicit val npsNormalizedErrorWrites: Writes[NpsNormalizedError] = Writes { error =>
    JsObject(
      Seq(
        "code"             -> JsString(error.code),
        "message"          -> JsString(error.message),
        "downstreamStatus" -> JsNumber(error.downstreamStatus)
      )
    )
  }

  case object UnprocessableEntity
      extends NpsNormalizedError(
        code = "UNPROCESSABLE_ENTITY",
        message = "downstream could not process data in request",
        downstreamStatus = 422
      )

  case object BadRequest extends NpsNormalizedError("BAD_REQUEST", "downstream received a malformed request", 400)

  case object AccessForbidden
      extends NpsNormalizedError(
        code = "ACCESS_FORBIDDEN",
        message = "downstream resource cannot be accessed by the calling client",
        downstreamStatus = 403
      )

  case object NotFound
      extends NpsNormalizedError(
        code = "NOT_FOUND",
        message = "downstream could not not find the specified resource",
        downstreamStatus = 404
      )

  case object ServiceUnavailable
      extends NpsNormalizedError(
        code = "SERVICE_UNAVAILABLE",
        message = "downstream is currently unable to handle request",
        downstreamStatus = 503
      )

  case object InternalServerError
      extends NpsNormalizedError(
        code = "INTERNAL_SERVER_ERROR",
        message = "downstream failed to fulfil request",
        downstreamStatus = 500
      )

  case class UnexpectedStatus(override val downstreamStatus: Int)
      extends NpsNormalizedError(
        code = "UNEXPECTED_STATUS_CODE",
        message = "downstream returned an unexpected status",
        downstreamStatus = downstreamStatus
      )

}
