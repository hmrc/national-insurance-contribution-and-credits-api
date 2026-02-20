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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, postRequestedFor, urlEqualTo}
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
import play.api.test.Helpers.{
  BAD_REQUEST,
  FORBIDDEN,
  INTERNAL_SERVER_ERROR,
  METHOD_NOT_ALLOWED,
  MULTIPLE_CHOICES,
  MULTI_STATUS,
  NOT_FOUND,
  OK,
  SERVICE_UNAVAILABLE,
  UNPROCESSABLE_ENTITY
}
import play.api.test.Injecting
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.*
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class NiContributionsAndCreditsConnectorItSpec
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

  private lazy val connector: NiContributionsAndCreditsConnector = inject[NiContributionsAndCreditsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "NiContributionsAndCreditsConnector" - {

    ".fetchContributionsAndCredits" - {

      val testPath = "/national-insurance/contributions-and-credits"

      val requestBody = NiContributionsAndCreditsRequest(
        Identifier(""),
        DateOfBirth(LocalDate.parse("2025-10-10")),
        StartTaxYear(2025),
        EndTaxYear(2026)
      )

      "when the NiContributionsAndCredits endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val successResponse = NiContributionsAndCreditsSuccessResponse(
            Some(TotalGraduatedPensionUnits(BigDecimal("100.0"))),
            Some(
              List(
                Class1ContributionAndCredits(
                  taxYear = Some(TaxYear(2022)),
                  numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                  contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                  contributionCategory = Some(ContributionCategory.None),
                  contributionCreditType = Some(NiContributionCreditType.C1),
                  primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                  class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                  creditSource = Some(CreditSource.NotKnown),
                  employerName = Some(EmployerName("ipOpMs")),
                  latePaymentPeriod = Some(LatePaymentPeriod.L)
                )
              )
            ),
            Some(
              List(
                Class2ContributionAndCredits(
                  taxYear = Some(TaxYear(2022)),
                  numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                  contributionCreditType = Some(NiContributionCreditType.C1),
                  class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
                  class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
                  class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                  creditSource = Some(CreditSource.NotKnown),
                  latePaymentPeriod = Some(LatePaymentPeriod.L)
                )
              )
            )
          )

          val successResponseJson =
            """{
              |  "totalGraduatedPensionUnits": 100,
              |  "class1ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCategoryLetter": "U",
              |      "contributionCategory": "(NONE)",
              |      "contributionCreditType": "C1",
              |      "primaryContribution": 99999999999999.98,
              |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
              |      "primaryPaidEarnings": 99999999999999.98,
              |      "creditSource": "NOT KNOWN",
              |      "employerName": "ipOpMs",
              |      "latePaymentPeriod": "L"
              |    }
              |  ],
              |  "class2ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCreditType": "C1",
              |      "class2Or3EarningsFactor": 99999999999999.98,
              |      "class2NIContributionAmount": 99999999999999.98,
              |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
              |      "creditSource": "NOT KNOWN",
              |      "latePaymentPeriod": "L"
              |    }
              |  ]
              |}""".stripMargin

          val responseBody = Json.parse(successResponseJson).toString()

          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          result shouldBe Right(SuccessResult(NiContributionAndCredits, successResponse))
          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns FORBIDDEN (403)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Forbidden",
              |   "code":"403.2"
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(FORBIDDEN)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.NotFound, None)
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.InternalServerError, None)
            )
          )
        }
      }
      "when the NiContributionsAndCredits endpoint returns an INTERNAL_SERVER_ERROR (503)" - {

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

        "should map to Service unavailable result" in {
          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(errorResponse)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.NiContributionAndCredits,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              post(urlEqualTo(testPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the NiContributionsAndCredits endpoint returns malformed JSON" - {
        "should return parsing error" in {
          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{ invalid json structure")
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the request to the downstream fails unexpectedly" - {
        "should return downstream error" in {
          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
              )
          )

          val result = connector.fetchContributionsAndCredits(MA, requestBody).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
