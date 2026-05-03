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

package uk.gov.hmrc.app.benefitEligibility.controller

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.app.benefitEligibility.service.{BenefitEligibilityDataRetrievalService, PaginationService}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class BenefitEligibilityDataController @Inject() (
    cc: ControllerComponents,
    identity: uk.gov.hmrc.app.benefitEligibility.controller.action.AuthAction,
    benefitEligibilityDataRetrievalService: BenefitEligibilityDataRetrievalService,
    paginationService: PaginationService,
    appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def fetchBenefitEligibilityData(): Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        BenefitEligibilityRequestHandler.handleStandardRequest(
          appConfig,
          request,
          benefitEligibilityDataRetrievalService.getEligibilityData
        )
      }
    } else identity.async(_ => Future.successful(NotFound))

  def getNextPage(): Action[AnyContent] =
    if (appConfig.benefitEligibilityInfoEndpointEnabled) {
      identity.async { implicit request =>
        BenefitEligibilityRequestHandler.handlePaginationRequest(
          appConfig,
          request,
          paginationService.paginate
        )
      }
    } else identity.async(_ => Future.successful(NotFound))

}
