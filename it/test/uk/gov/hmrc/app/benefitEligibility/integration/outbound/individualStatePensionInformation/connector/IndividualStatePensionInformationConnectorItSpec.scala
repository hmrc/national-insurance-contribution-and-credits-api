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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.connector

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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.IndividualStatePension
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponseHipOrigin,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class IndividualStatePensionInformationConnectorItSpec
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

  private lazy val connector: IndividualStatePensionInformationConnector =
    inject[IndividualStatePensionInformationConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "IndividualStatePensionInformationConnector" - {

    ".fetchIndividualStatePensionInformation" - {

      val testPath               = "/long-term-benefits/AB123456C/contributions"
      val identifier: Identifier = Identifier("AB123456C")

      "when the IndividualStatePension endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val individualStatePensionInformationSuccessResponse = IndividualStatePensionInformationSuccessResponse(
            identifier = identifier,
            numberOfQualifyingYears = Some(NumberOfQualifyingYears(35)),
            nonQualifyingYears = Some(NonQualifyingYears(5)),
            yearsToFinalRelevantYear = Some(YearsToFinalRelevantYear(3)),
            nonQualifyingYearsPayable = Some(NonQualifyingYearsPayable(2)),
            pre1975CCCount = Some(Pre1975CCCount(156)),
            dateOfEntry = Some(DateOfEntry("1975-04-06")),
            contributionsByTaxYear = Some(
              List(
                ContributionsByTaxYear(
                  taxYear = Some(TaxYear(2022)),
                  qualifyingTaxYear = Some(QualifyingTaxYear(true)),
                  payableAccepted = Some(PayableAccepted(false)),
                  amountNeeded = Some(AmountNeeded(BigDecimal("1250.50"))),
                  classThreePayable = Some(ClassThreePayable(BigDecimal("824.20"))),
                  classThreePayableBy = Some(ClassThreePayableBy("2028-04-05")),
                  classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2030-04-05")),
                  classTwoPayable = Some(ClassTwoPayable(BigDecimal("164.25"))),
                  classTwoPayableBy = Some(ClassTwoPayableBy("2028-01-31")),
                  classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2030-01-31")),
                  classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(12)),
                  totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("3456.78"))),
                  niEarnings = Some(NiEarnings(BigDecimal("45000.00"))),
                  coClassOnePaid = Some(CoClassOnePaid(BigDecimal("1234.56"))),
                  totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("52000.00"))),
                  niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
                  niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
                  underInvestigationFlag = Some(UnderInvestigationFlag(true)),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("48500.75"))),
                  otherCredits = Some(
                    List(
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1Credit),
                        creditSourceType = Some(CreditSourceType.JsaTapeInput),
                        contributionCreditCount = Some(ContributionCreditCount(15))
                      ),
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class3Credit),
                        creditSourceType = Some(CreditSourceType.CarersCredit),
                        contributionCreditCount = Some(ContributionCreditCount(52))
                      ),
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class2NormalRate),
                        creditSourceType = Some(CreditSourceType.ChildBenefit),
                        contributionCreditCount = Some(ContributionCreditCount(-5))
                      )
                    )
                  )
                ),
                ContributionsByTaxYear(
                  taxYear = Some(TaxYear(2023)),
                  qualifyingTaxYear = Some(QualifyingTaxYear(false)),
                  payableAccepted = Some(PayableAccepted(true)),
                  amountNeeded = Some(AmountNeeded(BigDecimal("2100.75"))),
                  classThreePayable = Some(ClassThreePayable(BigDecimal("876.80"))),
                  classThreePayableBy = Some(ClassThreePayableBy("2029-04-05")),
                  classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2031-04-05")),
                  classTwoPayable = Some(ClassTwoPayable(BigDecimal("175.60"))),
                  classTwoPayableBy = Some(ClassTwoPayableBy("2029-01-31")),
                  classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2031-01-31")),
                  classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(35)),
                  totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("2987.45"))),
                  niEarnings = Some(NiEarnings(BigDecimal("38500.25"))),
                  coClassOnePaid = Some(CoClassOnePaid(BigDecimal("987.65"))),
                  totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("41250.80"))),
                  niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(42)),
                  niEarningsVoluntary = Some(NiEarningsVoluntary(15)),
                  underInvestigationFlag = Some(UnderInvestigationFlag(false)),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("39875.90"))),
                  otherCredits = Some(
                    List(
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1Credit),
                        creditSourceType = Some(CreditSourceType.UniversalCredit),
                        contributionCreditCount = Some(ContributionCreditCount(26))
                      ),
                      OtherCredits(
                        contributionCreditType =
                          Some(IndividualStatePensionContributionCreditType.Class2VoluntaryDevelopmentWorkerRateA),
                        creditSourceType = Some(CreditSourceType.StatutoryMaternityPayCredit),
                        contributionCreditCount = Some(ContributionCreditCount(12))
                      ),
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class3RateC),
                        creditSourceType = Some(CreditSourceType.ModSpouseCivilPartnersCredits),
                        contributionCreditCount = Some(ContributionCreditCount(39))
                      ),
                      OtherCredits(
                        contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1EmployeeOnly),
                        creditSourceType = Some(CreditSourceType.SharedParentalPay),
                        contributionCreditCount = Some(ContributionCreditCount(-3))
                      )
                    )
                  )
                )
              )
            )
          )

          val successResponseJson =
            """{
              |  "identifier": "AB123456C",
              |  "numberOfQualifyingYears": 35,
              |  "nonQualifyingYears": 5,
              |  "yearsToFinalRelevantYear": 3,
              |  "nonQualifyingYearsPayable": 2,
              |  "pre1975CCCount": 156,
              |  "dateOfEntry": "1975-04-06",
              |  "contributionsByTaxYear": [
              |    {
              |      "taxYear": 2022,
              |      "qualifyingTaxYear": true,
              |      "payableAccepted": false,
              |      "amountNeeded": 1250.50,
              |      "classThreePayable": 824.20,
              |      "classThreePayableBy": "2028-04-05",
              |      "classThreePayableByPenalty": "2030-04-05",
              |      "classTwoPayable": 164.25,
              |      "classTwoPayableBy": "2028-01-31",
              |      "classTwoPayableByPenalty": "2030-01-31",
              |      "classTwoOutstandingWeeks": 12,
              |      "totalPrimaryContributions": 3456.78,
              |      "niEarnings": 45000.00,
              |      "coClassOnePaid": 1234.56,
              |      "totalPrimaryEarnings": 52000.00,
              |      "niEarningsSelfEmployed": 25,
              |      "niEarningsVoluntary": 8,
              |      "underInvestigationFlag": true,
              |      "primaryPaidEarnings": 48500.75,
              |      "otherCredits": [
              |        {
              |          "contributionCreditType": "CLASS 1 CREDIT",
              |          "creditSourceType": "JSA TAPE INPUT",
              |          "contributionCreditCount": 15
              |        },
              |        {
              |          "contributionCreditType": "CLASS 3 CREDIT",
              |          "creditSourceType": "CARER'S CREDIT",
              |          "contributionCreditCount": 52
              |        },
              |        {
              |          "contributionCreditType": "CLASS 2 - NORMAL RATE",
              |          "creditSourceType": "CHILD BENEFIT",
              |          "contributionCreditCount": -5
              |        }
              |      ]
              |    },
              |    {
              |      "taxYear": 2023,
              |      "qualifyingTaxYear": false,
              |      "payableAccepted": true,
              |      "amountNeeded": 2100.75,
              |      "classThreePayable": 876.80,
              |      "classThreePayableBy": "2029-04-05",
              |      "classThreePayableByPenalty": "2031-04-05",
              |      "classTwoPayable": 175.60,
              |      "classTwoPayableBy": "2029-01-31",
              |      "classTwoPayableByPenalty": "2031-01-31",
              |      "classTwoOutstandingWeeks": 35,
              |      "totalPrimaryContributions": 2987.45,
              |      "niEarnings": 38500.25,
              |      "coClassOnePaid": 987.65,
              |      "totalPrimaryEarnings": 41250.80,
              |      "niEarningsSelfEmployed": 42,
              |      "niEarningsVoluntary": 15,
              |      "underInvestigationFlag": false,
              |      "primaryPaidEarnings": 39875.90,
              |      "otherCredits": [
              |        {
              |          "contributionCreditType": "CLASS 1 CREDIT",
              |          "creditSourceType": "UNIVERSAL CREDIT",
              |          "contributionCreditCount": 26
              |        },
              |        {
              |          "contributionCreditType": "CLASS 2 - VOLUNTARY DEVELOPMENT WORKER'S RATE A",
              |          "creditSourceType": "STATUTORY MATERNITY PAY CREDIT",
              |          "contributionCreditCount": 12
              |        },
              |        {
              |          "contributionCreditType": "CLASS 3 - RATE C",
              |          "creditSourceType": "MOD SPOUSE/CIVIL PARTNER'S CREDITS",
              |          "contributionCreditCount": 39
              |        },
              |        {
              |          "contributionCreditType": "CLASS 1- EMPLOYEE ONLY",
              |          "creditSourceType": "SHARED PARENTAL PAY",
              |          "contributionCreditCount": -3
              |        }
              |      ]
              |    }
              |  ]
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          result shouldBe Right(
            SuccessResult(IndividualStatePension, individualStatePensionInformationSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the IndividualStatePension endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the IndividualStatePension endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the IndividualStatePension endpoint returns FORBIDDEN (403)" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the IndividualStatePension endpoint returns NOT_FOUND (404)" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.NotFound, None)
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the IndividualStatePension endpoint returns SERVICE_UNAVAILABLE (503)" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the IndividualStatePension endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )

          val result =
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          result shouldBe Right(
            FailureResult(
              ApiName.IndividualStatePension,
              ErrorReport(NpsNormalizedError.InternalServerError, None)
            )
          )
        }
      }

      "when the IndividualStatePension endpoint returns an unexpected statusCode" - {
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
              connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.IndividualStatePension,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the IndividualStatePension endpoint returns malformed JSON" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the IndividualStatePension endpoint returns valid JSON with missing required fields" - {
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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

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
            connector.fetchIndividualStatePensionInformation(MA, identifier).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
