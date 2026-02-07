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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*

sealed trait EligibilityCheckDataResult {
  def benefitType: BenefitType
  def allResults: List[ApiResult]
}

object EligibilityCheckDataResult {

  case class EligibilityCheckDataResultMA(
      class2MaReceiptsResult: Class2MaReceiptsResult,
      liabilityResult: LiabilityResult,
      contributionCreditResult: ContributionCreditResult
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.MA

    def allResults: List[ApiResult] =
      List(contributionCreditResult, class2MaReceiptsResult, liabilityResult)

  }

  case class EligibilityCheckDataResultESA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    def benefitType: BenefitType    = BenefitType.ESA
    def allResults: List[ApiResult] = List(contributionCreditResult)
  }

  case class EligibilityCheckDataResultJSA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    def benefitType: BenefitType             = BenefitType.JSA
    override def allResults: List[ApiResult] = List(contributionCreditResult)
  }

  case class EligibilityCheckDataResultGYSP(
      contributionCreditResult: List[ContributionCreditResult],
      schemeMembershipDetails: SchemeMembershipDetailsResult,
      benefitSchemeDetails: List[BenefitSchemeDetailsResult],
      longTermBenefitCalculationDetailsResult: LongTermBenefitCalculationDetailsResult,
      longTermBenefitNotes: List[LongTermBenefitNotesResult],
      marriageDetailsResult: MarriageDetailsResult,
      statePensionData: IndividualStatePensionResult
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.GYSP

    override def allResults: List[ApiResult] =
      contributionCreditResult ++ benefitSchemeDetails ++ longTermBenefitNotes ++ List(
        marriageDetailsResult,
        longTermBenefitCalculationDetailsResult,
        schemeMembershipDetails,
        statePensionData
      )

  }

  case class EligibilityCheckDataResultBSP(
      contributionCreditResult: ContributionCreditResult,
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    def benefitType: BenefitType = BenefitType.BSP

    override def allResults: List[ApiResult] = List(marriageDetailsResult, contributionCreditResult)
  }

}
