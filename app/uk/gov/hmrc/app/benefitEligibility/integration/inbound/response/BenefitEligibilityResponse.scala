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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound.response

import io.scalaland.chimney.dsl.into
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.common.OverallResultStatus.Success
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.BenefitSchemeDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  ApiResult,
  EligibilityCheckDataResult,
  NpsApiResponseStatus
}

case class BenefitEligibilityInfoRequestKey(
    `type`: BenefitType,
    nationalInsuranceNumber: Identifier
)

object BenefitEligibilityInfoRequestKey {

  def apply(eligibilityCheckDataRequest: EligibilityCheckDataRequest) = new BenefitEligibilityInfoRequestKey(
    eligibilityCheckDataRequest.benefitType,
    eligibilityCheckDataRequest.nationalInsuranceNumber
  )

}

sealed trait BenefitEligibilityInfoResponse { self =>
  def benefitType: BenefitType
  def correlationId: CorrelationId
  def nationalInsuranceNumber: Identifier
}

object BenefitEligibilityInfoResponse {

  implicit val benefitEligibilityInfoResponseWrites: Writes[BenefitEligibilityInfoResponse] = Writes {
    case r: BenefitEligibilityInfoResponseMa =>
      BenefitEligibilityInfoResponseMa.benefitEligibilityInfoResponseMaWrites.writes(r)
    case r: BenefitEligibilityInfoResponseBsp =>
      BenefitEligibilityInfoResponseBsp.benefitEligibilityInfoResponseBspWrites.writes(r)
    case r: BenefitEligibilityInfoResponseEsa =>
      BenefitEligibilityInfoResponseEsa.benefitEligibilityInfoResponseEsaWrites.writes(r)
    case r: BenefitEligibilityInfoResponseJsa =>
      BenefitEligibilityInfoResponseJsa.benefitEligibilityInfoResponseJsaWrites.writes(r)
    case r: BenefitEligibilityInfoResponseGysp =>
      BenefitEligibilityInfoResponseGysp.benefitEligibilityInfoResponseGyspWrites.writes(r)
    case r: BenefitEligibilityInfoErrorResponse =>
      BenefitEligibilityInfoErrorResponse.benefitEligibilityInfoErrorResponseWrites.writes(r)
  }

  def from(
      result: EligibilityCheckDataResult,
      correlationId: CorrelationId,
      requestKey: BenefitEligibilityInfoRequestKey
  ): BenefitEligibilityInfoResponse =
    if (OverallResultStatus.fromApiResults(result.allResults) == Success) {
      toSuccessResponse(result, correlationId, requestKey)
    } else BenefitEligibilityInfoErrorResponse.from(result, correlationId, requestKey)

  // TODO convert result to responses here
  private def toSuccessResponse(
      eligibilityCheckDataResult: EligibilityCheckDataResult,
      correlationId: CorrelationId,
      requestKey: BenefitEligibilityInfoRequestKey
  ): BenefitEligibilityInfoResponse =
    eligibilityCheckDataResult match {
      case result: EligibilityCheckDataResultMA =>
        BenefitEligibilityInfoResponseMa.from(result, correlationId, requestKey)
      case EligibilityCheckDataResult.EligibilityCheckDataResultESA(_) =>
        BenefitEligibilityInfoResponseEsa(correlationId, requestKey.nationalInsuranceNumber, List())
      case EligibilityCheckDataResult.EligibilityCheckDataResultJSA(_) =>
        BenefitEligibilityInfoResponseJsa(correlationId, requestKey.nationalInsuranceNumber, List())
      case EligibilityCheckDataResult.EligibilityCheckDataResultGYSP(_, _, _, _, _, _) =>
        BenefitEligibilityInfoResponseJsa(correlationId, requestKey.nationalInsuranceNumber, List())
      case EligibilityCheckDataResult.EligibilityCheckDataResultBSP(_, marriageDetailsResult) =>
        BenefitEligibilityInfoResponseJsa(correlationId, requestKey.nationalInsuranceNumber, List())
    }

}

case class BenefitEligibilityInfoResponseMa(
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    class2MAReceipts: Class2MAReceiptsSuccessResponse,
    liabilitySummaryDetails: LiabilitySummaryDetailsSuccessResponse,
    niContributionsAndCredits: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse {
  def benefitType: BenefitType = MA

}

object BenefitEligibilityInfoResponseMa {

  implicit val benefitEligibilityInfoResponseMaWrites: Writes[BenefitEligibilityInfoResponseMa] =
    Json.writes[BenefitEligibilityInfoResponseMa]

  def from(
      result: EligibilityCheckDataResultMA,
      correlationId: CorrelationId,
      requestKey: BenefitEligibilityInfoRequestKey
  ) =
    BenefitEligibilityInfoResponseMa(
      correlationId = correlationId,
      nationalInsuranceNumber = requestKey.nationalInsuranceNumber,
      niContributionsAndCredits = result.contributionCreditResult.getSuccess.get,
      class2MAReceipts = result.class2MaReceiptsResult.getSuccess.get,
      liabilitySummaryDetails = result.liabilityResult.getSuccess.get
    )

}

case class BenefitEligibilityInfoResponseBsp(
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    marriageDetails: MarriageDetailsSuccessResponse,
    niContributionsAndCredits: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse {
  def benefitType: BenefitType = BSP
}

object BenefitEligibilityInfoResponseBsp {

  implicit val benefitEligibilityInfoResponseBspWrites: Writes[BenefitEligibilityInfoResponseBsp] =
    Json.writes[BenefitEligibilityInfoResponseBsp]

}

case class BenefitEligibilityInfoResponseEsa(
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse {
  def benefitType: BenefitType = ESA
}

object BenefitEligibilityInfoResponseEsa {

  implicit val benefitEligibilityInfoResponseEsaWrites: Writes[BenefitEligibilityInfoResponseEsa] =
    Json.writes[BenefitEligibilityInfoResponseEsa]

}

case class BenefitEligibilityInfoResponseJsa(
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse {
  def benefitType: BenefitType = JSA
}

object BenefitEligibilityInfoResponseJsa {

  implicit val benefitEligibilityInfoResponseJsaWrites: Writes[BenefitEligibilityInfoResponseJsa] =
    Json.writes[BenefitEligibilityInfoResponseJsa]

}

case class BenefitEligibilityInfoResponseGysp(
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    benefitSchemeDetails: List[BenefitSchemeDetailsSuccessResponse],
    marriageDetails: MarriageDetailsSuccessResponse,
    longTermBenefitNotes: LongTermBenefitNotesSuccessResponse,
    schemeMembershipDetails: SchemeMembershipDetailsSuccessResponse,
    statePensionData: IndividualStatePensionInformationSuccessResponse,
    niContributionsAndCredits: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse {
  def benefitType: BenefitType = GYSP
}

object BenefitEligibilityInfoResponseGysp {

  implicit val benefitEligibilityInfoResponseGyspWrites: Writes[BenefitEligibilityInfoResponseGysp] =
    Json.writes[BenefitEligibilityInfoResponseGysp]

}

case class OverallResultSummary(totalCalls: Int, successful: Int, failed: Int)

object OverallResultSummary {
  implicit val writes: Writes[OverallResultSummary] = Json.writes[OverallResultSummary]

  def from(allResults: List[ApiResult]): OverallResultSummary = OverallResultSummary(
    totalCalls = allResults.size,
    successful = allResults.count(_.isSuccess),
    failed = allResults.count(_.isFailure)
  )

}

case class SanitizedApiResult(
    apiName: ApiName,
    status: NpsApiResponseStatus,
    error: Option[NpsNormalizedError]
)

object SanitizedApiResult {
  implicit val sanitizedSuccessApiResult: Writes[SanitizedApiResult] = Json.writes[SanitizedApiResult]
}

case class BenefitEligibilityInfoErrorResponse(
    overallResultStatus: OverallResultStatus,
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    benefitType: BenefitType,
    summary: OverallResultSummary,
    downStreams: List[SanitizedApiResult]
) extends BenefitEligibilityInfoResponse

object BenefitEligibilityInfoErrorResponse {

  implicit val benefitEligibilityInfoErrorResponseWrites: Writes[BenefitEligibilityInfoErrorResponse] =
    Json.writes[BenefitEligibilityInfoErrorResponse]

  def from(
      eligibilityCheckDataResult: EligibilityCheckDataResult,
      correlationId: CorrelationId,
      requestKey: BenefitEligibilityInfoRequestKey
  ): BenefitEligibilityInfoErrorResponse =

    eligibilityCheckDataResult
      .into[BenefitEligibilityInfoErrorResponse]
      .withFieldComputed(_.overallResultStatus, r => OverallResultStatus.fromApiResults(r.allResults))
      .withFieldConst(_.correlationId, correlationId)
      .withFieldConst(_.nationalInsuranceNumber, requestKey.nationalInsuranceNumber)
      .withFieldComputed(_.benefitType, _.benefitType)
      .withFieldComputed(_.summary, result => OverallResultSummary.from(result.allResults))
      .withFieldComputed(
        _.downStreams,
        result =>
          result.allResults
            .filter(_.isFailure)
            .map(
              _.into[SanitizedApiResult]
                .withFieldComputed(_.apiName, _.apiName)
                .withFieldComputed(
                  _.status,
                  result => if (result.isSuccess) NpsApiResponseStatus.Success else NpsApiResponseStatus.Failure
                )
                .withFieldComputed(_.error, result => result.getFailure.map(_.normalizedError))
                .transform
            )
      )
      .transform

}
