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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.AggregatedData.AggregatedDataBSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator

object AggregationServiceBSP {

  val aggregator: ResultAggregator[EligibilityCheckDataResultBSP] =
    (eligibilityCheckDataResultBSP: EligibilityCheckDataResultBSP) =>
      if (eligibilityCheckDataResultBSP.allResults.forall(_.isSuccess)) {

        val marriageDetailsResult = eligibilityCheckDataResultBSP.marriageDetailsResult.getSuccess.get

        Right(
          AggregatedDataBSP(
            marriageDetailsResult.marriageDetailsList.flatMap(_.status).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.startDate).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.startDateStatus).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.endDate).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.endDateStatus).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.spouseIdentifier).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.spouseForename).get,
            marriageDetailsResult.marriageDetailsList.flatMap(_.spouseSurname).get
          )
        )
      } else Left(BenefitEligibilityDataFetchError.from(eligibilityCheckDataResultBSP))

}
