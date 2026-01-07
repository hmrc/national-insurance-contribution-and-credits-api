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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

case class NormalizedError(code: NormalizedErrorStatusCode, message: String, downstreamStatus: Int)

sealed abstract class NormalizedErrorStatusCode(override val entryName: String) extends EnumEntry {
  val message: String
  val code: Int
}

object NormalizedErrorStatusCode extends Enum[NormalizedErrorStatusCode] with PlayJsonEnum[NormalizedErrorStatusCode] {

  val values: immutable.IndexedSeq[NormalizedErrorStatusCode] = findValues

  case object UnprocessableEntity extends NormalizedErrorStatusCode("UNPROCESSABLE_ENTITY") {
    val message = "downstream could not process data in request"
    val code    = 422
  }

  case object BadRequest extends NormalizedErrorStatusCode("BAD_REQUEST") {
    val message = "downstream received a malformed request"
    val code    = 400
  }

  case object AccessForbidden extends NormalizedErrorStatusCode("ACCESS_FORBIDDEN") {
    val message = "downstream resource cannot be accessed by the calling client"
    val code    = 403
  }

  case object NotFound extends NormalizedErrorStatusCode("NOT_FOUND") {
    val message = "downstream could not not find the specified resource"
    val code    = 404
  }

  case object InternalServerError extends NormalizedErrorStatusCode("INTERNAL_SERVER_ERROR") {
    val message = "downstream failed to fulfil request"
    val code    = 500
  }

  case class UnexpectedStatus(code: Int) extends NormalizedErrorStatusCode("UNEXPECTED_STATUS_CODE") {
    val message = "downstream returned an unexpected status"
  }

}
