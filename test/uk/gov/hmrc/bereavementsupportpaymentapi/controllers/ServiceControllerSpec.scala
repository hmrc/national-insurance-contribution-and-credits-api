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

package uk.gov.hmrc.bereavementsupportpaymentapi.controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.bereavementsupportpaymentapi.connectors.HipConnector
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.Validator

class ServiceControllerSpec extends AnyWordSpec with Matchers with MockFactory {

  private val validator = new Validator


  "GET /nationalInsuranceNumber-info" should {
    "return 200" in {
      val mockHipConnector: HipConnector = mock[HipConnector]
      (mockHipConnector.getCitizenInfo _).expects(*).returns("Valid mock response").once()
      val controller = new ServiceController(Helpers.stubControllerComponents(), mockHipConnector)

      val fakeRequest = FakeRequest("GET", "/nationalInsuranceNumber-info")

      val response = controller.getCitizenInfo(fakeRequest)
      status(response) shouldBe Status.OK
    }

    "Correct response body" in {
      val mockHipConnector: HipConnector = mock[HipConnector]
      (mockHipConnector.getCitizenInfo _).expects(*).returns("Valid mock response").once()
      val controller = new ServiceController(Helpers.stubControllerComponents(), mockHipConnector)

      val fakeRequest = FakeRequest("GET", "/nationalInsuranceNumber-info")

      val response = controller.getCitizenInfo(fakeRequest)
      contentAsString(response) shouldBe "Valid mock response"
    }
  }
}
