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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{when, withSettings}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.Helpers.{CONTENT_TYPE, defaultAwaitTimeout, status}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{HIPOutcome, NICCRequest, NICCResponse, NIContribution}

import scala.concurrent.{ExecutionContextExecutor, Future}

class NICCControllerSpec extends AnyWordSpec with Matchers {

  implicit val executor: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val materializer: Materializer = Materializer(ActorSystem())


  "POST /nationalInsuranceNumber-info" should {
    "return 200" in {
      val requestNino = Nino("BB 00 00 20 B")
      val requestStartYear = "2017"
      val requestEndYear = "2019"
      val requestDoB = "1998-04-23"
      val body = Json.obj(("dateOfBirth", "1998-04-23"))
      val fakeRequest = FakeRequest[AnyContentAsJson]("POST", "/nicc-json-service/nicc/v1/api/national-insurance/BB 00 00 20 B/from/2017/to/2019", FakeHeaders(Seq((CONTENT_TYPE, "application/json"))), AnyContentAsJson(body))
//      val fakeRequestV2 = FakeRequest("POST", "/nicc-json-service/nicc/v1/api/national-insurance/BB 00 00 20 B/from/2017/to/2019", )


      val mockHipConnector: HipConnector = mock[HipConnector](withSettings().verboseLogging())
      val expectedResponseObject: HttpResponse = HttpResponse.apply(200, Some(Json.toJson(NICCResponse(Seq(NIContribution(2018, "A", "A", BigDecimal(1), BigDecimal(1), "A", BigDecimal(1))), Seq()))))
      when(mockHipConnector.fetchData(any())(any())).thenReturn(Future.successful(expectedResponseObject))
      val controller = new NICCController(Helpers.stubControllerComponents(), mockHipConnector)

      val response = controller.postContributionsAndCredits(requestNino, requestStartYear, requestEndYear)(fakeRequest)
      status(response) shouldBe Status.OK
    }
  }

}
