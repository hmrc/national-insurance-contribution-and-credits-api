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

import com.google.inject.Inject
import play.api.libs.json._

case class Failure(reason: String, code: String) {
  def this(hipFailure: HIPFailure) = {
    this(hipFailure.`type`,
      hipFailure.reason)
  }
}

@Inject
object Failure {
  implicit val format: OFormat[Failure] = Json.format[Failure]


  final val ApiServiceFailure =
    new Failure("INTERNAL_SERVER_ERROR", "An error occurred whilst processing your request.")

  final val InvalidRequestFailure =
    new Failure("INVALID_REQUEST", "The request is invalid.")

  final val UnknownBusinessFailure =
    new Failure("UNKNOWN_BUSINESS_ERROR", "The remote endpoint has returned an unknown business validation error.")

  final val NinoNotFoundFailure =
    new Failure("NOT_FOUND_NINO", "The remote endpoint has indicated that the Nino provided cannot be found.")

  final val MatchNotFoundFailure =
    new Failure("NOT_FOUND_MATCH", "The remote endpoint has indicated that there is no match for the person details provided.")

  final val ServiceUnavailableFailure =
    new Failure("SERVER_ERROR", "Service unavailable.")

  final val ThrottledFailure =
    new Failure("MESSAGE_THROTTLED_OUT", "The application has reached its maximum rate limit.")
}

