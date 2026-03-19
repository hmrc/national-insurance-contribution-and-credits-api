package uk.gov.hmrc.app.benefitEligibility.controller.action

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.ImATeapot
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Configuration}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.http.SessionKeys.authToken
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with Injecting
    with Matchers
    with ScalaFutures() {

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
      SessionKeys.authToken -> authToken
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

        val authAction  = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => Future.successful(ImATeapot("")))

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

        val authAction  = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => Future.successful(ImATeapot("")))
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

        val authAction  = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => Future.successful(ImATeapot("")))
        blockResult.futureValue.header.status shouldBe FORBIDDEN
        blockResult.futureValue.header.headers.contains("correlationId")
      }

      "must return 403 when token is invalid" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.failed(new InvalidBearerToken))

        val authAction  = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => Future.successful(ImATeapot("")))
        blockResult.futureValue.header.status shouldBe FORBIDDEN
        blockResult.futureValue.header.headers.contains("correlationId")
      }

      "must return 403 when token is missing" in {
        when(
          mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(
            any[HeaderCarrier](),
            any[ExecutionContext]()
          )
        )
          .thenReturn(Future.failed(new MissingBearerToken))

        val authAction  = application.injector.instanceOf[AuthAction]
        val blockResult = authAction.invokeBlock(mockRequest, (_: Request[_]) => Future.successful(ImATeapot("")))
        blockResult.futureValue.header.status shouldBe FORBIDDEN
        blockResult.futureValue.header.headers.contains("correlationId")
      }
    }
  }

}
