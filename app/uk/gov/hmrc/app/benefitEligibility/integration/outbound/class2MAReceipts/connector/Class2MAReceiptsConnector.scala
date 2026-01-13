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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.common.{
  BenefitEligibilityError,
  Identifier,
  MaternityAllowanceSortType,
  ReceiptDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{Class2MaReceiptsResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.mapper.Class2MAReceiptsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.{
  Class2MAReceiptsErrorResponse400,
  Class2MAReceiptsErrorResponse403,
  Class2MAReceiptsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsResponseValidation.{
  class2MAReceiptsError422ResponseValidator,
  class2MAReceiptsErrorResponse400Validator,
  class2MAReceiptsErrorResponse403Validator,
  class2MAReceiptsSuccessResponseValidator
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.attemptStrictParse
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class Class2MAReceiptsConnector @Inject() (
    npsClient: NpsClient,
    class2MAReceiptsResponseMapper: Class2MAReceiptsResponseMapper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchClass2MAReceipts(
      identifier: Identifier,
      archived: Option[Boolean],
      receiptDate: Option[ReceiptDate],
      sortType: Option[MaternityAllowanceSortType]
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Class2MaReceiptsResult] = {

    def latestFilter: Option[String]      = archived.map(l => s"latest=$l&")
    def receiptDateFilter: Option[String] = receiptDate.map(r => s"receiptDate=${r.value}&")
    def typeFilter: Option[String]        = sortType.map(t => s"type=$t&")

    val options = latestFilter.combine(receiptDateFilter).combine(typeFilter).getOrElse("")
    val path =
      s"${appConfig.hipBaseUrl}/ni/class-2/${identifier.value}/maternity-allowance/receipts?$options".dropRight(1)

    npsClient
      .get(path)
      .flatMap { response =>
        val class2MAReceiptsResult =
          response.status match {
            case OK =>
              attemptStrictParse[Class2MAReceiptsSuccessResponse](response).map(
                class2MAReceiptsResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptStrictParse[Class2MAReceiptsErrorResponse400](response).map { resp =>
                logger.warn(s"Class2MAReceipts returned a 400: ${resp.failures.mkString(",")}")
                class2MAReceiptsResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptStrictParse[Class2MAReceiptsErrorResponse403](response).map { resp =>
                logger.warn(
                  s"Class2MAReceipts returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                class2MAReceiptsResponseMapper.toApiResult(resp)
              }
            case UNPROCESSABLE_ENTITY =>
              attemptStrictParse[Class2MAReceiptsErrorResponse422](response).map { resp =>
                logger.warn(s"Class2MAReceipts returned a 422: ${resp.failures.mkString(",")}")
                class2MAReceiptsResponseMapper.toApiResult(resp)
              }
            case NOT_FOUND =>
              Right(DownstreamErrorReport(Class2MAReceipts, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(DownstreamErrorReport(Class2MAReceipts, InternalServerError))
            case code => Right(DownstreamErrorReport(Class2MAReceipts, UnexpectedStatus(code)))
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
