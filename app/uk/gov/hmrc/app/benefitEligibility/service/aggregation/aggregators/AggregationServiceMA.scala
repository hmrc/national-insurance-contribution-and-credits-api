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

package uk.gov.hmrc.app.benefitEligibility.service.aggregation.aggregators

import cats.syntax.all.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.AggregatedData.AggregatedDataMA
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator

object AggregationServiceMA {

  val aggregator: ResultAggregator[EligibilityCheckDataResultMA] = {
    (eligibilityCheckDataSuccessResultMA: EligibilityCheckDataResultMA) =>
      val class2MaReceiptsResult = eligibilityCheckDataSuccessResultMA.class2MaReceiptsResult
      AggregatedDataMA(
        class2MaReceiptsResult.successResponse
          .map(_.class2MAReceiptDetails)
          .sequence
          .flatten
          .flatMap(_.receiptDate)
      )

  }

}
