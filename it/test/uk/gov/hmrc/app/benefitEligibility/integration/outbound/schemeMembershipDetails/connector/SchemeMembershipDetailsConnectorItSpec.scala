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

import com.github.tomakehurst.wiremock.client.WireMock
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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
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
                ),
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
            callback = None
          )

          val responseBody = Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

          val testPath = "/benefit-scheme/AB123456C/scheme-membership-details"

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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns FORBIDDEN (403)" - {
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

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Forbidden",
              |   "code":"403.2"
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.NotFound, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.InternalServerError, Some(response))
            )
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns SERVICE_UNAVAILABLE (503)" - {
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
            connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.SchemeMembershipDetails,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the SchemeMembershipDetails endpoint returns an unexpected statusCode" - {
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
              connector.fetchSchemeMembershipDetails(MA, Identifier("AB123456C"), None, None, None).value.futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.SchemeMembershipDetails,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
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

  }

}
