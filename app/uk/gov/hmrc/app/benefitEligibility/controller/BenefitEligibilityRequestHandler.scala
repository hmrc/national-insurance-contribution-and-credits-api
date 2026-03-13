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

import cats.Traverse.nonInheritedOps.toTraverseOps
import cats.data.{EitherT, Validated}
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple5Semigroupal}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.Results.{BadGateway, BadRequest, InternalServerError, Ok, UnprocessableEntity}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  Identifier,
  InvalidRequest,
  JsonValidationError
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.{
  BSPEligibilityCheckDataRequest,
  ESAEligibilityCheckDataRequest,
  EligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest,
  JSAEligibilityCheckDataRequest,
  MAEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.benefitEligibility.model.response.{
  BenefitEligibilityInfoResponse,
  ErrorCode,
  ErrorReason,
  ErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult
import uk.gov.hmrc.app.benefitEligibility.util.{RequestAwareLogger, SuccessfulResult}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object BenefitEligibilityRequestHandler {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def handleRequest(
      request: Request[AnyContent],
      fn: EligibilityCheckDataRequest => EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResult],
      paginationFunction: (UUID, Identifier) => EitherT[Future, BenefitEligibilityError, PaginationResult]
  )(implicit headerCarrier: HeaderCarrier, executionContext: ExecutionContext): Future[Result] =
    (request.headers.get("CorrelationId") match {
      case None =>
        logger.error("Missing header CorrelationID")
        EitherT.rightT[Future, BenefitEligibilityError](
          BadRequest(
            Json.toJson(ErrorResponse(ErrorCode.BadRequest, ErrorReason("Missing Header CorrelationId")))
          )
        )
      case Some(value) =>
        // TODO add validation for correlation id as UUID
        request.body.asJson match {
          case Some(requestJson) =>
            requestJson.validate[EligibilityCheckDataRequest] match {
              case JsSuccess(eligibilityCheckDataRequest, _) =>
                val maybeNextCursor = eligibilityCheckDataRequest match {
                  case bspReq: BSPEligibilityCheckDataRequest   => bspReq.nextCursor
                  case maReq: MAEligibilityCheckDataRequest     => maReq.nextCursor
                  case gyspReq: GYSPEligibilityCheckDataRequest => gyspReq.nextCursor
                  case _                                        => None
                }
                validateRequest(eligibilityCheckDataRequest) match {
                  case Left(validationError) =>
                    logger.error(s"Validation Error: ${validationError.errors.mkString(",")}")
                    EitherT.rightT[Future, BenefitEligibilityError](
                      UnprocessableEntity(
                        Json.toJson(
                          ErrorResponse(
                            ErrorCode.UnprocessableEntity,
                            ErrorReason(validationError.errors.mkString(","))
                          )
                        )
                      )
                    )
                  case Right(value) =>
                    maybeNextCursor match {
                      case Some(nextCursor) =>
                        paginationFunction(nextCursor.value, eligibilityCheckDataRequest.nationalInsuranceNumber).map {
                          paginationResult =>
                            BenefitEligibilityInfoResponse.from(
                              eligibilityCheckDataRequest.nationalInsuranceNumber,
                              paginationResult
                            ) match {
                              case Left(value)  => BadGateway(Json.toJson(value))
                              case Right(value) => Ok(Json.toJson(value))
                            }
                        }
                      case None =>
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
    }).value.map {
      case Left(error) =>
        logger.error(s"Internal server error ${error.toStringSafeToLogInProd}")
        InternalServerError(
          Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason("unexpected internal failure")))
        )
      case Right(response) => response
    }

  private[controller] def validateRequest(
      request: EligibilityCheckDataRequest
  ): Either[JsonValidationError, SuccessfulResult.type] =
    (
      Validated.condNel(
        Identifier.pattern.matches(request.nationalInsuranceNumber.value),
        SuccessfulResult,
        "invalid national insurance number format"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        "Start tax year after CY-1"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.endTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        "End tax year after CY-1"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value < request.niContributionsAndCredits.endTaxYear.value,
        SuccessfulResult,
        "Start tax year after end tax year"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value >= 1975,
        SuccessfulResult,
        "Start tax year before 1975"
      )
    ).mapN((_, _, _, _, _) => SuccessfulResult) match {
      case Validated.Valid(_)   => Right(SuccessfulResult)
      case Validated.Invalid(e) => Left(JsonValidationError(e.toList))
    }

}
