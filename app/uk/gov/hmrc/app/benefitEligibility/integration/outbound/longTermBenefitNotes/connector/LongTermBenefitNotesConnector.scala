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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.connector

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.LongTermBenefitNotes
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnexpectedStatus,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesResponseValidation.longTermBenefitNotesResponseValidator
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  LongTermBenefitNotesResult,
  NpsClient,
  NpsResponseHandler
}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.{attemptParse, attemptStrictParse}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class LongTermBenefitNotesConnector @Inject() (
    npsClient: NpsClient,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends NpsResponseHandler {

  val apiName: ApiName = ApiName.LongTermBenefitNotes

  private val logger = new RequestAwareLogger(this.getClass)

  def fetchLongTermBenefitNotes(
      benefitType: BenefitType,
      identifier: Identifier,
      longTermBenefitType: LongTermBenefitType,
      seqNo: AssociatedCalculationSequenceNumber
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, LongTermBenefitNotesResult] = {

    val path =
      s"${appConfig.hipBaseUrl}/long-term-benefits/${identifier.value}/calculation/${longTermBenefitType.entryName}/notes/${seqNo.value}"

    npsClient
      .get(path)
      .flatMap { response =>
        val longTermBenefitNotesResult =
          response.status match {
            case OK =>
              attemptStrictParse[LongTermBenefitNotesSuccessResponse](benefitType, response).map(
                toSuccessResult
              )
            case BAD_REQUEST =>
              attemptParse[NpsErrorResponse400](response).map { resp =>
                logger.warn(s"LongTermBenefitNotes returned a 400: $resp")
                toFailureResult(BadRequest, Some(resp))
              }
            case FORBIDDEN =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"LongTermBenefitNotes returned a 403: $resp"
                )
                toFailureResult(AccessForbidden, Some(resp))
              }
            case NOT_FOUND =>
              attemptParse[NpsSingleErrorResponse](response).map { resp =>
                logger.warn(
                  s"LongTermBenefitNotes returned a 404: $resp"
                )
                toFailureResult(NotFound, Some(resp))
              }
            case UNPROCESSABLE_ENTITY =>
              attemptParse[NpsMultiErrorResponse](response).map { resp =>
                logger.warn(
                  s"LongTermBenefitNotes returned a 422: $resp"
                )
                toFailureResult(UnprocessableEntity, Some(resp))
              }
            case INTERNAL_SERVER_ERROR =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"LongTermBenefitNotes returned a 500: $resp")
                toFailureResult(InternalServerError, Some(resp))
              }
            case SERVICE_UNAVAILABLE =>
              attemptParse[NpsErrorResponseHipOrigin](response).map { resp =>
                logger.warn(s"LongTermBenefitNotes returned a 503: $resp")
                toFailureResult(ServiceUnavailable, Some(resp))
              }
            case code => Right(toFailureResult(UnexpectedStatus(code), None))
          }

        EitherT.fromEither[Future](longTermBenefitNotesResult).leftMap { error =>
          logger.error(s"failed to process response from LongTermBenefitNotes: ${error.toString}")
          error
        }

      }
      .leftMap { error =>
        logger.error(s"call to downstream service failed: ${error.toString}")
        error
      }
  }

}
