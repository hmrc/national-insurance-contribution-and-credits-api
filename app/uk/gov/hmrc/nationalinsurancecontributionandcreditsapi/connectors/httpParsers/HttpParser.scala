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

import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors._
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors.Failure.ApiServiceFailure$

import scala.util.{Success, Try}

trait HttpParser extends Logging{

  implicit class KnownJsonResponse(response: HttpResponse) {
    def validateJson[T](implicit reads: Reads[T]): Option[T] = {
      Try(response.json) match {
        case Success(json: JsValue) => parseResult(json)
        case _ =>
          logger.warn("[KnownJsonResponse][validateJson] No JSON was returned")
          None
      }
    }

    private def parseResult[T](json: JsValue)
                              (implicit reads: Reads[T]): Option[T] = json.validate[T] match {
      case JsSuccess(value, _) => Some(value)
      case JsError(error) =>
        logger.warn(s"[KnownJsonResponse][validateJson] Unable to parse JSON: $error")
        None
    }
  }

  private val multipleErrorReads: Reads[Seq[Failure]] = (__ \ "failures").read[Seq[Failure]]

  def parseErrors(response: HttpResponse): Errors = {
    val errors = if ((response.json \ "failures").isDefined) {
      response.validateJson(multipleErrorReads).map(Errors(_))
    } else {
      response.validateJson[Failure].map(Errors(_))
    }

    lazy val unableToParseJsonError = {
      logger.warn(s"unable to parse errors from response: ${response.body}")
      Errors(ApiServiceFailure$)
    }

    errors getOrElse unableToParseJsonError
  }

  def parseServiceUnavailableError(response: HttpResponse): Errors = {
    (response.json \ "incidentReference").asOpt[String] match {
      //todo: adjust
      case Some("LTM000503") => Errors(ThrottledFailure)
      case _ => parseErrors(response)
    }
  }

}