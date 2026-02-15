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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector

import cats.data.EitherT
import play.api.http.Status.*
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
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{MarriageDetailsResult, NpsClient, NpsResponseHandler}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
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
      benefitType: BenefitType,
      identifier: Identifier,
      startYear: Option[StartYear],
      latestFilter: Option[FilterLatest],
      seq: Option[Int]
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, MarriageDetailsResult] = {

    def searchStartYear: Option[String] = startYear.map(sy => sy.value.toString)
    def latest: Option[String]          = latestFilter.map(l => l.value.toString)
    def sequence: Option[String]        = seq.map(s => s.toString)

    val options = List(
      RequestOption("searchStartYear", searchStartYear),
      RequestOption("latest", latest),
      RequestOption("sequence", sequence)
    )

    val path = RequestBuilder.buildPath(
      s"${appConfig.hipBaseUrl}/individual/${identifier.value}/marriage-cp",
      options
    )

    npsClient
      .get(path)
      .flatMap { response =>
        val marriageDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[MarriageDetailsSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(
                  s"MarriageDetails returned a 403: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 422: $resp")
                toFailureResult(UnprocessableEntity, Some(resp))
              }
            case NOT_FOUND =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 404: $resp")
                toFailureResult(NotFound, Some(resp))
              }
            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 500: $resp")
                toFailureResult(InternalServerError, Some(resp))
              }
            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](marriageDetailsResult).leftMap { error =>
          logger.error(s"failed to process response from MarriageDetails: ${error.toString}")
          error
        }
      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
