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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.GYSP
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CursorId, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP

final case class BenefitEligibilityInfoSuccessResponseGysp private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    marriageDetailsResult: FilteredMarriageDetails,
    longTermBenefitCalculationDetailsResult: FilteredLongTermBenefitCalculationDetails,
    schemeMembershipDetailsResult: FilteredSchemeMembershipDetails,
    individualStatePensionInfoResult: FilteredIndividualStatePensionInfo,
    niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
    nextCursor: Option[CursorId]
) extends BenefitEligibilityInfoSuccessResponse

object BenefitEligibilityInfoSuccessResponseGysp {

  implicit val benefitEligibilityInfoResponseGyspWrites: Writes[BenefitEligibilityInfoSuccessResponseGysp] =
    Json.writes[BenefitEligibilityInfoSuccessResponseGysp]

  def apply(
      nationalInsuranceNumber: Identifier,
      marriageDetailsResult: FilteredMarriageDetails,
      longTermBenefitCalculationDetailsResult: FilteredLongTermBenefitCalculationDetails,
      schemeMembershipDetailsResult: FilteredSchemeMembershipDetails,
      individualStatePensionInfoResult: FilteredIndividualStatePensionInfo,
      niContributionsAndCreditsResult: ContributionsAndCreditsResponse,
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
            result.longTermBenefitCalculationDetailsData.longTermBenefitNotesResults.map { case (seqNo, notesResult) =>
              (seqNo, notesResult.getSuccess.get)
            }.toMap
          ),
          schemeMembershipDetailsResult = FilteredSchemeMembershipDetails.from(
            result.benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.get,
            result.benefitSchemeMembershipDetailsData.benefitSchemeDetailsResults.map(_.getSuccess.get)
          ),
          individualStatePensionInfoResult =
            FilteredIndividualStatePensionInfo.from(result.statePensionData.getSuccess.get),
          niContributionsAndCreditsResult =
            ContributionsAndCreditsResponse.from(result.contributionCreditResult.getSuccess.get),
          nextCursor = result.batchId.map(CursorId.from)
        )
      )
    }

}
