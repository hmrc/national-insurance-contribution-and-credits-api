/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnexpectedStatus,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponse400,
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsResponseValidation.class2MAReceiptsSuccessResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{Class2MaReceiptsResult, NpsClient, NpsResponseHandler}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class Class2MAReceiptsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.Class2MAReceipts

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchClass2MAReceipts(
      benefitType: BenefitType,
      identifier: Identifier,
      archived: Option[Boolean],
      receiptDate: Option[ReceiptDate],
      sortType: Option[MaternityAllowanceSortType]
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Class2MaReceiptsResult] = {

    def latestFilter: Option[String]      = archived.map(l => l.toString)
    def receiptDateFilter: Option[String] = receiptDate.map(r => r.value.toString)
    def typeFilter: Option[String]        = sortType.map(t => t.entryName)

    val options = List(
      RequestOption("latest", latestFilter),
      RequestOption("receiptDate", receiptDateFilter),
      RequestOption("type", typeFilter)
    )
    val path =
      RequestBuilder.buildPath(
        s"${appConfig.hipBaseUrl}/class-2/${identifier.value}/maternity-allowance/receipts",
        options
      )

    npsClient
      .get(path)
      .flatMap { response =>
        val class2MAReceiptsResult =
          response.status match {
            case OK =>
              attemptStrictParse[Class2MAReceiptsSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"Class2MAReceipts returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"Class2MAReceipts returned a 403: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(s"Class2MAReceipts returned a 422: ${resp.failures.mkString(",")}")
                toFailureResult(UnprocessableEntity, Some(resp))
              }
            case NOT_FOUND =>
              Right(toFailureResult(NotFound, None))
            case INTERNAL_SERVER_ERROR =>
              Right(toFailureResult(InternalServerError, None))
            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"Class2MAReceipts returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](class2MAReceiptsResult).leftMap { error =>
          logger.error(s"failed to process response from class2MAReceipts: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
