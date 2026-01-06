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
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.{
  InternalServerError,
  NotFound,
  UnexpectedStatus
}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.MarriageDetailsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsClient
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.mapper.MarriageDetailsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError.{
  MarriageDetailsErrorResponse400,
  MarriageDetailsErrorResponse403,
  MarriageDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.attemptParse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MarriageDetailsConnector @Inject() (
    npsClient: NpsClient,
    marriageDetailsResponseMapper: MarriageDetailsResponseMapper
)(implicit ec: ExecutionContext) {

  def fetchMarriageDetails(
      path: String
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, MarriageDetailsResult] =
    npsClient
      .get(path)
      .flatMap { response =>
        val marriageDetailsResponse =
          response.status match {
            case OK =>
              attemptParse[MarriageDetailsSuccessResponse](response).map(marriageDetailsResponseMapper.toResult)
            case BAD_REQUEST =>
              attemptParse[MarriageDetailsErrorResponse400](response).map(marriageDetailsResponseMapper.toResult)
            case FORBIDDEN =>
              attemptParse[MarriageDetailsErrorResponse403](response).map(marriageDetailsResponseMapper.toResult)
            case UNPROCESSABLE_ENTITY =>
              attemptParse[MarriageDetailsErrorResponse422](response).map(marriageDetailsResponseMapper.toResult)
            case NOT_FOUND             => Right(marriageDetailsResponseMapper.toResult(NotFound))
            case INTERNAL_SERVER_ERROR => Right(marriageDetailsResponseMapper.toResult(InternalServerError))
            case code                  => Right(marriageDetailsResponseMapper.toResult(UnexpectedStatus(code)))
          }

        EitherT.fromEither[Future](marriageDetailsResponse)
      }

}
