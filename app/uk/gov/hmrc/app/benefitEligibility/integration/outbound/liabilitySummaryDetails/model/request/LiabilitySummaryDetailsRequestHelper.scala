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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.request

import cats.implicits.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.MAEligibilityCheckDataRequest

class LiabilitySummaryDetailsRequestHelper {

  def buildRequestPath(host: String, req: MAEligibilityCheckDataRequest): String = {
    def occurrenceNumber: Option[String]  = req.liabilityOccurrenceNumber.map(o => s"occurrenceNumber=${o.toString}&")
    def typeFilter: Option[String]        = req.liabilityType.map(t => s"type=$t&")
    def earliestStartDate: Option[String] = req.earliestLiabilityStartDate.map(d => s"earliestStartDate=${d.toString}&")
    def liabilityStartDate: Option[String] = req.liabilityStart.map(d => s"liabilityStartDate=${d.toString}&")
    def liabilityEndDate: Option[String]   = req.liabilityEnd.map(d => s"liabilityEndDate=${d.toString}&")

    val options = occurrenceNumber
      .combine(typeFilter)
      .combine(earliestStartDate)
      .combine(liabilityStartDate)
      .combine(liabilityEndDate)
      .getOrElse("")
    s"$host/person/${req.identifier}/liability-summary/${req.liabilitySearchCategoryHyphenated}$options".dropRight(1)
  }

}
