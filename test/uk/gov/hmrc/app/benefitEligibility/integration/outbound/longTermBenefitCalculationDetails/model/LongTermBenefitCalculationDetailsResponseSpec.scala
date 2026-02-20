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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model

import cats.data.Validated.Valid
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  OfficeDetails,
  OfficeLocationDecode,
  OfficeLocationValue
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.EnumOffidtp
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

import java.time.LocalDate

class LongTermBenefitCalculationDetailsResponseSpec extends AnyFreeSpec with Matchers {

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

    val jsonFormat = implicitly[Format[LongTermBenefitCalculationDetailsSuccessResponse]]

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
      val _: LongTermBenefitCalculationDetailsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(jsonString)
      val benefitCalculationDetailsSuccessResponse: LongTermBenefitCalculationDetailsSuccessResponse =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(benefitCalculationDetailsSuccessResponse)

      writtenJson shouldBe jValue
    }

  }

  "ErrorResponse400 (standard)" - {

    val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

    def benefitCalculationDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_400"),
        metaSchemaValidation = Some(Valid(()))
      )

    val npsStandardErrorResponse400 = NpsStandardErrorResponse400(
      HipOrigin.Hip,
      NpsMultiErrorResponse(
        Some(
          List(
            NpsSingleErrorResponse(
              NpsErrorReason("HTTP message not readable"),
              NpsErrorCode("")
            ),
            NpsSingleErrorResponse(
              NpsErrorReason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
              NpsErrorCode("")
            )
          )
        )
      )
    )

    val errorResponse400JsonString =
      """{
        |   "origin":"HIP",
        |   "response":{
        |      "failures":[
        |         {
        |            "reason":"HTTP message not readable",
        |            "code":""
        |         },
        |         {
        |            "reason":"Constraint violation: Invalid/Missing input parameter: <parameter>",
        |            "code":""
        |         }
        |      ]
        |   }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(npsStandardErrorResponse400) shouldBe Json.parse(errorResponse400JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsStandardErrorResponse400 =
        jsonFormat.reads(Json.parse(errorResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                                          = Json.parse(errorResponse400JsonString)
      val npsStandardErrorResponse400: NpsStandardErrorResponse400 = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                                     = jsonFormat.writes(npsStandardErrorResponse400)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      benefitCalculationDetails400JsonSchema.validateAndGetErrors(
        Json.toJson(npsStandardErrorResponse400)
      ) shouldBe Nil
    }
  }

  "ErrorResponse400 (hipFailureResponse)" - {

    val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

    def benefitCalculationDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val errorResponse400 = NpsErrorResponseHipOrigin(
      Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType("t1"),
            NpsErrorReason("r1")
          ),
          HipFailureItem(
            FailureType("t2"),
            NpsErrorReason("r2")
          )
        )
      )
    )

    val errorResponse400JsonString =
      """{
        |   "origin":"HIP",
        |   "response":{
        |      "failures":[
        |         {
        |            "type":"t1",
        |            "reason":"r1"
        |         },
        |         {
        |            "type":"t2",
        |            "reason":"r2"
        |         }
        |      ]
        |   }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse400) shouldBe Json.parse(errorResponse400JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsErrorResponseHipOrigin =
        jsonFormat.reads(Json.parse(errorResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                             = Json.parse(errorResponse400JsonString)
      val errorResponse400: NpsErrorResponseHipOrigin = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                        = jsonFormat.writes(errorResponse400)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      benefitCalculationDetails400JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse400)
      ) shouldBe Nil
    }
  }

  "ErrorResponse403" - {

    def benefitCalculationDetails403JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_403"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[NpsSingleErrorResponse]]

    val errorResponse403_1 =
      NpsSingleErrorResponse(NpsErrorReason("User Not Authorised"), NpsErrorCode("403.1"))

    val errorResponse403_2 =
      NpsSingleErrorResponse(NpsErrorReason("Forbidden"), NpsErrorCode("403.2"))

    val errorResponse403_1JsonString =
      """{
        |  "reason": "User Not Authorised",
        |  "code": "403.1"
        |}""".stripMargin

    val errorResponse403_2JsonString =
      """{
        |  "reason": "Forbidden",
        |  "code": "403.2"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse403_1) shouldBe Json.parse(
        errorResponse403_1JsonString
      )
      Json.toJson(errorResponse403_2) shouldBe Json.parse(
        errorResponse403_2JsonString
      )
    }

    "deserialises to the model class" in {
      val _: NpsSingleErrorResponse =
        jsonFormat.reads(Json.parse(errorResponse403_1JsonString)).get

      val _: NpsSingleErrorResponse =
        jsonFormat.reads(Json.parse(errorResponse403_2JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue1: JsValue                           = Json.parse(errorResponse403_1JsonString)
      val errorResponse403_1: NpsSingleErrorResponse = jsonFormat.reads(jValue1).get
      val writtenJson1: JsValue                      = jsonFormat.writes(errorResponse403_1)

      val jValue2: JsValue                           = Json.parse(errorResponse403_2JsonString)
      val errorResponse403_2: NpsSingleErrorResponse = jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue                      = jsonFormat.writes(errorResponse403_2)

      writtenJson1 shouldBe jValue1
      writtenJson2 shouldBe jValue2
    }

    "should match the openapi schema" in {
      benefitCalculationDetails403JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse403_1)
      ) shouldBe Nil

      benefitCalculationDetails403JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse403_2)
      ) shouldBe Nil
    }
  }

  "ErrorResponse422" - {

    def benefitCalculationDetails422JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_422"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[NpsMultiErrorResponse]]

    val errorResponse422 = NpsMultiErrorResponse(
      failures = Some(
        List(
          NpsSingleErrorResponse(
            NpsErrorReason("HTTP message not readable"),
            NpsErrorCode("A589")
          )
        )
      )
    )

    val errorResponse422JsonString =
      """{
        |  "failures": [
        |    {
        |      "reason": "HTTP message not readable",
        |      "code": "A589"
        |    }
        |  ]
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse422) shouldBe Json.parse(errorResponse422JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsMultiErrorResponse =
        jsonFormat.reads(Json.parse(errorResponse422JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                         = Json.parse(errorResponse422JsonString)
      val errorResponse422: NpsMultiErrorResponse = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                    = jsonFormat.writes(errorResponse422)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      benefitCalculationDetails422JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse422)
      ) shouldBe Nil
    }

  }

  "ErrorResponse500" - {

    val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

    def liabilitySummaryDetails500JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val errorResponse500 = NpsErrorResponseHipOrigin(
      Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType("t1"),
            NpsErrorReason("r1")
          ),
          HipFailureItem(
            FailureType("t2"),
            NpsErrorReason("r2")
          )
        )
      )
    )

    val errorResponse500JsonString =
      """{
        |   "origin":"HIP",
        |   "response":{
        |      "failures":[
        |         {
        |            "type":"t1",
        |            "reason":"r1"
        |         },
        |         {
        |            "type":"t2",
        |            "reason":"r2"
        |         }
        |      ]
        |   }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse500) shouldBe Json.parse(errorResponse500JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsErrorResponseHipOrigin =
        jsonFormat.reads(Json.parse(errorResponse500JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                             = Json.parse(errorResponse500JsonString)
      val errorResponse500: NpsErrorResponseHipOrigin = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                        = jsonFormat.writes(errorResponse500)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      liabilitySummaryDetails500JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse500)
      ) shouldBe Nil
    }
  }

  "ErrorResponse503" - {

    val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

    def benefitCalculationDetails503JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        benefitCalculationDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val errorResponse503 = NpsErrorResponseHipOrigin(
      Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            FailureType("t1"),
            NpsErrorReason("r1")
          ),
          HipFailureItem(
            FailureType("t2"),
            NpsErrorReason("r2")
          )
        )
      )
    )

    val errorResponse503JsonString =
      """{
        |   "origin":"HIP",
        |   "response":{
        |      "failures":[
        |         {
        |            "type":"t1",
        |            "reason":"r1"
        |         },
        |         {
        |            "type":"t2",
        |            "reason":"r2"
        |         }
        |      ]
        |   }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse503) shouldBe Json.parse(errorResponse503JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsErrorResponseHipOrigin =
        jsonFormat.reads(Json.parse(errorResponse503JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                             = Json.parse(errorResponse503JsonString)
      val errorResponse503: NpsErrorResponseHipOrigin = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                        = jsonFormat.writes(errorResponse503)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      benefitCalculationDetails503JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse503)
      ) shouldBe Nil
    }
  }

}
