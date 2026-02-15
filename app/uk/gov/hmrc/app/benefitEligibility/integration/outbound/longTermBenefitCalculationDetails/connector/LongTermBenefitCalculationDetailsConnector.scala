/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class LongTermBenefitCalculationDetailsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.LongTermBenefitCalculationDetails

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchBenefitCalculationDetails(
      benefitType: BenefitType,
      identifier: Identifier,
      sequenceNumber: Option[AssociatedCalculationSequenceNumber],
      longTermBenefitType: Option[LongTermBenefitType],
      pensionProcessingArea: Option[PensionProcessingArea]
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, LongTermBenefitCalculationDetailsResult] = {

    def seqNo: Option[String]             = sequenceNumber.map(s => s.value.toString)
    def typeFilter: Option[String]        = longTermBenefitType.map(t => t.entryName)
    def pensionProcessing: Option[String] = pensionProcessingArea.map(p => p.entryName)

    val options = List(
      RequestOption("seqNo", seqNo),
      RequestOption("type", typeFilter),
      RequestOption("pensionProcessingArea", pensionProcessing)
    )

    val path =
      RequestBuilder.buildPath(s"${appConfig.hipBaseUrl}/long-term-benefits/${identifier.value}/calculation", options)

    npsClient
      .get(path)
      .flatMap { response =>
        val benefitCalculationDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[LongTermBenefitCalculationDetailsSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"Benefit Calculation Details returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"Benefit Calculation Details returned a 403: code: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case NOT_FOUND =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(s"Benefit Calculation Details returned a 404: $resp")
                toFailureResult(NotFound, Some(resp))
              }

            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(s"Benefit Calculation Details returned a 422: $resp")
                toFailureResult(UnprocessableEntity, Some(resp))
              }

            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"Benefit Calculation Details returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"Benefit Calculation Details returned a 500: $resp")
                toFailureResult(InternalServerError, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](benefitCalculationDetailsResult).leftMap { error =>
          logger.error(s"failed to process response from Benefit Calculation Details: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
