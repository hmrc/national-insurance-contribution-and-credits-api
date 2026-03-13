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

package uk.gov.hmrc.app.benefitEligibility.model.nps

import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitSchemeMembershipDetailsData,
  LongTermBenefitCalculationDetailsData
}

sealed trait EligibilityCheckDataResult {
  def benefitType: BenefitType
  def allResults: List[ApiResult]
  def shouldPaginate: Boolean
}

object EligibilityCheckDataResult {

  case class EligibilityCheckDataResultMA(
      class2MaReceiptsResult: Class2MaReceiptsResult,
      liabilityResult: List[LiabilityResult],
      contributionCreditResult: ContributionCreditResult,
      nextCursor: Option[PaginationCursor]
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.MA

    def allResults: List[ApiResult] =
      liabilityResult ++ List(contributionCreditResult, class2MaReceiptsResult)

    def shouldPaginate: Boolean =
      if (allResults.exists(_.isFailure))
        false
      else
        liabilityResult.forall(_.getSuccess.get.callback.isDefined)

  }

  case class EligibilityCheckDataResultESA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    def benefitType: BenefitType    = BenefitType.ESA
    def allResults: List[ApiResult] = List(contributionCreditResult)

    def shouldPaginate: Boolean = false
  }

  case class EligibilityCheckDataResultJSA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    def benefitType: BenefitType             = BenefitType.JSA
    override def allResults: List[ApiResult] = List(contributionCreditResult)
    def shouldPaginate: Boolean              = false
  }

  case class EligibilityCheckDataResultGYSP(
      contributionCreditResult: ContributionCreditResult,
      benefitSchemeMembershipDetailsData: BenefitSchemeMembershipDetailsData,
      longTermBenefitCalculationDetailsData: LongTermBenefitCalculationDetailsData,
      marriageDetailsResult: MarriageDetailsResult,
      statePensionData: IndividualStatePensionResult,
      nextCursor: Option[PaginationCursor]
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.GYSP

    override def allResults: List[ApiResult] =
      benefitSchemeMembershipDetailsData.benefitSchemeDetailsResults ++ longTermBenefitCalculationDetailsData.longTermBenefitNotesResults ++ List(
        contributionCreditResult,
        marriageDetailsResult,
        benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult,
        longTermBenefitCalculationDetailsData.longTermBenefitCalculationDetailsResult,
        statePensionData
      )

    def shouldPaginate: Boolean =
      if (allResults.exists(_.isFailure))
        false
      else {
        marriageDetailsResult.getSuccess.get.marriageDetails._links.isDefined ||
        benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.get.callback.isDefined
      }

  }

  case class EligibilityCheckDataResultBSP(
      contributionCreditResult: ContributionCreditResult,
      marriageDetailsResult: MarriageDetailsResult,
      nextCursor: Option[PaginationCursor]
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.BSP

    override def allResults: List[ApiResult] = List(marriageDetailsResult, contributionCreditResult)

    def shouldPaginate: Boolean =
      if (allResults.exists(_.isFailure))
        false
      else {
        marriageDetailsResult.getSuccess.get.marriageDetails._links.isDefined
      }

  }

  case class EligibilityCheckDataResultBSPS(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.BSP_SEARCHLIGHT

    def allResults: List[ApiResult] = List(contributionCreditResult)

    def shouldPaginate: Boolean = false
  }

}
