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

import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityDataFetchError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.{AggregatedDataMa, NiContributionDataMa}
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator

object AggregationServiceMa {

  val aggregator: ResultAggregator[EligibilityCheckDataResultMA, AggregatedDataMa] = {
    (eligibilityCheckDataSuccessResultMA: EligibilityCheckDataResultMA) =>
      if (eligibilityCheckDataSuccessResultMA.allResults.forall(_.isSuccess)) {
        val isNiClass1 =
          eligibilityCheckDataSuccessResultMA.contributionCreditResult.flatMap(_.getSuccess).nonEmpty
        val niData: List[NiContributionDataMa] = if (isNiClass1) {
          eligibilityCheckDataSuccessResultMA.contributionCreditResult
            .flatMap(_.getSuccess)
            .flatMap(_.niClass1.get)
            .map(NiContributionDataMa(_))
        } else {
          eligibilityCheckDataSuccessResultMA.contributionCreditResult
            .flatMap(_.getSuccess)
            .flatMap(_.niClass2.get)
            .map(NiContributionDataMa(_))
        }
        Right(
          AggregatedDataMa(
            niData,
            List(),
            List()
          )
        )
      } else Left(BenefitEligibilityDataFetchError.from(eligibilityCheckDataSuccessResultMA))

  }

}
