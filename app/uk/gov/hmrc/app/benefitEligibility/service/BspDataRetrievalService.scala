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

package uk.gov.hmrc.app.benefitEligibility.service

import cats.data.EitherT
import cats.implicits.catsSyntaxTuple2Parallel
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, BenefitType}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.BSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.service.Test.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BspDataRetrievalService @Inject() (
    marriageDetailsConnector: MarriageDetailsConnector,
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: BSPEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultBSP] =

    (
      niContributionsAndCreditsConnector.fetchContributionsAndCredits(
        eligibilityCheckDataRequest.benefitType,
        NiContributionsAndCreditsRequest(
          eligibilityCheckDataRequest.nationalInsuranceNumber,
          eligibilityCheckDataRequest.contributionsAndCredits.dateOfBirth,
          eligibilityCheckDataRequest.contributionsAndCredits.startTaxYear,
          eligibilityCheckDataRequest.contributionsAndCredits.endTaxYear
        )
      ),
      marriageDetailsConnector.fetchMarriageDetails(
        eligibilityCheckDataRequest.benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber,
        eligibilityCheckDataRequest.marriageDetails.searchStartYear,
        eligibilityCheckDataRequest.marriageDetails.searchEndYear,
        eligibilityCheckDataRequest.marriageDetails.latest,
        eligibilityCheckDataRequest.marriageDetails.sequence
      )
    ).parTupled.map { case (contributionsAndCreditResult, marriageDetailsResult) =>
      EligibilityCheckDataResultBSP(
        contributionsAndCreditResult,
        marriageDetailsResult
      )

    }

}
