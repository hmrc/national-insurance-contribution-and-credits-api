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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.connector

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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  OfficeDetails,
  OfficeLocationDecode,
  OfficeLocationValue
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.EnumOffidtp
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class LongTermBenefitCalculationDetailsConnectorItSpec
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

  private lazy val connector: LongTermBenefitCalculationDetailsConnector =
    inject[LongTermBenefitCalculationDetailsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  "BenefitCalculationDetailsConnector" - {

    ".fetchBenefitCalculationDetails" - {

      val testPath                                             = "/long-term-benefits/AB123456C/calculation"
      val identifier: Identifier                               = Identifier("AB123456C")
      val seqNo: Option[AssociatedCalculationSequenceNumber]   = None
      val longTermBenefitType: Option[LongTermBenefitType]     = None
      val pensionProcessingArea: Option[PensionProcessingArea] = None

      "when the BenefitCalculationDetails endpoint returns OK (200) with valid response" - {
        "should parse response and map to result successfully" in {
          val benefitCalculationDetailsSuccessResponse = LongTermBenefitCalculationDetailsSuccessResponse(
            statePensionAgeBefore2010TaxYear = Some(StatePensionAgeBefore2010TaxYear(true)),
            statePensionAgeAfter2016TaxYear = Some(StatePensionAgeAfter2016TaxYear(true)),
            benefitCalculationDetailsList = Some(
              List(
                BenefitCalculationDetailsList(
                  additionalPensionAmountPre1997 = Some(AdditionalPensionAmountPre1997(10.56)),
                  additionalPensionAmountPost1997 = Some(AdditionalPensionAmountPost1997(10.56)),
                  pre97AgeRelatedAdditionalPension = Some(Pre97AgeRelatedAdditionalPension(10.56)),
                  post97AgeRelatedAdditionalPension = Some(Post97AgeRelatedAdditionalPension(10.56)),
                  basicPensionIncrementsCashValue = Some(BasicPensionIncrementsCashValue(10.56)),
                  additionalPensionIncrementsCashValue = Some(AdditionalPensionIncrementsCashValue(10.56)),
                  graduatedRetirementBenefitCashValue = Some(GraduatedRetirementBenefitCashValue(10.56)),
                  totalGuaranteedMinimumPension = Some(TotalGuaranteedMinimumPension(10.56)),
                  totalNonGuaranteedMinimumPension = Some(TotalNonGuaranteedMinimumPension(10.56)),
                  longTermBenefitsIncrementalCashValue = Some(LongTermBenefitsIncrementalCashValue(10.56)),
                  greatBritainPaymentAmount = Some(GreatBritainPaymentAmount(10.56)),
                  dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
                  notionalPost1997AdditionalPension = Some(NotionalPost1997AdditionalPension(10.56)),
                  notionalPre1997AdditionalPension = Some(NotionalPre1997AdditionalPension(10.56)),
                  inheritableNotionalAdditionalPensionIncrements =
                    Some(InheritableNotionalAdditionalPensionIncrements(10.56)),
                  conditionOneSatisfied = Some(ConditionOneSatisfied("H")),
                  reasonForFormIssue = Some(ReasonForFormIssue("REQUESTED BENEFIT CALCULATION")),
                  longTermBenefitsCategoryACashValue = Some(LongTermBenefitsCategoryACashValue(10.56)),
                  longTermBenefitsCategoryBLCashValue = Some(LongTermBenefitsCategoryBLCashValue(10.56)),
                  longTermBenefitsUnitValue = Some(LongTermBenefitsUnitValue(10.56)),
                  additionalNotionalPensionAmountPost2002 = Some(AdditionalNotionalPensionAmountPost2002(10.56)),
                  additionalPensionAmountPost2002 = Some(AdditionalPensionAmountPost2002(10.56)),
                  additionalNotionalPensionIncrementsInheritedPost2002 =
                    Some(AdditionalNotionalPensionIncrementsInheritedPost2002(10.56)),
                  additionalPensionIncrementsInheritedPost2002 =
                    Some(AdditionalPensionIncrementsInheritedPost2002(10.56)),
                  post02AgeRelatedAdditionalPension = Some(Post02AgeRelatedAdditionalPension(10.56)),
                  pre1975ShortTermBenefits = Some(Pre1975ShortTermBenefits(2)),
                  survivingSpouseAge = Some(SurvivingSpouseAge(45)),
                  operativeBenefitStartDate = Some(OperativeBenefitStartDate(LocalDate.parse("2022-06-27"))),
                  sicknessBenefitStatusForReports = Some(SicknessBenefitStatusForReports("Y")),
                  benefitCalculationDetail = Some(
                    BenefitCalculationDetail(
                      nationalInsuranceNumber = Identifier("AA123456"),
                      benefitType = LongTermBenefitType.All,
                      associatedCalculationSequenceNumber = AssociatedCalculationSequenceNumber(86),
                      calculationStatus = Some(CalculationStatus.Definitive),
                      substitutionMethod1 = Some(SubstitutionMethod1(235)),
                      substitutionMethod2 = Some(SubstitutionMethod2(235)),
                      calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27"))),
                      guaranteedMinimumPensionContractedOutDeductionsPre1988 =
                        Some(GuaranteedMinimumPensionContractedOutDeductionsPre1988(10.56)),
                      guaranteedMinimumPensionContractedOutDeductionsPost1988 =
                        Some(GuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
                      contractedOutDeductionsPre1988 = Some(ContractedOutDeductionsPre1988(10.56)),
                      contractedOutDeductionsPost1988 = Some(ContractedOutDeductionsPost1988(10.56)),
                      additionalPensionPercentage = Some(AdditionalPensionPercentage(10.56)),
                      basicPensionPercentage = Some(BasicPensionPercentage(86)),
                      survivorsBenefitAgeRelatedPensionPercentage =
                        Some(SurvivorsBenefitAgeRelatedPensionPercentage(10.56)),
                      additionalAgeRelatedPensionPercentage = Some(AdditionalAgeRelatedPensionPercentage(10.56)),
                      inheritedBasicPensionPercentage = Some(InheritedBasicPensionPercentage(10.56)),
                      inheritedAdditionalPensionPercentage = Some(InheritedAdditionalPensionPercentage(10.56)),
                      inheritedGraduatedPensionPercentage = Some(InheritedGraduatedPensionPercentage(10.56)),
                      inheritedGraduatedBenefit = Some(InheritedGraduatedBenefit(10.56)),
                      calculationSource = Some(CalculationSource.ApComponentSuspectAprilMayCalc),
                      payday = Some(Payday.Friday),
                      dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
                      husbandDateOfDeath = Some(HusbandDateOfDeath(LocalDate.parse("2022-06-27"))),
                      additionalPost1997PensionPercentage = Some(AdditionalPost1997PensionPercentage(10.56)),
                      additionalPost1997AgeRelatedPensionPercentage =
                        Some(AdditionalPost1997AgeRelatedPensionPercentage(10.56)),
                      additionalPensionNotionalPercentage = Some(AdditionalPensionNotionalPercentage(10.56)),
                      additionalPost1997PensionNotionalPercentage =
                        Some(AdditionalPost1997PensionNotionalPercentage(10.56)),
                      inheritedAdditionalPensionNotionalPercentage =
                        Some(InheritedAdditionalPensionNotionalPercentage(10.56)),
                      inheritableAdditionalPensionPercentage = Some(InheritableAdditionalPensionPercentage(90)),
                      additionalPost2002PensionNotionalPercentage =
                        Some(AdditionalPost2002PensionNotionalPercentage(10.56)),
                      additionalPost2002PensionPercentage = Some(AdditionalPost2002PensionPercentage(10.56)),
                      inheritedAdditionalPost2002PensionNotionalPercentage =
                        Some(InheritedAdditionalPost2002PensionNotionalPercentage(10.56)),
                      inheritedAdditionalPost2002PensionPercentage =
                        Some(InheritedAdditionalPost2002PensionPercentage(10.56)),
                      additionalPost2002AgeRelatedPensionPercentage =
                        Some(AdditionalPost2002AgeRelatedPensionPercentage(10.56)),
                      singleContributionConditionRulesApply = Some(SingleContributionConditionRulesApply(true)),
                      officeDetails = Some(
                        OfficeDetails(
                          officeLocationDecode = Some(OfficeLocationDecode(1)),
                          officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                          officeIdentifier = Some(EnumOffidtp.None)
                        )
                      ),
                      newStatePensionCalculationDetails = Some(
                        NewStatePensionCalculationDetails(
                          netAdditionalPensionPre1997 = Some(NetAdditionalPensionPre1997(10.56)),
                          oldRulesStatePensionEntitlement = Some(OldRulesStatePensionEntitlement(10.56)),
                          netRulesAmount = Some(NetRulesAmount(10.56)),
                          derivedRebateAmount = Some(DerivedRebateAmount(10.56)),
                          initialStatePensionAmount = Some(InitialStatePensionAmount(10.56)),
                          protectedPayment2016 = Some(ProtectedPayment2016(10.56)),
                          minimumQualifyingPeriodMet = Some(MinimumQualifyingPeriodMet(true)),
                          qualifyingYearsAfter2016 = Some(QualifyingYearsAfter2016(3)),
                          newStatePensionQualifyingYears = Some(NewStatePensionQualifyingYears(20)),
                          newStatePensionRequisiteYears = Some(NewStatePensionRequisiteYears(35)),
                          newStatePensionEntitlement = Some(NewStatePensionEntitlement(10.56)),
                          protectedPayment = Some(ProtectedPayment(10.56)),
                          pensionSharingOrderContractedOutEmploymentsGroup =
                            Some(PensionSharingOrderContractedOutEmploymentsGroup(true)),
                          pensionSharingOrderStateEarningsRelatedPensionScheme =
                            Some(PensionSharingOrderStateEarningsRelatedPensionScheme(true)),
                          considerReducedRateElection = Some(ConsiderReducedRateElection(true)),
                          weeklyBudgetingLoanAmount = Some(WeeklyBudgetingLoanAmount(10.56)),
                          calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27")))
                        )
                      )
                    )
                  )
                )
              )
            )
          )

          val successResponseJson =
            """{
              |"statePensionAgeBefore2010TaxYear": true,
              |"statePensionAgeAfter2016TaxYear": true,
              |"benefitCalculationDetailsList": [
              |  {
              |    "additionalPensionAmountPre1997": 10.56,
              |    "additionalPensionAmountPost1997": 10.56,
              |    "pre97AgeRelatedAdditionalPension": 10.56,
              |    "post97AgeRelatedAdditionalPension": 10.56,
              |    "basicPensionIncrementsCashValue": 10.56,
              |    "additionalPensionIncrementsCashValue": 10.56,
              |    "graduatedRetirementBenefitCashValue": 10.56,
              |    "totalGuaranteedMinimumPension": 10.56,
              |    "totalNonGuaranteedMinimumPension": 10.56,
              |    "longTermBenefitsIncrementalCashValue": 10.56,
              |    "greatBritainPaymentAmount": 10.56,
              |    "dateOfBirth": "2022-06-27",
              |    "notionalPost1997AdditionalPension": 10.56,
              |    "notionalPre1997AdditionalPension": 10.56,
              |    "inheritableNotionalAdditionalPensionIncrements": 10.56,
              |    "conditionOneSatisfied": "H",
              |    "reasonForFormIssue": "REQUESTED BENEFIT CALCULATION",
              |    "longTermBenefitsCategoryACashValue":10.56,
              |    "longTermBenefitsCategoryBLCashValue":10.56,
              |    "longTermBenefitsUnitValue":10.56,
              |    "additionalNotionalPensionAmountPost2002": 10.56,
              |    "additionalPensionAmountPost2002": 10.56,
              |    "additionalNotionalPensionIncrementsInheritedPost2002": 10.56,
              |    "additionalPensionIncrementsInheritedPost2002": 10.56,
              |    "post02AgeRelatedAdditionalPension": 10.56,
              |    "pre1975ShortTermBenefits": 2,
              |    "survivingSpouseAge": 45,
              |    "operativeBenefitStartDate": "2022-06-27",
              |    "sicknessBenefitStatusForReports": "Y",
              |    "benefitCalculationDetail": {
              |      "nationalInsuranceNumber": "AA123456",
              |      "benefitType": "ALL",
              |      "associatedCalculationSequenceNumber": 86,
              |      "calculationStatus": "DEFINITIVE",
              |      "substitutionMethod1": 235,
              |      "substitutionMethod2": 235,
              |      "calculationDate": "2022-06-27",
              |      "guaranteedMinimumPensionContractedOutDeductionsPre1988": 10.56,
              |      "guaranteedMinimumPensionContractedOutDeductionsPost1988": 10.56,
              |      "contractedOutDeductionsPre1988": 10.56,
              |      "contractedOutDeductionsPost1988": 10.56,
              |      "additionalPensionPercentage": 10.56,
              |      "basicPensionPercentage": 86,
              |      "survivorsBenefitAgeRelatedPensionPercentage": 10.56,
              |      "additionalAgeRelatedPensionPercentage": 10.56,
              |      "inheritedBasicPensionPercentage": 10.56,
              |      "inheritedAdditionalPensionPercentage": 10.56,
              |      "inheritedGraduatedPensionPercentage": 10.56,
              |      "inheritedGraduatedBenefit": 10.56,
              |      "calculationSource": "AP COMPONENT SUSPECT (APRIL - MAY CALC)",
              |      "payday": "FRIDAY",
              |      "dateOfBirth": "2022-06-27",
              |      "husbandDateOfDeath": "2022-06-27",
              |      "additionalPost1997PensionPercentage": 10.56,
              |      "additionalPost1997AgeRelatedPensionPercentage": 10.56,
              |      "additionalPensionNotionalPercentage": 10.56,
              |      "additionalPost1997PensionNotionalPercentage": 10.56,
              |      "inheritedAdditionalPensionNotionalPercentage": 10.56,
              |      "inheritableAdditionalPensionPercentage": 90,
              |      "additionalPost2002PensionNotionalPercentage": 10.56,
              |      "additionalPost2002PensionPercentage": 10.56,
              |      "inheritedAdditionalPost2002PensionNotionalPercentage": 10.56,
              |      "inheritedAdditionalPost2002PensionPercentage": 10.56,
              |      "additionalPost2002AgeRelatedPensionPercentage": 10.56,
              |      "singleContributionConditionRulesApply": true,
              |      "officeDetails": {
              |        "officeLocationDecode": 1,
              |        "officeLocationValue": "HQ STATIONARY STORE",
              |        "officeIdentifier": "(NONE)"
              |      },
              |      "newStatePensionCalculationDetails": {
              |        "netAdditionalPensionPre1997": 10.56,
              |        "oldRulesStatePensionEntitlement": 10.56,
              |        "netRulesAmount": 10.56,
              |        "derivedRebateAmount": 10.56,
              |        "initialStatePensionAmount": 10.56,
              |        "protectedPayment2016": 10.56,
              |        "minimumQualifyingPeriodMet": true,
              |        "qualifyingYearsAfter2016": 3,
              |        "newStatePensionQualifyingYears": 20,
              |        "newStatePensionRequisiteYears": 35,
              |        "newStatePensionEntitlement": 10.56,
              |        "protectedPayment": 10.56,
              |        "pensionSharingOrderContractedOutEmploymentsGroup": true,
              |        "pensionSharingOrderStateEarningsRelatedPensionScheme": true,
              |        "considerReducedRateElection": true,
              |        "weeklyBudgetingLoanAmount": 10.56,
              |        "calculationDate": "2022-06-27"
              |      }
              |    }
              |  }
              |]
        }""".stripMargin

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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          result shouldBe Right(
            SuccessResult(ApiName.LongTermBenefitCalculationDetails, benefitCalculationDetailsSuccessResponse)
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )

        }
      }

      "when the BenefitCalculationDetails endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.BadRequest, Some(response))
            )
          )
          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns FORBIDDEN (403)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns NOT_FOUND (404)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.NotFound, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns UNPROCESSABLE_ENTITY (422)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns SERVICE_UNAVAILABLE (503)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
            )
          )

          server.verify(
            getRequestedFor(urlEqualTo(testPath))
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            FailureResult(
              ApiName.LongTermBenefitCalculationDetails,
              ErrorReport(NpsNormalizedError.InternalServerError, Some(response))
            )
          )
        }
      }

      "when the BenefitCalculationDetails endpoint returns an unexpected statusCode" - {
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
                .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
                .value
                .futureValue

            result shouldBe Right(
              FailureResult(
                ApiName.LongTermBenefitCalculationDetails,
                ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
              )
            )
          }

        }
      }

      "when the BenefitCalculationDetails endpoint returns malformed JSON" - {
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[ParsingError]
        }
      }

      "when the BenefitCalculationDetails endpoint returns valid JSON with missing required fields" - {
        "should return validation error" in {

          val incompleteResponse =
            """{
              |"statePensionAgeBefore2010TaxYear": true,
              |"statePensionAgeAfter2016TaxYear": true,
              |"benefitCalculationDetailsList": [
              |  {
              |    "additionalPensionAmountPre1997": 10.56,
              |    "additionalPensionAmountPost1997": 10.56,
              |    "pre97AgeRelatedAdditionalPension": 10.56,
              |    "post97AgeRelatedAdditionalPension": 10.56,
              |    "basicPensionIncrementsCashValue": 10.56,
              |    "additionalPensionIncrementsCashValue": 10.56,
              |    "graduatedRetirementBenefitCashValue": 10.56,
              |    "totalGuaranteedMinimumPension": 10.56,
              |    "totalNonGuaranteedMinimumPension": 10.56,
              |    "longTermBenefitsIncrementalCashValue": 10.56,
              |    "greatBritainPaymentAmount": 10.56,
              |    "dateOfBirth": "2022-06-27",
              |    "notionalPost1997AdditionalPension": 10.56,
              |    "notionalPre1997AdditionalPension": 10.56,
              |    "inheritableNotionalAdditionalPensionIncrements": 10.56,
              |    "conditionOneSatisfied": "H",
              |    "reasonForFormIssue": "REQUESTED BENEFIT CALCULATION",
              |    "longTermBenefitsCategoryACashValue":10.56,
              |    "longTermBenefitsCategoryBLCashValue":10.56,
              |    "longTermBenefitsUnitValue":10.56,
              |    "additionalNotionalPensionAmountPost2002": 10.56,
              |    "additionalPensionAmountPost2002": 10.56,
              |    "additionalNotionalPensionIncrementsInheritedPost2002": 10.56,
              |    "additionalPensionIncrementsInheritedPost2002": 10.56,
              |    "post02AgeRelatedAdditionalPension": 10.56,
              |    "pre1975ShortTermBenefits": 2,
              |    "survivingSpouseAge": 45,
              |    "operativeBenefitStartDate": "2022-06-27",
              |    "sicknessBenefitStatusForReports": "Y",
              |    "benefitCalculationDetail": {
              |      "calculationStatus": "DEFINITIVE",
              |      "substitutionMethod1": 235,
              |      "substitutionMethod2": 235,
              |      "calculationDate": "2022-06-27",
              |      "guaranteedMinimumPensionContractedOutDeductionsPre1988": 10.56,
              |      "guaranteedMinimumPensionContractedOutDeductionsPost1988": 10.56,
              |      "contractedOutDeductionsPre1988": 10.56,
              |      "contractedOutDeductionsPost1988": 10.56,
              |      "additionalPensionPercentage": 10.56,
              |      "basicPensionPercentage": 86,
              |      "survivorsBenefitAgeRelatedPensionPercentage": 10.56,
              |      "additionalAgeRelatedPensionPercentage": 10.56,
              |      "inheritedBasicPensionPercentage": 10.56,
              |      "inheritedAdditionalPensionPercentage": 10.56,
              |      "inheritedGraduatedPensionPercentage": 10.56,
              |      "inheritedGraduatedBenefit": 10.56,
              |      "calculationSource": "AP COMPONENT SUSPECT (APRIL - MAY CALC)",
              |      "payday": "FRIDAY",
              |      "dateOfBirth": "2022-06-27",
              |      "husbandDateOfDeath": "2022-06-27",
              |      "additionalPost1997PensionPercentage": 10.56,
              |      "additionalPost1997AgeRelatedPensionPercentage": 10.56,
              |      "additionalPensionNotionalPercentage": 10.56,
              |      "additionalPost1997PensionNotionalPercentage": 10.56,
              |      "inheritedAdditionalPensionNotionalPercentage": 10.56,
              |      "inheritableAdditionalPensionPercentage": 90,
              |      "additionalPost2002PensionNotionalPercentage": 10.56,
              |      "additionalPost2002PensionPercentage": 10.56,
              |      "inheritedAdditionalPost2002PensionNotionalPercentage": 10.56,
              |      "inheritedAdditionalPost2002PensionPercentage": 10.56,
              |      "additionalPost2002AgeRelatedPensionPercentage": 10.56,
              |      "singleContributionConditionRulesApply": true,
              |      "officeDetails": {
              |        "officeLocationDecode": 1,
              |        "officeLocationValue": "HQ STATIONARY STORE",
              |        "officeIdentifier": "(NONE)"
              |      },
              |      "newStatePensionCalculationDetails": {
              |        "netAdditionalPensionPre1997": 10.56,
              |        "oldRulesStatePensionEntitlement": 10.56,
              |        "netRulesAmount": 10.56,
              |        "derivedRebateAmount": 10.56,
              |        "initialStatePensionAmount": 10.56,
              |        "protectedPayment2016": 10.56,
              |        "minimumQualifyingPeriodMet": true,
              |        "qualifyingYearsAfter2016": 3,
              |        "newStatePensionQualifyingYears": 20,
              |        "newStatePensionRequisiteYears": 35,
              |        "newStatePensionEntitlement": 10.56,
              |        "protectedPayment": 10.56,
              |        "pensionSharingOrderContractedOutEmploymentsGroup": true,
              |        "pensionSharingOrderStateEarningsRelatedPensionScheme": true,
              |        "considerReducedRateElection": true,
              |        "weeklyBudgetingLoanAmount": 10.56,
              |        "calculationDate": "2022-06-27"
              |      }
              |    }
              |  }
              |]
        }""".stripMargin

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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
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
              .fetchBenefitCalculationDetails(MA, identifier, seqNo, longTermBenefitType, pensionProcessingArea)
              .value
              .futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[NpsClientError]
        }
      }

    }
  }

}
