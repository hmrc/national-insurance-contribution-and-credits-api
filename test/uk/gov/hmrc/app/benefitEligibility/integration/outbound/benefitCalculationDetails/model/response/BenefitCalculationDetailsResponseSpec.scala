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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response

import cats.data.Validated
import cats.data.Validated.Valid
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response.BenefitCalculationDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.response.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.{
  OfficeDetails,
  OfficeLocationDecode,
  OfficeLocationValue
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.EnumOffidtp
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.BenefitCalculationDetailsFormats.*

import java.time.LocalDate

class BenefitCalculationDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val benefitCalculationDetailsOpenApiSpec =
    "test/resources/schemas/api/longTermBenefitCalculation/longTermBenefitCalculation.yaml"

  def benefitCalculationDetailsOpenApi: SimpleJsonSchema =
    SimpleJsonSchema(
      benefitCalculationDetailsOpenApiSpec,
      SpecVersion.VersionFlag.V7,
      Some("GetLongTermBenefitCalculationDetailsResponse"),
      metaSchemaValidation = Some(Valid(()))
    )

  "BenefitCalculationDetailsResponse" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsSuccessResponse]]

    val benefitCalculationDetailsSuccessResponse = BenefitCalculationDetailsSuccessResponse(
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
            additionalPensionIncrementsInheritedPost2002 = Some(AdditionalPensionIncrementsInheritedPost2002(10.56)),
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
                survivorsBenefitAgeRelatedPensionPercentage = Some(SurvivorsBenefitAgeRelatedPensionPercentage(10.56)),
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
                additionalPost1997PensionNotionalPercentage = Some(AdditionalPost1997PensionNotionalPercentage(10.56)),
                inheritedAdditionalPensionNotionalPercentage =
                  Some(InheritedAdditionalPensionNotionalPercentage(10.56)),
                inheritableAdditionalPensionPercentage = Some(InheritableAdditionalPensionPercentage(90)),
                additionalPost2002PensionNotionalPercentage = Some(AdditionalPost2002PensionNotionalPercentage(10.56)),
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

    val jsonString =
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

    "should match the openapi schema for a full response" in {
      benefitCalculationDetailsOpenApi.validateAndGetErrors(
        Json.toJson(benefitCalculationDetailsSuccessResponse)
      ) shouldBe Nil
    }

    "deserialises and serialises successfully" in {
      Json.toJson(benefitCalculationDetailsSuccessResponse) shouldBe Json.parse(jsonString)
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(jsonString)
      val benefitCalculationDetailsSuccessResponse: BenefitCalculationDetailsSuccessResponse =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(benefitCalculationDetailsSuccessResponse)

      writtenJson shouldBe jValue
    }

  }

  "StandardErrorResponse400" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsStandardErrorResponse400]]

    def benefitCalculationDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_400"),
        metaSchemaValidation = Some(Valid(()))
      )

    val standardErrorResponse400 = BenefitCalculationDetailsStandardErrorResponse400(
      HipOrigin.Hip,
      BenefitCalculationDetailsError.BenefitCalculationDetailsError400(
        List(
          BenefitCalculationDetailsErrorItem400(
            Reason("HTTP message not readable"),
            NpsErrorCode400.NpsErrorCode400_2
          ),
          BenefitCalculationDetailsErrorItem400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            NpsErrorCode400.NpsErrorCode400_1
          )
        )
      )
    )

    val standardErrorResponse400JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "reason": "HTTP message not readable",
        |       "code": "400.2"
        |     },
        |     {
        |       "reason": "Constraint violation: Invalid/Missing input parameter: <parameter>",
        |       "code": "400.1"
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(standardErrorResponse400) shouldBe Json.parse(
        standardErrorResponse400JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsStandardErrorResponse400 =
        jsonFormat.reads(Json.parse(standardErrorResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(standardErrorResponse400JsonString)
      val standardErrorResponse400Json: BenefitCalculationDetailsStandardErrorResponse400 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(standardErrorResponse400Json)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse400" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsHipFailureResponse400]]

    def hipFailureResponse400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse400 = BenefitCalculationDetailsHipFailureResponse400(
      HipOrigin.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType(""),
            Reason("")
          ),
          HipFailureItem(
            FailureType(""),
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse400JsonString =
      """{
        | "origin": "HIP",
        | "response": {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse400) shouldBe Json.parse(
        hipFailureResponse400JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsHipFailureResponse400 =
        jsonFormat.reads(Json.parse(hipFailureResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse400JsonString)
      val hipFailureResponse400Json: BenefitCalculationDetailsHipFailureResponse400 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse400Json)

      writtenJson shouldBe jValue
    }
  }

  "BenefitCalculationDetailsErrorResponse403" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsErrorResponse403]]

    val benefitCalculationDetailsErrorResponse403_2 =
      BenefitCalculationDetailsErrorResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

    val benefitCalculationDetailsErrorResponse403_2JsonString =
      """{
        |  "reason": "Forbidden",
        |  "code": "403.2"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(benefitCalculationDetailsErrorResponse403_2) shouldBe Json.parse(
        benefitCalculationDetailsErrorResponse403_2JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsErrorResponse403 =
        jsonFormat.reads(Json.parse(benefitCalculationDetailsErrorResponse403_2JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue = Json.parse(benefitCalculationDetailsErrorResponse403_2JsonString)
      val benefitCalculationDetailsErrorResponse403_2: BenefitCalculationDetailsErrorResponse403 =
        jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue = jsonFormat.writes(benefitCalculationDetailsErrorResponse403_2)

      writtenJson2 shouldBe jValue2
    }
  }

  "BenefitCalculationDetailsErrorResponse404" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsErrorResponse404]]

    val benefitCalculationDetailsErrorResponse404 =
      BenefitCalculationDetailsErrorResponse404(NpsErrorCode404.ErrorCode404, NpsErrorReason404.NotFound)

    val benefitCalculationDetailsErrorResponse404JsonString =
      """{
        |  "code": "404",
        |  "reason": "Not Found"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(benefitCalculationDetailsErrorResponse404) shouldBe Json.parse(
        benefitCalculationDetailsErrorResponse404JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsErrorResponse404 =
        jsonFormat.reads(Json.parse(benefitCalculationDetailsErrorResponse404JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue = Json.parse(benefitCalculationDetailsErrorResponse404JsonString)
      val benefitCalculationDetailsErrorResponse404Json: BenefitCalculationDetailsErrorResponse404 =
        jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue = jsonFormat.writes(benefitCalculationDetailsErrorResponse404Json)

      writtenJson2 shouldBe jValue2
    }
  }

  "BenefitCalculationDetailsErrorResponse422" - {

    def benefitCalculationDetails422JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_422"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsErrorResponse422]]

    val benefitCalculationDetailsErrorResponse422 = BenefitCalculationDetailsErrorResponse422(
      failures = Some(
        List(
          BenefitCalculationDetailsError.BenefitCalculationDetailsError422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      )
    )

    val benefitCalculationDetailsErrorResponse422JsonString =
      """{
        |  "failures": [
        |    {
        |      "reason": "HTTP message not readable",
        |      "code": "A589"
        |    }
        |  ]
        |}""".stripMargin

    "should match the openapi schema" in {

      val invalidResponse = BenefitCalculationDetailsErrorResponse422(
        Some(
          List(
            BenefitCalculationDetailsError.BenefitCalculationDetailsError422(
              Reason(
                "some reason with way too many letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters"
              ),
              ErrorCode422("")
            )
          )
        )
      )

      benefitCalculationDetails422JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.failures[0].code: must be at least 1 characters long""",
          """$.failures[0].reason: must be at most 128 characters long"""
        )

    }
    "deserialises and serialises successfully" in {
      Json.toJson(benefitCalculationDetailsErrorResponse422) shouldBe Json.parse(
        benefitCalculationDetailsErrorResponse422JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsErrorResponse422 =
        jsonFormat.reads(Json.parse(benefitCalculationDetailsErrorResponse422JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(benefitCalculationDetailsErrorResponse422JsonString)
      val benefitCalculationDetailsErrorResponse422: BenefitCalculationDetailsErrorResponse422 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(benefitCalculationDetailsErrorResponse422)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse500" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsHipFailureResponse500]]

    def hipFailureResponse500JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse500 = BenefitCalculationDetailsHipFailureResponse500(
      HipOrigin.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType(""),
            Reason("")
          ),
          HipFailureItem(
            FailureType(""),
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse500JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse500) shouldBe Json.parse(
        hipFailureResponse500JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsHipFailureResponse500 =
        jsonFormat.reads(Json.parse(hipFailureResponse500JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse500JsonString)
      val hipFailureResponse500Json: BenefitCalculationDetailsHipFailureResponse500 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse500Json)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse503" - {

    val jsonFormat = implicitly[Format[BenefitCalculationDetailsHipFailureResponse503]]

    def hipFailureResponse503JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse503 = BenefitCalculationDetailsHipFailureResponse503(
      HipOrigin.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType(""),
            Reason("")
          ),
          HipFailureItem(
            FailureType(""),
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse503JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse503) shouldBe Json.parse(
        hipFailureResponse503JsonString
      )
    }

    "deserialises to the model class" in {
      val _: BenefitCalculationDetailsHipFailureResponse503 =
        jsonFormat.reads(Json.parse(hipFailureResponse503JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse503JsonString)
      val hipFailureResponse503Json: BenefitCalculationDetailsHipFailureResponse503 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse503Json)

      writtenJson shouldBe jValue
    }
  }

}
