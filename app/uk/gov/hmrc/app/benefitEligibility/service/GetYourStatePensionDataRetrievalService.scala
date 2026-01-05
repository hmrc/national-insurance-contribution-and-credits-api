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
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, CorrelationId}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.GYSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.request.MarriageDetailsRequestHelper
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class GetYourStatePensionDataRetrievalService @Inject() (
    marriageDetailsConnector: MarriageDetailsConnector,
    marriageDetailsRequestHelper: MarriageDetailsRequestHelper,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: GYSPEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultGYSP] = {
    val correlationId = CorrelationId.generate
    for {
      marriageDetailsResult <- marriageDetailsConnector.fetchMarriageDetails(
        marriageDetailsRequestHelper.buildRequestPath(appConfig.hipBaseUrl, eligibilityCheckDataRequest)
      )
    } yield EligibilityCheckDataResultGYSP(
      marriageDetailsResult
    )
  }

}
