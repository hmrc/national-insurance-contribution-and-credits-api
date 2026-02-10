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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import io.scalaland.chimney.dsl.into
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.findValues
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.BenefitSchemeDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.LongTermBenefitCalculationDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  ApiResult,
  EligibilityCheckDataResult,
  NpsApiResponseStatus,
  NpsApiResult
}

import scala.collection.immutable

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
  def nationalInsuranceNumber: Identifier
}

object BenefitEligibilityInfoResponse {

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResult
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponse] =
    result match {
      case r: EligibilityCheckDataResultMA => BenefitEligibilityInfoSuccessResponseMa.from(nationalInsuranceNumber, r)
      case r: EligibilityCheckDataResult.EligibilityCheckDataResultESA =>
        BenefitEligibilityInfoSuccessResponseEsa.from(nationalInsuranceNumber, r)
      case r: EligibilityCheckDataResult.EligibilityCheckDataResultJSA =>
        BenefitEligibilityInfoSuccessResponseJsa.from(nationalInsuranceNumber, r)
      case r: EligibilityCheckDataResult.EligibilityCheckDataResultGYSP =>
        BenefitEligibilityInfoSuccessResponseGysp.from(nationalInsuranceNumber, r)
      case r: EligibilityCheckDataResult.EligibilityCheckDataResultBSP =>
        BenefitEligibilityInfoSuccessResponseBsp.from(nationalInsuranceNumber, r)
    }

}

sealed abstract class PaginationStatus(override val entryName: String) extends EnumEntry

object PaginationStatus extends Enum[PaginationStatus] with PlayJsonEnum[PaginationStatus] {
  val values: immutable.IndexedSeq[PaginationStatus] = findValues

  case object Running  extends PaginationStatus("RUNNING")
  case object Complete extends PaginationStatus("COMPLETE")
}

case class PaginationCursor(value: java.util.UUID)
case class Source(apiName: ApiName, status: PaginationStatus, url: String)
case class Pagination(cursor: PaginationCursor, sources: List[Source])

final case class BenefitEligibilityInfoSuccessResponseMa private (
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
      nationalInsuranceNumber: Identifier,
      class2MAReceiptsResult: Class2MAReceiptsSuccessResponse,
      liabilitySummaryDetailsResult: LiabilitySummaryDetailsSuccessResponse,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) = new BenefitEligibilityInfoSuccessResponseMa(
    MA,
    nationalInsuranceNumber,
    class2MAReceiptsResult,
    liabilitySummaryDetailsResult,
    niContributionsAndCreditsResult
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultMA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseMa] =

    (result.liabilityResult, result.class2MaReceiptsResult, result.contributionCreditResult) match {
      case (NpsApiResult.SuccessResult(_, l), NpsApiResult.SuccessResult(_, c2), NpsApiResult.SuccessResult(_, co)) =>
        Right(
          BenefitEligibilityInfoSuccessResponseMa(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = co,
            class2MAReceiptsResult = c2,
            liabilitySummaryDetailsResult = l
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}

final case class BenefitEligibilityInfoSuccessResponseBsp private (
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
      nationalInsuranceNumber: Identifier,
      marriageDetailsResult: MarriageDetailsSuccessResponse,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) = new BenefitEligibilityInfoSuccessResponseBsp(
    BSP,
    nationalInsuranceNumber,
    marriageDetailsResult,
    niContributionsAndCreditsResult
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultBSP
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseBsp] =

    (result.marriageDetailsResult, result.contributionCreditResult) match {
      case (
            NpsApiResult.SuccessResult(_, marriageDetailsSuccessResponse),
            NpsApiResult.SuccessResult(_, contributionsAndCreditsSuccessResponse)
          ) =>
        Right(
          BenefitEligibilityInfoSuccessResponseBsp(
            nationalInsuranceNumber = nationalInsuranceNumber,
            marriageDetailsResult = marriageDetailsSuccessResponse,
            niContributionsAndCreditsResult = contributionsAndCreditsSuccessResponse
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}

final case class BenefitEligibilityInfoSuccessResponseEsa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseEsa {

  implicit val benefitEligibilityInfoResponseEsaWrites: Writes[BenefitEligibilityInfoSuccessResponseEsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseEsa]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseEsa(
      ESA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultESA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseEsa] =

    result.contributionCreditResult match {
      case NpsApiResult.SuccessResult(_, niContributionsAndCreditsSuccessResponse) =>
        Right(
          BenefitEligibilityInfoSuccessResponseEsa(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}

final case class BenefitEligibilityInfoSuccessResponseJsa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseJsa {

  implicit val benefitEligibilityInfoResponseJsaWrites: Writes[BenefitEligibilityInfoSuccessResponseJsa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseJsa]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse
  ) =
    new BenefitEligibilityInfoSuccessResponseJsa(
      JSA,
      nationalInsuranceNumber,
      niContributionsAndCreditsResult
    )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultJSA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseJsa] =

    result.contributionCreditResult match {
      case NpsApiResult.SuccessResult(_, niContributionsAndCreditsSuccessResponse) =>
        Right(
          BenefitEligibilityInfoSuccessResponseJsa(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}

final case class BenefitEligibilityInfoSuccessResponseGysp private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    benefitSchemeDetailsResult: List[BenefitSchemeDetailsSuccessResponse],
    marriageDetailsResult: MarriageDetailsSuccessResponse,
    longTermBenefitCalculationDetailsResult: LongTermBenefitCalculationDetailsSuccessResponse,
    longTermBenefitNotesResult: LongTermBenefitNotesSuccessResponse,
    schemeMembershipDetailsResult: SchemeMembershipDetailsSuccessResponse,
    individualStatePensionInfoResult: IndividualStatePensionInformationSuccessResponse,
    niContributionsAndCreditsResult: List[NiContributionsAndCreditsSuccessResponse]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseGysp {

  implicit val benefitEligibilityInfoResponseGyspWrites: Writes[BenefitEligibilityInfoSuccessResponseGysp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseGysp]

  def apply(
      nationalInsuranceNumber: Identifier,
      benefitSchemeDetailsResult: List[BenefitSchemeDetailsSuccessResponse],
      marriageDetailsResult: MarriageDetailsSuccessResponse,
      longTermBenefitCalculationDetailsResult: LongTermBenefitCalculationDetailsSuccessResponse,
      longTermBenefitNotesResult: LongTermBenefitNotesSuccessResponse,
      schemeMembershipDetailsResult: SchemeMembershipDetailsSuccessResponse,
      individualStatePensionInfoResult: IndividualStatePensionInformationSuccessResponse,
      niContributionsAndCreditsResult: List[NiContributionsAndCreditsSuccessResponse]
  ) = new BenefitEligibilityInfoSuccessResponseGysp(
    GYSP,
    nationalInsuranceNumber,
    benefitSchemeDetailsResult,
    marriageDetailsResult,
    longTermBenefitCalculationDetailsResult,
    longTermBenefitNotesResult,
    schemeMembershipDetailsResult,
    individualStatePensionInfoResult,
    niContributionsAndCreditsResult
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultGYSP
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseGysp] =

    (
      result.schemeMembershipDetails,
      result.marriageDetailsResult,
      result.longTermBenefitCalculationDetailsResult,
      result.statePensionData
    ) match {
      case (
            NpsApiResult.SuccessResult(_, schemeMembership),
            NpsApiResult.SuccessResult(_, marriageDetails),
            NpsApiResult.SuccessResult(_, longTermBenefitCalculationDetails),
            NpsApiResult.SuccessResult(_, statePension)
          ) =>
        Right(
          BenefitEligibilityInfoSuccessResponseGysp(
            nationalInsuranceNumber,
            result.benefitSchemeDetails.flatMap(_.getSuccess),
            marriageDetails,
            longTermBenefitCalculationDetails,
            result.longTermBenefitNotes.flatMap(_.getSuccess).reduce { case (resp1, resp2) =>
              LongTermBenefitNotesSuccessResponse(resp1.longTermBenefitNotes ++ resp2.longTermBenefitNotes)
            },
            schemeMembership,
            statePension,
            result.contributionCreditResult.flatMap(_.getSuccess)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

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
    nationalInsuranceNumber: Identifier,
    benefitType: BenefitType,
    summary: OverallResultSummary,
    downStreams: List[SanitizedApiResult]
) extends BenefitEligibilityInfoResponse

object BenefitEligibilityInfoErrorResponse {

  implicit val benefitEligibilityInfoErrorResponseWrites: Writes[BenefitEligibilityInfoErrorResponse] =
    Json.writes[BenefitEligibilityInfoErrorResponse]

  def from(
      nationalInsuranceNumber: Identifier,
      eligibilityCheckDataResult: EligibilityCheckDataResult
  ): BenefitEligibilityInfoErrorResponse = {

    val allResults = eligibilityCheckDataResult.allResults

    eligibilityCheckDataResult
      .into[BenefitEligibilityInfoErrorResponse]
      .withFieldComputed(_.overallResultStatus, _ => OverallResultStatus.fromApiResults(allResults))
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
