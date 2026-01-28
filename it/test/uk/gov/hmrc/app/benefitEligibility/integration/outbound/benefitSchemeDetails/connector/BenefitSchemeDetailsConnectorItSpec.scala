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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.connector

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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

class BenefitSchemeDetailsConnectorItSpec
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

  private lazy val connector: BenefitSchemeDetailsConnector =
    inject[BenefitSchemeDetailsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "BenefitSchemeDetailsConnector" - {

    ".fetchBenefitSchemeDetails" - {

      val testPath               = "/benefit-scheme/AB123456C/benefit-scheme-details/S2345678C"
      val identifier: Identifier = Identifier("AB123456C")

      "when the BenefitSchemeDetails endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val benefitSchemeDetailsSuccessResponse = BenefitSchemeDetailsSuccessResponse(
            benefitSchemeDetails = BenefitSchemeDetails(
              magneticTapeNumber = Some(MagneticTapeNumber(54321)),
              schemeName = Some(BenefitSchemeName("EXAMPLE PENSION SCHEME")),
              schemeStartDate = Some(SchemeStartDate("1985-04-06")),
              schemeCessationDate = Some(SchemeCessationDate("2024-12-31")),
              contractedOutDeductionExtinguishedDate = Some(ContractedOutDeductionExtinguishedDate("2024-12-31")),
              paymentSuspensionDate = Some(PaymentSuspensionDate("2024-10-01")),
              recoveriesSuspendedDate = Some(RecoveriesSuspendedDate("2024-10-01")),
              paymentRestartDate = Some(PaymentRestartDate("2024-10-01")),
              recoveriesRestartedDate = Some(RecoveriesRestartedDate("2024-10-01")),
              schemeNature = Some(UnitTrusts),
              benefitSchemeInstitution = Some(BenefitSchemeInstitutionType.UnitTrust),
              accruedGMPLiabilityServiceDate = Some(AccruedGMPLiabilityServiceDate("1990-04-06")),
              rerouteToSchemeCessation = Some(RerouteToSchemeCessation.ReRouteToCessation),
              statementInhibitor = Some(StatementInhibitor.Set),
              certificateCancellationDate = Some(CertificateCancellationDate("2024-12-31")),
              suspendedDate = Some(SuspendedDate("2024-10-01")),
              isleOfManInterest = Some(IsleOfManInterest(false)),
              schemeWindingUp = Some(SchemeWindingUp(true)),
              revaluationRateSequenceNumber = Some(RevaluationRateSequenceNumber(12)),
              benefitSchemeStatus = Some(BenefitSchemeStatus.BlockOnProvider),
              dateFormallyCertified = Some(DateFormallyCertified("1985-04-06")),
              privatePensionSchemeSanctionDate = Some(PrivatePensionSchemeSanctionDate("1985-04-06")),
              currentOptimisticLock = CurrentOptimisticLock(4),
              schemeConversionDate = Some(SchemeConversionDate("2024-12-31")),
              schemeInhibitionStatus = SchemeInhibitionStatus.ConvertedStakeholderPension,
              reconciliationDate = Some(ReconciliationDate("2025-03-31")),
              schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
            ),
            schemeAddressDetailsList = List(
              SchemeAddressDetails(
                schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
                schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
                schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
                schemeAddressEndDate = Some(SchemeAddressEndDate("2024-12-31")),
                country = Some(Country.Scotland),
                areaDiallingCode = Some(AreaDiallingCode.Code0131), // Note: This would need to be added to the enum
                schemeTelephoneNumber = Some(SchemeTelephoneNumber("0131 000 0000")),
                schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C"),
                benefitSchemeAddressDetails = Some(
                  BenefitSchemeAddressDetails(
                    schemeAddressLine1 = Some(SchemeAddressLine1("1 Sample Road")),
                    schemeAddressLine2 = Some(SchemeAddressLine2("Unit 2")),
                    schemeAddressLocality = Some(SchemeAddressLocality("Old Quarter")),
                    schemeAddressPostalTown = Some(SchemeAddressPostalTown("Exampleburgh")),
                    schemePostcode = Some(SchemePostcode("EX2 2EX"))
                  )
                )
              )
            )
          )

          val successResponseJson =
            """{
              |  "benefitSchemeDetails": {
              |    "magneticTapeNumber": 54321,
              |    "schemeName": "EXAMPLE PENSION SCHEME",
              |    "schemeStartDate": "1985-04-06",
              |    "schemeCessationDate": "2024-12-31",
              |    "contractedOutDeductionExtinguishedDate": "2024-12-31",
              |    "paymentSuspensionDate": "2024-10-01",
              |    "recoveriesSuspendedDate": "2024-10-01",
              |    "paymentRestartDate": "2024-10-01",
              |    "recoveriesRestartedDate": "2024-10-01",
              |    "schemeNature": "UNIT TRUSTS",
              |    "benefitSchemeInstitution": "UNIT TRUST",
              |    "accruedGMPLiabilityServiceDate": "1990-04-06",
              |    "rerouteToSchemeCessation": "RE-ROUTE TO CESSATION",
              |    "statementInhibitor": "SET",
              |    "certificateCancellationDate": "2024-12-31",
              |    "suspendedDate": "2024-10-01",
              |    "isleOfManInterest": false,
              |    "schemeWindingUp": true,
              |    "revaluationRateSequenceNumber": 12,
              |    "benefitSchemeStatus": "BLOCK ON PROVIDER",
              |    "dateFormallyCertified": "1985-04-06",
              |    "privatePensionSchemeSanctionDate": "1985-04-06",
              |    "currentOptimisticLock": 4,
              |    "schemeConversionDate": "2024-12-31",
              |    "schemeInhibitionStatus": "Converted Stakeholder Pension",
              |    "reconciliationDate": "2025-03-31",
              |    "schemeContractedOutNumberDetails": "S2345678C"
              |  },
              |  "schemeAddressDetailsList": [
              |    {
              |      "schemeAddressType": "GENERAL CORRESPONDENCE",
              |      "schemeAddressSequenceNumber": 5,
              |      "schemeAddressStartDate": "2010-01-01",
              |      "schemeAddressEndDate": "2024-12-31",
              |      "country": "SCOTLAND",
              |      "areaDiallingCode": "0131 (99)",
              |      "schemeTelephoneNumber": "0131 000 0000",
              |      "schemeContractedOutNumberDetails": "S2345678C",
              |      "benefitSchemeAddressDetails": {
              |        "schemeAddressLine1": "1 Sample Road",
              |        "schemeAddressLine2": "Unit 2",
              |        "schemeAddressLocality": "Old Quarter",
              |        "schemeAddressPostalTown": "Exampleburgh",
              |        "schemePostcode": "EX2 2EX"
              |      }
              |    }
              |  ]
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          result shouldBe Right(
            SuccessResult(ApiName.BenefitSchemeDetails, benefitSchemeDetailsSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the BenefitSchemeDetails endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns FORBIDDEN (403)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns NOT_FOUND (404)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.NotFound, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns SERVICE_UNAVAILABLE (503)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.BenefitSchemeDetails,
              ErrorReport(NpsNormalizedError.InternalServerError, Some(response))
            )
          )
        }
      }

      "when the BenefitSchemeDetails endpoint returns an unexpected statusCode" - {
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
                .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
                .value
                .futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.BenefitSchemeDetails,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the BenefitSchemeDetails endpoint returns malformed JSON" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the BenefitSchemeDetails endpoint returns valid JSON with missing required fields" - {
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
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
              .fetchBenefitSchemeDetails(MA, identifier, SchemeContractedOutNumberDetails("S2345678C"))
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
