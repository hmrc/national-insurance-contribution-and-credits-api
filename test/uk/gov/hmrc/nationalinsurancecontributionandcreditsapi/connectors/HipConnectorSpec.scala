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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{OK, await, defaultAwaitTimeout}
import play.api.test.Injecting
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequest
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.NICCNino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper

class HipConnectorSpec extends AnyWordSpec with GuiceOneAppPerSuite with WireMockHelper with Injecting with Matchers {

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "microservice.services.hip.port" -> server.port(),
    ).build()

  lazy val connector: HipConnector = inject[HipConnector]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "createReceivable" should {
    "return 200" when {
      "200 is returned from downstream" in {
        val payload = NICCRequest(NICCNino("BB000200B"), "2013", "2015", "1998-03-23")

        stubPostServer(aResponse().withStatus(200), s"/nps/nps-json-service/nps/v1/api/national-insurance/${payload.nationalInsuranceNumber.nino}/contributions-and-credits/from/${payload.startTaxYear}/to/${payload.endTaxYear}")
        await(connector.fetchData(payload)).status shouldBe OK
      }
    }
    "return 4xx" when {
      "400 is returned from downstream" in {
        val payload = NICCRequest(NICCNino("BB000400B"), "2013", "2015", "1998-03-23")

        stubPostServer(aResponse().withStatus(400), s"/nps/nps-json-service/nps/v1/api/national-insurance/${payload.nationalInsuranceNumber.nino}/contributions-and-credits/from/${payload.startTaxYear}/to/${payload.endTaxYear}")
        await(connector.fetchData(payload)).status shouldBe BAD_REQUEST
      }

      "403 is returned from downstream" in {
        val payload = NICCRequest(NICCNino("BB000403B"), "2013", "2015", "1998-03-23")

        stubPostServer(aResponse().withStatus(403), s"/nps/nps-json-service/nps/v1/api/national-insurance/${payload.nationalInsuranceNumber.nino}/contributions-and-credits/from/${payload.startTaxYear}/to/${payload.endTaxYear}")
        await(connector.fetchData(payload)).status shouldBe FORBIDDEN
      }
      "422 is returned from downstream" in {
        val payload = NICCRequest(NICCNino("BB000422B"), "2013", "2015", "1998-03-23")

        stubPostServer(aResponse().withStatus(422), s"/nps/nps-json-service/nps/v1/api/national-insurance/${payload.nationalInsuranceNumber.nino}/contributions-and-credits/from/${payload.startTaxYear}/to/${payload.endTaxYear}")
        await(connector.fetchData(payload)).status shouldBe UNPROCESSABLE_ENTITY
      }
    }
  }
}
