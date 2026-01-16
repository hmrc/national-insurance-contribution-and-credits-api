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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response

import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.Class2MAReceiptsFormats.*
import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.{ErrorCode400, ErrorCode422, Identifier, Reason, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.enums.{
  ErrorCode403,
  ErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.{
  Class2MAReceiptsErrorResponse400,
  Class2MAReceiptsErrorResponse403,
  Class2MAReceiptsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema

import java.time.LocalDate

class Class2MaReceiptsResponseSpec extends AnyFreeSpec with Matchers {

  val class2MaReceiptsOpenApiSpec =
    "test/resources/schemas/api/class2MaternityAllowanceReceipts/class2MaternityAllowanceReceipts.json"

  "Class2MaReceiptsResponse" - {

    "Class2MAReceiptsSuccessResponse" - {

      def class2MaReceiptsSuccessResponseJsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("GetClass2MAReceiptsResponse"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[Class2MAReceiptsSuccessResponse]]

      val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
        Identifier("AA000001A"),
        List(
          Class2MAReceiptDetails(
            initials = Some(Initials("JP")),
            surname = Some(Surname("van Cholmondley-warner")),
            receivablePeriodStartDate = Some(ReceivablePeriodStartDate(LocalDate.parse("2025-12-10"))),
            receivablePeriodEndDate = Some(ReceivablePeriodEndDate(LocalDate.parse("2025-12-10"))),
            receivablePayment = Some(ReceivablePayment(10.56)),
            receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
            liabilityStartDate = Some(LiabilityStartDate(LocalDate.parse("2025-12-10"))),
            liabilityEndDate = Some(LiabilityEndDate(LocalDate.parse("2025-12-10"))),
            billAmount = Some(BillAmount(9999.98)),
            billScheduleNumber = Some(BillScheduleNumber(100)),
            isClosedRecord = Some(IsClosedRecord(true)),
            weeksPaid = Some(WeeksPaid(2))
          )
        )
      )

      val jsonString =
        """{
          |  "identifier": "AA000001A",
          |  "class2MAReceiptDetails": [
          |    {
          |      "initials": "JP",
          |      "surname": "van Cholmondley-warner",
          |      "receivablePeriodStartDate": "2025-12-10",
          |      "receivablePeriodEndDate": "2025-12-10",
          |      "receivablePayment": 10.56,
          |      "receiptDate": "2025-12-10",
          |      "liabilityStartDate": "2025-12-10",
          |      "liabilityEndDate": "2025-12-10",
          |      "billAmount": 9999.98,
          |      "billScheduleNumber": 100,
          |      "isClosedRecord": true,
          |      "weeksPaid": 2
          |    }
          |  ]
          |}""".stripMargin

      "should match the openapi schema" in {

        val invalidResponse = Class2MAReceiptsSuccessResponse(
          Identifier("AA000001A"),
          List(
            Class2MAReceiptDetails(
              initials = Some(Initials("2")),
              surname = Some(Surname("v")),
              receivablePeriodStartDate = Some(ReceivablePeriodStartDate(LocalDate.parse("2025-12-10"))),
              receivablePeriodEndDate = Some(ReceivablePeriodEndDate(LocalDate.parse("2025-12-10"))),
              receivablePayment = Some(ReceivablePayment(999999999999999.98)),
              receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
              liabilityStartDate = Some(LiabilityStartDate(LocalDate.parse("2025-12-10"))),
              liabilityEndDate = Some(LiabilityEndDate(LocalDate.parse("2025-12-10"))),
              billAmount = Some(BillAmount(999999999999999.98)),
              billScheduleNumber = Some(BillScheduleNumber(100)),
              isClosedRecord = Some(IsClosedRecord(true)),
              weeksPaid = Some(WeeksPaid(2))
            )
          )
        )

        Class2MAReceiptsResponseValidation.class2MAReceiptsSuccessResponseValidator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """Surname value is below the minimum character limit of 2""",
            """Initials value does not match regex pattern: ^([A-Za-z '-])+$""",
            """BillAmount value exceeds the maximum allowed limit of 99999999999999.98""",
            """ReceivablePayment value exceeds the maximum allowed limit of 99999999999999.98"""
          )
        )

        class2MaReceiptsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.class2MAReceiptDetails[0].billAmount: must have a maximum value of 9.999999999999998E13""",
            """$.class2MAReceiptDetails[0].initials: does not match the regex pattern ^([A-Za-z '-])+$""",
            """$.class2MAReceiptDetails[0].receivablePayment: must have a maximum value of 9.999999999999998E13""",
            """$.class2MAReceiptDetails[0].surname: must be at least 2 characters long"""
          )

      }

      "deserialises and serialises successfully" in {
        Json.toJson(class2MAReceiptsSuccessResponse) shouldBe Json.parse(jsonString)
      }

      "deserialises to the model class" in {
        val _: Class2MAReceiptsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue                                                  = Json.parse(jsonString)
        val class2MAReceiptsSuccessResponse: Class2MAReceiptsSuccessResponse = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(class2MAReceiptsSuccessResponse)

        writtenJson shouldBe jValue
      }

    }
    "Class2MAReceiptsErrorResponse400" - {

      val jsonFormat = implicitly[Format[Class2MAReceiptsErrorResponse400]]

      def class2MaReceipts400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("errorResponse_400"),
          metaSchemaValidation = Some(Valid(()))
        )

      val class2MAReceiptsErrorResponse400 = Class2MAReceiptsErrorResponse400(
        List(
          Class2MAReceiptsError.Class2MAReceiptsError400(
            Reason("HTTP message not readable"),
            ErrorCode400.ErrorCode400_2
          ),
          Class2MAReceiptsError.Class2MAReceiptsError400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            ErrorCode400.ErrorCode400_1
          )
        )
      )

      val class2MAReceiptsErrorResponse400JsonString =
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

        val invalidResponse = Class2MAReceiptsErrorResponse400(
          List(
            Class2MAReceiptsError.Class2MAReceiptsError400(
              Reason(
                "some reason with way to many letters letters letters letters  letters letters  letters letters  letters letters  letters letters  letters letters  letters letters"
              ),
              ErrorCode400.ErrorCode400_2
            ),
            Class2MAReceiptsError.Class2MAReceiptsError400(
              Reason(
                ""
              ),
              ErrorCode400.ErrorCode400_1
            )
          )
        )

        Class2MAReceiptsResponseValidation.class2MAReceiptsErrorResponse400Validator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            "Reason value exceeds the max character limit of 128",
            "Reason value is below the minimum character limit of 1"
          )
        )

        class2MaReceipts400JsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.failures[0].reason: must be at most 128 characters long""",
            """$.failures[1].reason: must be at least 1 characters long"""
          )

      }
      "deserialises and serialises successfully" in {
        Json.toJson(class2MAReceiptsErrorResponse400) shouldBe Json.parse(class2MAReceiptsErrorResponse400JsonString)
      }

      "deserialises to the model class" in {
        val _: Class2MAReceiptsErrorResponse400 =
          jsonFormat.reads(Json.parse(class2MAReceiptsErrorResponse400JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(class2MAReceiptsErrorResponse400JsonString)
        val class2MAReceiptsErrorResponse400: Class2MAReceiptsErrorResponse400 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(class2MAReceiptsErrorResponse400)

        writtenJson shouldBe jValue
      }
    }
    "Class2MAReceiptsErrorResponse403" - {

      val jsonFormat = implicitly[Format[Class2MAReceiptsErrorResponse403]]

      val class2MAReceiptsErrorResponse403_1 =
        Class2MAReceiptsErrorResponse403(ErrorReason403.UserUnauthorised, ErrorCode403.ErrorCode403_1)

      val class2MAReceiptsErrorResponse403_2 =
        Class2MAReceiptsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_2)

      val class2MAReceiptsErrorResponse403_1JsonString =
        """{
          |  "reason": "User Not Authorised",
          |  "code": "403.1"
          |}""".stripMargin

      val class2MAReceiptsErrorResponse403_2JsonString =
        """{
          |  "reason": "Forbidden",
          |  "code": "403.2"
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(class2MAReceiptsErrorResponse403_1) shouldBe Json.parse(
          class2MAReceiptsErrorResponse403_1JsonString
        )
        Json.toJson(class2MAReceiptsErrorResponse403_2) shouldBe Json.parse(
          class2MAReceiptsErrorResponse403_2JsonString
        )
      }

      "deserialises to the model class" in {
        val _: Class2MAReceiptsErrorResponse403 =
          jsonFormat.reads(Json.parse(class2MAReceiptsErrorResponse403_1JsonString)).get

        val _: Class2MAReceiptsErrorResponse403 =
          jsonFormat.reads(Json.parse(class2MAReceiptsErrorResponse403_2JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue1: JsValue = Json.parse(class2MAReceiptsErrorResponse403_1JsonString)
        val class2MAReceiptsErrorResponse403_1: Class2MAReceiptsErrorResponse403 = jsonFormat.reads(jValue1).get
        val writtenJson1: JsValue = jsonFormat.writes(class2MAReceiptsErrorResponse403_1)

        val jValue2: JsValue = Json.parse(class2MAReceiptsErrorResponse403_2JsonString)
        val class2MAReceiptsErrorResponse403_2: Class2MAReceiptsErrorResponse403 = jsonFormat.reads(jValue2).get
        val writtenJson2: JsValue = jsonFormat.writes(class2MAReceiptsErrorResponse403_2)

        writtenJson1 shouldBe jValue1
        writtenJson2 shouldBe jValue2
      }
    }
    "Class2MAReceiptsErrorResponse422" - {

      def class2MaReceipts422JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("errorResponse_422"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[Class2MAReceiptsErrorResponse422]]

      val class2MAReceiptsErrorResponse422 = Class2MAReceiptsErrorResponse422(
        failures = List(
          Class2MAReceiptsError.Class2MAReceiptsError422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      )

      val class2MAReceiptsErrorResponse422JsonString =
        """{
          |  "failures": [
          |    {
          |      "reason": "HTTP message not readable",
          |      "code": "A589"
          |    }
          |  ]
          |}""".stripMargin

      "should match the openapi schema" in {

        val invalidResponse = Class2MAReceiptsErrorResponse422(
          List(
            Class2MAReceiptsError.Class2MAReceiptsError422(
              Reason(
                "some reason with way too many letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters"
              ),
              ErrorCode422("")
            )
          )
        )

        Class2MAReceiptsResponseValidation.class2MAReceiptsError422ResponseValidator.validate(
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            "Reason value exceeds the max character limit of 128",
            "ErrorCode422 value is below the minimum character limit of 1"
          )
        )

        class2MaReceipts422JsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.failures[0].code: must be at least 1 characters long""",
            """$.failures[0].reason: must be at most 128 characters long"""
          )

      }
      "deserialises and serialises successfully" in {
        Json.toJson(class2MAReceiptsErrorResponse422) shouldBe Json.parse(class2MAReceiptsErrorResponse422JsonString)
      }

      "deserialises to the model class" in {
        val _: Class2MAReceiptsErrorResponse422 =
          jsonFormat.reads(Json.parse(class2MAReceiptsErrorResponse422JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(class2MAReceiptsErrorResponse422JsonString)
        val class2MAReceiptsErrorResponse422: Class2MAReceiptsErrorResponse422 = jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(class2MAReceiptsErrorResponse422)

        writtenJson shouldBe jValue
      }
    }
  }

}
