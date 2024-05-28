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

package uk.gov.hmrc.bereavementsupportpaymentapi.controllers

import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.bereavementsupportpaymentapi.connectors.HipConnector
import uk.gov.hmrc.bereavementsupportpaymentapi.models.Request

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.{AdditionalHeaderNames, RequestParams, Validator}

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector, validator: Validator)
    extends BackendController(cc) {



  def getCitizenInfo: Action[AnyContent] = Action.async { implicit request =>
    val queryParams: Map[String, Seq[String]] = request.queryString

    val flatQueryParams: Map[String, String] = queryParams.collect {
      case (key, values) if values.nonEmpty => key -> values.mkString(",")
    }

    val validatedFlatQueryParams = flatQueryParams.flatMap{
        case (RequestParams.NINO, value) =>
          validator.ninoValidator(value).map( value => RequestParams.NINO -> value)
        case (RequestParams.FORENAME, value) =>
          validator.textValidator(value).map ( value => RequestParams.FORENAME -> value)
        case (RequestParams.SURNAME, value) =>
          validator.textValidator(value).map ( value => RequestParams.SURNAME -> value)
        case (RequestParams.DATE_OF_BIRTH, value) =>
          validator.dobValidator(value).map ( value => RequestParams.DATE_OF_BIRTH -> value.toString )
        case (RequestParams.DATE_RANGE, value) =>
          validator.textValidator(value).map ( value => RequestParams.DATE_RANGE -> value )
        case _ => None //todo: throw exception with status code for any other parameter sent not expected
    }

    //Adding correlationId to Map and converting to Request model to process
    val queryParamsAsMap = validatedFlatQueryParams.map {
        case (key: String, value: String) => {
          key -> value
        }
      }.toMap
    val correlationId = request.headers.get(AdditionalHeaderNames.CORRELATION_ID).getOrElse("")
    val queryParamsWithCorId: Map[String, String] = (queryParamsAsMap + ( AdditionalHeaderNames.CORRELATION_ID -> correlationId)).toMap
    val processedRequest = Request.fromMap(queryParamsWithCorId)


    processedRequest match {
      case Some(processedRequest) => Future.successful(Ok(connector.getCitizenInfo(processedRequest)))
      case None => Future.successful(InternalServerError("There's been a problem with connecting to the backend server."))
    }
  }
}
