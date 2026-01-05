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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import cats.data.EitherT
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

abstract class NpsConnector[Response <: NpsApiResponse, Result <: NpsApiResult](npsClient: NpsClient) {

  def handleResponse(response: HttpResponse): Either[BenefitEligibilityError, Result]

  def fetchResult(
      path: String
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BenefitEligibilityError, Result] =
    npsClient
      .get(path)
      .flatMap(response => EitherT.fromEither[Future](handleResponse(response)))

}
