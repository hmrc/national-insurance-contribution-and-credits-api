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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers.actions

import play.api.mvc.Results.BadRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.IdentifierRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentifierAction @Inject()(
                                  val authConnector: AuthConnector,
                                  val parser: BodyParsers.Default
                                )(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised().retrieve(Retrievals.internalId and Retrievals.nino) {
      case Some(internalId) ~ nino =>
        block(IdentifierRequest(request, internalId, nino))

      case _ =>
        Future.successful(BadRequest)
    }.recoverWith {
      case _: NoActiveSession =>
        hc.sessionId
          .map(sessionId => block(IdentifierRequest(request, sessionId.value, None)))
          .getOrElse(Future.successful(BadRequest))
    }
  }
}
