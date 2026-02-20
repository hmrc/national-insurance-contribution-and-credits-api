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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.{
  BenefitSchemeDetailsSuccessResponse,
  BenefitSchemeName
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.{
  EmployersContractedOutNumberDetails,
  SchemeMembershipDetailsSuccessResponse,
  SchemeMembershipEndDate,
  SchemeMembershipStartDate
}

case class FilteredSchemeMembershipDetailsItem(
    schemeName: Option[BenefitSchemeName],
    schemeMembershipStartDate: Option[SchemeMembershipStartDate],
    schemeMembershipEndDate: Option[SchemeMembershipEndDate],
    employersContractedOutNumberDetails: Option[EmployersContractedOutNumberDetails]
)

object FilteredSchemeMembershipDetailsItem {

  implicit val filteredSchemeMembershipDetailsItemWrites: Writes[FilteredSchemeMembershipDetailsItem] =
    Json.writes[FilteredSchemeMembershipDetailsItem]

}

case class FilteredSchemeMembershipDetails(schemeMembershipDetails: List[FilteredSchemeMembershipDetailsItem])

object FilteredSchemeMembershipDetails {

  implicit val filteredSchemeMembershipDetailsWrites: Writes[FilteredSchemeMembershipDetails] =
    Json.writes[FilteredSchemeMembershipDetails]

  def from(
      schemeMembershipDetailsSuccessResponse: SchemeMembershipDetailsSuccessResponse,
      benefitSchemeDetailsSuccessResponse: List[BenefitSchemeDetailsSuccessResponse]
  ): FilteredSchemeMembershipDetails = {

    val m = benefitSchemeDetailsSuccessResponse.map { i =>
      (i.benefitSchemeDetails.schemeContractedOutNumberDetails.value, i.benefitSchemeDetails.schemeName)
    }.toMap

    FilteredSchemeMembershipDetails(
      schemeMembershipDetailsSuccessResponse.schemeMembershipDetailsSummaryList match {
        case Some(schemeMembershipDetailsSummaryList) =>
          schemeMembershipDetailsSummaryList.map { item =>
            FilteredSchemeMembershipDetailsItem(
              m.getOrElse(
                item.schemeMembershipDetails.employersContractedOutNumberDetails.map(_.value).getOrElse(""),
                None
              ),
              item.schemeMembershipDetails.schemeMembershipStartDate,
              item.schemeMembershipDetails.schemeMembershipEndDate,
              item.schemeMembershipDetails.employersContractedOutNumberDetails
            )
          }
        case None => Nil
      }
    )
  }

}
