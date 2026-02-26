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

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.{
  LongTermBenefitNotesSuccessResponse,
  Note
}

case class FilteredLongTermBenefitCalculationDetailsItem(
    guaranteedMinimumPensionContractedOutDeductionsPre1988: Option[
      GuaranteedMinimumPensionContractedOutDeductionsPre1988
    ],
    guaranteedMinimumPensionContractedOutDeductionsPost1988: Option[
      GuaranteedMinimumPensionContractedOutDeductionsPost1988
    ],
    contractedOutDeductionsPre1988: Option[ContractedOutDeductionsPre1988],
    contractedOutDeductionsPost1988: Option[ContractedOutDeductionsPost1988],
    longTermBenefitNotes: List[Note]
)

object FilteredLongTermBenefitCalculationDetailsItem {

  implicit val filteredLongTermBenefitCalculationDetailsItemWrites
      : Writes[FilteredLongTermBenefitCalculationDetailsItem] =
    Json.writes[FilteredLongTermBenefitCalculationDetailsItem]

}

case class FilteredLongTermBenefitCalculationDetails(
    benefitCalculationDetails: List[FilteredLongTermBenefitCalculationDetailsItem]
)

object FilteredLongTermBenefitCalculationDetails {

  implicit val filteredLongTermBenefitCalculationDetailsWrites: Writes[FilteredLongTermBenefitCalculationDetails] =
    Json.writes[FilteredLongTermBenefitCalculationDetails]

  def from(
      longTermBenefitCalculationDetailsSuccessResponse: LongTermBenefitCalculationDetailsSuccessResponse,
      longTermBenefitNotesSuccessResponse: List[LongTermBenefitNotesSuccessResponse]
  ): FilteredLongTermBenefitCalculationDetails = FilteredLongTermBenefitCalculationDetails(
    longTermBenefitCalculationDetailsSuccessResponse.benefitCalculationDetailsList match {
      case Some(benefitCalculationDetailsList) =>
        benefitCalculationDetailsList.map { item =>
          FilteredLongTermBenefitCalculationDetailsItem(
            item.benefitCalculationDetail.flatMap(_.guaranteedMinimumPensionContractedOutDeductionsPre1988),
            item.benefitCalculationDetail.flatMap(_.guaranteedMinimumPensionContractedOutDeductionsPost1988),
            item.benefitCalculationDetail.flatMap(_.contractedOutDeductionsPre1988),
            item.benefitCalculationDetail.flatMap(_.contractedOutDeductionsPost1988),
            longTermBenefitNotesSuccessResponse.flatMap(_.longTermBenefitNotes)
          )
        }
      case None => Nil
    }
  )

}
