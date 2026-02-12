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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.error

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class ErrorCode(override val entryName: String) extends EnumEntry

object ErrorCode extends Enum[ErrorCode] with PlayJsonEnum[ErrorCode] {
  val values: immutable.IndexedSeq[ErrorCode] = findValues

  case object BadRequest          extends ErrorCode("BAD_REQUEST")
  case object Forbidden           extends ErrorCode("FORBIDDEN")
  case object UnprocessableEntity extends ErrorCode("UNPROCESSABLE_ENTITY")
  case object InternalServerError extends ErrorCode("INTERNAL_SERVER_ERROR")
}
