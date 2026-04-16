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
import cats.instances.future.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.connectors.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultJSA
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  CorrelationId,
  DataRetrievalServiceError
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.JSAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class JobSeekersAllowanceDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector
)(
    implicit ec: ExecutionContext
) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def fetchEligibilityData(
      eligibilityCheckDataRequest: JSAEligibilityCheckDataRequest
  )(
      implicit hc: HeaderCarrier,
      correlationId: CorrelationId
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultJSA] =

    niContributionsAndCreditsConnector
      .fetchContributionsAndCredits(
        eligibilityCheckDataRequest.benefitType,
        NiContributionsAndCreditsRequest(
          eligibilityCheckDataRequest.nationalInsuranceNumber,
          eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth,
          eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
          eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
        )
      )
      .map(EligibilityCheckDataResultJSA(_))
      .leftMap { error =>
        logger.info("Fetch JSA eligibility data failed: " + error.getMessage)
        DataRetrievalServiceError(List(error))
      }

}
