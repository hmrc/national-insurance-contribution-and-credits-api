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

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.AppConfig
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequestPayload
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors.{Failure, Response}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services.NICCService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class NICCController @Inject()(cc: ControllerComponents,
                               identity: AuthAction,
                               niccService: NICCService,
                               config: AppConfig)
  extends BackendController(cc) {

  private val logger: Logger = Logger(this.getClass)

  def postContributionsAndCredits: Action[AnyContent] = identity.async { implicit request =>

    logger.info("Setting up request!")
    val correlationId: String = UUID.randomUUID().toString
    val correlationIdHeader = "correlationId" -> correlationId

    request.body.asJson match {
      case Some(json) =>
        json.validate[NICCRequestPayload].fold(
          _ => Future.successful(BadRequest(Json.toJson(new Response(Seq(new Failure("There was a problem with the request", "400"))))).withHeaders(correlationIdHeader)),
          requestObject => niccService.statusMapping(requestObject.nationalInsuranceNumber, requestObject.startTaxYear.taxYear, requestObject.endTaxYear.taxYear, requestObject.dateOfBirth.format(ISO_LOCAL_DATE), correlationId)
        )
      case None => Future.successful(BadRequest(Json.toJson(new Response(Seq(new Failure("Missing JSON data", "400"))))).withHeaders(correlationIdHeader))
    }
  }



}
