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

import io.scalaland.chimney.dsl.into
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor
import uk.gov.hmrc.app.benefitEligibility.service.PaginationResult

object BenefitEligibilityInfoResponse {

  private def toContributionCreditResult(maybeContributionCreditResult: Option[ContributionCreditResult]) =
    maybeContributionCreditResult match {
      case Some(result) =>
        result match {
          case NpsApiResult.FailureResult(apiName, result) => ContributionsAndCreditsResponse(None, Nil, Nil)
          case NpsApiResult.SuccessResult(apiName, successResponse) =>
            ContributionsAndCreditsResponse.from(successResponse)
        }
      case None => ContributionsAndCreditsResponse(None, Nil, Nil)
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
        case PaginationType.BspSearchLightPagination =>
          Right(
            BenefitEligibilityInfoSuccessResponseSearchLight(
              BenefitType.from(paginationResult.paginationType),
              paginationResult.nationalInsuranceNumber,
              toContributionCreditResult(paginationResult.contributionCreditResult.contributionCreditResult),
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
