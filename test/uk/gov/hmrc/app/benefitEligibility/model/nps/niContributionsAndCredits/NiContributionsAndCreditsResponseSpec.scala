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

package uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits

import cats.data.Validated.Valid
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.model.common.{NpsErrorReason, ReceiptDate, TaxYear}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

import java.time.LocalDate

class NiContributionsAndCreditsResponseSpec extends AnyFreeSpec with Matchers {

  val niContributionsAndCreditsOpenApiSpec =
    "test/resources/schemas/api/niContributionsAndCredits/niContributionsAndCredits.yaml"

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
        totalGraduatedPensionUnits = Some(TotalGraduatedPensionUnits(53)),
        class1ContributionAndCredits = Some(
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
        class2Or3ContributionAndCredits = None
      )

      val niContributionsAndCreditsSuccessResponse2 = NiContributionsAndCreditsSuccessResponse(
        Some(TotalGraduatedPensionUnits(53)),
        class1ContributionAndCredits = None,
        class2Or3ContributionAndCredits = Some(
          List(
            Class2or3ContributionAndCredits(
              taxYear = Some(TaxYear(2022)),
              numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
              contributionCreditType = Some(NiContributionCreditType.C1),
              class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
              class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
              class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
              creditSource = Some(CreditSource.NotKnown),
              latePaymentPeriod = Some(LatePaymentPeriod.L),
              receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
            )
          )
        )
      )

      val fullSuccessResponse: NiContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
        Some(TotalGraduatedPensionUnits(53)),
        class1ContributionAndCredits = Some(
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
        class2Or3ContributionAndCredits = Some(
          List(
            Class2or3ContributionAndCredits(
              taxYear = Some(TaxYear(2022)),
              numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
              contributionCreditType = Some(NiContributionCreditType.C1),
              class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
              class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
              class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
              creditSource = Some(CreditSource.NotKnown),
              latePaymentPeriod = Some(LatePaymentPeriod.L),
              receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
            )
          )
        )
      )

      val jsonStringClass1ContributionAndCredits =
        """{
          |  "totalGraduatedPensionUnits":53,
          |  "class1ContributionAndCredits": [
          |    {
          |      "taxYear": 2022,
          |      "numberOfContributionsAndCredits": 53,
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

      val jsonStringClass2Or3ContributionAndCredits =
        """{
          |  "totalGraduatedPensionUnits":53,
          |  "class2Or3ContributionAndCredits": [
          |    {
          |      "taxYear": 2022,
          |      "numberOfContributionsAndCredits": 53,
          |      "contributionCreditType": "C1",
          |      "class2Or3EarningsFactor": 99999999999999.98,
          |      "class2Or3NIContributionAmount": 99999999999999.98,
          |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
          |      "creditSource": "NOT KNOWN",
          |      "latePaymentPeriod": "L",
          |      "receiptDate": "2025-10-10"
          |    }
          |  ]
          |}""".stripMargin

      val jsonStringFullResponse =
        """{
          |   "totalGraduatedPensionUnits":53,
          |   "class1ContributionAndCredits":[
          |      {
          |         "taxYear":2022,
          |         "numberOfContributionsAndCredits":53,
          |         "contributionCategoryLetter":"U",
          |         "contributionCategory":"(NONE)",
          |         "contributionCreditType":"C1",
          |         "primaryContribution":99999999999999.98,
          |         "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
          |         "primaryPaidEarnings":99999999999999.98,
          |         "creditSource":"NOT KNOWN",
          |         "employerName":"ipOpMs",
          |         "latePaymentPeriod":"L"
          |      }
          |   ],
          |   "class2Or3ContributionAndCredits":[
          |      {
          |         "taxYear":2022,
          |         "numberOfContributionsAndCredits":53,
          |         "contributionCreditType":"C1",
          |         "class2Or3EarningsFactor":99999999999999.98,
          |         "class2Or3NIContributionAmount":99999999999999.98,
          |         "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
          |         "creditSource":"NOT KNOWN",
          |         "latePaymentPeriod":"L"
          |      }
          |   ]
          |}""".stripMargin

      "should match the openapi schema for a full response" in {
        niContributionsAndCreditsResponseSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(fullSuccessResponse)
        ) shouldBe Nil
      }

      "deserialises and serialises successfully" in {
        Json.toJson(niContributionsAndCreditsSuccessResponse1) shouldBe Json.parse(
          jsonStringClass1ContributionAndCredits
        )
        Json.toJson(niContributionsAndCreditsSuccessResponse2) shouldBe Json.parse(
          jsonStringClass2Or3ContributionAndCredits
        )
      }

      "deserialises to the model class" in {
        val _: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(Json.parse(jsonStringClass1ContributionAndCredits)).get
        val _: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(Json.parse(jsonStringClass2Or3ContributionAndCredits)).get
      }

      "deserialises and reserialises to the same thing (class1ContributionAndCredits)" in {
        val jValue: JsValue = Json.parse(jsonStringClass1ContributionAndCredits)
        val niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsSuccessResponse)

        writtenJson shouldBe jValue
      }

      "deserialises and reserialises to the same thing (class2Or3ContributionAndCredits)" in {
        val jValue: JsValue = Json.parse(jsonStringClass2Or3ContributionAndCredits)
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

    // TODO - replicate these test across all the response specs where appropriate
    "NiContributionsAndCreditsResponse (400 HipErrorResponse) " - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      val jsonStringHipErrorResponse400 =
        """{
          |  "origin": "HIP",
          |  "response": {
          |   "failures": [
          |    {
          |      "type": "blah_1",
          |      "reason": "reason_1"
          |    },
          |    {
          |      "type": "blah_2",
          |      "reason": "reason_2"
          |    }
          |  ]
          | }
          |}""".stripMargin

      val hipFailureResponse400 = NpsErrorResponseHipOrigin(
        origin = HipOrigin.Hip,
        response = HipFailureResponse(failures =
          List(
            HipFailureItem(`type` = FailureType("blah_1"), reason = NpsErrorReason("reason_1")),
            HipFailureItem(`type` = FailureType("blah_2"), reason = NpsErrorReason("reason_2"))
          )
        )
      )

      "deserialises and serialises successfully" in {
        Json.toJson(hipFailureResponse400) shouldBe Json.parse(
          jsonStringHipErrorResponse400
        )
      }

      "deserialises to the model class" in {
        val _: NpsErrorResponseHipOrigin =
          jsonFormat.reads(Json.parse(jsonStringHipErrorResponse400)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue                                                 = Json.parse(jsonStringHipErrorResponse400)
        val niContributionsAndCreditsResponse400: NpsErrorResponseHipOrigin = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse400)

        writtenJson shouldBe jValue
      }
    }

    "NiContributionsAndCreditsResponse (400 StandardError) " - {

      val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

      val jsonStringStandardErrorResponse400 =
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

      val standardErrorResponse400 = NpsStandardErrorResponse400(
        origin = HipOrigin.Hip,
        response = NpsMultiErrorResponse(
          failures = Some(
            List(
              NpsSingleErrorResponse(reason = NpsErrorReason("reason_1"), code = NpsErrorCode("400.1")),
              NpsSingleErrorResponse(reason = NpsErrorReason("reason_2"), code = NpsErrorCode("400.2"))
            )
          )
        )
      )

      "deserialises and serialises successfully" in {
        Json.toJson(standardErrorResponse400) shouldBe Json.parse(
          jsonStringStandardErrorResponse400
        )
      }

      "deserialises to the model class" in {
        val _: NpsStandardErrorResponse400 =
          jsonFormat.reads(Json.parse(jsonStringStandardErrorResponse400)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonStringStandardErrorResponse400)
        val niContributionsAndCreditsResponse400: NpsStandardErrorResponse400 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse400)

        writtenJson shouldBe jValue
      }
    }

    "NiContributionsAndCreditsResponse (403) " - {

      val jsonFormat = implicitly[Format[NpsSingleErrorResponse]]

      val jsonStringResponse403 =
        """{
          |   "reason":"Forbidden",
          |   "code":"403.2"
          |}""".stripMargin

      val npsSingleErrorResponse =
        NpsSingleErrorResponse(reason = NpsErrorReason("Forbidden"), code = NpsErrorCode("403.2"))

      "deserialises and serialises successfully" in {
        Json.toJson(npsSingleErrorResponse) shouldBe Json.parse(
          jsonStringResponse403
        )
      }

      "deserialises to the model class" in {
        val _: NpsSingleErrorResponse =
          jsonFormat.reads(Json.parse(jsonStringResponse403)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue                                              = Json.parse(jsonStringResponse403)
        val niContributionsAndCreditsResponse403: NpsSingleErrorResponse = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse403)

        writtenJson shouldBe jValue
      }
    }

    "NiContributionsAndCreditsResponse (422) " - {

      val jsonFormat = implicitly[Format[NpsMultiErrorResponse]]

      val jsonStringResponse422 =
        """{
          |  "failures": [
          |    {
          |      "reason": "HTTP message not readable",
          |      "code": "A589"
          |    },
          |    {
          |      "reason": "HTTP message not writeable",
          |      "code": "B690"
          |    }
          |  ]
          |}""".stripMargin

      val multiError = NpsMultiErrorResponse(
        failures = Some(
          List(
            NpsSingleErrorResponse(reason = NpsErrorReason("HTTP message not readable"), code = NpsErrorCode("A589")),
            NpsSingleErrorResponse(reason = NpsErrorReason("HTTP message not writeable"), code = NpsErrorCode("B690"))
          )
        )
      )

      "deserialises and serialises successfully" in {
        Json.toJson(multiError) shouldBe Json.parse(
          jsonStringResponse422
        )
      }

      "deserialises to the model class" in {
        val _: NpsMultiErrorResponse =
          jsonFormat.reads(Json.parse(jsonStringResponse422)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue                                             = Json.parse(jsonStringResponse422)
        val niContributionsAndCreditsResponse422: NpsMultiErrorResponse = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse422)

        writtenJson shouldBe jValue
      }
    }

    "NiContributionsAndCreditsResponse (503) " - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      val jsonStringResponse503 =
        """{
          |  "origin": "HIP",
          |  "response": {
          |   "failures": [
          |    {
          |      "type": "blah_1",
          |      "reason": "reason_1"
          |    },
          |    {
          |      "type": "blah_2",
          |      "reason": "reason_2"
          |    }
          |  ]
          | }
          |}""".stripMargin

      val hipFailureResponse503 = NpsErrorResponseHipOrigin(
        origin = HipOrigin.Hip,
        response = HipFailureResponse(failures =
          List(
            HipFailureItem(`type` = FailureType("blah_1"), reason = NpsErrorReason("reason_1")),
            HipFailureItem(`type` = FailureType("blah_2"), reason = NpsErrorReason("reason_2"))
          )
        )
      )

      "deserialises and serialises successfully" in {
        Json.toJson(hipFailureResponse503) shouldBe Json.parse(
          jsonStringResponse503
        )
      }

      "deserialises to the model class" in {
        val _: NpsErrorResponseHipOrigin =
          jsonFormat.reads(Json.parse(jsonStringResponse503)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue                                                 = Json.parse(jsonStringResponse503)
        val niContributionsAndCreditsResponse503: NpsErrorResponseHipOrigin = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(niContributionsAndCreditsResponse503)

        writtenJson shouldBe jValue
      }
    }

  }

}
