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

package uk.gov.hmrc.app.benefitEligibility.connectors

import cats.data.EitherT
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.connectors.util.{NpsClient, NpsResponseHandler}
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnexpectedStatus,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.MarriageDetailsResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.{
  NpsErrorResponse400,
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.attemptParse
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MarriageDetailsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.MarriageDetails

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchMarriageDetails(
      identifier: Identifier
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, MarriageDetailsResult] = {

    val path = s"/individual/${identifier.value}/marriage-cp"

    fetchMarriageDetailsData(path)
  }

  def fetchMarriageDetailsData(path: String)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, MarriageDetailsResult] =
    npsClient
      .get(s"${appConfig.baseUrl(apiName)}$path")
      .flatMap { response =>
        logger.info(s"attempting to parse response from $apiName")

        val marriageDetailsResult =
          response.status match {

            case OK =>
              attemptParse[MarriageDetailsSuccessResponse](response).map(
                toSuccessResult
              )

            case BAD_REQUEST =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsErrorResponse400](response).map(resp => toFailureResult(BadRequest, Some(resp)))

            case FORBIDDEN =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsMultiErrorResponse](response).map(resp => toFailureResult(AccessForbidden, Some(resp)))

            case UNPROCESSABLE_ENTITY =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                toFailureResult(UnprocessableEntity, Some(resp))
              }
            case NOT_FOUND =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsSingleErrorResponse](response).map(resp => toFailureResult(NotFound, Some(resp)))

            case INTERNAL_SERVER_ERROR =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                toFailureResult(InternalServerError, Some(resp))
              }

            case SERVICE_UNAVAILABLE =>
              logger.warn(s"$apiName returned a ${response.status}: ${response.body}")
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                toFailureResult(ServiceUnavailable, Some(resp))
              }

            case code =>
              logger.warn(s"$apiName returned an unexpected status: $code: ${response.body}")
              Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](marriageDetailsResult).leftMap {
          case error: JsonValidationError =>
            logger.error(s"failed to process ${response.status} response from $apiName: ${error.toString}")
            error
          case error: InvalidJsonError =>
            logger.error(s"failed to process ${response.status} response from $apiName: ${error.toString}")
            error
          case error => error
        }
      }
      .leftMap {
        case error: JsonValidationError => error
        case error: InvalidJsonError    => error
        case error =>
          logger.error(s"call to downstream service $apiName failed: ${error.toString}")
          error
      }

}
