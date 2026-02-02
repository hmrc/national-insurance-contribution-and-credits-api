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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.connector

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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponse422Special,
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  Callback,
  LiabilitySummaryDetailsSuccessResponse,
  OccurrenceNumber
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class LiabilitySummaryDetailsConnectorItSpec
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

  private lazy val connector: LiabilitySummaryDetailsConnector = inject[LiabilitySummaryDetailsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "LiabilitySummaryDetailsConnector" - {

    ".fetchLiabilitySummaryDetails" - {

      val testPath               = "/person/AB123456C/liability-summary/ABROAD"
      val identifier: Identifier = Identifier("AB123456C")
      val liabilitySearchCategoryHyphenated: LiabilitySearchCategoryHyphenated =
        LiabilitySearchCategoryHyphenated.Abroad
      val occurrenceNumber: Option[LiabilitiesOccurrenceNumber] = None
      val typeFilter: Option[LiabilitySearchCategoryHyphenated] = None
      val earliestStartDate: Option[LocalDate]                  = None
      val liabilityStartDate: Option[LocalDate]                 = None
      val liabilityEndDate: Option[LocalDate]                   = None

      "when the LiabilitySummaryDetails endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val successResponse = LiabilitySummaryDetailsSuccessResponse(
            liabilityDetailsList = Some(List()),
            callback = Some(Callback(""))
          )

          val successResponseJson = """{
                                      |"liabilityDetailsList": [],
                                      |"liabilityEmploymentDetailsList":[],
                                      |"callback": ""
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          result shouldBe Right(SuccessResult(Liabilities, successResponse))
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the LiabilitySummaryDetails endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns FORBIDDEN (403)" - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns NOT_FOUND (404)" - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.NotFound, None)
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                            = implicitly[Reads[NpsErrorResponse422Special]]
          val response: NpsErrorResponse422Special = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {

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

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withBody(errorResponse)
              )
          )

          val result =
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.InternalServerError, Some(response))
            )
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns an SERVICE_UNAVAILABLE (503)" - {
        "should map to InternalServerError result" in {

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

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(errorResponse)
              )
          )

          val result =
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.Liabilities,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )
        }
      }

      "when the LiabilitySummaryDetails endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

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
              connector
                .fetchLiabilitySummaryDetails(
                  MA,
                  identifier,
                  liabilitySearchCategoryHyphenated,
                  occurrenceNumber,
                  typeFilter,
                  earliestStartDate,
                  liabilityStartDate,
                  liabilityEndDate
                )
                .value
                .futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.Liabilities,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the LiabilitySummaryDetails endpoint returns malformed JSON" - {
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
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
            connector
              .fetchLiabilitySummaryDetails(
                MA,
                identifier,
                liabilitySearchCategoryHyphenated,
                occurrenceNumber,
                typeFilter,
                earliestStartDate,
                liabilityStartDate,
                liabilityEndDate
              )
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
