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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.JSAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultJSA

import scala.concurrent.{ExecutionContext, Future}

class JobSeekersAllowanceDataRetrievalService(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: JSAEligibilityCheckDataRequest
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultJSA] =
    EitherT.pure[Future, BenefitEligibilityError](
      EligibilityCheckDataResultJSA()
    )

}
