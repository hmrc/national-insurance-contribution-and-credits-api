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

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.Validator

@Singleton()
class ServiceController @Inject()(cc: ControllerComponents, connector: HipConnector, validators: Validator)
    extends BackendController(cc) {



  def getCitizenInfo: Action[AnyContent] = Action.async { implicit request =>
    // TODO add validation
    val queryParams: Map[String, Seq[String]] = request.queryString

    val flatQueryParams: Map[String, String] = queryParams.map {
      case (key, values) => key -> values.mkString(",")
    }

    val validatedNino = validators.ninoValidator(nino)
    }

    }

    println(flatQueryParams)
    // TODO call to backend

    Future.successful(Ok(connector.getCitizenInfo()))
  }
}
