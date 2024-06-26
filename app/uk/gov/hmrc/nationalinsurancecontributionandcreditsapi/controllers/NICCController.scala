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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers

import play.api.Logger
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequestPayload
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.NICCNino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services.NICCService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton()
class NICCController @Inject()(cc: ControllerComponents,
                               niccService: NICCService)
  extends BackendController(cc) {

  private val logger: Logger = Logger(this.getClass)

  def postContributionsAndCredits(nationalInsuranceNumber: NICCNino,
                                  startTaxYear: String,
                                  endTaxYear: String): Action[NICCRequestPayload] = Action(parse.json[NICCRequestPayload]).async { implicit request =>


    logger.info("Setting up request!")

    niccService.statusMapping(nationalInsuranceNumber, startTaxYear, endTaxYear, request.body.dateOfBirth.toString)

  }
}
