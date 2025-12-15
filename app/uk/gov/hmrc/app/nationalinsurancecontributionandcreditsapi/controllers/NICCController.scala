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

package uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.controllers

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.NICCRequestPayload
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.{Failure, Response}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.services.NICCService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class NICCController @Inject() (cc: ControllerComponents, identity: AuthAction, niccService: NICCService)
    extends BackendController(cc) {

  def postContributionsAndCredits: Action[AnyContent] = identity.async { implicit request =>
    val correlationId: String = UUID.randomUUID().toString
    val correlationIdHeader   = "correlationId" -> correlationId

    request.body.asJson match {
      case Some(data) =>
        data.validate[NICCRequestPayload] match {
          case JsSuccess(niccRequestPayload, _) =>
            niccService.statusMapping(
              niccRequestPayload.nationalInsuranceNumber,
              niccRequestPayload.startTaxYear.taxYear,
              niccRequestPayload.endTaxYear.taxYear,
              niccRequestPayload.dateOfBirth.format(ISO_LOCAL_DATE),
              correlationId
            )

          case JsError(_) =>
            Future.successful(
              BadRequest(Json.toJson(new Response(Seq(new Failure("There was a problem with the request", "400")))))
                .withHeaders(correlationIdHeader)
            )
        }
      case None =>
        Future.successful(
          BadRequest(Json.toJson(new Response(Seq(new Failure("Missing JSON data", "400")))))
            .withHeaders(correlationIdHeader)
        )
    }
  }

}
