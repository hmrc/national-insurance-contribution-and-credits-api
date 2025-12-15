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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.reqeust

import cats.implicits.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.MAEligibilityCheckDataRequest

class Class2MAReceiptsRequestHelper {

  def buildRequestPath(host: String, req: MAEligibilityCheckDataRequest): String = {
    def latestFilter: Option[String]      = req.archived.map(l => s"latest=$l&")
    def receiptDateFilter: Option[String] = req.receiptDate.map(r => s"receiptDate=$r&")
    def typeFilter: Option[String]        = req.sortBy.map(t => s"type=$t")

    val options = latestFilter.combine(receiptDateFilter).combine(typeFilter.map(result => s"?$result"))
    s"$host/ni/class-2/${req.identifier}/maternity-allowance/receipts$options"
  }

}
