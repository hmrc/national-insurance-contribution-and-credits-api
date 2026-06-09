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
import uk.gov.hmrc.app.benefitEligibility.controller.BenefitEligibilityErrorHandler.*
import uk.gov.hmrc.app.benefitEligibility.controller.RequestHelper.*
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.{
  BSPEligibilityCheckDataRequest,
  ESAEligibilityCheckDataRequest,
  EligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest,
  JSAEligibilityCheckDataRequest,
  MAEligibilityCheckDataRequest,
  SearchlightEligibilityCheckDataRequest
}
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
    identity.async { implicit request =>
      val maybeResult = for {
        headerValues <- EitherT.fromEither[Future](validateHeaders(request))
        correlationId = headerValues
        eligibilityRequest <- EitherT.fromEither[Future](parseAndValidateRequest(request))
        shouldProcess = {
          eligibilityRequest match {
            case req: ESAEligibilityCheckDataRequest         => appConfig.esaEnabled
            case req: JSAEligibilityCheckDataRequest         => appConfig.jsaEnabled
            case req: BSPEligibilityCheckDataRequest         => appConfig.bspEnabled
            case req: MAEligibilityCheckDataRequest          => appConfig.maEnabled
            case req: GYSPEligibilityCheckDataRequest        => appConfig.gyspEnabled
            case req: SearchlightEligibilityCheckDataRequest => appConfig.searchlightEnabled
          }
        }
        response <-
          if (shouldProcess) {
            benefitEligibilityDataRetrievalService
              .getEligibilityData(
                eligibilityRequest,
                correlationId
              )
              .map { eligibilityCheckDataResult =>
                buildResponse(eligibilityRequest, eligibilityCheckDataResult).withHeaders(
                  "CorrelationId" -> correlationId.value.toString
                )
              }

          } else EitherT.rightT[Future, BenefitEligibilityError](NotFound)

      } yield response

      maybeResult.value.map {
        case Right(result) => result
        case Left(error)   => handleError(error, request)
      }
    }

  def getNextPage: Action[AnyContent] =
    if (appConfig.gyspEnabled || appConfig.bspEnabled || appConfig.maEnabled) {
      identity.async { implicit request =>
        val maybeResult = for {
          headerValues <- EitherT.fromEither[Future](validateHeaders(request))
          correlationId = headerValues
          pageTaskId       <- EitherT.fromEither[Future](parsePageTaskId(request))
          paginationResult <- paginationService.paginate(pageTaskId)
        } yield buildResponse(paginationResult).withHeaders("CorrelationId" -> correlationId.value.toString)

        maybeResult.value.map {
          case Right(result) => result
          case Left(error)   => handleError(error, request)
        }
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
