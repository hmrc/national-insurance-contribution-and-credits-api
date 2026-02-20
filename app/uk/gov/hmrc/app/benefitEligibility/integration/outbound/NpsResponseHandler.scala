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

import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}

trait NpsResponseHandler {

  def apiName: ApiName

  def toSuccessResult[A <: ErrorReport, B <: NpsSuccessfulApiResponse](response: B): NpsApiResult[A, B] =
    SuccessResult(apiName, response)

  def toFailureResult[A <: ErrorReport, B <: NpsSuccessfulApiResponse](
      normalizedError: NpsNormalizedError,
      npsError: Option[NpsError]
  ): NpsApiResult[A, B] =
    npsError match {
      case Some(errorResponse) =>
        FailureResult(apiName, ErrorReport(normalizedError, Some(errorResponse)).asInstanceOf[A])
      case None => FailureResult(apiName, ErrorReport(normalizedError, None).asInstanceOf[A])
    }

}
