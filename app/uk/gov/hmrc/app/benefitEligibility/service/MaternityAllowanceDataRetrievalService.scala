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
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.AccessForbidden
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, EndTaxYear, StartTaxYear}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.Failure
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ContributionCreditResult, LiabilityResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector.Class2MAReceiptsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.reqeust.Class2MAReceiptsRequestHelper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{EligibilityCheckDataResult, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.util.ContributionCreditTaxWindowCalculator
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class MaternityAllowanceDataRetrievalService @Inject() (
    class2MAReceiptsConnector: Class2MAReceiptsConnector,
    class2MAReceiptsRequestHelper: Class2MAReceiptsRequestHelper,
    appConfig: AppConfig
)(
    implicit ec: ExecutionContext
) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: MAEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultMA] = {

    val windows = ContributionCreditTaxWindowCalculator.createWindows(
      StartTaxYear(eligibilityCheckDataRequest.startTaxYear),
      EndTaxYear(eligibilityCheckDataRequest.endTaxYear)
    )

    for {
      class2MaReceiptsResult <- class2MAReceiptsConnector.fetchClass2MAReceipts(
        class2MAReceiptsRequestHelper.buildRequestPath(appConfig.hipBaseUrl, eligibilityCheckDataRequest)
      )
      liabilityResult = LiabilityResult(Failure, None, Some(NpsNormalizedError(AccessForbidden, "", 403)))
      contributionCreditResult = windows.map { windows =>
        ContributionCreditResult(
          Failure,
          None,
          Some(NpsNormalizedError(AccessForbidden, "", 403))
        )
      }
    } yield EligibilityCheckDataResultMA(
      class2MaReceiptsResult,
      liabilityResult,
      contributionCreditResult
    )
  }

}
