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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.mapper

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.MarriageDetails
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError
import NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.MarriageDetailsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsResponseMapper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.{
  MarriageDetailsError,
  MarriageDetailsResponse
}

class MarriageDetailsResponseMapper extends NpsResponseMapper[MarriageDetailsResponse, MarriageDetailsResult] {

  def toApiResult(response: MarriageDetailsResponse): MarriageDetailsResult =
    response match {
      case MarriageDetailsError.MarriageDetailsErrorResponse400(failures) =>
        DownstreamErrorReport(MarriageDetails, BadRequest)
      case MarriageDetailsError.MarriageDetailsErrorResponse403(reason, code) =>
        DownstreamErrorReport(MarriageDetails, AccessForbidden)
      case MarriageDetailsError.MarriageDetailsErrorResponse422(failures) =>
        DownstreamErrorReport(MarriageDetails, UnprocessableEntity)
      case response: MarriageDetailsSuccessResponse => DownstreamSuccessResponse(MarriageDetails, response)
    }

}
