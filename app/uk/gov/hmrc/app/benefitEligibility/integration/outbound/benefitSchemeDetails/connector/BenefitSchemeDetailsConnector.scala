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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.BenefitSchemeDetails
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.UnexpectedStatus
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsResponseValidation.benefitSchemeDetailsResponseValidationValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.{
  BenefitSchemeDetailsSuccessResponse,
  SchemeContractedOutNumberDetails
}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BenefitSchemeDetailsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseMapperV2 {

  val apiName: ApiName = ApiName.BenefitSchemeDetails

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchBenefitSchemeDetails(
      benefitType: BenefitType,
      identifier: Identifier,
      schemeContractedOutNumberDetails: SchemeContractedOutNumberDetails
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, BenefitSchemeDetailsResult] = {

    val path =
      s"${appConfig.hipBaseUrl}/benefit-scheme/${identifier.value}/benefit-scheme-details/${schemeContractedOutNumberDetails.value}"

    npsClient
      .get(path)
      .flatMap { response =>
        val benefitSchemeDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[BenefitSchemeDetailsSuccessResponse](benefitType, response).map(toApiResult)
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"Benefit Scheme Details returned a 400: $resp")
                toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptParse[NpsErrorResponse403](response).map { resp =>
                logger.warn(
                  s"Benefit Scheme Details returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                toApiResult(resp)
              }
            case NOT_FOUND =>
              attemptParse[NpsErrorResponse404](response).map { resp =>
                logger.warn(s"Benefit Scheme Details returned a 404: $resp")
                toApiResult(resp)
              }

            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsErrorResponse422](response).map { resp =>
                logger.warn(s"Benefit Scheme Details returned a 422: $resp")
                toApiResult(resp)
              }

            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponse503](response).map { resp =>
                logger.warn(s"Benefit Scheme Details returned a 503: $resp")
                toApiResult(resp)
              }
            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponse500](response).map { resp =>
                logger.warn(s"Benefit Scheme Details returned a 500: $resp")
                toApiResult(resp)
              }
            case code => Right(FailureResult(BenefitSchemeDetails, ErrorReport(UnexpectedStatus(code), None)))
          }

        EitherT.fromEither[Future](benefitSchemeDetailsResult).leftMap { error =>
          logger.error(s"failed to process response from Benefit Scheme Details: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
