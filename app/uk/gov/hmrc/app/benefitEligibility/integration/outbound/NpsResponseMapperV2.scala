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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}

trait NpsResponseMapperV2 {

  def apiName: ApiName

  def toApiResult[A <: ErrorReport, B <: NpsSuccessfulApiResponse](response: B): NpsApiResult[A, B] =
    SuccessResult(apiName, response)

  def toApiResult[A <: ErrorReport, B <: NpsSuccessfulApiResponse](npsError: NpsError): NpsApiResult[A, B] = {
    val errorReport =
      npsError match {
        case errorResponse: NpsErrorResponse400 => ErrorReport(BadRequest, Some(errorResponse))
        case errorResponse: NpsErrorResponse403 => ErrorReport(AccessForbidden, Some(errorResponse))
        case errorResponse: NpsErrorResponse404 => ErrorReport(NotFound, Some(errorResponse))
        case errorResponse: NpsErrorResponse422 => ErrorReport(UnprocessableEntity, Some(errorResponse))
        case errorResponse: NpsErrorResponse500 => ErrorReport(InternalServerError, Some(errorResponse))
        case errorResponse: NpsErrorResponse503 => ErrorReport(ServiceUnavailable, Some(errorResponse))
      }

    FailureResult(apiName, errorReport.asInstanceOf[A])

  }

}
