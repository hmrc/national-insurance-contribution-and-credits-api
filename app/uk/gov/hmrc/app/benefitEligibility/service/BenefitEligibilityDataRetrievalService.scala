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
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitEligibilityError, CorrelationId, FeatureDisabled}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.model.request.*
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BenefitEligibilityDataRetrievalService @Inject() (
    maternityAllowanceDataRetrievalService: MaternityAllowanceDataRetrievalService,
    employmentSupportAllowanceDataRetrievalService: EmploymentSupportAllowanceDataRetrievalService,
    jobSeekersAllowanceDataRetrievalService: JobSeekersAllowanceDataRetrievalService,
    getYourStatePensionDataRetrievalService: GetYourStatePensionDataRetrievalService,
    bspDataRetrievalService: BereavementSupportPaymentDataRetrievalService,
    searchlightDataRetrievalService: SearchlightDataRetrievalService,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def getEligibilityData(
      request: EligibilityCheckDataRequest,
      correlationId: CorrelationId
  )(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResult] = {
    implicit val cid: CorrelationId = correlationId
    request match {
      case request: MAEligibilityCheckDataRequest if appConfig.maEnabled =>
        maternityAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: ESAEligibilityCheckDataRequest if appConfig.esaEnabled =>
        employmentSupportAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: JSAEligibilityCheckDataRequest if appConfig.jsaEnabled =>
        jobSeekersAllowanceDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: GYSPEligibilityCheckDataRequest if appConfig.gyspEnabled =>
        getYourStatePensionDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: BSPEligibilityCheckDataRequest if appConfig.bspEnabled =>
        bspDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: SearchlightEligibilityCheckDataRequest if appConfig.searchlightEnabled =>
        searchlightDataRetrievalService.fetchEligibilityData(request).widen[EligibilityCheckDataResult]
      case request: SearchlightEligibilityCheckDataRequest if !appConfig.searchlightEnabled =>
        EitherT.left[EligibilityCheckDataResult](
          Future.successful(FeatureDisabled(s"feature disabled: SEARCHLIGHT"))
        )
      case request =>
        EitherT.left[EligibilityCheckDataResult](
          Future.successful(FeatureDisabled(s"feature disabled: ${request.benefitType.entryName}"))
        )
    }
  }

}
