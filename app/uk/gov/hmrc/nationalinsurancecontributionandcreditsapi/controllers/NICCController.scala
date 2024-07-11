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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequestPayload
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.TaxYear
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services.NICCService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class NICCController @Inject()(cc: ControllerComponents,
                               identity: AuthAction,
                               niccService: NICCService)
  extends BackendController(cc) {

  private val logger: Logger = Logger(this.getClass)

  def postContributionsAndCredits(startTaxYear: TaxYear,
                                  endTaxYear: TaxYear): Action[AnyContent] = identity.async { implicit request =>


    logger.info("Setting up request!")

    request.body.asJson match {
      case Some(json) =>
        json.validate[NICCRequestPayload].fold(
          _ => Future.successful(BadRequest("There was a problem with the request")),
          requestObject => niccService.statusMapping(requestObject.nationalInsuranceNumber, startTaxYear.taxYear, endTaxYear.taxYear, requestObject.dateOfBirth.toString)
        )
      case None => Future.successful(BadRequest("Missing JSON data"))
    }
  }
}
