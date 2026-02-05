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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataSuccessResult.{
  EligibilityCheckDataSuccessResultBsp,
  EligibilityCheckDataSuccessResultEsa,
  EligibilityCheckDataSuccessResultGysp,
  EligibilityCheckDataSuccessResultJsa,
  EligibilityCheckDataSuccessResultMa
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess

trait EligibilityCheckDataResult {
  def benefitType: BenefitType
  def allResults: List[ApiResult]

  def asSuccess: Option[EligibilityCheckDataSuccessResult]
}

object EligibilityCheckDataResult {

  case class EligibilityCheckDataResultMA(
      class2MaReceiptsResult: Class2MaReceiptsResult,
      liabilityResult: LiabilityResult,
      contributionCreditResult: ContributionCreditResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType = BenefitType.MA

    val allResults: List[ApiResult] =
      List(contributionCreditResult, class2MaReceiptsResult, liabilityResult)

    def asSuccess: Option[EligibilityCheckDataSuccessResultMa] =
      (class2MaReceiptsResult.getSuccess, liabilityResult.getSuccess, contributionCreditResult.getSuccess) match {
        case (Some(c2ma), Some(liabilities), Some(creditsAndContributions)) =>
          Some(EligibilityCheckDataSuccessResultMa(c2ma, liabilities, creditsAndContributions))
        case _ => None
      }

  }

  case class EligibilityCheckDataResultESA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    val benefitType: BenefitType    = BenefitType.ESA
    def allResults: List[ApiResult] = List(contributionCreditResult)

    def asSuccess: Option[EligibilityCheckDataSuccessResultEsa] =
      contributionCreditResult.getSuccess.map(EligibilityCheckDataSuccessResultEsa(_))

  }

  case class EligibilityCheckDataResultJSA(contributionCreditResult: ContributionCreditResult)
      extends EligibilityCheckDataResult {
    val benefitType: BenefitType             = BenefitType.JSA
    override def allResults: List[ApiResult] = List(contributionCreditResult)

    def asSuccess: Option[EligibilityCheckDataSuccessResultJsa] =
      contributionCreditResult.getSuccess.map(EligibilityCheckDataSuccessResultJsa(_))

  }

  case class EligibilityCheckDataResultGYSP(
      contributionCreditResult: List[ContributionCreditResult],
      schemeMembershipDetails: SchemeMembershipDetailsResult,
      benefitSchemeDetails: List[BenefitSchemeDetailsResult],
      marriageDetailsResult: MarriageDetailsResult,
      longTermBenefitNotes: LongTermBenefitNotesResult,
      statePensionData: IndividualStatePensionResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType = BenefitType.GYSP

    override def allResults: List[ApiResult] = contributionCreditResult ++ benefitSchemeDetails ++ List(
      marriageDetailsResult,
      longTermBenefitNotes,
      schemeMembershipDetails,
      statePensionData
    )

    def asSuccess: Option[EligibilityCheckDataSuccessResultGysp] =
      (
        schemeMembershipDetails.getSuccess,
        marriageDetailsResult.getSuccess,
        longTermBenefitNotes.getSuccess,
        statePensionData.getSuccess
      ) match {
        case (
              Some(schemeMembership),
              Some(marriageDetails),
              Some(benefitNotes),
              Some(statePension)
            ) =>
          Some(
            EligibilityCheckDataSuccessResultGysp(
              contributionCreditResult.flatMap(_.getSuccess),
              schemeMembership,
              benefitSchemeDetails.flatMap(_.getSuccess),
              marriageDetails,
              benefitNotes,
              statePension
            )
          )
        case _ => None
      }

  }

  case class EligibilityCheckDataResultBSP(
      contributionCreditResult: ContributionCreditResult,
      marriageDetailsResult: MarriageDetailsResult
  ) extends EligibilityCheckDataResult {
    val benefitType: BenefitType = BenefitType.BSP

    override def allResults: List[ApiResult] = List(marriageDetailsResult, contributionCreditResult)

    def asSuccess: Option[EligibilityCheckDataSuccessResultBsp] =
      (contributionCreditResult.getSuccess, marriageDetailsResult.getSuccess) match {
        case (Some(contributionCredits), Some(marriageDetails)) =>
          Some(EligibilityCheckDataSuccessResultBsp(marriageDetails, contributionCredits))
        case _ => None
      }

  }

}
