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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound

import io.scalaland.chimney.dsl.into
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ApiResult, NpsApiResponseStatus, NpsError}

case class SanitizedApiResult(
    apiName: ApiName,
    status: NpsApiResponseStatus,
    error: Option[NpsError]
)

object SanitizedApiResult {
  implicit val sanitizedSuccessApiResult: Writes[SanitizedApiResult] = Json.writes[SanitizedApiResult]
}

case class FailureSummaryResponse(
    overallResultStatus: OverallResultStatus,
    correlationId: CorrelationId,
    benefitType: BenefitType,
    summary: OverallResultSummary,
    downStreams: List[SanitizedApiResult]
)

object FailureSummaryResponse {
  implicit val writes: Writes[FailureSummaryResponse] = Json.writes[FailureSummaryResponse]

  def from(
      benefitEligibilityDataFetchError: BenefitEligibilityDataFetchError,
      correlationId: CorrelationId
  ): FailureSummaryResponse =
    benefitEligibilityDataFetchError
      .into[FailureSummaryResponse]
      .withFieldConst(_.correlationId, correlationId)
      .withFieldComputed(_.summary, _.overallResultSummary)
      .withFieldComputed(
        _.downStreams,
        _.downStreams.map(
          _.into[SanitizedApiResult]
            .withFieldComputed(_.apiName, _.apiName)
            .withFieldComputed(_.status, _.status)
            .withFieldComputed(_.error, _.error)
            .transform
        )
      )
      .transform

}
