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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector

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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus
}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class MarriageDetailsConnectorItSpec
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

  private lazy val connector: MarriageDetailsConnector =
    inject[MarriageDetailsConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  private val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
    marriageDetails = MarriageDetailsSuccess.MarriageDetails(
      activeMarriage = MarriageDetailsSuccess.ActiveMarriage(false),
      marriageDetailsList = Some(
        List(
          MarriageDetailsSuccess.MarriageDetailsListElement(
            sequenceNumber = MarriageDetailsSuccess.SequenceNumber(1),
            status = CivilPartner,
            startDate = Some(MarriageStartDate(LocalDate.parse("2000-05-20"))),
            startDateStatus = Some(MarriageStartDateStatus.Verified),
            endDate = Some(MarriageEndDate(LocalDate.parse("2015-08-01"))),
            endDateStatus = Some(MarriageEndDateStatus.Verified),
            spouseIdentifier = Some(Identifier("AA987654C")),
            spouseForename = Some(SpouseForename("Jordan")),
            spouseSurname = Some(SpouseSurname("Kurt")),
            separationDate = Some(SeparationDate(LocalDate.parse("2014-01-01"))),
            reconciliationDate = Some(ReconciliationDate(LocalDate.parse("2014-12-01")))
          )
        )
      ),
      _links = Some(
        MarriageDetailsSuccess.Links(
          MarriageDetailsSuccess.SelfLink(
            Some(MarriageDetailsSuccess.Href("/individual/AA123456A/marriage-cp")),
            Some(MarriageDetailsSuccess.Methods.get)
          )
        )
      )
    )
  )

  private val successResponseJson =
    """{
      |  "marriageDetails": {
      |    "activeMarriage": false,
      |    "marriageDetailsList": [
      |      {
      |        "sequenceNumber": 1,
      |        "status": "CIVIL PARTNER",
      |        "startDate": "2000-05-20",
      |        "startDateStatus": "VERIFIED",
      |        "endDate": "2015-08-01",
      |        "endDateStatus": "VERIFIED",
      |        "spouseIdentifier": "AA987654C",
      |        "spouseForename": "Jordan",
      |        "spouseSurname": "Kurt",
      |        "separationDate": "2014-01-01",
      |        "reconciliationDate": "2014-12-01"
      |      }
      |    ],
      |    "_links": {
      |      "self": {
      |        "href": "/individual/AA123456A/marriage-cp",
      |        "methods": "get"
      |      }
      |    }
      |  }
      |}""".stripMargin

  "MarriageDetailsConnector" - {

    ".fetchMarriageDetails" - {

      val testPath               = "/individual/AB123456C/marriage-cp"
      val identifier: Identifier = Identifier("AB123456C")

      "when the MarriageDetails endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {

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
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          result shouldBe Right(
            SuccessResult(ApiName.MarriageDetails, marriageDetailsSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the MarriageDetails endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns FORBIDDEN (403)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "failures": [
              |    {
              |      "reason": "Forbidden",
              |      "code": "403.2"
              |    }
              |  ]
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
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Not found",
              |   "code":"404"
              |}""".stripMargin

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
                  .withBody(errorResponse)
              )
          )

          val result =
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.NotFound, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(errorResponse)
              )
          )

          val result =
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns SERVICE_UNAVAILABLE (503)" - {
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
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the MarriageDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
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

          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
                  .withHeader("Content-Type", "application/json")
                  .withBody(errorResponse)
              )
          )

          val result =
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.MarriageDetails,
              ErrorReport(NpsNormalizedError.InternalServerError, Some(response))
            )
          )
        }
      }

      "when the MarriageDetails endpoint returns an unexpected statusCode" - {
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
                .fetchMarriageDetails(MA, identifier, None, None, None)
                .value
                .futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.MarriageDetails,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the MarriageDetails endpoint returns malformed JSON" - {
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
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the MarriageDetails endpoint returns valid JSON with missing required fields" - {
        "should return validation error" in {

          val incompleteResponse =
            """{
              |  "numberOfQualifyingYears": 35,
              |  "nonQualifyingYears": 5,
              |  "yearsToFinalRelevantYear": 3
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
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

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
            connector
              .fetchMarriageDetails(MA, identifier, None, None, None)
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
