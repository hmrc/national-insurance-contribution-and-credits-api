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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.mapper

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.LongTermBenefitNotes
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{LongTermBenefitNotesResult, NpsResponseMapper}

class LongTermBenefitNotesResponseMapper
    extends NpsResponseMapper[LongTermBenefitNotesResponse, LongTermBenefitNotesResult] {

  def toApiResult(response: LongTermBenefitNotesResponse): LongTermBenefitNotesResult =
    response match {
      case _: LongTermBenefitNotesError.LongTermBenefitNotesErrorResponse400 =>
        DownstreamErrorReport(LongTermBenefitNotes, BadRequest)
      case _: LongTermBenefitNotesError.LongTermBenefitNotesErrorResponse403 =>
        DownstreamErrorReport(LongTermBenefitNotes, AccessForbidden)
      case _: LongTermBenefitNotesError.LongTermBenefitNotesErrorResponse404 =>
        DownstreamErrorReport(LongTermBenefitNotes, NotFound)
      case _: LongTermBenefitNotesError.LongTermBenefitNotesErrorResponse422 =>
        DownstreamErrorReport(LongTermBenefitNotes, UnprocessableEntity)
      case _: LongTermBenefitNotesError.LongTermBenefitNotesHipFailureResponse500 =>
        DownstreamErrorReport(LongTermBenefitNotes, InternalServerError)
      case _: LongTermBenefitNotesError.LongTermBenefitNotesHipFailureResponse503 =>
        DownstreamErrorReport(LongTermBenefitNotes, ServiceUnavailable)
      case response: LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse =>
        DownstreamSuccessResponse(LongTermBenefitNotes, response)
    }

}
