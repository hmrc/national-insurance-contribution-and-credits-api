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
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus,
  MarriageStatus
}

case class FilteredMarriageDetailsItem(
    status: MarriageStatus,
    startDate: Option[MarriageStartDate],
    startDateStatus: Option[MarriageStartDateStatus],
    endDate: Option[MarriageEndDate],
    endDateStatus: Option[MarriageEndDateStatus],
    spouseIdentifier: Option[Identifier],
    spouseForename: Option[SpouseForename],
    spouseSurname: Option[SpouseSurname]
)

object FilteredMarriageDetailsItem {

  implicit val filteredMarriageDetailsItemWrites: Writes[FilteredMarriageDetailsItem] =
    Json.writes[FilteredMarriageDetailsItem]

}

case class FilteredMarriageDetails(
    marriageDetails: List[FilteredMarriageDetailsItem]
)

object FilteredMarriageDetails {

  implicit val filteredMarriageDetailsWrites: Writes[FilteredMarriageDetails] =
    Json.writes[FilteredMarriageDetails]

  def from(marriageDetailsSuccessResponse: MarriageDetailsSuccessResponse): FilteredMarriageDetails =
    FilteredMarriageDetails(
      marriageDetailsSuccessResponse.marriageDetails.marriageDetailsList match {
        case Some(marriageDetailsList) =>
          marriageDetailsList.map { item =>
            FilteredMarriageDetailsItem(
              item.status,
              item.startDate,
              item.startDateStatus,
              item.endDate,
              item.endDateStatus,
              item.spouseIdentifier,
              item.spouseForename,
              item.spouseSurname
            )
          }
        case None => Nil
      }
    )

}
