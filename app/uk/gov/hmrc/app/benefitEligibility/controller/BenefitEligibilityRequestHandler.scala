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
import play.api.mvc.Results.{BadRequest, InternalServerError, NotFound, Ok, UnprocessableEntity}
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
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult
import uk.gov.hmrc.app.benefitEligibility.util.{RequestAwareLogger, SuccessfulResult}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object BenefitEligibilityRequestHandler {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def handleStandardRequest(
      request: Request[AnyContent],
      fn: (EligibilityCheckDataRequest, CorrelationId) => EitherT[
        Future,
        BenefitEligibilityError,
        EligibilityCheckDataResult
      ]
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Result] =
    (
      validateHeaders(request) match {
        case Right(correlationId, originatorId) =>
          request.body.asJson match {
            case Some(requestJson) =>
              requestJson.validate[EligibilityCheckDataRequest] match {
                case JsSuccess(eligibilityCheckDataRequest, _) =>
                  if (OriginatorIdType.from(eligibilityCheckDataRequest.benefitType) == originatorId) {
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
                        logger.info("Fetching benefit eligibility data")
                        fn(eligibilityCheckDataRequest, correlationId).map { result =>
                          BenefitEligibilityInfoResponse.from(
                            eligibilityCheckDataRequest.nationalInsuranceNumber,
                            result
                          ) match {
                            case Left(value) =>
                              InternalServerError(Json.toJson(value))
                            case Right(value) =>
                              Ok(Json.toJson(value))
                          }
                        }
                    }
                  } else {
                    EitherT.rightT[Future, BenefitEligibilityError](
                      BadRequest(
                        Json.toJson(
                          ErrorResponse(
                            ErrorCode.BadRequest,
                            ErrorReason(s"Originator Id doesnt match benefit type")
                          )
                        )
                      )
                    )
                  }
                case JsError(errors) =>
                  val errorMessage = errors
                    .map { error =>
                      if (error._1.toString.isEmpty) error._2.flatMap(_.messages).mkString(",")
                      else s"${error._1.toString} ${error._2.flatMap(_.messages).mkString(",")}"
                    }
                    .mkString("[", ", ", "]")
                  logger.error(s"bad request ${errors.mkString(",")}")
                  EitherT.rightT[Future, BenefitEligibilityError](
                    BadRequest(
                      Json.toJson(
                        ErrorResponse(
                          ErrorCode.BadRequest,
                          ErrorReason(s"incompatible json, request body does not match schema - $errorMessage")
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
    ).value.map { v =>
      getCorrelationId(request) match {
        case Left(error) =>
          BadRequest(
            Json.toJson(ErrorResponse(ErrorCode.BadRequest, error))
          )
        case Right(correlationId) =>
          v match {
            case Left(error) =>
              logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
              InternalServerError(
                Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason("unexpected internal failure")))
              ).withHeaders("CorrelationId" -> correlationId.value.toString)
            case Right(response) => response.withHeaders("CorrelationId" -> correlationId.value.toString)
          }
      }
    }

  def handlePaginationRequest(
      request: Request[AnyContent],
      paginationFunction: PaginationCursor => EitherT[Future, BenefitEligibilityError, PaginationResult]
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Result] =
    (validateHeaders(request) match {
      case Right(correlationId, originatorId) =>
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
              nextCursor
            )
              .map { paginationResult =>
                logger.info("Getting paginated data")
                BenefitEligibilityInfoResponse.from(
                  paginationResult
                ) match {
                  case Left(value) =>
                    InternalServerError(Json.toJson(value))
                  case Right(value) =>
                    Ok(Json.toJson(value))
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
    }).value.map { v =>
      getCorrelationId(request) match {
        case Left(error) =>
          BadRequest(
            Json.toJson(ErrorResponse(ErrorCode.BadRequest, error))
          )
        case Right(correlationId) =>
          v match {
            case Left(error) =>
              error match {
                case RecordNotFound(id) =>
                  logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
                  NotFound(
                    Json.toJson(
                      ErrorResponse(ErrorCode.NotFound, ErrorReason(s"record not found for cursorId: ${id.value}"))
                    )
                  ).withHeaders("CorrelationId" -> correlationId.value.toString)
                case _ =>
                  logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
                  InternalServerError(
                    Json.toJson(
                      ErrorResponse(ErrorCode.InternalServerError, ErrorReason("unexpected internal failure"))
                    )
                  ).withHeaders("CorrelationId" -> correlationId.value.toString)
              }
            case Right(response) => response.withHeaders("CorrelationId" -> correlationId.value.toString)
          }
      }
    }

  private def validateHeaders(request: Request[AnyContent])(
      implicit headerCarrier: HeaderCarrier
  ): Either[ErrorReason, (CorrelationId, OriginatorIdType)] =
    getAcceptHeader(request) match {
      case Right(_) =>
        getOriginatorId(request) match {
          case Right(id) =>
            getCorrelationId(request) match {
              case Right(correlationId)     => Right(correlationId, id)
              case Left(error: ErrorReason) => Left(error)
            }
          case Left(error: ErrorReason) => Left(error)
        }
      case Left(error: ErrorReason) => Left(error)

    }

  private def getOriginatorId(request: Request[AnyContent])(
      implicit headerCarrier: HeaderCarrier
  ): Either[ErrorReason, OriginatorIdType] = {
    logger.info("Validating Originator Id")
    request.headers.get("gov-uk-originator-id") match {
      case None =>
        logger.error("Missing header 'gov-uk-originator-id'")
        Left(ErrorReason("Missing header 'gov-uk-originator-id'"))
      case Some(acceptHeader) =>
        RequestValidations.validateOriginatorId(Some(acceptHeader))
    }
  }

  private def getAcceptHeader(request: Request[AnyContent])(
      implicit headerCarrier: HeaderCarrier
  ): Either[ErrorReason, SuccessfulResult.type] = {
    logger.info("Validating Accept Header")
    request.headers.get("Accept") match {
      case None =>
        logger.error("Missing header Accept")
        Left(ErrorReason("Missing Header Accept"))
      case Some(acceptHeader) =>
        RequestValidations.validateAcceptHeader(Some(acceptHeader))
    }
  }

  private[controller] def getCorrelationId(request: Request[AnyContent])(
      implicit headerCarrier: HeaderCarrier
  ): Either[ErrorReason, CorrelationId] = {
    logger.info("Validating CorrelationId")
    request.headers.get("CorrelationId") match {
      case None =>
        logger.error("Missing header CorrelationID")
        Left(
          ErrorReason("Missing Header CorrelationId")
        )
      case Some(correlationId) =>
        RequestValidations.validateCorrelationId(correlationId) match {
          case Right(id) =>
            logger.info("CorrelationId is Valid")
            Right(id)
          case Left(error) =>
            logger.error("Correlation Id is not a valid UUID")
            Left(error)
        }
    }
  }

}
