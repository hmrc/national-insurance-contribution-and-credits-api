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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CallSystem, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.*

object EligibilityCheckDataRequest {

  implicit val reads: Reads[EligibilityCheckDataRequest] = Reads { json =>
    (json \ "system").toOption match {
      case Some(callSystem) =>
        callSystem.validate[CallSystem].flatMap { case CallSystem.SEARCHLIGHT =>
          json.validate[SearchlightEligibilityCheckDataRequest]
        }
      case None =>
        (json \ "benefitType").validate[BenefitType].flatMap {
          case BenefitType.ESA  => json.validate[ESAEligibilityCheckDataRequest]
          case BenefitType.JSA  => json.validate[JSAEligibilityCheckDataRequest]
          case BenefitType.BSP  => json.validate[BSPEligibilityCheckDataRequest]
          case BenefitType.MA   => json.validate[MAEligibilityCheckDataRequest]
          case BenefitType.GYSP => json.validate[GYSPEligibilityCheckDataRequest]
        }
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
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object BSPEligibilityCheckDataRequest {

  implicit val bspEligibilityCheckDataRequestReads: Reads[BSPEligibilityCheckDataRequest] =
    Json.reads[BSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new BSPEligibilityCheckDataRequest(BSP, nationalInsuranceNumber, niContributionsAndCredits)

}

final case class MAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    liabilities: LiabilitiesRequestParams
) extends EligibilityCheckDataRequest

object MAEligibilityCheckDataRequest {

  def apply(
      nationalInsuranceNumber: Identifier,
      contributionsAndCredits: ContributionsAndCreditsRequestParams,
      liabilities: LiabilitiesRequestParams
  ) = new MAEligibilityCheckDataRequest(
    MA,
    nationalInsuranceNumber,
    contributionsAndCredits,
    liabilities
  )

  implicit val maEligibilityCheckDataRequestReads: Reads[MAEligibilityCheckDataRequest] =
    Json.reads[MAEligibilityCheckDataRequest]

}

final case class GYSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams,
    longTermBenefitCalculation: Option[LongTermBenefitCalculationRequestParams]
) extends EligibilityCheckDataRequest

object GYSPEligibilityCheckDataRequest {

  implicit val gyspEligibilityCheckDataRequestReads: Reads[GYSPEligibilityCheckDataRequest] =
    Json.reads[GYSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams,
      longTermBenefitCalculation: Option[LongTermBenefitCalculationRequestParams]
  ) = new GYSPEligibilityCheckDataRequest(
    GYSP,
    nationalInsuranceNumber,
    niContributionsAndCredits,
    longTermBenefitCalculation
  )

}

final case class SearchlightEligibilityCheckDataRequest private (
    system: CallSystem,
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCreditsRequestParams
) extends EligibilityCheckDataRequest

object SearchlightEligibilityCheckDataRequest {

  implicit val bspSearchlightEligibilityCheckDataRequestReads: Reads[SearchlightEligibilityCheckDataRequest] =
    Json.reads[SearchlightEligibilityCheckDataRequest]

  def apply(
      benefitType: BenefitType,
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCreditsRequestParams
  ) = new SearchlightEligibilityCheckDataRequest(
    CallSystem.SEARCHLIGHT,
    benefitType,
    nationalInsuranceNumber,
    niContributionsAndCredits
  )

}
