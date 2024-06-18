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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors

import play.api.libs.json._

case class Failure(code: String, message: String)

object Failure {
  implicit val format: OFormat[Failure] = Json.format[Failure]
}

//todo: adjust according to responses errors from backend
object ApiServiceFailure$
  extends Failure("INTERNAL_SERVER_ERROR", "An error occurred whilst processing your request.")

object InvalidRequestFailure$
  extends Failure("INVALID_REQUEST", "The request is invalid.")

object UnknownBusinessFailure$
  extends Failure("UNKNOWN_BUSINESS_ERROR", "The remote endpoint has returned an unknown business validation error.")

object NinoNotFoundFailure$
  extends Failure("NOT_FOUND_NINO", "The remote endpoint has indicated that the Nino provided cannot be found.")

object MatchNotFoundFailure$
  extends Failure("NOT_FOUND_MATCH", "The remote endpoint has indicated that there is no match for the person details provided.")

object ServiceUnavailableFailure$
  extends Failure("SERVER_ERROR", "Service unavailable.")

object ThrottledFailure$
  extends Failure("MESSAGE_THROTTLED_OUT", "The application has reached its maximum rate limit.")
