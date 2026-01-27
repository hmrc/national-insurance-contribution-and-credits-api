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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.connector

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, getRequestedFor, urlEqualTo}
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
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class SchemeMembershipDetailsConnectorItSpec
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

  private lazy val connector: SchemeMembershipDetailsConnector = inject[SchemeMembershipDetailsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "SchemeMembershipDetailsConnector" - {

    ".fetchSchemeMembershipDetails" - {

      val testPath = "/benefit-scheme/AB123456C/scheme-membership-details"

      "when the SchemeMembershipDetails endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
            schemeMembershipDetailsSummaryList = Some(
              List(
                SchemeMembershipDetailsSummary(
                  stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
                  schemeMembershipDetails = SchemeMembershipDetails(
                    nationalInsuranceNumber = Identifier("AA123456"),
                    schemeMembershipSequenceNumber = SchemeMembershipSequenceNumber(123),
                    schemeMembershipOccurrenceNumber = SchemeMembershipOccurrenceNumber(1),
                    schemeMembershipStartDate = Some(SchemeMembershipStartDate(LocalDate.of(2022, 6, 27))),
                    contractedOutEmployerIdentifier = Some(ContractedOutEmployerIdentifier(789)),
                    schemeMembershipEndDate = Some(SchemeMembershipEndDate(LocalDate.of(2022, 6, 27))),
                    methodOfPreservationType = Some(MethodOfPreservation.NotApplicable0),
                    totalLinkedGuaranteedMinimumPensionContractedOutDeductions =
                      Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(BigDecimal("10.56"))),
                    accruedPensionContractedOutDeductionsValue =
                      Some(AccruedPensionContractedOutDeductionsValue(BigDecimal("10.56"))),
                    totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988 =
                      Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(BigDecimal("10.56"))),
                    accruedPensionContractedOutDeductionsValuePost1988 =
                      Some(AccruedPensionContractedOutDeductionsValuePost1988(BigDecimal("10.56"))),
                    revaluationRate = Some(RevaluationRate.None),
                    guaranteedMinimumPensionReconciliationStatus =
                      Some(GuaranteedMinimumPensionReconciliationStatus.NotApplicable),
                    employeesReference = Some(EmployeesReference("123/456/ABC")),
                    finalYearEarnings = Some(FinalYearEarnings(BigDecimal("10.56"))),
                    penultimateYearEarnings = Some(PenultimateYearEarnings(BigDecimal("10.56"))),
                    retrospectiveEarnings = Some(RetrospectiveEarnings(BigDecimal("10.56"))),
                    furtherPaymentsConfirmation = Some(FurtherPaymentsConfirmation.FurtherPaymentAllowed),
                    survivorStatus = Some(SurvivorStatus.NotApplicable),
                    transferPremiumElectionDate = Some(TransferPremiumElectionDate(LocalDate.of(2022, 6, 27))),
                    revaluationApplied = Some(RevaluationApplied(true)),
                    stateEarningsRelatedPensionsSchemeNonRestorationValue =
                      Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(BigDecimal("10.56"))),
                    stateEarningsRelatedPensionsSchemeValuePost1988 =
                      Some(StateEarningsRelatedPensionsSchemeValuePost1988(BigDecimal("10.56"))),
                    apparentUnnotifiedTerminationStatus =
                      Some(ApparentUnnotifiedTerminationStatus.NoApparentUnnotifiedTermination),
                    terminationMicrofilmNumber = Some(TerminationMicrofilmNumber(789)),
                    debitVoucherMicrofilmNumber = Some(DebitVoucherMicrofilmNumber(40599123)),
                    creationMicrofilmNumber = Some(CreationMicrofilmNumber(40599123)),
                    inhibitSchemeProcessing = Some(InhibitSchemeProcessing(true)),
                    extensionDate = Some(ExtensionDate(LocalDate.of(2022, 6, 27))),
                    guaranteedMinimumPensionContractedOutDeductionsRevalued =
                      Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(BigDecimal("10.56"))),
                    clericalCalculationInvolved = Some(Clercalc.NoClericalCalculationInvolved),
                    clericallyControlledTotal = Some(ClericallyControlledTotal(BigDecimal("10.56"))),
                    clericallyControlledTotalPost1988 = Some(ClericallyControlledTotalPost1988(BigDecimal("10.56"))),
                    certifiedAmount = Some(CertifiedAmount(BigDecimal("10.56"))),
                    enforcementStatus = Some(Enfcment.NotEnforced),
                    stateSchemePremiumDeemed = Some(SspDeem.SspTypeReceivablesToBeTreatAsDeemed),
                    transferTakeUpDate = Some(TransferTakeUpDate(LocalDate.of(2022, 6, 27))),
                    schemeMembershipTransferSequenceNumber = Some(SchemeMembershipTransferSequenceNumber(123)),
                    contributionCategoryFinalYear = Some(ContCatLetter.A),
                    contributionCategoryPenultimateYear = Some(ContCatLetter.A),
                    contributionCategoryRetrospectiveYear = Some(ContCatLetter.A),
                    protectedRightsStartDate = Some(ProtectedRightsStartDate(LocalDate.of(2022, 6, 27))),
                    schemeMembershipDebitReason = Some(SchemeMembershipDebitReason.NotApplicable),
                    technicalAmount = Some(TechnicalAmount(BigDecimal("10.56"))),
                    minimumFundTransferAmount = Some(MinimumFundTransferAmount(BigDecimal("10.56"))),
                    actualTransferValue = Some(ActualTransferValue(BigDecimal("10.56"))),
                    schemeSuspensionType = Some(SchemeSuspensionType.NoSuspension),
                    guaranteedMinimumPensionConversionApplied = Some(GuaranteedMinimumPensionConversionApplied(true)),
                    employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S3123456B")),
                    schemeCreatingContractedOutNumberDetails =
                      Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
                    schemeTerminatingContractedOutNumberDetails =
                      Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                    importingAppropriateSchemeNumberDetails =
                      Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                    apparentUnnotifiedTerminationDestinationDetails =
                      Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                  )
                )
              )
            ),
            callback = Some(
              Callback(
                callbackURL = Some(
                  CallbackURL(
                    "some-url"
                  )
                )
              )
            )
          )

          val successResponseJson =
            """{
              |  "callback": {
              |    "callbackURL": "some-url"
              |  },
              |  "schemeMembershipDetailsSummaryList": [
              |    {
              |      "schemeMembershipDetails": {
              |        "accruedPensionContractedOutDeductionsValue": 10.56,
              |        "accruedPensionContractedOutDeductionsValuePost1988": 10.56,
              |        "actualTransferValue": 10.56,
              |        "apparentUnnotifiedTerminationStatus": "No Apparent Unnotified Termination",
              |        "apparentUnnotifiedTerminationDestinationDetails": "S2123456B",
              |        "certifiedAmount": 10.56,
              |        "clericalCalculationInvolved": "NO CLERICAL CALCULATION INVOLVED",
              |        "clericallyControlledTotal": 10.56,
              |        "clericallyControlledTotalPost1988": 10.56,
              |        "contractedOutEmployerIdentifier": 789,
              |        "contributionCategoryFinalYear": "A",
              |        "contributionCategoryPenultimateYear": "A",
              |        "contributionCategoryRetrospectiveYear": "A",
              |        "creationMicrofilmNumber": 40599123,
              |        "debitVoucherMicrofilmNumber": 40599123,
              |        "employeesReference": "123/456/ABC",
              |        "employersContractedOutNumberDetails": "S3123456B",
              |        "enforcementStatus": "NOT ENFORCED",
              |        "extensionDate": "2022-06-27",
              |        "finalYearEarnings": 10.56,
              |        "furtherPaymentsConfirmation": "FURTHER PAYMENT ALLOWED",
              |        "guaranteedMinimumPensionContractedOutDeductionsRevalued": 10.56,
              |        "guaranteedMinimumPensionConversionApplied": true,
              |        "guaranteedMinimumPensionReconciliationStatus": "NOT APPLICABLE",
              |        "importingAppropriateSchemeNumberDetails": "S2123456B",
              |        "inhibitSchemeProcessing": true,
              |        "methodOfPreservationType": "NOT APPLICABLE (0)",
              |        "minimumFundTransferAmount": 10.56,
              |        "nationalInsuranceNumber": "AA123456",
              |        "penultimateYearEarnings": 10.56,
              |        "protectedRightsStartDate": "2022-06-27",
              |        "retrospectiveEarnings": 10.56,
              |        "revaluationApplied": true,
              |        "revaluationRate": "(NONE)",
              |        "schemeCreatingContractedOutNumberDetails": "A7123456Q",
              |        "schemeMembershipDebitReason": "NOT APPLICABLE",
              |        "schemeMembershipEndDate": "2022-06-27",
              |        "schemeMembershipOccurrenceNumber": 1,
              |        "schemeMembershipSequenceNumber": 123,
              |        "schemeMembershipStartDate": "2022-06-27",
              |        "schemeMembershipTransferSequenceNumber": 123,
              |        "schemeSuspensionType": "NO SUSPENSION",
              |        "stateEarningsRelatedPensionsSchemeNonRestorationValue": 10.56,
              |        "stateEarningsRelatedPensionsSchemeValuePost1988": 10.56,
              |        "stateSchemePremiumDeemed": "SSP TYPE RECEIVABLES TO BE TREAT AS DEEMED",
              |        "survivorStatus": "NOT APPLICABLE",
              |        "technicalAmount": 10.56,
              |        "schemeTerminatingContractedOutNumberDetails": "S2123456B",
              |        "terminationMicrofilmNumber": 789,
              |        "totalLinkedGuaranteedMinimumPensionContractedOutDeductions": 10.56,
              |        "totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988": 10.56,
              |        "transferPremiumElectionDate": "2022-06-27",
              |        "transferTakeUpDate": "2022-06-27"
              |      },
              |      "stakeholderPensionSchemeType": "Non-Stakeholder Pension"
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            SuccessResult(ApiName.SchemeMembershipDetails, schemeMembershipDetailsSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the SchemeMembershipDetails endpoint returns BAD_REQUEST (400)" - {
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
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          val result =
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.BadRequest)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns BAD_REQUEST (403)" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.AccessForbidden)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns BAD_REQUEST (404)" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.NotFound)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns BAD_REQUEST (422)" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.UnprocessableEntity)
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )

          val result =
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe Right(
            FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.InternalServerError)
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED, SERVICE_UNAVAILABLE)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              get(urlEqualTo(testPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            val result =
              connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

            result shouldBe Right(
              FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.UnexpectedStatus(statusCode))
            )
          }

        }
      }

      "when the SchemeMembershipDetails endpoint returns malformed JSON" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }

    ".buildPath" - {
      "should with all filters" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          Some(SequenceNumber(1)),
          Some(TransferSequenceNumber(2)),
          Some(OccurrenceNumber(2))
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?seqNo=1&transferSeqNo=2&occurrenceNo=2"""
      }

      "should with SequenceNumber filter" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          Some(SequenceNumber(1)),
          None,
          None
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?seqNo=1"""
      }

      "should with TransferSequenceNumber filter" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          None,
          Some(TransferSequenceNumber(2)),
          None
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?transferSeqNo=2"""
      }

      "should with OccurrenceNumber filter" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          None,
          None,
          Some(OccurrenceNumber(2))
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?occurrenceNo=2"""
      }

      "should with SequenceNumber and TransferSequenceNumber filters" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          Some(SequenceNumber(1)),
          Some(TransferSequenceNumber(2)),
          None
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?seqNo=1&transferSeqNo=2"""
      }

      "should with SequenceNumber and OccurrenceNumber filters" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          Some(SequenceNumber(1)),
          None,
          Some(OccurrenceNumber(2))
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?seqNo=1&occurrenceNo=2"""
      }

      "should with TransferSequenceNumber and OccurrenceNumber filters" in {
        connector.buildPath(
          "http://localhost:6000",
          Identifier("AB123456C"),
          None,
          Some(TransferSequenceNumber(2)),
          Some(OccurrenceNumber(2))
        ) shouldBe """http://localhost:6000/benefit-scheme/AB123456C/scheme-membership-details?transferSeqNo=2&occurrenceNo=2"""
      }
    }

  }

}
