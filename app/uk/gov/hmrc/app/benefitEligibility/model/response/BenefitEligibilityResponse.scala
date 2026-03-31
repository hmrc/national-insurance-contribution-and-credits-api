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

package uk.gov.hmrc.app.benefitEligibility.model.response

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import io.scalaland.chimney.dsl.into
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.findValues
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.npsNormalizedErrorWrites
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult

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
    case r: BenefitEligibilityInfoSuccessResponseSearchLight =>
      BenefitEligibilityInfoSuccessResponseSearchLight.benefitEligibilityInfoResponseBspSearchlightWrites.writes(r)
  }

}

sealed trait BenefitEligibilityInfoResponse { self =>
  def benefitType: BenefitType
  def nationalInsuranceNumber: Identifier
}

object BenefitEligibilityInfoResponse {

  private def toContributionCreditResult(maybeContributionCreditResult: Option[ContributionCreditResult]) =
    maybeContributionCreditResult match {
      case Some(result) =>
        result match {
          case NpsApiResult.FailureResult(apiName, result) => NiContributionsAndCreditsSuccessResponse(None, None, None)
          case NpsApiResult.SuccessResult(apiName, successResponse) => successResponse
        }
      case None => NiContributionsAndCreditsSuccessResponse(None, None, None)
    }

  def from(
      paginationResult: PaginationResult
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponse] =
    if (paginationResult.allResults.exists(_.isFailure))
      Left(
        BenefitEligibilityInfoErrorResponse
          .from(
            BenefitType.from(paginationResult.paginationType),
            paginationResult.nationalInsuranceNumber,
            paginationResult.allResults
          )
      )
    else
      paginationResult.paginationType match {
        case PaginationType.MaPagination =>
          Right(
            BenefitEligibilityInfoSuccessResponseMa(
              paginationResult.nationalInsuranceNumber,
              FilteredClass2MaReceipts(Nil),
              paginationResult.liabilitiesResult.map(toFilteredLiabilitySummaryDetails),
              toContributionCreditResult(paginationResult.contributionCreditResult.contributionCreditResult),
              paginationResult.getNextCursor.map(CursorId.from)
            )
          )

        case PaginationType.GyspPagination =>

          val filteredMarriageDetails: FilteredMarriageDetails = getFilteredMarriageDetails(paginationResult)
          val filteredSchemeMembershipDetails                  = getFilteredSchemeMembershipDetails(paginationResult)

          Right(
            BenefitEligibilityInfoSuccessResponseGysp(
              nationalInsuranceNumber = paginationResult.nationalInsuranceNumber,
              marriageDetailsResult = filteredMarriageDetails,
              longTermBenefitCalculationDetailsResult = FilteredLongTermBenefitCalculationDetails(Nil),
              schemeMembershipDetailsResult = filteredSchemeMembershipDetails,
              individualStatePensionInfoResult = FilteredIndividualStatePensionInfo(None, Nil),
              niContributionsAndCreditsResult =
                toContributionCreditResult(paginationResult.contributionCreditResult.contributionCreditResult),
              paginationResult.getNextCursor.map(CursorId.from)
            )
          )
        case PaginationType.BspPagination =>
          val filteredMarriageDetails: FilteredMarriageDetails = getFilteredMarriageDetails(paginationResult)

          Right(
            BenefitEligibilityInfoSuccessResponseBsp(
              paginationResult.nationalInsuranceNumber,
              toContributionCreditResult(paginationResult.contributionCreditResult.contributionCreditResult),
              filteredMarriageDetails,
              paginationResult.getNextCursor.map(CursorId.from)
            )
          )

      }

  private def getFilteredMarriageDetails(paginationResult: PaginationResult) =
    paginationResult.marriageDetailsResult match {
      case Some(marriageDetailsResult) =>
        marriageDetailsResult match {
          case NpsApiResult.FailureResult(apiName, result) => FilteredMarriageDetails(Nil)
          case NpsApiResult.SuccessResult(apiName, result) => FilteredMarriageDetails.from(result)
        }
      case None => FilteredMarriageDetails(Nil)
    }

  private def getFilteredSchemeMembershipDetails(paginationResult: PaginationResult) = {
    val maybeSchemeMembershipDetails = paginationResult.benefitSchemeMembershipDetailsData

    maybeSchemeMembershipDetails match {
      case Some(benefitSchemeMembershipDetailsData) =>
        benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess match {
          case Some(resp) =>
            FilteredSchemeMembershipDetails.from(
              resp,
              benefitSchemeMembershipDetailsData.benefitSchemeDetailsResults.flatMap(_.getSuccess)
            )
          case None => FilteredSchemeMembershipDetails(Nil)
        }

      case None => FilteredSchemeMembershipDetails(Nil)
    }
  }

  private def toFilteredLiabilitySummaryDetails(liabilityResult: LiabilityResult): FilteredLiabilitySummaryDetails =
    liabilityResult match {
      case NpsApiResult.FailureResult(apiName, result) => FilteredLiabilitySummaryDetails(Nil)
      case NpsApiResult.SuccessResult(apiName, successResponse) =>
        FilteredLiabilitySummaryDetails.from(successResponse)
    }

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
      case r: EligibilityCheckDataResult.EligibilityCheckDataResultSearchLight =>
        BenefitEligibilityInfoSuccessResponseSearchLight.from(nationalInsuranceNumber, r)
    }

}

sealed abstract class PaginationStatus(override val entryName: String) extends EnumEntry

object PaginationStatus extends Enum[PaginationStatus] with PlayJsonEnum[PaginationStatus] {
  val values: immutable.IndexedSeq[PaginationStatus] = findValues

  case object Running  extends PaginationStatus("RUNNING")
  case object Complete extends PaginationStatus("COMPLETE")
}

final case class BenefitEligibilityInfoSuccessResponseMa private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    class2MAReceiptsResult: FilteredClass2MaReceipts,
    liabilitySummaryDetailsResult: List[FilteredLiabilitySummaryDetails],
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseMa {

  implicit val benefitEligibilityInfoResponseMaWrites: Writes[BenefitEligibilityInfoSuccessResponseMa] =
    Json.writes[BenefitEligibilityInfoSuccessResponseMa]

  def apply(
      nationalInsuranceNumber: Identifier,
      class2MAReceiptsResult: FilteredClass2MaReceipts,
      liabilitySummaryDetailsResult: List[FilteredLiabilitySummaryDetails],
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
      nextCursor: Option[CursorId]
  ) = new BenefitEligibilityInfoSuccessResponseMa(
    MA,
    nationalInsuranceNumber,
    class2MAReceiptsResult,
    liabilitySummaryDetailsResult,
    niContributionsAndCreditsResult,
    nextCursor
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultMA
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseMa] =

    if (result.allResults.exists(_.isFailure)) {
      Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    } else {
      Right(
        BenefitEligibilityInfoSuccessResponseMa(
          nationalInsuranceNumber = nationalInsuranceNumber,
          niContributionsAndCreditsResult = result.contributionCreditResult.getSuccess.get,
          class2MAReceiptsResult = FilteredClass2MaReceipts.from(result.class2MaReceiptsResult.getSuccess.get),
          liabilitySummaryDetailsResult =
            result.liabilityResult.map(r => FilteredLiabilitySummaryDetails.from(r.getSuccess.get)),
          nextCursor = result.nextCursor.map(CursorId.from)
        )
      )
    }

}

final case class BenefitEligibilityInfoSuccessResponseBsp private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
    marriageDetailsResult: FilteredMarriageDetails,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseBsp {

  implicit val benefitEligibilityInfoResponseBspWrites: Writes[BenefitEligibilityInfoSuccessResponseBsp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseBsp]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
      marriageDetailsResult: FilteredMarriageDetails,
      nextCursor: Option[CursorId]
  ) = new BenefitEligibilityInfoSuccessResponseBsp(
    BSP,
    nationalInsuranceNumber,
    niContributionsAndCreditsResult,
    marriageDetailsResult,
    nextCursor
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
            niContributionsAndCreditsResult = contributionsAndCreditsSuccessResponse,
            marriageDetailsResult = FilteredMarriageDetails.from(marriageDetailsSuccessResponse),
            nextCursor = result.nextCursor.map(CursorId.from)
          )
        )
      case _ => Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    }

}

final case class BenefitEligibilityInfoSuccessResponseSearchLight(
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseSearchLight {

  implicit val benefitEligibilityInfoResponseBspSearchlightWrites
      : Writes[BenefitEligibilityInfoSuccessResponseSearchLight] =
    Json.writes[BenefitEligibilityInfoSuccessResponseSearchLight]

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultSearchLight
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseSearchLight] =

    result.contributionCreditResult match {
      case (
            NpsApiResult.SuccessResult(_, contributionsAndCreditsSuccessResponse)
          ) =>
        Right(
          BenefitEligibilityInfoSuccessResponseSearchLight(
            result.benefitType,
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = contributionsAndCreditsSuccessResponse,
            result.nextCursor.map(CursorId.from)
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
    marriageDetailsResult: FilteredMarriageDetails,
    longTermBenefitCalculationDetailsResult: FilteredLongTermBenefitCalculationDetails,
    schemeMembershipDetailsResult: FilteredSchemeMembershipDetails,
    individualStatePensionInfoResult: FilteredIndividualStatePensionInfo,
    niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoResponse
    with BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseGysp {

  implicit val benefitEligibilityInfoResponseGyspWrites: Writes[BenefitEligibilityInfoSuccessResponseGysp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseGysp]

  def apply(
      nationalInsuranceNumber: Identifier,
      marriageDetailsResult: FilteredMarriageDetails,
      longTermBenefitCalculationDetailsResult: FilteredLongTermBenefitCalculationDetails,
      schemeMembershipDetailsResult: FilteredSchemeMembershipDetails,
      individualStatePensionInfoResult: FilteredIndividualStatePensionInfo,
      niContributionsAndCreditsResult: NiContributionsAndCreditsSuccessResponse,
      nextCursor: Option[CursorId]
  ) = new BenefitEligibilityInfoSuccessResponseGysp(
    GYSP,
    nationalInsuranceNumber,
    marriageDetailsResult,
    longTermBenefitCalculationDetailsResult,
    schemeMembershipDetailsResult,
    individualStatePensionInfoResult,
    niContributionsAndCreditsResult,
    nextCursor
  )

  def from(
      nationalInsuranceNumber: Identifier,
      result: EligibilityCheckDataResultGYSP
  ): Either[BenefitEligibilityInfoErrorResponse, BenefitEligibilityInfoSuccessResponseGysp] =

    if (result.allResults.exists(_.isFailure)) {
      Left(BenefitEligibilityInfoErrorResponse.from(nationalInsuranceNumber, result))
    } else {
      Right(
        BenefitEligibilityInfoSuccessResponseGysp(
          nationalInsuranceNumber = nationalInsuranceNumber,
          marriageDetailsResult = FilteredMarriageDetails.from(result.marriageDetailsResult.getSuccess.get),
          longTermBenefitCalculationDetailsResult = FilteredLongTermBenefitCalculationDetails.from(
            result.longTermBenefitCalculationDetailsData.longTermBenefitCalculationDetailsResult.getSuccess.get,
            result.longTermBenefitCalculationDetailsData.longTermBenefitNotesResults.map(_.getSuccess.get)
          ),
          schemeMembershipDetailsResult = FilteredSchemeMembershipDetails.from(
            result.benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.get,
            result.benefitSchemeMembershipDetailsData.benefitSchemeDetailsResults.map(_.getSuccess.get)
          ),
          individualStatePensionInfoResult =
            FilteredIndividualStatePensionInfo.from(result.statePensionData.getSuccess.get),
          niContributionsAndCreditsResult = result.contributionCreditResult.getSuccess.get,
          nextCursor = result.nextCursor.map(CursorId.from)
        )
      )
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
    status: OverallResultStatus,
    nationalInsuranceNumber: Identifier,
    benefitType: BenefitType,
    summary: OverallResultSummary,
    downStreams: List[SanitizedApiResult]
) extends BenefitEligibilityInfoResponse

object BenefitEligibilityInfoErrorResponse {

  implicit val benefitEligibilityInfoErrorResponseWrites: Writes[BenefitEligibilityInfoErrorResponse] =
    Json.writes[BenefitEligibilityInfoErrorResponse]

  def from(
      benefitType: BenefitType,
      nationalInsuranceNumber: Identifier,
      allResults: List[ApiResult]
  ): BenefitEligibilityInfoErrorResponse =

    BenefitEligibilityInfoErrorResponse(
      status = OverallResultStatus.fromApiResults(allResults),
      nationalInsuranceNumber = nationalInsuranceNumber,
      benefitType = benefitType,
      summary = OverallResultSummary.from(allResults),
      downStreams = allResults.map { result =>
        SanitizedApiResult(
          result.apiName,
          if (result.isSuccess) NpsApiResponseStatus.Success else NpsApiResponseStatus.Failure,
          result.getFailure.map(_.normalizedError)
        )
      }
    )

  def from(
      nationalInsuranceNumber: Identifier,
      eligibilityCheckDataResult: EligibilityCheckDataResult
  ): BenefitEligibilityInfoErrorResponse = {

    val allResults = eligibilityCheckDataResult.allResults

    eligibilityCheckDataResult
      .into[BenefitEligibilityInfoErrorResponse]
      .withFieldComputed(_.status, _ => OverallResultStatus.fromApiResults(allResults))
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
