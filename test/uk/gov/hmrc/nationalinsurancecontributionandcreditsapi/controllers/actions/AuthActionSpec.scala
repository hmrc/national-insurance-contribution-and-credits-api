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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.ImATeapot
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Configuration}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AuthConnector, BearerTokenExpired, InvalidBearerToken, UnsupportedAuthProvider}
import uk.gov.hmrc.http.SessionKeys.authToken
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AuthActionSpec extends AnyWordSpec with GuiceOneAppPerSuite with WireMockHelper with Injecting with Matchers with ScalaFutures() {

  val authAction: AuthAction = inject[AuthAction]

  val mockAuthConnector: AuthConnector = MockitoSugar.mock[AuthConnector]

  val application: Application = new GuiceApplicationBuilder()
    .configure(
      Configuration("metrics.enabled" -> "false", "auditing.enabled" -> false)
    )
    .overrides(
      bind[AuthConnector].toInstance(mockAuthConnector)
    )
    .build()
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  def mockRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withSession(
      SessionKeys.authToken -> authToken,
    )

  "Auth Action" should {
    "the user is logged in" when {
      "must return I am a teapot" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.successful(()))

        val authAction = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => {
          Future.successful(ImATeapot(""))
        })

        blockResult.futureValue.header.status shouldBe IM_A_TEAPOT
        blockResult.futureValue.header.headers.contains("correlationId")
      }
    }
    "the user is not logged in" when {
      "must return 403 when token is from Unsupported Auth Provider" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.failed(new UnsupportedAuthProvider))

        val authAction = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => {
          Future.successful(ImATeapot(""))
        })
        blockResult.futureValue.header.status shouldBe FORBIDDEN
        blockResult.futureValue.header.headers.contains("correlationId")
      }

      "must return 403 when token is expired" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.failed(new BearerTokenExpired))

        val authAction = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => {
          Future.successful(ImATeapot(""))
        })
        blockResult.futureValue.header.status shouldBe FORBIDDEN
        blockResult.futureValue.header.headers.contains("correlationId")
      }

      "must return 500 when token is invalid" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.failed(new InvalidBearerToken))

        val authAction = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => {
          Future.successful(ImATeapot(""))
        })
        blockResult.futureValue.header.status shouldBe INTERNAL_SERVER_ERROR
        blockResult.futureValue.header.headers.contains("correlationId")
      }
    }
  }
}
