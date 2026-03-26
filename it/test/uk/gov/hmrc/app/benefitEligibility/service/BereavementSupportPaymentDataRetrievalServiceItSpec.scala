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
import uk.gov.hmrc.app.benefitEligibility.model.common.PaginationType.BSP
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.model.request.BSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.ContributionsAndCreditsRequestParams
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

class BereavementSupportPaymentDataRetrievalServiceItSpec
    extends AnyFreeSpec
    with DefaultPlayMongoRepositorySupport[PageTask]
    with EitherValues
    with WireMockHelper
    with Matchers
    with Injecting
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

  lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        play.api.inject.bind[UuidGenerator].toInstance(uuidGenerator),
        play.api.inject.bind[MongoComponent].toInstance(mongoComponent)
      )
      .configure(
        "microservice.services.hip.nps.niContributionAndCredits.port" -> server.port,
        "microservice.services.hip.nps.marriageDetails.port"          -> server.port
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  private lazy val service: BereavementSupportPaymentDataRetrievalService =
    app.injector.instanceOf[BereavementSupportPaymentDataRetrievalService]

  // wiremock server must be started before the repo is injected else suite will fail
  // perm fix: declare protected val repository: PlayMongoRepository[A] in PlayMongoRepositorySupport as a def (library update)
  server.start()

  override protected val repository: BenefitEligibilityRepositoryImpl =
    inject[BenefitEligibilityRepositoryImpl]

  override protected def checkTtlIndex = false

  "BereavementSupportPaymentDataRetrievalService" - {

    ".fetchEligibilityData" - {

      val npsCreditsAndContributionsPath   = "/national-insurance/contributions-and-credits"
      val npsIndividualMarriageDetailsPath = "/individual/GD379251T/marriage-cp"

      val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
        MarriageDetailsSuccess.MarriageDetails(
          MarriageDetailsSuccess.ActiveMarriage(true),
          Some(
            List(
              MarriageDetailsSuccess
                .MarriageDetailsListElement(
                  sequenceNumber = MarriageDetailsSuccess.SequenceNumber(2),
                  status = CivilPartner,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                )
            )
          ),
          Some(
            MarriageDetailsSuccess.Links(
              MarriageDetailsSuccess.SelfLink(
                Some(MarriageDetailsSuccess.Href("")),
                Some(MarriageDetailsSuccess.Methods.get)
              )
            )
          )
        )
      )

      val marriageDetailsSuccessResponseBody = Json.toJson(marriageDetailsSuccessResponse).toString()

      val bspEligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
        Identifier("GD379251T"),
        ContributionsAndCreditsRequestParams(
          DateOfBirth(LocalDate.parse("2025-10-10")),
          StartTaxYear(2025),
          EndTaxYear(2026)
        )
      )

      "when all NPS endpoint returns OK (200) with valid responses" - {
        "should parse responses and map to result successfully" in {
          val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
            Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result = service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                niContributionsAndCreditsSuccessResponse
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              Some(
                PaginationCursor(
                  PaginationType.BSP,
                  PageTaskId(UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba"))
                )
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )
          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.BadRequest, Some(response))
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.BadRequest, Some(response))
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )
          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.NotFound, None)
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.InternalServerError, None)
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultBSP(
              FailureResult(
                ApiName.NiContributionAndCredits,
                ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              None
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
              get(urlEqualTo(npsIndividualMarriageDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(marriageDetailsSuccessResponseBody)
                )
            )

            val result =
              service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

            result shouldBe Right(
              EligibilityCheckDataResultBSP(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
                ),
                SuccessResult(
                  ApiName.MarriageDetails,
                  marriageDetailsSuccessResponse
                ),
                None
              )
            )

            server.verify(
              postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
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
            get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(bspEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(npsIndividualMarriageDetailsPath))
          )
        }
      }
    }
  }

}
