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

package uk.gov.hmrc.app.benefitEligibility.controller

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.service.BenefitEligibilityDataRetrievalService
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.{Failure, Response}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService
)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier)
    extends BackendController(cc) {

  def benefitEligibilityData: Action[AnyContent] = identity.async { implicit request =>
    val correlationId: String = UUID.randomUUID().toString
    val correlationIdHeader   = "correlationId" -> correlationId

    request.body.asJson match {
      case Some(data) =>
        data.validate[EligibilityCheckDataRequest] match {
          case JsSuccess(request, _) =>
            benefitEligibilityDataRetrievalService
              .getEligibilityData(request)(headerCarrier.withExtraHeaders("correlationId" -> correlationId), ec)
              .leftMap { result =>
                BadRequest(Json.toJson(new Response(Seq(new Failure("There was a problem with the request", "400")))))
                  .withHeaders("correlationId" -> correlationId)
              }
              .map(result => Ok("success"))
              .value
              .map {
                case Left(value)  => value
                case Right(value) => value
              }

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
