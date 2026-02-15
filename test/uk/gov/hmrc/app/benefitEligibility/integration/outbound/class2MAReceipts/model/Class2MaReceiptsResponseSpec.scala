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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.common.{Identifier, NpsErrorReason, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

import java.time.LocalDate

class Class2MaReceiptsResponseSpec extends AnyFreeSpec with Matchers {

  val class2MaReceiptsOpenApiSpec =
    "test/resources/schemas/api/class2MaternityAllowanceReceipts/class2MaternityAllowanceReceipts.yaml"

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
          MA,
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

      "should match the openapi schema for a full response" in {
        class2MaReceiptsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(class2MAReceiptsSuccessResponse)
        ) shouldBe Nil

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

    "ErrorResponse400 (standard)" - {

      val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

      def class2MaReceipts400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
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
        class2MaReceipts400JsonSchema.validateAndGetErrors(
          Json.toJson(npsStandardErrorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse400 (hipFailureResponse)" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def class2MaReceipts400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
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
        class2MaReceipts400JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse403" - {

      def class2MaReceipts403JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
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
        class2MaReceipts403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_1)
        ) shouldBe Nil

        class2MaReceipts403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_2)
        ) shouldBe Nil
      }
    }

    "ErrorResponse422" - {

      def class2MaReceipts422JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
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
        class2MaReceipts422JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse422)
        ) shouldBe Nil
      }

    }

    "ErrorResponse503" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def class2MaReceipts503JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          class2MaReceiptsOpenApiSpec,
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
        class2MaReceipts503JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse503)
        ) shouldBe Nil
      }
    }
  }

}
