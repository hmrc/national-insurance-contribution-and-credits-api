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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector

import cats.data.EitherT
import com.google.inject.Inject
import io.scalaland.chimney.dsl.into
import play.api.http.Status.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.common.TextualErrorStatusCode.{InternalServerError, NotFound}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.Class2MaReceiptsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.mapper.Class2MAReceiptsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.{
  Class2MAReceiptsError422Response,
  Class2MAReceiptsErrorResponse400,
  Class2MAReceiptsErrorResponse403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResult, NpsClient}
import uk.gov.hmrc.app.benefitEligibility.util.HttpParsing.attemptParse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class Class2MAReceiptsConnector @Inject() (
    npsClient: NpsClient,
    class2MAReceiptsResponseMapper: Class2MAReceiptsResponseMapper
)(implicit ec: ExecutionContext) {

  def fetchClass2MAReceipts(
      path: String
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Class2MaReceiptsResult] =
    npsClient
      .get(path)
      .flatMap { response =>
        val class2MAReceiptsResponse =
          response.status match {
            case OK =>
              attemptParse[Class2MAReceiptsSuccessResponse](response).map(class2MAReceiptsResponseMapper.toResult)
            case BAD_REQUEST =>
              attemptParse[Class2MAReceiptsErrorResponse400](response).map(class2MAReceiptsResponseMapper.toResult)
            case FORBIDDEN =>
              attemptParse[Class2MAReceiptsErrorResponse403](response).map(class2MAReceiptsResponseMapper.toResult)
            case UNPROCESSABLE_ENTITY =>
              attemptParse[Class2MAReceiptsError422Response](response).map(class2MAReceiptsResponseMapper.toResult)
            case NOT_FOUND => Right(class2MAReceiptsResponseMapper.toResult(NotFound))
            case _         => Right(class2MAReceiptsResponseMapper.toResult(InternalServerError))
          }

        EitherT.fromEither[Future](class2MAReceiptsResponse)

      }

}
