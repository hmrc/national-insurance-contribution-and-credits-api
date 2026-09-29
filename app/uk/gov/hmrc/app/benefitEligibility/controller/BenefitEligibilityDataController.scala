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
import play.api.mvc.*
import play.api.mvc.Results.{InternalServerError, Ok}
import uk.gov.hmrc.app.benefitEligibility.controller.BenefitEligibilityErrorHandler.*
import uk.gov.hmrc.app.benefitEligibility.controller.RequestHelper.*
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitEligibilityError, CorrelationId, GeneralError}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.response.*
import uk.gov.hmrc.app.benefitEligibility.repository.BatchId
import uk.gov.hmrc.app.benefitEligibility.service.{BatchResult, BatchService, BenefitEligibilityDataRetrievalService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: uk.gov.hmrc.app.benefitEligibility.controller.action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService,
    batchService: BatchService
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
      def retrieveAndHandleResponse(correlationId: CorrelationId, eligibilityRequest: EligibilityCheckDataRequest) = {
        val maybeResult = for {
          response <-
            benefitEligibilityDataRetrievalService
              .getEligibilityData(eligibilityRequest, correlationId)
              .flatMap(buildResponseForFetchData(eligibilityRequest, _))
        } yield response
        val futureResult = deriveFutureResult(maybeResult)
        futureResult.map(_.withHeaders("CorrelationId" -> correlationId.value.toString))
      }

      val interimResult = for {
        correlationID               <- retrieveAndValidateCorrelationId(request.headers)
        eligibilityCheckDataRequest <- parseAndValidateRequest(request)
      } yield retrieveAndHandleResponse(correlationID, eligibilityCheckDataRequest)

      interimResult match {
        case Right(result) => result
        case Left(error)   => Future.successful(handleError(error, request.headers))
      }
    }

  private def buildResponseForFetchData(
      eligibilityRequest: EligibilityCheckDataRequest,
      result: EligibilityCheckDataResult
  ): EitherT[Future, BenefitEligibilityError, Result] =
    BenefitEligibilityInfoResponse
      .from(
        eligibilityRequest.nationalInsuranceNumber,
        result
      ) match {
      case Left(errorResponse: BenefitEligibilityInfoErrorResponse) =>
        EitherT.fromEither(Left(GeneralError(errorResponse.status.entryName)))
      case Right(successResponse) => EitherT.fromEither(Right(Ok(Json.toJson(successResponse))))
    }

  def getNextBatch(cursorId: Option[String]): Action[AnyContent] =
    identity.async(parse.default) { implicit request =>
      def retrieveAndHandleResponse(batchId: BatchId, correlationId: CorrelationId) = {
        val maybeResult = for {
          result <- batchService
            .processBatch(batchId)
            .map(batchResult => buildResponseForNextBatch(batchResult))
        } yield result
        val futureResult = deriveFutureResult(maybeResult)
        futureResult.map(_.withHeaders("CorrelationId" -> correlationId.value.toString))
      }

      val interimResult = for {
        correlationID <- retrieveAndValidateCorrelationId(request.headers)
        batchId       <- parseBatchId(cursorId)
      } yield retrieveAndHandleResponse(batchId, correlationID)
      interimResult match {
        case Right(result) => result
        case Left(error)   => Future.successful(handleError(error, request.headers))
      }
    }

  private def buildResponseForNextBatch(
      batchResult: BatchResult
  ): Result =
    BenefitEligibilityInfoResponse
      .from(batchResult) match {
      case Left(errorResponse)    => InternalServerError(Json.toJson(errorResponse))
      case Right(successResponse) => Ok(Json.toJson(successResponse))
    }

  private def deriveFutureResult(
      maybeResult: EitherT[Future, BenefitEligibilityError, Result]
  )(implicit request: Request[_]) =
    maybeResult.value.map {
      case Right(result) => result
      case Left(error)   => handleError(error, request.headers)
    }

}
