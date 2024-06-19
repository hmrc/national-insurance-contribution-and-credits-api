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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers

import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{NICCRequest, NICCRequestPayload, NICCResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import play.api.Logger
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors.{Failures, Failure}

@Singleton()
class NICCController @Inject()(cc: ControllerComponents,
                               connector: HipConnector)
                              (implicit ec: ExecutionContext)
  extends BackendController(cc) {

  val logger: Logger = Logger(this.getClass)

  def postContributionsAndCredits(nationalInsuranceNumber: Nino,
                                  startTaxYear: String,
                                  endTaxYear: String): Action[NICCRequestPayload] = Action(parse.json[NICCRequestPayload]).async { implicit request =>

    val body = request.body
    logger.info("Setting up request!")
    val newRequest = new NICCRequest(nationalInsuranceNumber, startTaxYear, endTaxYear, body.dateOfBirth.toString)


    connector.fetchData(newRequest).map { response => {
      response.status match {
        case OK => response.json.validate[NICCResponse] match {
          case JsSuccess(data, _) => Ok(Json.toJson(data))
          case JsError(errors) => InternalServerError
        }
        case BAD_REQUEST => response.json.validate[Failures] match {
          case JsSuccess(data, _) => BadRequest(Json.toJson(data))
          case JsError(errors) => InternalServerError
        }
        case UNPROCESSABLE_ENTITY => response.json.validate[Failures] match {
          case JsSuccess(data, _) => UnprocessableEntity(Json.toJson(data))
          case JsError(errors) => InternalServerError
        }
        case _ => InternalServerError
      }
    }
    }
  }
}
