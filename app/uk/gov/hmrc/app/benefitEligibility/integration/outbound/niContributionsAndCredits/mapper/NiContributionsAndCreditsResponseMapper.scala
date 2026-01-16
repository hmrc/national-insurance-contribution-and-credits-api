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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.mapper

import io.scalaland.chimney.dsl.into
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.{
  NiContributionsAndCreditsError,
  NiContributionsAndCreditsResponse
}

class NiContributionsAndCreditsResponseMapper
    extends NpsResponseMapper[NiContributionsAndCreditsResponse, ContributionCreditResult] {

  def toApiResult(
      response: NiContributionsAndCreditsResponse
  ): ContributionCreditResult =
    response match {
      case NiContributionsAndCreditsError.NiContributionsAndCreditsResponse400(failures) =>
        DownstreamErrorReport(NiContributionAndCredits, BadRequest)
      case NiContributionsAndCreditsError.NiContributionsAndCreditsResponse403(reason, code) =>
        DownstreamErrorReport(NiContributionAndCredits, AccessForbidden)
      case NiContributionsAndCreditsError.NiContributionsAndCreditsResponse422(failures) =>
        DownstreamErrorReport(NiContributionAndCredits, UnprocessableEntity)
      case response: NiContributionsAndCreditsSuccessResponse =>
        DownstreamSuccessResponse(NiContributionAndCredits, response)
    }

}
