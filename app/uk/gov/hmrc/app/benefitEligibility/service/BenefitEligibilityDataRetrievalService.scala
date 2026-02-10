/*
 * Copyright 2024 HM Revenue & Customs
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
import cats.syntax.all.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  BSPEligibilityCheckDataRequest,
  ESAEligibilityCheckDataRequest,
  EligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest,
  JSAEligibilityCheckDataRequest,
  MAEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BenefitEligibilityDataRetrievalService @Inject() (
    maternityAllowanceDataRetrievalService: MaternityAllowanceDataRetrievalService,
    employmentSupportAllowanceDataRetrievalService: EmploymentSupportAllowanceDataRetrievalService,
    jobSeekersAllowanceDataRetrievalService: JobSeekersAllowanceDataRetrievalService,
    getYourStatePensionDataRetrievalService: GetYourStatePensionDataRetrievalService,
    bspDataRetrievalService: BspDataRetrievalService
)(implicit ec: ExecutionContext) {

  def getEligibilityData(
      request: EligibilityCheckDataRequest
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResult] =
    request match {
      case request: MAEligibilityCheckDataRequest =>
        maternityAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: ESAEligibilityCheckDataRequest =>
        employmentSupportAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: JSAEligibilityCheckDataRequest =>
        jobSeekersAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: GYSPEligibilityCheckDataRequest =>
        getYourStatePensionDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: BSPEligibilityCheckDataRequest =>
        bspDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
    }

}
