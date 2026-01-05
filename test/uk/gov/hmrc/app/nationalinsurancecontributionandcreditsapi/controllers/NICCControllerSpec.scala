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

package uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{when, withSettings}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.controllers.actions.AuthAction
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.{NICCClass1, NICCClass2, NPSResponse}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.{
  ErrorResponse,
  Failure,
  Failures,
  HIPErrorResponse,
  HIPFailure,
  HIPResponse,
  Response
}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.connectors.FakeAuthAction

import scala.concurrent.Future

class NICCControllerSpec
    extends AnyFreeSpec
    with GuiceOneAppPerSuite
    with OptionValues
    with ScalaFutures
    with should.Matchers
    with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    Span(5, Seconds),
    Span(50, Millis)
  )

  val mockHipConnector: HipConnector = mock[HipConnector](withSettings().verboseLogging())

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .overrides(
      inject.bind[HipConnector].toInstance(mockHipConnector),
      inject.bind[AuthAction].to[FakeAuthAction]
    )
    .configure(
      "auditing.enabled" -> false
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockHipConnector)
  }

  def url: String = s"/contribution-and-credits"

  val body: JsObject = Json.obj(
    "startTaxYear"            -> "2017",
    "endTaxYear"              -> "2019",
    "nationalInsuranceNumber" -> "BB000000B",
    "dateOfBirth"             -> "1998-04-23",
    "customerCorrelationID"   -> "bb6b16c4-8a7b-42aa-a986-1ea5df66f576"
  )

  "return 400 when the request nino is invalid" in {
    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(
        NPSResponse(
          Option(
            Seq(
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              )
            )
          ),
          Option(Seq())
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 400 when the request taxYear is invalid" in {
    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(
        NPSResponse(
          Option(
            Seq(
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              )
            )
          ),
          Option(Seq())
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 400 when the request body is invalid" in {
    val body = Json.parse(
      "" +
        "{" +
        "    \"startTaxYear\": \"20B18\"," +
        "    \"endTaxYear\": \"2023\"," +
        "    \"nationalInsuranceNumber\": \"BB000200A\"," +
        "    \"dateOfBirth\": \"1970-08-31\"," +
        "    \"customerCorrelationID\": \"fbb53666-469c-4d36-8e6d-151ef3c424e1\"" +
        "}"
    )

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(
        NPSResponse(
          Option(
            Seq(
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              )
            )
          ),
          Option(Seq())
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))
    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 200 and empty object when the request is valid but response body is not valid " in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "" +
          "{" +
          " \"niContribution\": [" +
          "   {" +
          "     \"taxYear\": 2022," +
          "     \"numberOfCredits\": 53," +
          "     \"contributionCreditTypeCode\": \"C2\"," +
          "     \"class1ContributionStatus\": \"CLASS 2 - NORMAL RATE\"," +
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
          "     \"class2Or3CreditStatus\": 99999999999999.98," +
          "     \"class1ContributionStatus\": \"COMPLIANCE & YIELD INCOMPLETE\"," +
          "     \"primaryPaidEarnings\": 99999999999999.98" +
          "   }" +
          " ]" +
          "}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 400 when the request is valid but response is 400, origin is HIP and response body is valid" in {
    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.toJson(
        HIPErrorResponse(
          "HIP",
          HIPResponse(
            Seq(
              HIPFailure("HTTP message not readable", ""),
              HIPFailure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST")
            )
          )
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 500 when the request is valid but response is 400, origin is HIP and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse(
        "" +
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
          "}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 400 when the request is valid but response is 400, origin is HoD and response body is valid" in {
    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.toJson(
        ErrorResponse(
          "HoD",
          Response(
            Seq(
              Failure("HTTP message not readable", ""),
              Failure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST")
            )
          )
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 500 when the request is valid but response is 400, origin is HoD and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse(
        "" +
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
          "}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 500 when the request is valid but response is 400 and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 400,
      json = Json.parse(
        "" +
          "{" +
          " \"errors\": [" +
          "   {" +
          "     \"message\": \"Constraint Violation - Invalid/Missing input parameter\"," +
          "     \"reason\": \"BAD_REQUEST\"" +
          "   }" +
          " ]" +
          "}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 422 when the request is valid but response is 422 and response body is valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 422,
      json = Json.toJson(
        Failures(
          Seq(
            Failure("HTTP message not readable", ""),
            Failure("Constraint Violation - Invalid/Missing input parameter", "BAD_REQUEST")
          )
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(UNPROCESSABLE_ENTITY)
    result.header.headers.contains("correlationId")
  }

  "return 500 when the request is valid but response is 422 and response body is invalid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 422,
      json = Json.parse(
        "" +
          "{" +
          " \"errors\": [" +
          "   {" +
          "     \"message\": \"Constraint Violation - Invalid/Missing input parameter\"," +
          "     \"reason\": \"BAD_REQUEST\"" +
          "   }" +
          " ]" +
          "}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 500 when the request is valid and response is not expected http status " in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 501,
      json = Json.toJson(
        NPSResponse(
          Option(
            Seq(
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              )
            )
          ),
          Option(Seq())
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 404 when the request is valid but a valid 404 Not Found response is returned" in {
    //    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 404,
      json = Json.toJson(
        Failure("Not Found", "404")
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(NOT_FOUND)
    result.header.headers.contains("correlationId")
  }

  "return 404 when the request is valid but an invalid 404 Not Found response is returned" in {
    //    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 404,
      json = Json.toJson(""),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(INTERNAL_SERVER_ERROR)
    result.header.headers.contains("correlationId")
  }

  "return 404 when the request is missing a part of the url" in {
    //    val body = Json.obj("dateOfBirth" -> "1998-04-23")

    val request = FakeRequest("POST", "/contribution")
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue

    result.header.status should be(NOT_FOUND)
    result.header.headers.contains("correlationId")
  }

  "return 400 when the request is missing a body" in {
    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")

    val result = route(app, request).value.futureValue

    result.header.status should be(BAD_REQUEST)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid and response is valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.toJson(
        NPSResponse(
          Option(
            Seq(
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              ),
              NICCClass1(
                Some(2022),
                Some("s"),
                Some("(NONE)"),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some("COMPLIANCE & YIELD INCOMPLETE"),
                Some(BigDecimal(99999999999999.98))
              )
            )
          ),
          Some(
            Seq(
              NICCClass2(
                Some(2022),
                Some(53),
                Some("C1"),
                Some(BigDecimal(99999999999999.98)),
                Some(BigDecimal(99999999999999.98)),
                Some("NOT KNOWN/NOT APPLICABLE")
              )
            )
          )
        )
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid and response is json and is valid" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "{\"niClass1\":[{\"taxYear\":2018,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":3189.12,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":35000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2019,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2020,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2021,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2022,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"CS\"},{\"taxYear\":2023,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"}]}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid and response json is missing fields (tax year)" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "{\"niClass1\":[{\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":3189.12,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":35000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2019,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2020,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2021,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2022,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"CS\"},{\"taxYear\":2023,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"}]}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid and response json is missing fields (primaryPaidEarnings)" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "{\"niClass1\":[{\"taxYear\":2018,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":3189.12,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":35000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2019,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"contributionCreditType\":\"EON\"},{\"taxYear\":2020,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2021,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2022,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"CS\"},{\"taxYear\":2023,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"}]}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid and response json is missing fields (contributionCategory)" in {

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "{\"niClass1\":[{\"taxYear\":2018,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":3189.12,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":35000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2019,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"contributionCreditType\":\"EON\"},{\"taxYear\":2020,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2021,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2022,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"CS\"},{\"taxYear\":2023,\"contributionCategoryLetter\":\"A\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"}]}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(body)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

  "return 200 when the request is valid but missing a customerCorrelationID and response is json and is valid" in {

    val noCorIdBody: JsObject = Json.obj(
      "startTaxYear"            -> "2017",
      "endTaxYear"              -> "2019",
      "nationalInsuranceNumber" -> "BB000000B",
      "dateOfBirth"             -> "1998-04-23"
    )

    val expectedResponseObject: HttpResponse = HttpResponse.apply(
      status = 200,
      json = Json.parse(
        "{\"niClass1\":[{\"taxYear\":2018,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":3189.12,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":35000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2019,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"},{\"taxYear\":2020,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2021,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"C1\"},{\"taxYear\":2022,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"CS\"},{\"taxYear\":2023,\"contributionCategoryLetter\":\"A\",\"contributionCategory\":\"STANDARD RATE\",\"primaryContribution\":1964.16,\"class1ContributionStatus\":\"VALID\",\"primaryPaidEarnings\":25000.00,\"contributionCreditType\":\"EON\"}]}"
      ),
      headers = Map.empty
    )
    when(mockHipConnector.fetchData(any(), any())(any())).thenReturn(Future.successful(expectedResponseObject))

    val request = FakeRequest("POST", url)
      .withHeaders(CONTENT_TYPE -> "application/json")
      .withJsonBody(noCorIdBody)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)
    result.header.headers.contains("correlationId")
  }

}
