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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.connector

import cats.data.EitherT
import cats.implicits.catsSyntaxSemigroup
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.SchemeMembershipDetails
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
import uk.gov.hmrc.app.benefitEligibility.common.{
  ApiName,
  BenefitEligibilityError,
  BenefitType,
  Identifier,
  RequestBuilder,
  RequestOption,
  SchemeMembershipDetailsOccurrenceNumber,
  SequenceNumber,
  TransferSequenceNumber
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.FailureResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsError.{
  SchemeMembershipDetailsErrorResponse400,
  SchemeMembershipDetailsErrorResponse403,
  SchemeMembershipDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  NpsClient,
  NpsResponseHandler,
  SchemeMembershipDetailsResult
}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SchemeMembershipDetailsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.SchemeMembershipDetails

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchSchemeMembershipDetails(
      benefitType: BenefitType,
      nationalInsuranceNumber: Identifier,
      sequenceNumber: Option[SequenceNumber],
      transferSequenceNumber: Option[TransferSequenceNumber],
      occurrenceNumber: Option[SchemeMembershipDetailsOccurrenceNumber]
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, SchemeMembershipDetailsResult] = {

    def sequenceNumberFilter: Option[String]         = sequenceNumber.map(sn => sn.value.toString)
    def transferSequenceNumberFilter: Option[String] = transferSequenceNumber.map(tsn => tsn.value.toString)
    def occurrenceNumberFilter: Option[String]       = occurrenceNumber.map(on => on.value.toString)

    val options = List(
      RequestOption("seqNo", sequenceNumberFilter),
      RequestOption("transferSeqNo", transferSequenceNumberFilter),
      RequestOption("occurrenceNo", occurrenceNumberFilter)
    )

    val path =
      RequestBuilder.buildPath(
        s"${appConfig.hipBaseUrl}/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details",
        options
      )

    npsClient
      .get(path)
      .flatMap { response =>
        val schemeMembershipDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[SchemeMembershipDetailsSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 403: $resp")
                toFailureResult(AccessForbidden, Some(resp))
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 422: $resp")
                toFailureResult(UnprocessableEntity, Some(resp))
              }

            case NOT_FOUND =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 404: $resp")
                toFailureResult(NotFound, Some(resp))
              }
            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 500: $resp")
                toFailureResult(InternalServerError, Some(resp))
              }

            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](schemeMembershipDetailsResult).leftMap { error =>
          logger.error(s"failed to process response from SchemeMembershipDetails: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
