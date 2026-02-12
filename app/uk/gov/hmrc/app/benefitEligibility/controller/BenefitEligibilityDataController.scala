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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.app.benefitEligibility.common.CorrelationId
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.error.{Error500, ErrorCode, ErrorReason}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response.{
  BenefitEligibilityInfoErrorResponse,
  BenefitEligibilityInfoResponse
}
import uk.gov.hmrc.app.benefitEligibility.service.BenefitEligibilityDataRetrievalService
import uk.gov.hmrc.app.benefitEligibility.util.{BenefitEligibilityRequestErrorHandler, RequestAwareLogger}
import uk.gov.hmrc.app.config.AppConfig
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

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchBenefitEligibilityData: Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        val correlationId: CorrelationId = CorrelationId.generate
        val correlationIdHeader          = "correlationId" -> correlationId.value.toString

        BenefitEligibilityRequestErrorHandler.validate(request.body.asJson).value match {
          case eligibilityRequest: EligibilityCheckDataRequest =>
            benefitEligibilityDataRetrievalService
              .getEligibilityData(eligibilityRequest)
              .map(BenefitEligibilityInfoResponse.from(eligibilityRequest.nationalInsuranceNumber, _))
              .value
              .map {
                case Left(serviceError) =>
                  logger.warn(s"Benefit Eligibility Data Retrieval Service returned a 500: $serviceError")
                  InternalServerError(
                    Json.toJson(
                      new Error500(
                        ErrorCode.InternalServerError,
                        ErrorReason("an unexpected error occurred while processing the request")
                      )
                    )
                  ).withHeaders("correlationId" -> correlationId.value.toString)

                case Right(benefitEligibilityInfoResponse) =>
                  benefitEligibilityInfoResponse match {
                    case Left(errorResponse) =>
                      logger.warn(s"Benefit Eligibility Data Retrieval Service returned a 502: $errorResponse")
                      BadGateway(Json.toJson(errorResponse))
                    case Right(successResponse) => Ok(Json.toJson(successResponse))
                  }
              }

          case errors: Result =>
            Future.successful(errors.withHeaders("correlationId" -> correlationId.value.toString))
        }
      }
    } else identity.async(_ => Future.successful(NotFound))

}
