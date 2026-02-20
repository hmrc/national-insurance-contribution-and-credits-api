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
import uk.gov.hmrc.app.benefitEligibility.common.ReceiptDate
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*

case class FilteredClass2MaReceipts(
    receiptDates: List[ReceiptDate]
)

object FilteredClass2MaReceipts {
  implicit val filteredClass2MaReceiptsWrites: Writes[FilteredClass2MaReceipts] = Json.writes[FilteredClass2MaReceipts]

  def from(class2MAReceiptsSuccessResponse: Class2MAReceiptsSuccessResponse): FilteredClass2MaReceipts =
    FilteredClass2MaReceipts(
      class2MAReceiptsSuccessResponse.class2MAReceiptDetails.flatMap(_.receiptDate)
    )

}
