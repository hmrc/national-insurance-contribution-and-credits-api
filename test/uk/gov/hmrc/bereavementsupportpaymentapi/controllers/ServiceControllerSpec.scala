package uk.gov.hmrc.bereavementsupportpaymentapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

class ServiceControllerSpec extends AnyWordSpec with Matchers {

  private val fakeRequest = FakeRequest("GET", "/")
  private val controller = new ServiceController(Helpers.stubControllerComponents())

  "GET /" should {
    "return 200" in {
      val result = controller.getNinoInfo()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
