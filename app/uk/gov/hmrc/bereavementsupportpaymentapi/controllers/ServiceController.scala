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
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.{AdditionalHeaderNames, Validator}

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector, validator: Validator)
    extends BackendController(cc) {



  def getCitizenInfo: Action[AnyContent] = Action.async { implicit request =>
    val queryParams: Map[String, Seq[String]] = request.queryString

    val flatQueryParams: Map[String, String] = queryParams.collect {
      case (key, values) if values.nonEmpty => key -> values.mkString(",")
    }

    val validatedFlatQueryParams = flatQueryParams.map{
        case ("nino", value) => {
          validator.ninoValidator(value) match {
            case Some(value) => value
            case None => println("issue validating nino") //throw exception, return error
          }
        }
        case ("forename", value) => {
          validator.textValidator(value) match {
            case Some(value) => value
            case None => println("issue validating forename") //throw exception, return error
          }
        }
        case ("surname", value) => {
        validator.textValidator(value) match {
          case Some(value) => value
          case None => println("issue validating lastname") //throw exception, return error
        }
      }
        case ("dateOfBirth", value) => {
          validator.dobValidator(value) match {
            case Some(value) => value
            case None => println("issue validating dateOfBirth") //throw exception, return error
          }
        }
        case ("dateRange", value) => {
          validator.textValidator(value) match {
            case Some(value) => value
            case None => println("issue validating dateRange") //throw exception, return error
          }
        }
        case _ => //todo: throw exception with status code for any other parameter sent not expected
    }

    //Adding correlationId to Map and converting to Request model to process
    val correlationId = request.headers.get(AdditionalHeaderNames.CORRELATION_ID).getOrElse("")
    val queryParamsAsMap = validatedFlatQueryParams.map {
        case (key: String, value: String) => key -> value
      }.toMap
    val queryParamsWithCorId = (queryParamsAsMap + ( AdditionalHeaderNames.CORRELATION_ID -> correlationId)).toMap
    val processedRequest = Request.fromMap(queryParamsWithCorId)

    println(s"flatQueryParams request is $flatQueryParams")
    println(s"validatedFlatQueryParams request is $validatedFlatQueryParams")
    println("queryParamsAsMap request is " + queryParamsAsMap.map {
      case (key, values) => key -> values.mkString(",")
    } )
    println(s"queryParamsWithCorId request is $queryParamsWithCorId")


    println(s"stored request is $processedRequest.toString")

    Future.successful(Ok(connector.getCitizenInfo(processedRequest match { case Some(storedRequest) => storedRequest})))
  }
}
