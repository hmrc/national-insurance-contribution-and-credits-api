/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.*
import play.api.mvc.Results.{BadGateway, BadRequest, InternalServerError, Ok, UnprocessableEntity}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.*
import uk.gov.hmrc.app.benefitEligibility.model.response.{
  BenefitEligibilityInfoResponse,
  ErrorCode,
  ErrorReason,
  ErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.repository.{PageTaskId, PaginationCursor}
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult.SuccessfulResult
import uk.gov.hmrc.app.benefitEligibility.util.{RequestAwareLogger, SuccessfulResult}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object BenefitEligibilityRequestHandler {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def handleStandardRequest(
      request: Request[AnyContent],
      fn: EligibilityCheckDataRequest => EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResult]
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Result] =
    (
      processHeaders(request) match {
        case Right(_) =>
          request.body.asJson match {
            case Some(requestJson) =>
              requestJson.validate[EligibilityCheckDataRequest] match {
                case JsSuccess(eligibilityCheckDataRequest, _) =>
                  RequestValidations.validateRequest(eligibilityCheckDataRequest) match {
                    case Left(validationError) =>
                      logger.error(s"Validation Error: ${validationError.messages.mkString(",")}")
                      EitherT.rightT[Future, BenefitEligibilityError](
                        UnprocessableEntity(
                          Json.toJson(
                            ErrorResponse(
                              ErrorCode.UnprocessableEntity,
                              ErrorReason(validationError.messages.mkString(","))
                            )
                          )
                        )
                      )
                    case Right(value) =>
                      fn(eligibilityCheckDataRequest).map { result =>
                        BenefitEligibilityInfoResponse.from(
                          eligibilityCheckDataRequest.nationalInsuranceNumber,
                          result
                        ) match {
                          case Left(value)  => BadGateway(Json.toJson(value))
                          case Right(value) => Ok(Json.toJson(value))
                        }
                      }
                  }
                case JsError(errors) =>
                  logger.error(s"bad request ${errors.mkString(",")}")
                  EitherT.rightT[Future, BenefitEligibilityError](
                    BadRequest(
                      Json.toJson(
                        ErrorResponse(
                          ErrorCode.BadRequest,
                          ErrorReason(s"incompatible json, request body does not match schema")
                        )
                      )
                    )
                  )
              }
            case None =>
              logger.error("invalid json")
              EitherT.rightT[Future, BenefitEligibilityError](
                BadRequest(
                  Json.toJson(ErrorResponse(ErrorCode.BadRequest, ErrorReason("invalid json")))
                )
              )
          }
        case Left(error: ErrorReason) =>
          EitherT.rightT[Future, BenefitEligibilityError](
            BadRequest(
              Json.toJson(ErrorResponse(ErrorCode.BadRequest, error))
            )
          )
      }
    ).value.map {
      case Left(error) =>
        logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
        InternalServerError(
          Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason("unexpected internal failure")))
        )
      case Right(response) => response
    }

  def handlePaginationRequest(
      request: Request[AnyContent],
      paginationFunction: (PageTaskId) => EitherT[Future, BenefitEligibilityError, PaginationResult]
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Result] =
    (processHeaders(request) match {
      case Right(_) =>
        val maybeNextCursor =
          request.queryString
            .get("cursorId")
            .flatMap(_.headOption)
            .map(id => PaginationCursor.from(CursorId(id)))
        maybeNextCursor match {
          case Some(Failure(e)) =>
            EitherT.rightT[Future, BenefitEligibilityError](
              BadRequest(
                Json.toJson(
                  ErrorResponse(
                    ErrorCode.BadRequest,
                    ErrorReason("invalid nextCursor " + e.getMessage)
                  )
                )
              )
            )
          case Some(Success(nextCursor)) =>
            paginationFunction(
              nextCursor.pageTaskId
            )
              .map { paginationResult =>
                BenefitEligibilityInfoResponse.from(
                  paginationResult
                ) match {
                  case Left(value)  => BadGateway(Json.toJson(value))
                  case Right(value) => Ok(Json.toJson(value))
                }
              }
          case None =>
            EitherT.rightT[Future, BenefitEligibilityError](
              BadRequest(
                Json.toJson(
                  ErrorResponse(
                    ErrorCode.BadRequest,
                    ErrorReason("Paginate request sent with no next cursor")
                  )
                )
              )
            )
        }
      case Left(error: ErrorReason) =>
        EitherT.rightT[Future, BenefitEligibilityError](
          BadRequest(
            Json.toJson(ErrorResponse(ErrorCode.BadRequest, error))
          )
        )
    }).value.map {
      case Left(error) =>
        logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
        InternalServerError(
          Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason("unexpected internal failure")))
        )
      case Right(response) => response
    }

  private[controller] def processHeaders(request: Request[AnyContent])(
      implicit headerCarrier: HeaderCarrier,
      executionContext: ExecutionContext
  ): Either[ErrorReason, SuccessfulResult.type] =
    request.headers.get("CorrelationId") match {
      case None =>
        logger.error("Missing header CorrelationID")
        Left(
          ErrorReason("Missing Header CorrelationId")
        )
      case Some(value) =>
        RequestValidations.validateCorrelationId(value) match {
          case Right(success) => Right(success)
          case Left(error) =>
            logger.error("Correlation Id is not a valid UUID")
            Left(
              ErrorReason(error.errors.mkString(","))
            )
        }
    }

}
