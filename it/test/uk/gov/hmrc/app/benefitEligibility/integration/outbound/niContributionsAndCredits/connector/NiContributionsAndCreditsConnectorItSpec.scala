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
import play.api.libs.json.Json
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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.reqeust.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.*
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
            List(
              NicClass1(
                taxYear = Some(TaxYear(2022)),
                contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                contributionCategory = Some(ContributionCategory.None),
                contributionCreditType = Some(ContributionCreditType.C1),
                primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                creditSource = Some(CreditSource.NotKnown),
                employerName = Some(EmployerName("ipOpMs")),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            ),
            List(
              NicClass2(
                taxYear = Some(TaxYear(2022)),
                noOfCreditsAndConts = Some(NumberOfCreditsAndContributions(53)),
                contributionCreditType = Some(ContributionCreditType.C1),
                class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
                class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          )

          val successResponseJson =
            """{
              |  "niClass1": [
              |    {
              |      "taxYear": 2022,
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
              |  "niClass2": [
              |    {
              |      "taxYear": 2022,
              |      "noOfCreditsAndConts": 53,
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(DownstreamSuccessResponse(NiContributionAndCredits, successResponse))
          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (400)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |"failures":[
              | {
              |   "reason":"Some reason",
              |   "code":"400.2"
              | }
              |]
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.BadRequest)
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (403)" - {
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.AccessForbidden)
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (404)" - {
        "should parse error response and map to result" in {

          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
              )
          )

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.NotFound)
          )

          server.verify(
            postRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns BAD_REQUEST (422)" - {
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.UnprocessableEntity)
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe Right(
            DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.InternalServerError)
          )
        }
      }

      "when the NiContributionsAndCredits endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED, SERVICE_UNAVAILABLE)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              post(urlEqualTo(testPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

            result shouldBe Right(
              DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.UnexpectedStatus(statusCode))
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the NiContributionsAndCredits endpoint returns valid JSON with missing required fields" - {
        "should return validation error" in {

          val incompleteResponse = """{"identifier":"AB123456C"}"""

          server.stubFor(
            post(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(incompleteResponse)
              )
          )

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ValidationError]
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

          val result = connector.fetchContributionsAndCredits(requestBody).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DownstreamError]
        }
      }

    }
  }

}
