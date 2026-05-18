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

import cats.data.EitherT
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Ok}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.app.benefitEligibility.controller.BenefitEligibilityResultHandler.*
import uk.gov.hmrc.app.benefitEligibility.controller.RequestHelper.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.response.BenefitEligibilityInfoResponse
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitEligibilityDataRetrievalService,
  PaginationResult,
  PaginationService
}

import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: uk.gov.hmrc.app.benefitEligibility.controller.action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService,
    paginationService: PaginationService,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def fetchBenefitEligibilityData(): Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        val result = for {
          headerValues <- EitherT.fromEither[Future](validateHeaders(request, appConfig))
          (correlationId, originatorId) = headerValues
          eligibilityRequest <- EitherT.fromEither[Future](parseAndValidateRequest(request))
          _ <- EitherT.fromEither[Future](validateOriginatorMatch(eligibilityRequest, originatorId, appConfig))
          eligibilityCheckDataResult <- benefitEligibilityDataRetrievalService.getEligibilityData(
            eligibilityRequest,
            correlationId
          )
        } yield buildResponse(eligibilityRequest, eligibilityCheckDataResult)

        result.value.map(handleFinalResult(_, request))
      }
    } else identity.async(_ => Future.successful(NotFound))

  def getNextPage(): Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        val result = for {
          headerValues <- EitherT.fromEither[Future](validateHeaders(request, appConfig))
          (correlationId, originatorId) = headerValues
          nextCursor <- EitherT.fromEither[Future](parsePaginationCursor(request))
          _ <- EitherT.fromEither[Future](validatePaginationOriginatorMatch(nextCursor, originatorId, appConfig))
          paginationResult <- paginationService.paginate(nextCursor)
        } yield buildResponse(paginationResult)

        result.value.map(handleFinalResult(_, request))
      }
    } else identity.async(_ => Future.successful(NotFound))

  private def buildResponse(
      eligibilityRequest: EligibilityCheckDataRequest,
      result: EligibilityCheckDataResult
  ): Result =
    BenefitEligibilityInfoResponse
      .from(
        eligibilityRequest.nationalInsuranceNumber,
        result
      ) match {
      case Left(errorResponse)    => InternalServerError(Json.toJson(errorResponse))
      case Right(successResponse) => Ok(Json.toJson(successResponse))
    }

  private def buildResponse(
      paginationResult: PaginationResult
  ): Result =
    BenefitEligibilityInfoResponse
      .from(paginationResult) match {
      case Left(errorResponse)    => InternalServerError(Json.toJson(errorResponse))
      case Right(successResponse) => Ok(Json.toJson(successResponse))
    }

}
