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

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.{Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess._
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema

import java.time.LocalDate

class Class2MaReceiptsResponseSpec extends AnyFreeSpec with Matchers {

  val class2MaReceiptsOpenApiSpec = "test/resources/schemas/class2MaReceipts/Class_2_Maternity_Allowance_Receipts.json"

  def class2MaReceiptsOpenApi: SimpleJsonSchema =
    SimpleJsonSchema(
      class2MaReceiptsOpenApiSpec,
      SpecVersion.VersionFlag.V7,
      Some("GetClass2MAReceiptsResponse"),
      metaSchemaValidation = Some(Valid(()))
    )

  "Class2MaReceiptsResponse" - {

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

      class2MaReceiptsOpenApi.validateAndGetErrors(
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

}
