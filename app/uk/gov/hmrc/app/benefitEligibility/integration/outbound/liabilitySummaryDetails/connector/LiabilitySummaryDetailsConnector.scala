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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{InternalServerError, NotFound, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.mapper.LiabilitySummaryDetailsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsResponseValidation.liabilitySummaryDetailsResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.{
  LiabilitySummaryDetailsSuccessResponse,
  OccurrenceNumber
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.LiabilitySearchCategoryHyphenated
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{LiabilityResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class LiabilitySummaryDetailsConnector @Inject() (
    npsClient: NpsClient,
    liabilitySummaryDetailsResponseMapper: LiabilitySummaryDetailsResponseMapper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchLiabilitySummaryDetails(
      benefitType: BenefitType,
      identifier: Identifier,
      liabilitySearchCategoryHyphenated: LiabilitySearchCategoryHyphenated,
      liabilityOccurrenceNumber: Option[OccurrenceNumber],
      liabilityType: Option[LiabilitySearchCategoryHyphenated],
      earliestLiabilityStartDate: Option[LocalDate],
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, LiabilityResult] = {

    def occurrenceNumber: Option[String]   = liabilityOccurrenceNumber.map(o => s"occurrenceNumber=${o.toString}&")
    def typeFilter: Option[String]         = liabilityType.map(t => s"type=$t&")
    def earliestStartDate: Option[String]  = earliestLiabilityStartDate.map(d => s"earliestStartDate=${d.toString}&")
    def liabilityStartDate: Option[String] = startDate.map(d => s"liabilityStartDate=$d&")
    def liabilityEndDate: Option[String]   = endDate.map(d => s"liabilityEndDate=$d&")

    val options = occurrenceNumber
      .combine(typeFilter)
      .combine(earliestStartDate)
      .combine(liabilityStartDate)
      .combine(liabilityEndDate)
      .getOrElse("")
    val path =
      s"${appConfig.hipBaseUrl}/person/${identifier.value}/liability-summary/${liabilitySearchCategoryHyphenated.entryName}?$options"
        .dropRight(1)

    npsClient
      .get(path)
      .flatMap { response =>
        val liabilityResult =
          response.status match {
            case OK =>
              attemptStrictParse[LiabilitySummaryDetailsSuccessResponse](benefitType, response).map(
                liabilitySummaryDetailsResponseMapper.toApiResult
              )
            case BAD_REQUEST =>
              attemptParse[LiabilitySummaryDetailsErrorResponse400](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 400: ${resp.failures.mkString(",")}")
                liabilitySummaryDetailsResponseMapper.toApiResult(resp)
              }
            case FORBIDDEN =>
              attemptParse[LiabilitySummaryDetailsErrorResponse403](response).map { resp =>
                logger.warn(
                  s"LiabilitySummaryDetails returned a 403: code: ${resp.code.entryName}, reason: ${resp.reason.entryName}"
                )
                liabilitySummaryDetailsResponseMapper.toApiResult(resp)
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[LiabilitySummaryDetailsErrorResponse422](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 422: ${resp.failures.mkString(",")}")
                liabilitySummaryDetailsResponseMapper.toApiResult(resp)
              }
            case NOT_FOUND =>
              Right(DownstreamErrorReport(Liabilities, NotFound))
            case INTERNAL_SERVER_ERROR =>
              Right(DownstreamErrorReport(Liabilities, InternalServerError))
            case code => Right(DownstreamErrorReport(Liabilities, UnexpectedStatus(code)))
          }

        EitherT.fromEither[Future](liabilityResult).leftMap { error =>
          logger.error(s"failed to process response from liabilitySummaryDetails: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
