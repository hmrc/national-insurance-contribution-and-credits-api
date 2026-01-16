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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector

import cats.data.EitherT
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import play.api.libs.json.Json
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{MarriageDetails, NiContributionAndCredits}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ContributionCreditResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.mapper.NiContributionsAndCreditsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.reqeust.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.reqeust.NiContributionsAndCreditsRequest.niContributionsAndCreditsRequestWrites
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.{
  NiContributionsAndCreditsResponse400,
  NiContributionsAndCreditsResponse403,
  NiContributionsAndCreditsResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.attemptStrictParse
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class NiContributionsAndCreditsConnector @Inject() (
    npsClient: NpsClient,
    niContributionsAndCreditsResponseMapper: NiContributionsAndCreditsResponseMapper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def path = s"${appConfig.hipBaseUrl}/national-insurance/contributions-and-credits"

  def fetchContributionsAndCredits(
      request: NiContributionsAndCreditsRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, ContributionCreditResult] =
    npsClient
      .post(path, request)
      .flatMap { response =>
        val contributionsAndCreditsResult =
          response.status match {
            case OK =>
              attemptStrictParse[NiContributionsAndCreditsSuccessResponse](response).map(
                niContributionsAndCreditsResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptStrictParse[NiContributionsAndCreditsResponse400](response).map { resp =>
                logger.warn(s"ContributionsAndCredits returned a 400: ${resp.failures.mkString(",")}")
                niContributionsAndCreditsResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptStrictParse[NiContributionsAndCreditsResponse403](response).map { resp =>
                logger.warn(
                  s"ContributionsAndCredits returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                niContributionsAndCreditsResponseMapper.toApiResult(resp)
              }
            case UNPROCESSABLE_ENTITY =>

              attemptStrictParse[NiContributionsAndCreditsResponse422](response).map { resp =>
                logger.warn(s"ContributionsAndCredits returned a 422: ${resp.failures.mkString(",")}")
                niContributionsAndCreditsResponseMapper.toApiResult(resp)
              }

            case NOT_FOUND => Right(DownstreamErrorReport(NiContributionAndCredits, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(DownstreamErrorReport(NiContributionAndCredits, InternalServerError))
            case code => Right(DownstreamErrorReport(NiContributionAndCredits, UnexpectedStatus(code)))
          }

        EitherT.fromEither[Future](contributionsAndCreditsResult).leftMap { error =>
          logger.error(s"failed to process response from ContributionsAndCredits: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }

}
