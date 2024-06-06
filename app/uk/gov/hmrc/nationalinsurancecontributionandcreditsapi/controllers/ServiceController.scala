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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.Request
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector)
    extends BackendController(cc) {

  def getCitizenInfo(nationalInsuranceNumber: String, startTaxYear: Int, endTaxYear: Int): Action[AnyContent] =
    Action.async { implicit request =>

      val jsonBody: JsValue = request.body.asJson.getOrElse(Json.obj())
      val dateOfBirth: Option[String] = ((jsonBody \ "dateOfBirth").asOpt[String])

      val newRequest = new Request(nationalInsuranceNumber, startTaxYear, endTaxYear, dateOfBirth match {
        case Some(dateOfBirth) => dateOfBirth
        case _ => ""//throw an error
      })
     val response = connector.getCitizenInfo(newRequest)


    Future.successful(Ok(s"Received = \nBody as String $newRequest\nResponse: $response"))
  }
}
