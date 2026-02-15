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
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnexpectedStatus
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponse400,
  NpsErrorResponseHipOrigin,
  NpsSingleErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationResponseValidation.individualStatePensionInformationResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  IndividualStatePensionResult,
  NpsClient,
  NpsResponseHandler
}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IndividualStatePensionInformationConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.IndividualStatePension

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
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"IndividualStatePensionInformation returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"IndividualStatePensionInformation returned a 403: code: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"IndividualStatePensionInformation returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case NOT_FOUND =>
              Right(toFailureResult(NotFound, None))
            case INTERNAL_SERVER_ERROR =>
              Right(toFailureResult(InternalServerError, None))
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
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
