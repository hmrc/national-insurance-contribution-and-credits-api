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

import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  NotFound,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}

trait NpsResponseMapperV2[
    A <: NpsSuccessfulApiResponse,
    B <: NpsApiResult[NpsNormalizedError, NpsSuccessfulApiResponse]
] {

  def apiName: ApiName
  def toApiResult(response: A): DownstreamSuccessResponse[A] = DownstreamSuccessResponse(apiName, response)

  def toApiResult(npsError: NpsError): DownstreamErrorReport =
    npsError match {
      case _: NpsErrorResponse400            => DownstreamErrorReport(apiName, BadRequest)
      case NpsErrorResponse403(code, reason) => DownstreamErrorReport(apiName, AccessForbidden)
      case NpsErrorResponse404(code, reason) => DownstreamErrorReport(apiName, NotFound)
      case NpsErrorResponse422(_)            => DownstreamErrorReport(apiName, UnprocessableEntity)
    }

}
