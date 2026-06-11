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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.{InternalServerError, Ok}
import play.api.mvc.{Action, AnyContent, BodyParser, ControllerComponents, RequestHeader, Result}
import uk.gov.hmrc.app.benefitEligibility.controller.BenefitEligibilityErrorHandler.*
import uk.gov.hmrc.app.benefitEligibility.controller.RequestHelper.*
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.response.{
  BenefitEligibilityInfoResponse,
  ErrorCode,
  ErrorReason,
  ErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitEligibilityDataRetrievalService,
  PaginationResult,
  PaginationService
}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: uk.gov.hmrc.app.benefitEligibility.controller.action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService,
    paginationService: PaginationService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private def customJsonBodyParser(): BodyParser[JsValue] = {
    def processError(requestHeader: RequestHeader) =
      RequestHelper.getAndValidateCorrelationId(requestHeader.headers) match {
        case Left(error) => Left(handleError(error, requestHeader.headers)(hc(requestHeader)))
        case Right(correlationId) =>
          Left(
            BadRequest(
              Json.toJson(ErrorResponse(ErrorCode.BadRequest, ErrorReason("invalid JSON")))
            ).withHeaders("CorrelationId" -> correlationId.value.toString)
          )
      }

    BodyParser("custom json parser") { request =>
      parse
        .tolerantText(request)
        .map {
          case Right(body) =>
            RequestHelper.getAndValidateCorrelationId(request.headers) match {
              case Left(error) => Left(handleError(error, request.headers)(hc(request)))
              case Right(_) =>
                Json.parse(body) match {
                  case json => Right(json)
                }
            }

          case Left(_) => processError(request)

        }
        .recover { case _ => processError(request) }
    }
  }

  def fetchBenefitEligibilityData(): Action[JsValue] =

    identity.async(customJsonBodyParser()) { implicit request =>
      val maybeResult = for {
        headerValues <- EitherT.fromEither[Future](validateHeaders(request.headers))
        correlationId = headerValues
        eligibilityRequest <- EitherT.fromEither[Future](parseAndValidateRequest(request))
        response <-
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

      } yield response

      maybeResult.value.map {
        case Right(result) => result
        case Left(error)   => handleError(error, request.headers)
      }
    }

  def getNextPage(cursorId: Option[String]): Action[AnyContent] =
    identity.async(parse.default) { implicit request =>
      val maybeResult = for {
        headerValues <- EitherT.fromEither[Future](validateHeaders(request.headers))
        correlationId = headerValues
        pageTaskId       <- EitherT.fromEither[Future](parsePageTaskId(cursorId))
        paginationResult <- paginationService.paginate(pageTaskId)
      } yield buildResponse(paginationResult).withHeaders("CorrelationId" -> correlationId.value.toString)

      maybeResult.value.map {
        case Right(result) => result
        case Left(error)   => handleError(error, request.headers)
      }
    }

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
