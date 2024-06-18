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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.httpParsers

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{HIPOutcome, NICCResponse}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors.{ApiServiceFailure$, Failure, Errors, ThrottledFailure$}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames.CORRELATION_ID

object ApiHttpParser extends HttpParser with Logging {

  implicit val apiHttpReads: HttpReads[HIPOutcome] =
    (method: String, url: String, response: HttpResponse) => {

      if (response.status != OK) {
        val correlationId = response.header(CORRELATION_ID).getOrElse("NOT FOUND")

        logger.warn("[ApiHttpParser][read] - Error response received from HIP\n" +
          s"URL: $url\n" +
          s"Status code: ${response.status}\n" +
          s"Correlation ID: $correlationId\n" +
          s"Body: ${response.body}"
        )
      }

      response.status match {

        case OK => response.validateJson[NICCResponse] match {
          case Some(result) => Right(result)
          case None => Left(Errors(ApiServiceFailure$))
        }
        case BAD_REQUEST => Left(parseErrors(response))
        case TOO_MANY_REQUESTS => Left(Errors(ThrottledFailure$))
        case SERVICE_UNAVAILABLE => Left(parseServiceUnavailableError(response))
        case BAD_GATEWAY => Left(Errors(Seq(Failure(BAD_GATEWAY.toString, response.body))))
        case _ => Left(parseErrors(response))
      }
    }
}
