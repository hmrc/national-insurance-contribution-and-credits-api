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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

class IndividualStatePensionInformationResponseSpec extends AnyFreeSpec with Matchers {

  val individualStatePensionInformationOpenApiSpec =
    "test/resources/schemas/api/individualStatePensionInformation/individualStatePensionInformation.yaml"

  "IndividualStatePensionInformationResponse" - {

    "IndividualStatePensionInformationSuccessResponse" - {

      def individualStatePensionInformationSuccessResponseJsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          individualStatePensionInformationOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("GetNIRecordResponse"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[IndividualStatePensionInformationSuccessResponse]]

      val individualStatePensionInformationSuccessResponse = IndividualStatePensionInformationSuccessResponse(
        identifier = Identifier("AA000001A"),
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
              coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("52000.00"))),
              niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
              niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
              underInvestigationFlag = Some(UnderInvestigationFlag(true)),
              totalPrimaryPaidEarnings = Some(TotalPrimaryPaidEarnings(BigDecimal("48500.75"))),
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
              coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("41250.80"))),
              niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(42)),
              niEarningsVoluntary = Some(NiEarningsVoluntary(15)),
              underInvestigationFlag = Some(UnderInvestigationFlag(false)),
              totalPrimaryPaidEarnings = Some(TotalPrimaryPaidEarnings(BigDecimal("39875.90"))),
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

      val jsonString =
        """{
          |  "identifier": "AA000001A",
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
          |      "coPrimaryPaidEarnings": 52000.00,
          |      "niEarningsSelfEmployed": 25,
          |      "niEarningsVoluntary": 8,
          |      "underInvestigationFlag": true,
          |      "totalPrimaryPaidEarnings": 48500.75,
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
          |      "coPrimaryPaidEarnings": 41250.80,
          |      "niEarningsSelfEmployed": 42,
          |      "niEarningsVoluntary": 15,
          |      "underInvestigationFlag": false,
          |      "totalPrimaryPaidEarnings": 39875.90,
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

      "should match the openapi schema validation for the fields required for a given benefit Type (MA)" in {

        val invalidResponse = IndividualStatePensionInformationSuccessResponse(
          identifier = Identifier("AA000001A"),
          numberOfQualifyingYears = Some(NumberOfQualifyingYears(350)),
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
                coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("52000.00"))),
                niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
                niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
                underInvestigationFlag = Some(UnderInvestigationFlag(true)),
                totalPrimaryPaidEarnings = Some(TotalPrimaryPaidEarnings(BigDecimal("485009999999999.75"))),
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
                coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("41250.80"))),
                niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(42)),
                niEarningsVoluntary = Some(NiEarningsVoluntary(15)),
                underInvestigationFlag = Some(UnderInvestigationFlag(false)),
                totalPrimaryPaidEarnings = Some(TotalPrimaryPaidEarnings(BigDecimal("39875.90"))),
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

        IndividualStatePensionInformationResponseValidation.individualStatePensionInformationResponseValidator.validate(
          MA,
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """NumberOfQualifyingYears value exceeds the maximum allowed limit of 100""",
            """TotalPrimaryPaidEarnings value exceeds the maximum allowed limit of 99999999999999.98"""
          )
        )

        individualStatePensionInformationSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.contributionsByTaxYear[0].totalPrimaryPaidEarnings: must have a maximum value of 9.999999999999998E13""",
            """$.numberOfQualifyingYears: must have a maximum value of 100"""
          )
      }

      "should match the openapi schema for a full response" in {
        individualStatePensionInformationSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(individualStatePensionInformationSuccessResponse)
        ) shouldBe Nil

      }

      "deserialises and serialises successfully" in {
        Json.toJson(individualStatePensionInformationSuccessResponse) shouldBe Json.parse(jsonString)
      }

      "deserialises to the model class" in {
        val _: IndividualStatePensionInformationSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonString)
        val individualStatePensionInformationSuccessResponse: IndividualStatePensionInformationSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(individualStatePensionInformationSuccessResponse)

        writtenJson shouldBe jValue
      }

    }

    "ErrorResponse400 (standard)" - {

      val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

      def individualStatePensionInformationOpenApiSpec400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          individualStatePensionInformationOpenApiSpec,
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
        individualStatePensionInformationOpenApiSpec400JsonSchema.validateAndGetErrors(
          Json.toJson(npsStandardErrorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse400 (hipFailureResponse)" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def individualStatePensionInformationOpenApiSpec400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          individualStatePensionInformationOpenApiSpec,
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
        individualStatePensionInformationOpenApiSpec400JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse403" - {

      def individualStatePensionInformationOpenApiSpec403JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          individualStatePensionInformationOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("errorResourceObj_403_Forbidden"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[NpsSingleErrorResponse]]

      val errorResponse403 =
        NpsSingleErrorResponse(NpsErrorReason("Forbidden"), NpsErrorCode("403.2"))

      val errorResponse403JsonString =
        """{
          |  "reason": "Forbidden",
          |  "code": "403.2"
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(errorResponse403) shouldBe Json.parse(
          errorResponse403JsonString
        )
      }

      "deserialises to the model class" in {
        val _: NpsSingleErrorResponse =
          jsonFormat.reads(Json.parse(errorResponse403JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue1: JsValue                         = Json.parse(errorResponse403JsonString)
        val errorResponse403: NpsSingleErrorResponse = jsonFormat.reads(jValue1).get
        val writtenJson1: JsValue                    = jsonFormat.writes(errorResponse403)

        writtenJson1 shouldBe jValue1
      }

      "should match the openapi schema" in {
        individualStatePensionInformationOpenApiSpec403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403)
        ) shouldBe Nil
      }
    }

    "ErrorResponse503" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def individualStatePensionInformationOpenApiSpec503JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          individualStatePensionInformationOpenApiSpec,
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
        individualStatePensionInformationOpenApiSpec503JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse503)
        ) shouldBe Nil
      }
    }
  }

}
