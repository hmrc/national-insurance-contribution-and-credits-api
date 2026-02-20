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

package uk.gov.hmrc.app.benefitEligibility.controller.action

import play.api.libs.json.Json
import play.api.mvc.Results.{Forbidden, InternalServerError}
import play.api.mvc.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response.{ErrorCode, ErrorReason, ErrorResponse}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.Failure
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthAction @Inject() (
    val authConnector: AuthConnector,
    val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[Request, AnyContent]
    with ActionFunction[Request, Request]
    with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(AuthProvider.PrivilegedApplication)) {
      block(request)
    }.recover {
      case e: UnsupportedAuthProvider =>
        Forbidden(Json.toJson(ErrorResponse(ErrorCode.Forbidden, ErrorReason(e.msg))))
      case e: BearerTokenExpired =>
        Forbidden(Json.toJson(ErrorResponse(ErrorCode.Forbidden, ErrorReason(e.msg))))
      case e =>
        InternalServerError(Json.toJson(ErrorResponse(ErrorCode.InternalServerError, ErrorReason(e.getMessage))))
    }
  }

}
