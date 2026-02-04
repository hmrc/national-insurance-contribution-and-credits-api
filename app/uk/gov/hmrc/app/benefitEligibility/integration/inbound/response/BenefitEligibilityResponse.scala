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
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.common.OverallResultStatus.Success
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataSuccessResult.*
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
  EligibilityCheckDataSuccessResult,
  NpsApiResponseStatus
}

trait BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponse {

  implicit val benefitEligibilityInfoSuccessResponseWrites: Writes[BenefitEligibilityInfoSuccessResponse] = Writes {
    case r: BenefitEligibilityInfoSuccessResponseMa =>
      BenefitEligibilityInfoSuccessResponseMa.benefitEligibilityInfoResponseMaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseBsp =>
      BenefitEligibilityInfoSuccessResponseBsp.benefitEligibilityInfoResponseBspWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseEsa =>
      BenefitEligibilityInfoSuccessResponseEsa.benefitEligibilityInfoResponseEsaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseJsa =>
      BenefitEligibilityInfoSuccessResponseJsa.benefitEligibilityInfoResponseJsaWrites.writes(r)
    case r: BenefitEligibilityInfoSuccessResponseGysp =>
      BenefitEligibilityInfoSuccessResponseGysp.benefitEligibilityInfoResponseGyspWrites.writes(r)
  }

}

sealed trait BenefitEligibilityInfoResponse { self =>
  def benefitType: BenefitType
  def correlationId: CorrelationId
  def nationalInsuranceNumber: Identifier
}

object BenefitEligibilityInfoResponse {

  def from(
      result: EligibilityCheckDataResult,
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponse] =
    if (OverallResultStatus.fromApiResults(result.allResults) == Success) {
      Right(toSuccessResponse(result.asSuccess.get, correlationId, nationalInsuranceNumber))
    } else
      Left(BenefitEligibilityInfoErrorResponse.from(result, correlationId, nationalInsuranceNumber))

  private def toSuccessResponse(
      successResult: EligibilityCheckDataSuccessResult,
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier
  ): BenefitEligibilityInfoSuccessResponse =
    successResult match {
      case result: EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultMa =>
        BenefitEligibilityInfoSuccessResponseMa.from(correlationId, nationalInsuranceNumber, result)

      case result: EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultEsa =>
        BenefitEligibilityInfoSuccessResponseEsa.from(correlationId, nationalInsuranceNumber, result)

      case result: EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultJsa =>
        BenefitEligibilityInfoSuccessResponseJsa.from(correlationId, nationalInsuranceNumber, result)

      case result: EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultGysp =>
        BenefitEligibilityInfoSuccessResponseGysp.from(correlationId, nationalInsuranceNumber, result)

      case result: EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultBsp =>
        BenefitEligibilityInfoSuccessResponseBsp.from(correlationId, nationalInsuranceNumber, result)
    }

}

final case class BenefitEligibilityInfoSuccessResponseMa private (
    correlationId: CorrelationId,
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    class2MAReceiptsResult: Class2MAReceiptsSuccessResponse,
    liabilitySummaryDetailsResult: LiabilitySummaryDetailsSuccessResponse,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseMa {

  implicit val benefitEligibilityInfoResponseMaWrites: Writes[BenefitEligibilityInfoSuccessResponseMa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseMa]

  def apply(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      class2MAReceiptsResult: Class2MAReceiptsSuccessResponse,
      liabilitySummaryDetailsResult: LiabilitySummaryDetailsSuccessResponse,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) = new BenefitEligibilityInfoSuccessResponseMa(
    correlationId,
    MA,
    nationalInsuranceNumber,
    class2MAReceiptsResult,
    liabilitySummaryDetailsResult,
    niContributionsAndCreditsResult
  )

  def from(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataSuccessResultMa
  ) =
    BenefitEligibilityInfoSuccessResponseMa(
      correlationId = correlationId,
      nationalInsuranceNumber = nationalInsuranceNumber,
      niContributionsAndCreditsResult = result.niContributionsAndCreditsSuccessResponse,
      class2MAReceiptsResult = result.class2MAReceiptsSuccessResponse,
      liabilitySummaryDetailsResult = result.liabilitySummaryDetailsSuccessResponse
    )

}

final case class BenefitEligibilityInfoSuccessResponseBsp private (
    correlationId: CorrelationId,
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    marriageDetailsResult: MarriageDetailsSuccessResponse,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseBsp {

  implicit val benefitEligibilityInfoResponseBspWrites: Writes[BenefitEligibilityInfoSuccessResponseBsp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseBsp]

  def apply(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      marriageDetailsResult: MarriageDetailsSuccessResponse,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) = new BenefitEligibilityInfoSuccessResponseBsp(
    correlationId,
    BSP,
    nationalInsuranceNumber,
    marriageDetailsResult,
    niContributionsAndCreditsResult
  )

  def from(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataSuccessResultBsp
  ) =
    BenefitEligibilityInfoSuccessResponseBsp(
      correlationId = correlationId,
      nationalInsuranceNumber = nationalInsuranceNumber,
      marriageDetailsResult = result.marriageDetailsSuccessResponse,
      niContributionsAndCreditsResult = result.niContributionsAndCreditsSuccessResponse
    )

}

final case class BenefitEligibilityInfoSuccessResponseEsa private (
    correlationId: CorrelationId,
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseEsa {

  implicit val benefitEligibilityInfoResponseEsaWrites: Writes[BenefitEligibilityInfoSuccessResponseEsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseEsa]

  def apply(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseEsa(
      correlationId,
      ESA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataSuccessResultEsa
  ): BenefitEligibilityInfoSuccessResponseEsa =
    BenefitEligibilityInfoSuccessResponseEsa(
      correlationId = correlationId,
      nationalInsuranceNumber = nationalInsuranceNumber,
      niContributionsAndCreditsResult = result.niContributionsAndCreditsSuccessResponse
    )

}

final case class BenefitEligibilityInfoSuccessResponseJsa private (
    correlationId: CorrelationId,
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseJsa {

  implicit val benefitEligibilityInfoResponseJsaWrites: Writes[BenefitEligibilityInfoSuccessResponseJsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseJsa]

  def apply(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseJsa(
      correlationId,
      JSA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataSuccessResultJsa
  ) =
    BenefitEligibilityInfoSuccessResponseJsa(
      correlationId = correlationId,
      nationalInsuranceNumber = nationalInsuranceNumber,
      niContributionsAndCreditsResult = result.niContributionsAndCreditsSuccessResponse
    )

}

final case class BenefitEligibilityInfoSuccessResponseGysp private (
    benefitType: BenefitType,
    correlationId: CorrelationId,
    nationalInsuranceNumber: Identifier,
    benefitSchemeDetailsResult: List[BenefitSchemeDetailsSuccessResponse],
    marriageDetailsResult: MarriageDetailsSuccessResponse,
    longTermBenefitNotesResult: LongTermBenefitNotesSuccessResponse,
    schemeMembershipDetailsResult: SchemeMembershipDetailsSuccessResponse,
    individualStatePensionInfoResult: IndividualStatePensionInformationSuccessResponse,
    niContributionsAndCreditsResult: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseGysp {

  def apply(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      benefitSchemeDetailsResult: List[BenefitSchemeDetailsSuccessResponse],
      marriageDetailsResult: MarriageDetailsSuccessResponse,
      longTermBenefitNotesResult: LongTermBenefitNotesSuccessResponse,
      schemeMembershipDetailsResult: SchemeMembershipDetailsSuccessResponse,
      individualStatePensionInfoResult: IndividualStatePensionInformationSuccessResponse,
      niContributionsAndCreditsResult: List[NiContributionsAndCreditsSuccessResponse]
  ) = new BenefitEligibilityInfoSuccessResponseGysp(
    GYSP,
    correlationId,
    nationalInsuranceNumber,
    benefitSchemeDetailsResult,
    marriageDetailsResult,
    longTermBenefitNotesResult,
    schemeMembershipDetailsResult,
    individualStatePensionInfoResult,
    niContributionsAndCreditsResult
  )

  implicit val benefitEligibilityInfoResponseGyspWrites: Writes[BenefitEligibilityInfoSuccessResponseGysp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseGysp]

  def from(
      correlationId: CorrelationId,
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataSuccessResultGysp
  ) =
    BenefitEligibilityInfoSuccessResponseGysp(
      correlationId = correlationId,
      nationalInsuranceNumber = nationalInsuranceNumber,
      benefitSchemeDetailsResult = result.benefitSchemeDetails,
      marriageDetailsResult = result.marriageDetails,
      longTermBenefitNotesResult = result.longTermBenefitNotes,
      schemeMembershipDetailsResult = result.schemeMembershipDetails,
      individualStatePensionInfoResult = result.statePensionData,
      niContributionsAndCreditsResult = result.contributionCredit
    )

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
      nationalInsuranceNumber: Identifier
  ): BenefitEligibilityInfoErrorResponse = {

    val allResults = eligibilityCheckDataResult.allResults

    eligibilityCheckDataResult
      .into[BenefitEligibilityInfoErrorResponse]
      .withFieldComputed(_.overallResultStatus, _ => OverallResultStatus.fromApiResults(allResults))
      .withFieldConst(_.correlationId, correlationId)
      .withFieldConst(_.nationalInsuranceNumber, nationalInsuranceNumber)
      .withFieldComputed(_.benefitType, _.benefitType)
      .withFieldComputed(_.summary, _ => OverallResultSummary.from(allResults))
      .withFieldComputed(
        _.downStreams,
        _ =>
          allResults
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

}
