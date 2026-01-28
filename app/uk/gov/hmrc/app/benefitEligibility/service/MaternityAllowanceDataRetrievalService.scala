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

import cats.Semigroup
import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector.Class2MAReceiptsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.connector.LiabilitySummaryDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.service.Test.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.util.ContributionCreditTaxWindowCalculator
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class MaternityAllowanceDataRetrievalService @Inject() (
    class2MAReceiptsConnector: Class2MAReceiptsConnector,
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    liabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector
)(
    implicit ec: ExecutionContext
) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: MAEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultMA] =
    (
      class2MAReceiptsConnector.fetchClass2MAReceipts(
        eligibilityCheckDataRequest.benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber,
        eligibilityCheckDataRequest.class2MaReceipts.archived,
        eligibilityCheckDataRequest.class2MaReceipts.receiptDate,
        eligibilityCheckDataRequest.class2MaReceipts.sortBy
      ),
      niContributionsAndCreditsConnector.fetchContributionsAndCredits(
        eligibilityCheckDataRequest.benefitType,
        NiContributionsAndCreditsRequest(
          eligibilityCheckDataRequest.nationalInsuranceNumber,
          eligibilityCheckDataRequest.contributionsAndCredits.dateOfBirth,
          eligibilityCheckDataRequest.contributionsAndCredits.startTaxYear,
          eligibilityCheckDataRequest.contributionsAndCredits.endTaxYear
        )
      ),
      liabilitySummaryDetailsConnector.fetchLiabilitySummaryDetails(
        eligibilityCheckDataRequest.benefitType,
        eligibilityCheckDataRequest.nationalInsuranceNumber,
        eligibilityCheckDataRequest.liabilities.liabilitySearchCategoryHyphenated,
        eligibilityCheckDataRequest.liabilities.liabilityOccurrenceNumber,
        eligibilityCheckDataRequest.liabilities.liabilityType,
        eligibilityCheckDataRequest.liabilities.earliestLiabilityStartDate,
        eligibilityCheckDataRequest.liabilities.liabilityStart,
        eligibilityCheckDataRequest.liabilities.liabilityEnd
      )
    ).parTupled.map { case (class2MaReceiptsResult, contributionsAndCreditResult, liabilityResult) =>
      EligibilityCheckDataResultMA(
        class2MaReceiptsResult,
        liabilityResult,
        contributionsAndCreditResult
      )
    }

}

object Test {

  implicit val benefitEligibilityErrorSemiGroup: Semigroup[BenefitEligibilityError] =
    new Semigroup[BenefitEligibilityError] {
      override def combine(x: BenefitEligibilityError, y: BenefitEligibilityError): BenefitEligibilityError = x
    }

}
