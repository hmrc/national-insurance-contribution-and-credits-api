/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.*
import play.api.test.Injecting
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class Class2MAReceiptsConnectorItSpec
    extends AnyFreeSpec
    with EitherValues
    with GuiceOneAppPerSuite
    with WireMockHelper
    with Injecting
    with Matchers
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val defaultPatience: PatienceConfig = PatienceConfig(
    timeout = Span(10, Seconds),
    interval = Span(100, Millis)
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.hip.port" -> server.port
      )
      .build()

  private lazy val connector: Class2MAReceiptsConnector = inject[Class2MAReceiptsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "Class2MAReceiptsConnector" - {

    ".fetchClass2MAReceipts" - {

      val testPath                                     = "/class-2/AB123456C/maternity-allowance/receipts"
      val identifier: Identifier                       = Identifier("AB123456C")
      val archived: Option[Boolean]                    = None
      val receiptDate: Option[ReceiptDate]             = None
      val sortType: Option[MaternityAllowanceSortType] = None

      "when the Class2MAReceipts endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val successResponse = Class2MAReceiptsSuccessResponse(
            identifier = Identifier("AB123456C"),
            class2MAReceiptDetails = List()
          )

          val successResponseJson = """{
                                      |"identifier":"AB123456C",
                                      |"class2MAReceiptDetails":[]
                                      |}""".stripMargin

          val responseBody = Json.parse(successResponseJson).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe Right(SuccessResult(Class2MAReceipts, successResponse))
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the Class2MAReceipts endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |    "failures": [
              |      {
              |        "reason": "reason_1",
              |        "code": "400.1"
              |      },
              |      {
              |        "reason": "reason_2",
              |        "code": "400.2"
              |      }
              |    ]
              |  }
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |   "failures": [
              |    {
              |      "type": "Type of Failure",
              |      "reason": "Reason for Failure"
              |    },
              |    {
              |      "type": "Type of ';'",
              |      "reason": "Reason for Failure"
              |    }
              |  ]
              | }
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns FORBIDDEN (403)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Forbidden",
              |   "code":"403.2"
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(FORBIDDEN)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.NotFound, None)
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns UNPROCESSABLE_ENTITY (422)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |"failures":[
              | {
              |   "reason":"Some reason",
              |   "code":"fail code"
              | }
              |]
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.InternalServerError, None)
            )
          )
        }
      }

      "when the Class2MAReceipts endpoint returns SERVICE_UNAVAILABLE (503)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |   "failures": [
              |    {
              |      "type": "Type of Failure",
              |      "reason": "Reason for Failure"
              |    },
              |    {
              |      "type": "Type of ';'",
              |      "reason": "Reason for Failure"
              |    }
              |  ]
              | }
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Class2MAReceipts,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the Class2MAReceipts endpoint returns an unexpected statusCode" - {
        "should map to UnexpectedStatus result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              get(urlEqualTo(testPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            val result =
              connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.Class2MAReceipts,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the Class2MAReceipts endpoint returns malformed JSON" - {
        "should return parsing error" in {
          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{ invalid json structure")
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the Class2MAReceipts endpoint returns valid JSON with missing required fields" - {
        "should return validation error" in {

          val incompleteResponse = """{"identifier":"AB123456C"}"""

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(incompleteResponse)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ValidationError]
        }
      }

      "when the request to the downstream fails unexpectedly" - {
        "should return downstream error" in {
          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
              )
          )

          val result =
            connector.fetchClass2MAReceipts(MA, identifier, archived, receiptDate, sortType).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
