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
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}

trait NpsResponseMapperV2 {

  def apiName: ApiName

  def toApiResult(response: NpsSuccessfulApiResponse): DownstreamSuccessResponse[NpsSuccessfulApiResponse] =
    DownstreamSuccessResponse(apiName, response)

  def toApiResult(npsError: NpsError): DownstreamErrorReport =
    npsError match {
      case _: NpsErrorResponse400 => DownstreamErrorReport(apiName, BadRequest)
      case _: NpsErrorResponse403 => DownstreamErrorReport(apiName, AccessForbidden)
      case _: NpsErrorResponse404 => DownstreamErrorReport(apiName, NotFound)
      case _: NpsErrorResponse422 => DownstreamErrorReport(apiName, UnprocessableEntity)
      case _: NpsErrorResponse500 => DownstreamErrorReport(apiName, InternalServerError)
      case _: NpsErrorResponse503 => DownstreamErrorReport(apiName, ServiceUnavailable)
    }

}
