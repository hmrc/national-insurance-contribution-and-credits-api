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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.connector

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
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.Injecting
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.LongTermBenefitNotes
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.GYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.response.LongTermBenefitNotesSuccess.*
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class LongTermBenefitNotesConnectorItSpec
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

  private lazy val connector: LongTermBenefitNotesConnector =
    inject[LongTermBenefitNotesConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "LongTermBenefitNotesConnector" - {

    ".fetchLongTermBenefitNotes" - {

      val testPath                                 = "/long-term-benefits/AB123456C/calculation/ALL/notes/1123232"
      val identifier: Identifier                   = Identifier("AB123456C")
      val longTermBenefitType: LongTermBenefitType = LongTermBenefitType.All
      val seqNo: Int                               = 1123232

      "when the LongTermBenefitNotes endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val longTermBenefitNotesSuccessResponse = LongTermBenefitNotesSuccessResponse(List.empty)

          val successResponseJson =
            """{
              | "longTermBenefitNotes": []
              |}
              |""".stripMargin

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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamSuccessResponse(LongTermBenefitNotes, longTermBenefitNotesSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the LongTermBenefitNotes endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.BadRequest)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.BadRequest)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns FORBIDDEN (403)" - {
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.AccessForbidden)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "code":"404",
              |   "reason":"Not Found"
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.NotFound)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns UNPROCESSABLE_ENTITY (422)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "failures":[
              |    {
              |      "reason":"Reason",
              |      "code":"Code"
              |    }
              |  ]
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.UnprocessableEntity)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |    "failures": [
              |      {
              |        "type": "blah_1",
              |        "reason": "reason_1"
              |      },
              |      {
              |        "type": "blah_2",
              |        "reason": "reason_2"
              |      },
              |      {
              |        "type": "blah_3",
              |        "reason": "reason_3"
              |      }
              |    ]
              |  }
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.InternalServerError)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns SERVICE_UNAVAILABLE (503)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |    "failures": [
              |      {
              |        "type": "blah_1",
              |        "reason": "reason_1"
              |      },
              |      {
              |        "type": "blah_2",
              |        "reason": "reason_2"
              |      },
              |      {
              |        "type": "blah_3",
              |        "reason": "reason_3"
              |      }
              |    ]
              |  }
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.ServiceUnavailable)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LongTermBenefitNotes endpoint returns an unexpected statusCode" - {
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
              connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

            result shouldBe Right(
              DownstreamErrorReport(LongTermBenefitNotes, NpsNormalizedError.UnexpectedStatus(statusCode))
            )
          }

        }
      }

      "when the LongTermBenefitNotes endpoint returns malformed JSON" - {
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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the LongTermBenefitNotes endpoint returns valid JSON with missing required fields" - {
        "should return validation error" in {

          val incompleteResponse =
            """{
              |}""".stripMargin

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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

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
            connector.fetchLongTermBenefitNotes(GYSP, identifier, longTermBenefitType, seqNo).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
