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

package uk.gov.hmrc.app.benefitEligibility.service

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
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.LiabilitySearchCategoryHyphenated.Abroad
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.*
import uk.gov.hmrc.app.benefitEligibility.model.request.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.{
  BenefitEligibilityRepositoryImpl,
  PageTask,
  PageTaskId,
  PaginationCursor
}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.ExecutionContext

class MaternityAllowanceDataRetrievalServiceItSpec
    extends AnyFreeSpec
    with DefaultPlayMongoRepositorySupport[PageTask]
    with EitherValues
    with WireMockHelper
    with Injecting
    with Matchers
    with ScalaFutures {

  val uuidGenerator: UuidGenerator = new UuidGenerator {
    override def generate: UUID = UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba")
  }

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = Instant.parse("2007-12-03T10:15:30.00Z")
  }

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val defaultPatience: PatienceConfig = PatienceConfig(
    timeout = Span(10, Seconds),
    interval = Span(100, Millis)
  )

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        play.api.inject.bind[UuidGenerator].toInstance(uuidGenerator),
        play.api.inject.bind[MongoComponent].toInstance(mongoComponent)
      )
      .configure(
        "microservice.services.hip.nps.class2MaReceipts.port"         -> server.port,
        "microservice.services.hip.nps.liabilities.port"              -> server.port,
        "microservice.services.hip.nps.niContributionAndCredits.port" -> server.port
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  private lazy val service: MaternityAllowanceDataRetrievalService =
    app.injector.instanceOf[MaternityAllowanceDataRetrievalService]

  // wiremock server must be started before the repo is injected else suite will fail
  // perm fix: declare protected val repository: PlayMongoRepository[A] in PlayMongoRepositorySupport as a def (library update)
  server.start()

  override protected val repository: BenefitEligibilityRepositoryImpl =
    inject[BenefitEligibilityRepositoryImpl]

  override protected def checkTtlIndex = false

  "MaternityAllowanceDataRetrievalService" - {

    ".fetchEligibilityData" - {

      val npsCreditsAndContributionsPath = "/national-insurance/contributions-and-credits"
      val npsClass2MaReceiptsPath        = "/class-2/GD379251T/maternity-allowance/receipts"
      val npsLiabilitySummaryDetailsPath = "/person/GD379251T/liability-summary/ABROAD"

      val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
        Some(Identifier("AA000001A")),
        Some(
          List(
            Class2MAReceiptDetails(
              initials = Some(Initials("JP")),
              surname = Some(Surname("van Cholmondley-warner")),
              receivablePayment = Some(ReceivablePayment(10.56)),
              receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
              liabilityStart = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
              liabilityEnd = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
              billAmount = Some(BillAmount(9999.98)),
              billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
              isClosedRecord = Some(IsClosedRecord(true)),
              weeksPaid = Some(WeeksPaid(2))
            )
          )
        ),
        callBack = None
      )

      val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
        Some(
          List(
            LiabilityDetailsList(
              identifier = Identifier("RN000001A"),
              `type` = EnumLiabtp.Abroad,
              occurrenceNumber = OccurrenceNumber(1),
              startDateStatus = Some(EnumLtpsdttp.StartDateHeld),
              endDateStatus = Some(EnumLtpedttp.EndDateHeld),
              startDate = StartDate(LocalDate.parse("2026-01-01")),
              endDate = Some(EndDate(LocalDate.parse("2026-01-01"))),
              country = Some(Country.GreatBritain),
              trainingCreditApprovalStatus = Some(EnumAtcredfg.NoCreditForApprovedTraining),
              casepaperReferenceNumber = Some(CasepaperReferenceNumber("SCH/123/4")),
              homeResponsibilitiesProtectionBenefitReference =
                Some(HomeResponsibilitiesProtectionBenefitReference("12345678AB")),
              homeResponsibilitiesProtectionRate = Some(HomeResponsibilitiesProtectionRate(10.56)),
              lostCardNotificationReason = Some(EnumLcheadtp.NotApplicable),
              lostCardRulingReason = Some(EnumLcruletp.NotApplicable),
              homeResponsibilityProtectionCalculationYear = Some(HomeResponsibilityProtectionCalculationYear(2022)),
              awardAmount = Some(AwardAmount(10.56)),
              resourceGroupIdentifier = Some(ResourceGroupIdentifier(789)),
              homeResponsibilitiesProtectionIndicator = Some(EnumHrpIndicator.None),
              officeDetails = Some(
                OfficeDetails(
                  officeLocationDecode = Some(OfficeLocationDecode(1)),
                  officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                  officeIdentifier = Some(EnumOffidtp.None)
                )
              )
            )
          )
        ),
        None
      )

      val class2MAReceiptsSuccessResponseBody = Json.toJson(class2MAReceiptsSuccessResponse).toString()

      val liabilitySummaryDetailsSuccessResponseBody = Json.toJson(liabilitySummaryDetailsSuccessResponse).toString()

      val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
        Identifier("GD379251T"),
        ContributionsAndCreditsRequestParams(
          DateOfBirth(LocalDate.parse("2025-10-10")),
          StartTaxYear(2025),
          EndTaxYear(2026)
        ),
        LiabilitiesRequestParams(List(Abroad), None, None, None)
      )

      "when all NPS endpoint returns OK (200) with valid responses" - {
        "should parse responses and map to result successfully" in {
          val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
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
                Class2or3ContributionAndCredits(
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

          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result = service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.NiContributionAndCredits,
                niContributionsAndCreditsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )
          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )

        }
      }

      "when an NPS endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.BadRequest, Some(response))
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )
        }
      }

      "when an NPS endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.BadRequest, Some(response))
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )
          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns FORBIDDEN (403)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Forbidden",
              |   "code":"403.2"
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(FORBIDDEN)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.NotFound, None)
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )
          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.InternalServerError, None)
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )

        }
      }

      "when an NPS endpoint returns an INTERNAL_SERVER_ERROR (503)" - {

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
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(errorResponse)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultMA(
              SuccessResult(
                ApiName.Class2MAReceipts,
                class2MAReceiptsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.Liabilities,
                  liabilitySummaryDetailsSuccessResponse
                )
              ),
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              post(urlEqualTo(npsCreditsAndContributionsPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            server.stubFor(
              get(urlEqualTo(npsClass2MaReceiptsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(class2MAReceiptsSuccessResponseBody)
                )
            )

            server.stubFor(
              get(urlEqualTo(npsLiabilitySummaryDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(liabilitySummaryDetailsSuccessResponseBody)
                )
            )

            val result =
              service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

            result shouldBe Right(
              EligibilityCheckDataResultMA(
                SuccessResult(
                  ApiName.Class2MAReceipts,
                  class2MAReceiptsSuccessResponse
                ),
                List(
                  SuccessResult(
                    ApiName.Liabilities,
                    liabilitySummaryDetailsSuccessResponse
                  )
                ),
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
                ),
                None
              )
            )

            server.verify(
              postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
            )
          }

        }
      }

      "when an NPS endpoint returns malformed JSON" - {
        "should return parsing error" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{ invalid json structure")
              )
          )
          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }

      "when a request to a downstream fails unexpectedly" - {
        "should return downstream error" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsClass2MaReceiptsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsLiabilitySummaryDetailsPath))
          )
        }
      }
      "should parse successfully and map to result with a next cursor" in {
        val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
          Some(
            List(
              LiabilityDetailsList(
                identifier = Identifier("RN000001A"),
                `type` = EnumLiabtp.Abroad,
                occurrenceNumber = OccurrenceNumber(1),
                startDateStatus = Some(EnumLtpsdttp.StartDateHeld),
                endDateStatus = Some(EnumLtpedttp.EndDateHeld),
                startDate = StartDate(LocalDate.parse("2026-01-01")),
                endDate = Some(EndDate(LocalDate.parse("2026-01-01"))),
                country = Some(Country.GreatBritain),
                trainingCreditApprovalStatus = Some(EnumAtcredfg.NoCreditForApprovedTraining),
                casepaperReferenceNumber = Some(CasepaperReferenceNumber("SCH/123/4")),
                homeResponsibilitiesProtectionBenefitReference =
                  Some(HomeResponsibilitiesProtectionBenefitReference("12345678AB")),
                homeResponsibilitiesProtectionRate = Some(HomeResponsibilitiesProtectionRate(10.56)),
                lostCardNotificationReason = Some(EnumLcheadtp.NotApplicable),
                lostCardRulingReason = Some(EnumLcruletp.NotApplicable),
                homeResponsibilityProtectionCalculationYear = Some(HomeResponsibilityProtectionCalculationYear(2022)),
                awardAmount = Some(AwardAmount(10.56)),
                resourceGroupIdentifier = Some(ResourceGroupIdentifier(789)),
                homeResponsibilitiesProtectionIndicator = Some(EnumHrpIndicator.None),
                officeDetails = Some(
                  OfficeDetails(
                    officeLocationDecode = Some(OfficeLocationDecode(1)),
                    officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                    officeIdentifier = Some(EnumOffidtp.None)
                  )
                )
              )
            )
          ),
          Some(Callback(Some(CallbackUrl("SomeCallBackUrl"))))
        )

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
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
              Class2or3ContributionAndCredits(
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

        val niContributionsAndCreditsSuccessResponseBody =
          Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(niContributionsAndCreditsSuccessResponseBody)
            )
        )

        server.stubFor(
          get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(class2MAReceiptsSuccessResponseBody)
            )
        )

        server.stubFor(
          get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(liabilitySummaryDetailsSuccessResponse).toString)
            )
        )

        val result = service.fetchEligibilityData(maEligibilityCheckDataRequest).value.futureValue

        result shouldBe Right(
          EligibilityCheckDataResultMA(
            SuccessResult(
              ApiName.Class2MAReceipts,
              class2MAReceiptsSuccessResponse
            ),
            List(
              SuccessResult(
                ApiName.Liabilities,
                liabilitySummaryDetailsSuccessResponse
              )
            ),
            SuccessResult(
              ApiName.NiContributionAndCredits,
              niContributionsAndCreditsSuccessResponse
            ),
            Some(
              PaginationCursor(
                PaginationType.MaPagination,
                PageTaskId(UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba"))
              )
            )
          )
        )
      }
    }
  }

}
