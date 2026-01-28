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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.IndividualStatePension
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.FailureResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.mapper.IndividualStatePensionInformationResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError.{
  IndividualStatePensionInformationErrorResponse400,
  IndividualStatePensionInformationErrorResponse403,
  IndividualStatePensionInformationErrorResponse503
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationResponseValidation.individualStatePensionInformationResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{IndividualStatePensionResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IndividualStatePensionInformationConnector @Inject() (
    npsClient: NpsClient,
    individualStatePensionInformationResponseMapper: IndividualStatePensionInformationResponseMapper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchIndividualStatePensionInformation(
      benefitType: BenefitType,
      identifier: Identifier
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, IndividualStatePensionResult] = {

    val path = s"${appConfig.hipBaseUrl}/long-term-benefits/${identifier.value}/contributions"

    npsClient
      .get(path)
      .flatMap { response =>
        val individualStatePensionResult =
          response.status match {
            case OK =>
              attemptStrictParse[IndividualStatePensionInformationSuccessResponse](benefitType, response).map(
                individualStatePensionInformationResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptParse[IndividualStatePensionInformationErrorResponse400](response).map { resp =>
                logger.warn(s"IndividualStatePensionInformation returned a 400: $resp")
                individualStatePensionInformationResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptParse[IndividualStatePensionInformationErrorResponse403](response).map { resp =>
                logger.warn(
                  s"IndividualStatePensionInformation returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                individualStatePensionInformationResponseMapper.toApiResult(resp)
              }
            case SERVICE_UNAVAILABLE =>
              attemptParse[IndividualStatePensionInformationErrorResponse503](response).map { resp =>
                logger.warn(s"IndividualStatePensionInformation returned a 503: $resp")
                individualStatePensionInformationResponseMapper.toApiResult(resp)
              }
            case NOT_FOUND =>
              Right(FailureResult(IndividualStatePension, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(FailureResult(IndividualStatePension, InternalServerError))
            case code => Right(FailureResult(IndividualStatePension, UnexpectedStatus(code)))
          }

        EitherT.fromEither[Future](individualStatePensionResult).leftMap { error =>
          logger.error(s"failed to process response from IndividualStatePensionInformation: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
