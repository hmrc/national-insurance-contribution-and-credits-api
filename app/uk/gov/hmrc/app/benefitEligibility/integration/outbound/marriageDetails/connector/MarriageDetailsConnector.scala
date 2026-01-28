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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.MarriageDetails
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, BenefitType}
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.FailureResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.mapper.MarriageDetailsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError.{
  MarriageDetailsErrorResponse400,
  MarriageDetailsErrorResponse403,
  MarriageDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{MarriageDetailsResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MarriageDetailsConnector @Inject() (
    npsClient: NpsClient,
    marriageDetailsResponseMapper: MarriageDetailsResponseMapper
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchMarriageDetails(
      benefitType: BenefitType,
      path: String
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, MarriageDetailsResult] =
    npsClient
      .get(path)
      .flatMap { response =>
        val marriageDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[MarriageDetailsSuccessResponse](benefitType, response).map(
                marriageDetailsResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptParse[MarriageDetailsErrorResponse400](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 400: ${resp.failures.mkString(",")}")
                marriageDetailsResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptParse[MarriageDetailsErrorResponse403](response).map { resp =>
                logger.warn(
                  s"MarriageDetails returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                marriageDetailsResponseMapper.toApiResult(resp)
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[MarriageDetailsErrorResponse422](response).map { resp =>
                logger.warn(s"MarriageDetails returned a 422: ${resp.failures.mkString(",")}")
                marriageDetailsResponseMapper.toApiResult(resp)
              }
            case NOT_FOUND => Right(FailureResult(MarriageDetails, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(FailureResult(MarriageDetails, InternalServerError))
            case code => Right(FailureResult(MarriageDetails, UnexpectedStatus(code)))
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
