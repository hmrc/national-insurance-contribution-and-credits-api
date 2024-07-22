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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{when, withSettings}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.{FakeAuthAction, HipConnector}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors._
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{NICCClass1, NICCClass2, NPSResponse}

import scala.concurrent.Future
import scala.language.postfixOps

class NICCControllerSpec extends AnyFreeSpec with GuiceOneAppPerSuite with OptionValues with ScalaFutures with should.Matchers with BeforeAndAfterEach {
  val mockHipConnector: HipConnector = mock[HipConnector](withSettings().verboseLogging())

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .overrides(
      inject.bind[HipConnector].toInstance(mockHipConnector),
      inject.bind[AuthAction].to[FakeAuthAction]
    )
    .configure(
      "auditing.enabled" -> false
    ).build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockHipConnector)
  }

  def url: String = s"/nicc-service/v1/api/contribution-and-credits"

  val body: JsObject = Json.obj(
    "startTaxYear" -> "2017",
    "endTaxYear" -> "2019",
    "nationalInsuranceNumber" -> "BB000000B",
    "dateOfBirth" -> "1998-04-23",
    "customerCorrelationId" -> "bb6b16c4-8a7b-42aa-a986-1ea5df66f576"
  )

  "return 400 when the request nino is invalid" in {
    val body = Json.obj("dateOfBirth" -> "1998-04-23")


    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(NPSResponse(
        Seq(NICCClass1(2022, "s", "(NONE)", "C1", BigDecimal(99999999999999.98), "COMPLIANCE & YIELD INCOMPLETE", BigDecimal(99999999999999.98))),
        Seq(),
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(400)
  }

  "return 400 when the request taxYear is invalid" in {
    val body = Json.obj("dateOfBirth" -> "1998-04-23")


    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(NPSResponse(
        Seq(NICCClass1(2022, "s", "(NONE)", "C1", BigDecimal(99999999999999.98), "COMPLIANCE & YIELD INCOMPLETE", BigDecimal(99999999999999.98))),
        Seq(),
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(400)
  }

  "return 400 when the body is invalid" in {
    val body = Json.obj("dateOfBirth" -> "199B8-04-23")


    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(NPSResponse(
        Seq(NICCClass1(2022, "s", "(NONE)", "C1", BigDecimal(99999999999999.98), "COMPLIANCE & YIELD INCOMPLETE", BigDecimal(99999999999999.98))),
        Seq(),
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))
    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(400)
  }

  "return 500 when the request is valid but response body is not valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse("" +
        "{" +
        " \"niContribution\": [" +
        "   {" +
        "     \"taxYear\": 2022," +
        "     \"numberOfCredits\": 53," +
        "     \"contributionCreditTypeCode\": \"C2\"," +
        "     \"contributionCreditType\": \"CLASS 2 - NORMAL RATE\"," +
        "     \"class2Or3EarningsFactor\": 99999999999999.98," +
        "     \"class2NicAmount\": 99999999999999.98," +
        "     \"class2Or3CreditStatus\": \"NOT KNOWN/NOT APPLICABLE\"" +
        "   }" +
        " ]," +
        " \"niCredit\": [" +
        "   {" +
        "     \"taxYear\": 2022," +
        "     \"contributionCategoryLetter\": \"s\"," +
        "     \"contributionCategory\": \"(NONE)\"," +
        "     \"totalContribution\": 99999999999999.98," +
        "     \"primaryContribution\": 99999999999999.98," +
        "     \"class1ContributionStatus\": \"COMPLIANCE & YIELD INCOMPLETE\"," +
        "     \"primaryPaidEarnings\": 99999999999999.98" +
        "   }" +
        " ]" +
        "}"),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)

  }

  "return 400 when the request is valid but response is 400, origin is HIP and response body is valid" in {
    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.toJson(HIPErrorResponse("HIP",
        HIPResponse(Seq(HIPFailure("HTTP message not readable", ""), HIPFailure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST"))
        ))),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)

  }

  "return 500 when the request is valid but response is 400, origin is HIP and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse("" +
        "{" +
        "  \"origin\": \"HIP\"," +
        "  \"response\": {" +
        "    \"failures\": [" +
        "      {" +
        "       \"code\": \"HTTP message not readable\", " +
        "       \"reason\": \"\" " +
        "      }," +
        "      {" +
        "        \"code\": \"Constraint Violation - Invalid/Missing input parameter\"," +
        "        \"reason\": \"BAD_REQUEST\"" +
        "      }" +
        "    ]" +
        "  }" +
        "}"),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)

    println(result.header.status)
  }

  "return 400 when the request is valid but response is 400, origin is HoD and response body is valid" in {
    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.toJson(ErrorResponse("HoD",
        Response(Seq(Failure("HTTP message not readable", ""), Failure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST"))
        ))),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)

  }

  "return 500 when the request is valid but response is 400, origin is HoD and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse("" +
        "{" +
        "  \"origin\": \"HoD\"," +
        "  \"response\": {" +
        "    \"failures\": [" +
        "      {" +
        "         \"reason\": \"HTTP message not readable\"," +
        "         \"type\": \"\"" +
        "       }," +
        "      {" +
        "        \"reason\": \"Constraint Violation - Invalid/Missing input parameter\"," +
        "        \"type\": \"BAD_REQUEST\"" +
        "      }" +
        "    ]" +
        "  }" +
        "}"),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
  }

  "return 500 when the request is valid but response is 400 and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse("" +
        "{" +
        " \"errors\": [" +
        "   {" +
        "     \"message\": \"Constraint Violation - Invalid/Missing input parameter\"," +
        "     \"reason\": \"BAD_REQUEST\"" +
        "   }" +
        " ]" +
        "}"),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)

  }

  "return 422 when the request is valid but response is 422 and response body is valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 422,
      json = Json.toJson(Failures(
        Seq(Failure("HTTP message not readable", ""), Failure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST"))
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(UNPROCESSABLE_ENTITY)

  }

  "return 500 when the request is valid but response is 422 and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 422,
      json = Json.parse("" +
        "{" +
        " \"errors\": [" +
        "   {" +
        "     \"message\": \"Constraint Violation - Invalid/Missing input parameter\"," +
        "     \"reason\": \"BAD_REQUEST\"" +
        "   }" +
        " ]" +
        "}"),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)

  }


  "return 500 when the request is valid and response is not expected http status " in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 501,
      json = Json.toJson(NPSResponse(
        Seq(NICCClass1(2022, "s", "(NONE)", "C1", BigDecimal(99999999999999.98), "COMPLIANCE & YIELD INCOMPLETE", BigDecimal(99999999999999.98))),
        Seq(),
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)

  }

  "return 404 when the request is missing a part of the url" in {
    //    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val request = FakeRequest("POST", "/nicc-service/v1/contribution-and-credits")
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(NOT_FOUND)

  }

  "return 400 when the request is missing a body" in {
    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
  }

  "return 200 when the request is valid and response is valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(NPSResponse(
        Seq(NICCClass1(2022, "s", "(NONE)", "C1", BigDecimal(99999999999999.98), "COMPLIANCE & YIELD INCOMPLETE", BigDecimal(99999999999999.98))),
        Seq(NICCClass2(2022, 53, "C1", BigDecimal(99999999999999.98), BigDecimal(99999999999999.98), "NOT KNOWN/NOT APPLICABLE")),
      )),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
  }
}