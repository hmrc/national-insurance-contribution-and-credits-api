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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.GYSP
import uk.gov.hmrc.app.benefitEligibility.common.OverallResultStatus.{Failure, Partial, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.AggregatedData.AggregatedDataGYSP
import uk.gov.hmrc.app.benefitEligibility.service.aggregation.ResultAggregation.ResultAggregator

object AggregationServiceGYSP {

  val aggregator: ResultAggregator[EligibilityCheckDataResultGYSP] =
    (eligibilityCheckDataSuccessResultGYSP: EligibilityCheckDataResultGYSP) =>
      if (eligibilityCheckDataSuccessResultGYSP.overallResultStatus == Success) {

        val marriageDetailsResult     = eligibilityCheckDataSuccessResultGYSP.marriageDetailsResult
        val marriageDetailsResultList = marriageDetailsResult.successResponse.flatMap(_.marriageDetailsList)

        Right(
          AggregatedDataGYSP(
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

      } else if (eligibilityCheckDataSuccessResultGYSP.overallResultStatus == Failure) {
        Left(
          BenefitEligibilityDataFetchError(
            Failure,
            GYSP,
            eligibilityCheckDataSuccessResultGYSP.resultSummary,
            eligibilityCheckDataSuccessResultGYSP.allResults.filter(_.status == Failure)
          )
        )

      } else {
        Left(
          BenefitEligibilityDataFetchError(
            Partial,
            GYSP,
            eligibilityCheckDataSuccessResultGYSP.resultSummary,
            eligibilityCheckDataSuccessResultGYSP.allResults.filter(_.status == Success) ++
              eligibilityCheckDataSuccessResultGYSP.allResults.filter(_.status == Failure)
          )
        )
      }

}
