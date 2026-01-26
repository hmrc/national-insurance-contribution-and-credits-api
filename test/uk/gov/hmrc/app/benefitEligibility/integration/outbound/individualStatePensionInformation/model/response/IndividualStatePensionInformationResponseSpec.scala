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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.enums.{
  ContributionCreditType,
  CreditSourceType
}
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.IndividualStatePensionInformation.*

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
              totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("52000.00"))),
              niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
              niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
              underInvestigationFlag = Some(UnderInvestigationFlag(true)),
              primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("48500.75"))),
              otherCredits = Some(
                List(
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class1Credit),
                    creditSourceType = Some(CreditSourceType.JsaTapeInput),
                    contributionCreditCount = Some(ContributionCreditCount(15))
                  ),
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class3Credit),
                    creditSourceType = Some(CreditSourceType.CarersCredit),
                    contributionCreditCount = Some(ContributionCreditCount(52))
                  ),
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class2NormalRate),
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
                    contributionCreditType = Some(ContributionCreditType.Class1Credit),
                    creditSourceType = Some(CreditSourceType.UniversalCredit),
                    contributionCreditCount = Some(ContributionCreditCount(26))
                  ),
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class2VoluntaryDevelopmentWorkerRateA),
                    creditSourceType = Some(CreditSourceType.StatutoryMaternityPayCredit),
                    contributionCreditCount = Some(ContributionCreditCount(12))
                  ),
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class3RateC),
                    creditSourceType = Some(CreditSourceType.ModSpouseCivilPartnersCredits),
                    contributionCreditCount = Some(ContributionCreditCount(39))
                  ),
                  OtherCredits(
                    contributionCreditType = Some(ContributionCreditType.Class1EmployeeOnly),
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
                totalPrimaryEarnings = Some(TotalPrimaryEarnings(BigDecimal("52000.00"))),
                niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
                niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
                underInvestigationFlag = Some(UnderInvestigationFlag(true)),
                primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("485009999999999.75"))),
                otherCredits = Some(
                  List(
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class1Credit),
                      creditSourceType = Some(CreditSourceType.JsaTapeInput),
                      contributionCreditCount = Some(ContributionCreditCount(15))
                    ),
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class3Credit),
                      creditSourceType = Some(CreditSourceType.CarersCredit),
                      contributionCreditCount = Some(ContributionCreditCount(52))
                    ),
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class2NormalRate),
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
                      contributionCreditType = Some(ContributionCreditType.Class1Credit),
                      creditSourceType = Some(CreditSourceType.UniversalCredit),
                      contributionCreditCount = Some(ContributionCreditCount(26))
                    ),
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class2VoluntaryDevelopmentWorkerRateA),
                      creditSourceType = Some(CreditSourceType.StatutoryMaternityPayCredit),
                      contributionCreditCount = Some(ContributionCreditCount(12))
                    ),
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class3RateC),
                      creditSourceType = Some(CreditSourceType.ModSpouseCivilPartnersCredits),
                      contributionCreditCount = Some(ContributionCreditCount(39))
                    ),
                    OtherCredits(
                      contributionCreditType = Some(ContributionCreditType.Class1EmployeeOnly),
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
            """PrimaryPaidEarnings value exceeds the maximum allowed limit of 99999999999999.98"""
          )
        )

        individualStatePensionInformationSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.contributionsByTaxYear[0].primaryPaidEarnings: must have a maximum value of 9.999999999999998E13""",
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
    "HipFailureResponse400" - {

      val jsonFormat = implicitly[Format[IndividualStatePensionInformationHipFailureResponse400]]

      val hipFailureResponse400 = IndividualStatePensionInformationHipFailureResponse400(
        origin = HipOrigin.Hip,
        response = HipFailureResponse(failures =
          List(
            HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
            HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
            HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
          )
        )
      )

      val hipFailureResponse400JsonString =
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

      "deserialises and serialises successfully" in {
        Json.toJson(hipFailureResponse400) shouldBe Json.parse(
          hipFailureResponse400JsonString
        )
      }

      "deserialises to the model class" in {
        val _: IndividualStatePensionInformationHipFailureResponse400 =
          jsonFormat.reads(Json.parse(hipFailureResponse400JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(hipFailureResponse400JsonString)
        val hipFailureResponse400: IndividualStatePensionInformationHipFailureResponse400 =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse400)

        writtenJson shouldBe jValue
      }
    }
    "StandardErrorResponse400" - {

      val jsonFormat = implicitly[Format[IndividualStatePensionInformationStandardErrorResponse400]]

      val standardErrorResponse400 = IndividualStatePensionInformationStandardErrorResponse400(
        origin = HipOrigin.Hip,
        response = ErrorResponse400(
          failures = List(
            ErrorResourceObj400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
            ErrorResourceObj400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
          )
        )
      )

      val standardErrorResponse400JsonString =
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

      "deserialises and serialises successfully" in {
        Json.toJson(standardErrorResponse400) shouldBe Json.parse(
          standardErrorResponse400JsonString
        )
      }

      "deserialises to the model class" in {
        val _: IndividualStatePensionInformationStandardErrorResponse400 =
          jsonFormat.reads(Json.parse(standardErrorResponse400JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(standardErrorResponse400JsonString)
        val standardErrorResponse400: IndividualStatePensionInformationStandardErrorResponse400 =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(standardErrorResponse400)

        writtenJson shouldBe jValue
      }
    }
    "IndividualStatePensionInformationErrorResponse403" - {

      val jsonFormat = implicitly[Format[IndividualStatePensionInformationErrorResponse403]]

      val individualStatePensionInformationErrorResponse403_1 =
        IndividualStatePensionInformationErrorResponse403(
          NpsErrorReason403.UserUnauthorised,
          NpsErrorCode403.NpsErrorCode403_1
        )

      val individualStatePensionInformationErrorResponse403_2 =
        IndividualStatePensionInformationErrorResponse403(
          NpsErrorReason403.Forbidden,
          NpsErrorCode403.NpsErrorCode403_2
        )

      val individualStatePensionInformationErrorResponse403_1JsonString =
        """{
          |  "reason": "User Not Authorised",
          |  "code": "403.1"
          |}""".stripMargin

      val individualStatePensionInformationErrorResponse403_2JsonString =
        """{
          |  "reason": "Forbidden",
          |  "code": "403.2"
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(individualStatePensionInformationErrorResponse403_1) shouldBe Json.parse(
          individualStatePensionInformationErrorResponse403_1JsonString
        )
        Json.toJson(individualStatePensionInformationErrorResponse403_2) shouldBe Json.parse(
          individualStatePensionInformationErrorResponse403_2JsonString
        )
      }

      "deserialises to the model class" in {
        val _: IndividualStatePensionInformationErrorResponse403 =
          jsonFormat.reads(Json.parse(individualStatePensionInformationErrorResponse403_1JsonString)).get

        val _: IndividualStatePensionInformationErrorResponse403 =
          jsonFormat.reads(Json.parse(individualStatePensionInformationErrorResponse403_2JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue1: JsValue = Json.parse(individualStatePensionInformationErrorResponse403_1JsonString)
        val individualStatePensionInformationErrorResponse403_1: IndividualStatePensionInformationErrorResponse403 =
          jsonFormat.reads(jValue1).get
        val writtenJson1: JsValue = jsonFormat.writes(individualStatePensionInformationErrorResponse403_1)

        val jValue2: JsValue = Json.parse(individualStatePensionInformationErrorResponse403_2JsonString)
        val individualStatePensionInformationErrorResponse403_2: IndividualStatePensionInformationErrorResponse403 =
          jsonFormat.reads(jValue2).get
        val writtenJson2: JsValue = jsonFormat.writes(individualStatePensionInformationErrorResponse403_2)

        writtenJson1 shouldBe jValue1
        writtenJson2 shouldBe jValue2
      }
    }

    "IndividualStatePensionInformationErrorResponse503" - {

      val jsonFormat = implicitly[Format[IndividualStatePensionInformationErrorResponse503]]

      val individualStatePensionInformationErrorResponse503 = IndividualStatePensionInformationErrorResponse503(
        origin = HipOrigin.Hip,
        response = HipFailureResponse(failures =
          List(
            HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
            HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
            HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
          )
        )
      )

      val individualStatePensionInformationErrorResponse503JsonString =
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

      "deserialises and serialises successfully" in {
        Json.toJson(individualStatePensionInformationErrorResponse503) shouldBe Json.parse(
          individualStatePensionInformationErrorResponse503JsonString
        )
      }

      "deserialises to the model class" in {
        val _: IndividualStatePensionInformationErrorResponse503 =
          jsonFormat.reads(Json.parse(individualStatePensionInformationErrorResponse503JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(individualStatePensionInformationErrorResponse503JsonString)
        val individualStatePensionInformationErrorResponse503: IndividualStatePensionInformationErrorResponse503 =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(individualStatePensionInformationErrorResponse503)

        writtenJson shouldBe jValue
      }
    }
  }

}
