package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{HIPOutcome, NICCRequest, NICCResponse, NIContribution}

import scala.concurrent.{ExecutionContextExecutor, Future}

class NICCControllerSpec extends AnyWordSpec with Matchers{


  implicit val executor: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global


  "GET /nationalInsuranceNumber-info" should {
    "return 200" in {
      val requestObject = NICCRequest(Nino("AA123456B"), "2017", "2019", "1998-04-23")

      val mockHipConnector: HipConnector = mock[HipConnector]

      val expectedResponseObject: HIPOutcome = Right(NICCResponse(Seq(NIContribution(2018, "A", "A", BigDecimal(1), BigDecimal(1), "A", BigDecimal(1))), Seq()))
      when(mockHipConnector.fetchData(requestObject)(HeaderCarrier())).thenReturn(Future(expectedResponseObject))
      val controller = new NICCController(Helpers.stubControllerComponents(), mockHipConnector)

//      val fakeRequest = FakeRequest("GET", "/nino-info")

      val response = controller.getContributionsAndCredits(Nino("AA123456B"), "2017", "2019")
      response shouldBe 200
    }
  }

}
