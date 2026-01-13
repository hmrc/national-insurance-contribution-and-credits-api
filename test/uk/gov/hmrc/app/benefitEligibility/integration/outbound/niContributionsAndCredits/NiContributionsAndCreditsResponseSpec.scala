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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.{ErrorCode400, ErrorCode422, Reason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsResponseValidation
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.ContributionCreditFormats.*

class NiContributionsAndCreditsResponseSpec extends AnyFreeSpec with Matchers {

  val niContributionsAndCreditsOpenApiSpec =
    "test/resources/schemas/api/niContributionsAndCredits/NI_Contributions_and_Credits.json"

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

      val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
        List(
          NicClass1(
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
        ),
        List(
          NicClass2(
            taxYear = Some(TaxYear(2022)),
            noOfCreditsAndConts = Some(NumberOfCreditsAndConts(53)),
            contributionCreditType = Some(ContributionCreditType.C1),
            class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
            class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
            class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
            creditSource = Some(CreditSource.NotKnown),
            latePaymentPeriod = Some(LatePaymentPeriod.L)
          )
        )
      )

      val jsonString =
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

      "should match the openapi schema" in {

        val invalidResponse = NiContributionsAndCreditsSuccessResponse(
          List(
            NicClass1(
              taxYear = Some(TaxYear(3000)),
              contributionCategoryLetter = Some(ContributionCategoryLetter("22")),
              contributionCategory = Some(ContributionCategory.None),
              contributionCreditType = Some(ContributionCreditType.C1),
              primaryContribution = Some(PrimaryContribution(BigDecimal("-99999999999999.98"))),
              class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
              primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("-99999999999999.98"))),
              creditSource = Some(CreditSource.NotKnown),
              employerName = Some(EmployerName("12345678")),
              latePaymentPeriod = Some(LatePaymentPeriod.Lx)
            )
          ),
          List(
            NicClass2(
              taxYear = Some(TaxYear(3000)),
              noOfCreditsAndConts = Some(NumberOfCreditsAndConts(100)),
              contributionCreditType = Some(ContributionCreditType.C1),
              class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("-99999999999999.98"))),
              class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("-99999999999999.98"))),
              class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
              creditSource = Some(CreditSource.NotKnown),
              latePaymentPeriod = Some(LatePaymentPeriod.L)
            )
          )
        )

        NiContributionsAndCreditsResponseValidation.niContributionsAndCreditsSuccessResponseValidator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """TaxYear value exceeds the maximum allowed limit of 2099""",
            """ContributionCategoryLetter value exceeds the max character limit of 1""",
            """ContributionCategoryLetter value does not match regex pattern: ^[A-Z]$""",
            """EmployerName value exceeds the max character limit of 6""",
            """EmployerName value does not match regex pattern: ^([A-Za-z ])+$""",
            """PrimaryContribution value is below the minimum allowed limit of 0""",
            """PrimaryPaidEarnings value is below the minimum allowed limit of 0""",
            """TaxYear value exceeds the maximum allowed limit of 2099""",
            """NumberOfCreditsAndConts value exceeds the maximum allowed limit of 53""",
            """Class2Or3EarningsFactor value is below the minimum allowed limit of 0""",
            """Class2NIContributionAmount value is below the minimum allowed limit of 0"""
          )
        )

        niContributionsAndCreditsResponseSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.niClass1[0].contributionCategoryLetter: does not match the regex pattern ^[A-Z]$""",
            """$.niClass1[0].contributionCategoryLetter: must be at most 1 characters long""",
            """$.niClass1[0].employerName: does not match the regex pattern ^([A-Za-z ])+$""",
            """$.niClass1[0].employerName: must be at most 6 characters long""",
            """$.niClass1[0].primaryContribution: must have a minimum value of 0""",
            """$.niClass1[0].primaryPaidEarnings: must have a minimum value of 0""",
            """$.niClass1[0].taxYear: must have a maximum value of 2099""",
            """$.niClass2[0].class2NIContributionAmount: must have a minimum value of 0""",
            """$.niClass2[0].class2Or3EarningsFactor: must have a minimum value of 0""",
            """$.niClass2[0].noOfCreditsAndConts: must have a maximum value of 53""",
            """$.niClass2[0].taxYear: must have a maximum value of 2099"""
          )

      }

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsSuccessResponse) shouldBe Json.parse(jsonString)
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonString)
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
            ErrorCode400.ErrorCode400_2
          ),
          NiContributionsAndCredits400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            ErrorCode400.ErrorCode400_1
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

      "should match the openapi schema" in {

        val invalidResponse = NiContributionsAndCreditsResponse400(
          List(
            NiContributionsAndCredits400(
              Reason(
                "some reason with way to many letters letters letters letters  letters letters  letters letters  letters letters  letters letters  letters letters  letters letters"
              ),
              ErrorCode400.ErrorCode400_2
            ),
            NiContributionsAndCredits400(
              Reason(
                ""
              ),
              ErrorCode400.ErrorCode400_1
            )
          )
        )

        NiContributionsAndCreditsResponseValidation.niContributionsAndCreditsResponse400Validator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            "Reason value exceeds the max character limit of 120",
            "Reason value is below the minimum character limit of 1"
          )
        )

        niContributionsAndCredits400JsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.failures[0].reason: must be at most 120 characters long""",
            """$.failures[1].reason: must be at least 1 characters long"""
          )

      }
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
        NiContributionsAndCreditsResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_2)

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

      "should match the openapi schema" in {

        val invalidResponse = NiContributionsAndCreditsResponse422(
          List(
            NiContributionsAndCredits422(
              Reason(
                "some reason with way too many letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters"
              ),
              ErrorCode422("")
            )
          )
        )

        NiContributionsAndCreditsResponseValidation.niContributionsAndCreditsResponse422Validator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            "Reason value exceeds the max character limit of 120",
            "ErrorCode422 value is below the minimum character limit of 1"
          )
        )

        niContributionsAndCreditsResponse422JsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.failures[0].code: must be at least 1 characters long""",
            """$.failures[0].reason: must be at most 120 characters long"""
          )

      }
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
