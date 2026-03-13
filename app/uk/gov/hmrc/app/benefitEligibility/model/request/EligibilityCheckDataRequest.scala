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

package uk.gov.hmrc.app.benefitEligibility.model.request

import play.api.libs.json.*
import EligibilityCheckDataRequestParams.*
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.{BSP, BSP_SEARCHLIGHT, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, Identifier}
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor

object EligibilityCheckDataRequest {

  implicit val reads: Reads[EligibilityCheckDataRequest] = Reads { json =>
    (json \ "benefitType").validate[BenefitType].flatMap {
      case BenefitType.ESA             => json.validate[ESAEligibilityCheckDataRequest]
      case BenefitType.JSA             => json.validate[JSAEligibilityCheckDataRequest]
      case BenefitType.BSP             => json.validate[BSPEligibilityCheckDataRequest]
      case BenefitType.MA              => json.validate[MAEligibilityCheckDataRequest]
      case BenefitType.GYSP            => json.validate[GYSPEligibilityCheckDataRequest]
      case BenefitType.BSP_SEARCHLIGHT => json.validate[BSPSEligibilityCheckDataRequest]
    }
  }

}

sealed trait EligibilityCheckDataRequest {
  def benefitType: BenefitType
  def nationalInsuranceNumber: Identifier
  def niContributionsAndCredits: ContributionsAndCreditsRequestParams
}

final case class ESAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object ESAEligibilityCheckDataRequest {

  implicit val esaEligibilityCheckDataRequestReads: Reads[ESAEligibilityCheckDataRequest] =
    Json.reads[ESAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new ESAEligibilityCheckDataRequest(ESA, nationalInsuranceNumber, niContributionsAndCredits)

}

final case class JSAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object JSAEligibilityCheckDataRequest {

  implicit val jsaEligibilityCheckDataRequestReads: Reads[JSAEligibilityCheckDataRequest] =
    Json.reads[JSAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new JSAEligibilityCheckDataRequest(JSA, nationalInsuranceNumber, niContributionsAndCredits)

}

final case class BSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    nextCursor: Option[PaginationCursor]
) extends EligibilityCheckDataRequest

object BSPEligibilityCheckDataRequest {

  implicit val bspEligibilityCheckDataRequestReads: Reads[BSPEligibilityCheckDataRequest] =
    Json.reads[BSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams,
      nextCursor: Option[PaginationCursor]
  ) = new BSPEligibilityCheckDataRequest(BSP, nationalInsuranceNumber, niContributionsAndCredits, nextCursor)

}

final case class MAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    liabilities: LiabilitiesRequestParams,
    nextCursor: Option[PaginationCursor]
) extends EligibilityCheckDataRequest

object MAEligibilityCheckDataRequest {

  def apply(
      nationalInsuranceNumber: Identifier,
      contributionsAndCredits: ContributionsAndCreditsRequestParams,
      liabilities: LiabilitiesRequestParams,
      nextCursor: Option[PaginationCursor]
  ) = new MAEligibilityCheckDataRequest(
    MA,
    nationalInsuranceNumber,
    contributionsAndCredits,
    liabilities,
    nextCursor
  )

  implicit val maEligibilityCheckDataRequestReads: Reads[MAEligibilityCheckDataRequest] =
    Json.reads[MAEligibilityCheckDataRequest]

}

final case class GYSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    longTermBenefitCalculation: Option[LongTermBenefitCalculationRequestParams],
    nextCursor: Option[PaginationCursor]
) extends EligibilityCheckDataRequest

object GYSPEligibilityCheckDataRequest {

  implicit val gyspEligibilityCheckDataRequestReads: Reads[GYSPEligibilityCheckDataRequest] =
    Json.reads[GYSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams,
      longTermBenefitCalculation: Option[LongTermBenefitCalculationRequestParams],
      nextCursor: Option[PaginationCursor]
  ) = new GYSPEligibilityCheckDataRequest(
    GYSP,
    nationalInsuranceNumber,
    niContributionsAndCredits,
    longTermBenefitCalculation,
    nextCursor
  )

}

final case class BSPSEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object BSPSEligibilityCheckDataRequest {

  implicit val bspsEligibilityCheckDataRequestReads: Reads[BSPSEligibilityCheckDataRequest] =
    Json.reads[BSPSEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new BSPSEligibilityCheckDataRequest(BSP_SEARCHLIGHT, nationalInsuranceNumber, niContributionsAndCredits)

}
