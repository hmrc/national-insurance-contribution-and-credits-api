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

import cats.data.{EitherT, Validated}
import cats.implicits.catsSyntaxTuple6Semigroupal
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.Results.{BadGateway, BadRequest, InternalServerError, Ok, UnprocessableEntity}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, Identifier, ValidationError}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.util.{RequestAwareLogger, SuccessfulResult}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

object BenefitEligibilityRequestHandler {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def handleRequest(
      request: Request[AnyContent],
      fn: EligibilityCheckDataRequest => EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResult]
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
        request.body.asJson match {
          case Some(requestJson) =>
            requestJson.validate[EligibilityCheckDataRequest] match {
              case JsSuccess(request, _) =>
                validateRequest(request) match {
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
                    fn(request).map { result =>
                      BenefitEligibilityInfoResponse.from(request.nationalInsuranceNumber, result) match {
                        case Left(value)  => BadGateway(Json.toJson(value))
                        case Right(value) => Ok(Json.toJson(value))
                      }
                    }
                }
              case JsError(errors) =>
                logger.error(s"bad request ${errors.flatMap(_._2).mkString(",")}")
                EitherT.rightT[Future, BenefitEligibilityError](
                  BadRequest(
                    Json.toJson(ErrorResponse(ErrorCode.BadRequest, ErrorReason(errors.flatMap(_._2).mkString(","))))
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
      case Left(value) =>
        logger.error(s"Internal server error ${value.toStringSafeToLogInProd}")
        InternalServerError(
          Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason(value.toStringSafeToLogInProd)))
        )
      case Right(value) => value
    }

  def validateRequest(
      request: EligibilityCheckDataRequest
  ): Either[ValidationError, SuccessfulResult.type] =
    (
      Validated.condNel(
        Identifier.pattern.matches(request.nationalInsuranceNumber.value),
        SuccessfulResult,
        "invalid national insurance number format"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value >= 1975,
        SuccessfulResult,
        "Start tax year before 1975"
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
    ).mapN((_, _, _, _, _, _) => SuccessfulResult) match {
      case Validated.Valid(_)   => Right(SuccessfulResult)
      case Validated.Invalid(e) => Left(ValidationError(e.toList))
    }

}
