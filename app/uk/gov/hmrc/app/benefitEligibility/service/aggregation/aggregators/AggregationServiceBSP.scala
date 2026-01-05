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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.common.OverallResultStatus.{Failure, Partial, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.AggregatedData.AggregatedDataBSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator

object AggregationServiceBSP {

  val aggregator: ResultAggregator[EligibilityCheckDataResultBSP] =
    (eligibilityCheckDataSuccessResultBSP: EligibilityCheckDataResultBSP) =>
      if (eligibilityCheckDataSuccessResultBSP.overallResultStatus == Success) {

        val marriageDetailsResult     = eligibilityCheckDataSuccessResultBSP.marriageDetailsResult
        val marriageDetailsResultList = marriageDetailsResult.successResponse.flatMap(_.marriageDetailsList)

        Right(
          AggregatedDataBSP(
            marriageDetailsResultList.flatMap(_.status).get,
            marriageDetailsResultList.flatMap(_.startDate).get,
            marriageDetailsResultList.flatMap(_.startDateStatus).get,
            marriageDetailsResultList.flatMap(_.endDate).get,
            marriageDetailsResultList.flatMap(_.endDateStatus).get,
            marriageDetailsResultList.flatMap(_.spouseIdentifier).get,
            marriageDetailsResultList.flatMap(_.spouseForename).get,
            marriageDetailsResultList.flatMap(_.spouseSurname).get
          )
        )

      } else if (eligibilityCheckDataSuccessResultBSP.overallResultStatus == Failure) {
        Left(
          BenefitEligibilityDataFetchError(
            Failure,
            BSP,
            eligibilityCheckDataSuccessResultBSP.resultSummary,
            eligibilityCheckDataSuccessResultBSP.allResults.filter(_.status == Failure)
          )
        )

      } else {
        Left(
          BenefitEligibilityDataFetchError(
            Partial,
            BSP,
            eligibilityCheckDataSuccessResultBSP.resultSummary,
            eligibilityCheckDataSuccessResultBSP.allResults.filter(_.status == Success) ++
              eligibilityCheckDataSuccessResultBSP.allResults.filter(_.status == Failure)
          )
        )
      }

}
