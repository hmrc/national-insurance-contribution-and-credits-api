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
import uk.gov.hmrc.app.benefitEligibility.common.CorrelationId
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response.{
  BenefitEligibilityInfoErrorResponse,
  BenefitEligibilityInfoResponse
}
import uk.gov.hmrc.app.benefitEligibility.service.BenefitEligibilityDataRetrievalService
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.{Failure, Response}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def fetchBenefitEligibilityData: Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        val correlationId: CorrelationId = CorrelationId.generate
        val correlationIdHeader          = "correlationId" -> correlationId.value.toString

        request.body.asJson match {
          case Some(data) =>
            data.validate[EligibilityCheckDataRequest] match {
              case JsSuccess(request, _) =>
                benefitEligibilityDataRetrievalService
                  .getEligibilityData(request)
                  .map(BenefitEligibilityInfoResponse.from(request.nationalInsuranceNumber, _))
                  .value
                  .map {
                    case Left(serviceError) =>
                      InternalServerError(
                        Json.toJson(new Response(Seq(new Failure("There was a problem processing the request", "500"))))
                      ).withHeaders("correlationId" -> correlationId.value.toString)

                    case Right(benefitEligibilityInfoResponse) =>
                      benefitEligibilityInfoResponse match {
                        case Left(errorResponse)    => BadGateway(Json.toJson(errorResponse))
                        case Right(successResponse) => Ok(Json.toJson(successResponse))
                      }
                  }

              case JsError(err) =>
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
    } else identity.async(_ => Future.successful(NotFound))

}
