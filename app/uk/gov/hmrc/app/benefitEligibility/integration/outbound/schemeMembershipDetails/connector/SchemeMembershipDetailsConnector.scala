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
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.common.{
  BenefitEligibilityError,
  BenefitType,
  Identifier,
  OccurrenceNumber,
  SequenceNumber,
  TransferSequenceNumber
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.mapper.SchemeMembershipDetailsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsError.{
  SchemeMembershipDetailsErrorResponse400,
  SchemeMembershipDetailsErrorResponse403,
  SchemeMembershipDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsResponseValidation.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsClient, SchemeMembershipDetailsResult}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SchemeMembershipDetailsConnector @Inject() (
    npsClient: NpsClient,
    schemeMembershipDetailsResponseMapper: SchemeMembershipDetailsResponseMapper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchSchemeMembershipDetails(
      benefitType: BenefitType,
      nationalInsuranceNumber: Identifier,
      sequenceNumber: Option[SequenceNumber],
      transferSequenceNumber: Option[TransferSequenceNumber],
      occurrenceNumber: Option[OccurrenceNumber]
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, SchemeMembershipDetailsResult] = {

    val path =
      buildPath(appConfig.hipBaseUrl, nationalInsuranceNumber, sequenceNumber, transferSequenceNumber, occurrenceNumber)

    npsClient
      .get(path)
      .flatMap { response =>
        val schemeMembershipDetailsResult =
          response.status match {
            case OK =>
              attemptStrictParse[SchemeMembershipDetailsSuccessResponse](benefitType, response).map(
                schemeMembershipDetailsResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptParse[SchemeMembershipDetailsErrorResponse400](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 400: ${resp.failures.mkString(",")}")
                schemeMembershipDetailsResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptParse[SchemeMembershipDetailsErrorResponse403](response).map { resp =>
                logger.warn(
                  s"SchemeMembershipDetails returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                schemeMembershipDetailsResponseMapper.toApiResult(resp)
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[SchemeMembershipDetailsErrorResponse422](response).map { resp =>
                logger.warn(s"SchemeMembershipDetails returned a 422: ${resp.failures.mkString(",")}")
                schemeMembershipDetailsResponseMapper.toApiResult(resp)
              }

            case NOT_FOUND => Right(DownstreamErrorReport(SchemeMembershipDetails, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(DownstreamErrorReport(SchemeMembershipDetails, InternalServerError))
            case code => Right(DownstreamErrorReport(SchemeMembershipDetails, UnexpectedStatus(code)))
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

  private[connector] def buildPath(
      hipBaseUrl: String,
      nationalInsuranceNumber: Identifier,
      sequenceNumber: Option[SequenceNumber],
      transferSequenceNumber: Option[TransferSequenceNumber],
      occurrenceNumber: Option[OccurrenceNumber]
  ) = {
    def sequenceNumberFilter: Option[String]         = sequenceNumber.map(sn => s"seqNo=${sn.value}&")
    def transferSequenceNumberFilter: Option[String] = transferSequenceNumber.map(tsn => s"transferSeqNo=${tsn.value}&")
    def occurrenceNumberFilter: Option[String]       = occurrenceNumber.map(on => s"occurrenceNo=${on.value}&")

    val options =
      sequenceNumberFilter.combine(transferSequenceNumberFilter).combine(occurrenceNumberFilter).getOrElse("")

    s"$hipBaseUrl/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details?$options"
      .dropRight(1)
  }

}
