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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequest
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.{DateOfBirth, TaxYear}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class NICCController @Inject()(cc: ControllerComponents)
                              (ec: ExecutionContext)
  extends BackendController(cc) {

  def getContributionsAndCredits(nationalInsuranceNumber: Nino,
                                 startTaxYear: TaxYear,
                                 endTaxYear: TaxYear): Action[AnyContent] = Action.async { implicit request =>

    val jsonBody: JsValue = request.body.asJson.getOrElse(Json.obj())

    val dateOfBirth: String = (jsonBody("dateOfBirth")).as[String]

    val newRequest = new NICCRequest(nationalInsuranceNumber, startTaxYear, endTaxYear, dateOfBirth)

    /*val result = connector.fetchData(newRequest)

    result.map {
      case dataFetch@Right(_) => dataFetch
      case Left(Errors(errors)) => None
    }*/

    //      connector.fetchData(newRequest) match {
    //        case data: NICCResponse => {
    //          val builder = Json.newBuilder
    //          builder += ("", data)
    //          val result = builder.result()
    //          Future.successful(Ok(result))
    //        }
    //        case _ => Future.successful(BadRequest)
  }


  //    connector.fetchData(newRequest).map(Ok.apply)

}
