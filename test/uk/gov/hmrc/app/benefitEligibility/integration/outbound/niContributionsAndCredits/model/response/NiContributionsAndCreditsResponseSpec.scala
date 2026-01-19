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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.{
  BenefitType,
  ErrorCode422,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorReason403,
  Reason
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums._
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.ContributionCreditFormats._

class NiContributionsAndCreditsResponseSpec extends AnyFreeSpec with Matchers {

  val niContributionsAndCreditsOpenApiSpec =
    "test/resources/schemas/api/niContributionsAndCredits/niContributionsAndCredits.json"

  "NiContributionsAndCreditsResponse" - {

    "NiContributionsAndCreditsSuccessResponse" - {

      def niContributionsAndCreditsResponseSuccessResponseJsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          niContributionsAndCreditsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("PostNIContCredResponse"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[NiContributionsAndCreditsSuccessResponse]]

      val niContributionsAndCreditsSuccessResponse1 = NiContributionsAndCreditsSuccessResponse(
        Some(
          List(
            NiClass1(
              taxYear = Some(TaxYear(2022)),
              contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
              contributionCategory = Some(ContributionCategory.None),
              contributionCreditType = Some(ContributionCreditType.C1),
              primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
              class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
              primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
              creditSource = Some(CreditSource.NotKnown),
              employerName = Some(EmployerName("ipOpMs")),
              latePaymentPeriod = Some(LatePaymentPeriod.L)
            )
          )
        ),
        None
      )

      val niContributionsAndCreditsSuccessResponse2 = NiContributionsAndCreditsSuccessResponse(
        niClass1 = None,
        niClass2 = Some(
          List(
            NiClass2(
              taxYear = Some(TaxYear(2022)),
              noOfCreditsAndConts = Some(NumberOfCreditsAndContributions(53)),
              contributionCreditType = Some(ContributionCreditType.C1),
              class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
              class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
              class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
              creditSource = Some(CreditSource.NotKnown),
              latePaymentPeriod = Some(LatePaymentPeriod.L)
            )
          )
        )
      )

      val jsonStringNiClass1 =
        """{
          |  "niClass1": [
          |    {
          |      "taxYear": 2022,
          |      "contributionCategoryLetter": "U",
          |      "contributionCategory": "(NONE)",
          |      "contributionCreditType": "C1",
          |      "primaryContribution": 99999999999999.98,
          |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
          |      "primaryPaidEarnings": 99999999999999.98,
          |      "creditSource": "NOT KNOWN",
          |      "employerName": "ipOpMs",
          |      "latePaymentPeriod": "L"
          |    }
          |  ]
          |}""".stripMargin

      val jsonStringNiClass2 =
        """{
          |  "niClass2": [
          |    {
          |      "taxYear": 2022,
          |      "noOfCreditsAndConts": 53,
          |      "contributionCreditType": "C1",
          |      "class2Or3EarningsFactor": 99999999999999.98,
          |      "class2NIContributionAmount": 99999999999999.98,
          |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
          |      "creditSource": "NOT KNOWN",
          |      "latePaymentPeriod": "L"
          |    }
          |  ]
          |}""".stripMargin

      val jsonStringFullResponse = """{
                                     |  "niClass1": [
                                     |    {
                                     |      "taxYear": 2022,
                                     |      "contributionCategoryLetter": "U",
                                     |      "contributionCategory": "(NONE)",
                                     |      "contributionCreditType": "C1",
                                     |      "primaryContribution": 99999999999999.98,
                                     |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
                                     |      "primaryPaidEarnings": 99999999999999.98,
                                     |      "creditSource": "NOT KNOWN",
                                     |      "employerName": "ipOpMs",
                                     |      "latePaymentPeriod": "L"
                                     |    }
                                     |  ],
                                     |  "niClass2": [
                                     |    {
                                     |      "taxYear": 2022,
                                     |      "noOfCreditsAndConts": 53,
                                     |      "contributionCreditType": "C1",
                                     |      "class2Or3EarningsFactor": 99999999999999.98,
                                     |      "class2NIContributionAmount": 99999999999999.98,
                                     |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
                                     |      "creditSource": "NOT KNOWN",
                                     |      "latePaymentPeriod": "L"
                                     |    }
                                     |  ]
                                     |}""".stripMargin

      "should validate the fields required for a given benefit type in line with the openapi schema (niClass1)" in {

        val invalidResponse = NiContributionsAndCreditsSuccessResponse(
          Some(
            List(
              NiClass1(
                taxYear = Some(TaxYear(3000)),
                contributionCategoryLetter = Some(ContributionCategoryLetter("A")),
                contributionCategory = Some(ContributionCategory.None),
                contributionCreditType = Some(ContributionCreditType.C1),
                primaryContribution = Some(PrimaryContribution(BigDecimal("100"))),
                class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("-99999999999999.98"))),
                creditSource = Some(CreditSource.NotKnown),
                employerName = Some(EmployerName("Surf")),
                latePaymentPeriod = Some(LatePaymentPeriod.Lx)
              )
            )
          ),
          None
//          Some(
//            List(
//              NiClass2(
//                taxYear = Some(TaxYear(3000)),
//                noOfCreditsAndConts = Some(NumberOfCreditsAndContributions(100)),
//                contributionCreditType = Some(ContributionCreditType.C1),
//                class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("100"))),
//                class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("100"))),
//                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
//                creditSource = Some(CreditSource.NotKnown),
//                latePaymentPeriod = Some(LatePaymentPeriod.L)
//              )
//            )
//          )
          //          )
        )

        NiContributionsAndCreditsResponseValidation.niContributionsAndCreditsSuccessResponseValidator.validate(
          BenefitType.MA,
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """TaxYear value exceeds the maximum allowed limit of 2099""",
            """PrimaryPaidEarnings value is below the minimum allowed limit of 0"""
          )
        )

        niContributionsAndCreditsResponseSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.niClass1[0].primaryPaidEarnings: must have a minimum value of 0""",
            """$.niClass1[0].taxYear: must have a maximum value of 2099"""
          )

      }
      "should validate the fields required for a given benefit type in line with the openapi schema (niClass2)" in {

        val invalidResponse = NiContributionsAndCreditsSuccessResponse(
          niClass1 = None,
          niClass2 = Some(
            List(
              NiClass2(
                taxYear = Some(TaxYear(3000)),
                noOfCreditsAndConts = Some(NumberOfCreditsAndContributions(100)),
                contributionCreditType = Some(ContributionCreditType.C1),
                class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("100"))),
                class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("100"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L)
              )
            )
          )
        )

        NiContributionsAndCreditsResponseValidation.niContributionsAndCreditsSuccessResponseValidator.validate(
          BenefitType.MA,
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """TaxYear value exceeds the maximum allowed limit of 2099""",
            """NumberOfCreditsAndContributions value exceeds the maximum allowed limit of 53"""
          )
        )

        niContributionsAndCreditsResponseSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.niClass2[0].noOfCreditsAndConts: must have a maximum value of 53""",
            """$.niClass2[0].taxYear: must have a maximum value of 2099"""
          )

      }

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsSuccessResponse1) shouldBe Json.parse(jsonStringNiClass1)
        Json.toJson(niContributionsAndCreditsSuccessResponse2) shouldBe Json.parse(jsonStringNiClass2)
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsSuccessResponse = jsonFormat.reads(Json.parse(jsonStringNiClass1)).get
        val _: NiContributionsAndCreditsSuccessResponse = jsonFormat.reads(Json.parse(jsonStringNiClass2)).get
      }

      "deserialises and reserialises to the same thing (NiClass1)" in {
        val jValue: JsValue = Json.parse(jsonStringNiClass1)
        val niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsSuccessResponse)

        writtenJson shouldBe jValue
      }

      "deserialises and reserialises to the same thing (NiClass2)" in {
        val jValue: JsValue = Json.parse(jsonStringNiClass2)
        val niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsSuccessResponse)

        writtenJson shouldBe jValue
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonStringFullResponse)
        val niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsSuccessResponse)

        writtenJson shouldBe jValue
      }

    }

    "NiContributionsAndCreditsResponse400" - {

      val jsonFormat = implicitly[Format[NiContributionsAndCreditsResponse400]]

      def niContributionsAndCredits400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          niContributionsAndCreditsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("errorResponse_400"),
          metaSchemaValidation = Some(Valid(()))
        )

      val niContributionsAndCreditsResponse400 = NiContributionsAndCreditsResponse400(
        List(
          NiContributionsAndCredits400(
            Reason("HTTP message not readable"),
            NpsErrorCode400.NpsErrorCode400_2
          ),
          NiContributionsAndCredits400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            NpsErrorCode400.NpsErrorCode400_1
          )
        )
      )

      val niContributionsAndCreditsResponse400JsonString =
        """{
          |  "failures": [
          |    {
          |      "reason": "HTTP message not readable",
          |      "code": "400.2"
          |    },
          |    {
          |      "reason": "Constraint violation: Invalid/Missing input parameter: <parameter>",
          |      "code": "400.1"
          |    }
          |  ]
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsResponse400) shouldBe Json.parse(
          niContributionsAndCreditsResponse400JsonString
        )
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsResponse400 =
          jsonFormat.reads(Json.parse(niContributionsAndCreditsResponse400JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(niContributionsAndCreditsResponse400JsonString)
        val niContributionsAndCreditsResponse400: NiContributionsAndCreditsResponse400 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse400)

        writtenJson shouldBe jValue
      }
    }
    "NiContributionsAndCreditsResponse403" - {

      val jsonFormat = implicitly[Format[NiContributionsAndCreditsResponse403]]

      val niContributionsAndCreditsResponse403 =
        NiContributionsAndCreditsResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

      val niContributionsAndCreditsResponse403JsonString =
        """{
          |  "reason": "Forbidden",
          |  "code": "403.2"
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsResponse403) shouldBe Json.parse(
          niContributionsAndCreditsResponse403JsonString
        )
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsResponse403 =
          jsonFormat.reads(Json.parse(niContributionsAndCreditsResponse403JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(niContributionsAndCreditsResponse403JsonString)
        val niContributionsAndCreditsResponse403: NiContributionsAndCreditsResponse403 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse403)

        writtenJson shouldBe jValue

      }
    }
    "NiContributionsAndCreditsResponse422" - {

      def niContributionsAndCreditsResponse422JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          niContributionsAndCreditsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("errorResponse_422"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[NiContributionsAndCreditsResponse422]]

      val niContributionsAndCreditsResponse422 = NiContributionsAndCreditsResponse422(
        failures = List(
          NiContributionsAndCredits422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      )

      val niContributionsAndCreditsResponse422JsonString =
        """{
          |  "failures": [
          |    {
          |      "reason": "HTTP message not readable",
          |      "code": "A589"
          |    }
          |  ]
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsResponse422) shouldBe Json.parse(
          niContributionsAndCreditsResponse422JsonString
        )
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsResponse422 =
          jsonFormat.reads(Json.parse(niContributionsAndCreditsResponse422JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(niContributionsAndCreditsResponse422JsonString)
        val niContributionsAndCreditsResponse422: NiContributionsAndCreditsResponse422 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse422)

        writtenJson shouldBe jValue
      }
    }
  }

}
