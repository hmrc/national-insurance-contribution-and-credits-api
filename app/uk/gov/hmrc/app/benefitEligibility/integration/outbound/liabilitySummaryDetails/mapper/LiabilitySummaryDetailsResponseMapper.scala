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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.mapper

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ApiResult, LiabilityResult, NpsResponseMapper}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse

class LiabilitySummaryDetailsResponseMapper
    extends NpsResponseMapper[LiabilitySummaryDetailsResponse, LiabilityResult] {

  def toApiResult(response: LiabilitySummaryDetailsResponse): LiabilityResult =
    response match {
      case LiabilitySummaryDetailsErrorResponse400(failures) =>
        DownstreamErrorReport(Liabilities, BadRequest)
      case LiabilitySummaryDetailsErrorResponse403(reason, code) =>
        DownstreamErrorReport(Liabilities, AccessForbidden)
      case LiabilitySummaryDetailsErrorResponse422(failures, askUser, fixRequired, workItemRaised) =>
        DownstreamErrorReport(Liabilities, UnprocessableEntity)
      case response: LiabilitySummaryDetailsSuccessResponse => DownstreamSuccessResponse(Liabilities, response)
    }

}
