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
  NpsErrorResponse422Special,
  NpsErrorResponseHipOrigin,
  NpsSingleErrorResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsResponseValidation.liabilitySummaryDetailsResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  LiabilitySummaryDetailsSuccessResponse,
  OccurrenceNumber
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{LiabilityResult, NpsClient, NpsResponseHandler}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class LiabilitySummaryDetailsConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.Liabilities

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchLiabilitySummaryDetails(
      benefitType: BenefitType,
      identifier: Identifier,
      liabilitySearchCategoryHyphenated: LiabilitySearchCategoryHyphenated,
      liabilityOccurrenceNumber: Option[LiabilitiesOccurrenceNumber],
      liabilityType: Option[LiabilitySearchCategoryHyphenated],
      earliestLiabilityStartDate: Option[LocalDate],
      startDate: Option[LocalDate],
      endDate: Option[LocalDate]
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, LiabilityResult] = {

    def occurrenceNumber: Option[String]   = liabilityOccurrenceNumber.map(o => o.toString)
    def typeFilter: Option[String]         = liabilityType.map(t => t.entryName)
    def earliestStartDate: Option[String]  = earliestLiabilityStartDate.map(d => d.toString)
    def liabilityStartDate: Option[String] = startDate.map(d => d.toString)
    def liabilityEndDate: Option[String]   = endDate.map(d => d.toString)

    val options = List(
      RequestOption("occurrenceNumber", occurrenceNumber),
      RequestOption("type", typeFilter),
      RequestOption("earliestStartDate", earliestStartDate),
      RequestOption("liabilityStartDate", liabilityStartDate),
      RequestOption("liabilityEndDate", liabilityEndDate)
    )

    val path =
      RequestBuilder.buildPath(
        s"${appConfig.hipBaseUrl}/person/${identifier.value}/liability-summary/${liabilitySearchCategoryHyphenated.entryName}",
        options
      )

    npsClient
      .get(path)
      .flatMap { response =>
        val liabilityResult =
          response.status match {
            case OK =>
              attemptStrictParse[LiabilitySummaryDetailsSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"LiabilitySummaryDetails returned a 403: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsErrorResponse422Special](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 422: $resp")
                toFailureResult(UnprocessableEntity, Some(resp))
              }
            case NOT_FOUND =>
              Right(toFailureResult(NotFound, None))

            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 500: $resp")
                toFailureResult(InternalServerError, Some(resp))
              }
            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"LiabilitySummaryDetails returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
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
