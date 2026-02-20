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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  EndDate,
  LiabilitySummaryDetailsSuccessResponse,
  StartDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*

case class FilteredLiabilitySummaryDetailItem(
    startDate: StartDate,
    endDate: Option[EndDate]
)

object FilteredLiabilitySummaryDetailItem {

  implicit val filteredLiabilitySummaryDetailItemWrites: Writes[FilteredLiabilitySummaryDetailItem] =
    Json.writes[FilteredLiabilitySummaryDetailItem]

}

case class FilteredLiabilitySummaryDetails(
    liabilityDetails: List[FilteredLiabilitySummaryDetailItem]
)

object FilteredLiabilitySummaryDetails {

  implicit val filteredLiabilitySummaryDetailsWrites: Writes[FilteredLiabilitySummaryDetails] =
    Json.writes[FilteredLiabilitySummaryDetails]

  def from(
      liabilitySummaryDetailsSuccessResponse: LiabilitySummaryDetailsSuccessResponse
  ): FilteredLiabilitySummaryDetails = FilteredLiabilitySummaryDetails(
    liabilitySummaryDetailsSuccessResponse.liabilityDetailsList match {
      case Some(liabilityDetailsList) =>
        liabilityDetailsList.map { item =>
          FilteredLiabilitySummaryDetailItem(
            item.startDate,
            item.endDate
          )
        }
      case None => Nil
    }
  )

}
