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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model

import cats.data.Validated
import cats.data.Validated.Valid
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

import java.time.LocalDate

class LiabilitySummaryDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val liabilitySummaryDetailsOpenApiSpec =
    "test/resources/schemas/api/liabilitySummaryDetails/liabilitySummaryDetails.yaml"

  def liabilitySummaryDetailsOpenApi: SimpleJsonSchema =
    SimpleJsonSchema(
      liabilitySummaryDetailsOpenApiSpec,
      SpecVersion.VersionFlag.V7,
      Some("GetLiabilitySummaryResponseHIP"),
      metaSchemaValidation = Some(Valid(()))
    )

  "LiabilitySummaryDetailsResponse" - {

    val jsonFormat = implicitly[Format[LiabilitySummaryDetailsSuccessResponse]]

    val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
      Some(
        List(
          LiabilityDetailsList(
            identifier = Identifier("RN000001A"),
            `type` = EnumLiabtp.Abroad,
            occurrenceNumber = OccurrenceNumber(1),
            startDateStatus = Some(EnumLtpsdttp.StartDateHeld),
            endDateStatus = Some(EnumLtpedttp.EndDateHeld),
            startDate = StartDate(LocalDate.parse("2026-01-01")),
            endDate = Some(EndDate(LocalDate.parse("2026-01-01"))),
            country = Some(Country.GreatBritain),
            trainingCreditApprovalStatus = Some(EnumAtcredfg.NoCreditForApprovedTraining),
            casepaperReferenceNumber = Some(CasepaperReferenceNumber("SCH/123/4")),
            homeResponsibilitiesProtectionBenefitReference =
              Some(HomeResponsibilitiesProtectionBenefitReference("12345678AB")),
            homeResponsibilitiesProtectionRate = Some(HomeResponsibilitiesProtectionRate(10.56)),
            lostCardNotificationReason = Some(EnumLcheadtp.NotApplicable),
            lostCardRulingReason = Some(EnumLcruletp.NotApplicable),
            homeResponsibilityProtectionCalculationYear = Some(HomeResponsibilityProtectionCalculationYear(2022)),
            awardAmount = Some(AwardAmount(10.56)),
            resourceGroupIdentifier = Some(ResourceGroupIdentifier(789)),
            homeResponsibilitiesProtectionIndicator = Some(EnumHrpIndicator.None),
            officeDetails = Some(
              OfficeDetails(
                officeLocationDecode = Some(OfficeLocationDecode(1)),
                officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                officeIdentifier = Some(EnumOffidtp.None)
              )
            )
          )
        )
      ),
      Some(Callback(Some(CallbackUrl("/some/url"))))
    )

    val jsonString =
      """{
        | "liabilityDetailsList":[
        |   {
        |    "identifier":"RN000001A",
        |    "type":"ABROAD",
        |    "occurrenceNumber":1,
        |    "startDateStatus":"START DATE HELD",
        |    "endDateStatus":"END DATE HELD",
        |    "startDate":"2026-01-01",
        |     "endDate":"2026-01-01",
        |     "country":"GREAT BRITAIN",
        |     "trainingCreditApprovalStatus":"NO CREDIT FOR APPROVED TRAINING",
        |     "casepaperReferenceNumber":"SCH/123/4",
        |     "homeResponsibilitiesProtectionBenefitReference":"12345678AB",
        |     "homeResponsibilitiesProtectionRate":10.56,
        |     "lostCardNotificationReason":"NOT APPLICABLE",
        |     "lostCardRulingReason":"NOT APPLICABLE",
        |     "homeResponsibilityProtectionCalculationYear":2022,
        |     "awardAmount":10.56,"resourceGroupIdentifier":789,
        |     "homeResponsibilitiesProtectionIndicator":"(NONE)",
        |     "officeDetails":{
        |       "officeLocationDecode":1,
        |       "officeLocationValue":"HQ STATIONARY STORE",
        |       "officeIdentifier":"(NONE)"
        |     }
        |   }
        | ],
        | "callback":{"callbackURL":"/some/url"}
        }""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(liabilitySummaryDetailsSuccessResponse) shouldBe Json.parse(jsonString)
    }

    "deserialises to the model class" in {
      val _: LiabilitySummaryDetailsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                                                                = Json.parse(jsonString)
      val liabilitySummaryDetailsSuccessResponse: LiabilitySummaryDetailsSuccessResponse = jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(liabilitySummaryDetailsSuccessResponse)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema for a full response" in {
      liabilitySummaryDetailsOpenApi.validateAndGetErrors(
        Json.toJson(liabilitySummaryDetailsSuccessResponse)
      ) shouldBe Nil
    }

  }

  "ErrorResponse400 (standard)" - {

    val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

    def liabilitySummaryDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
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
      liabilitySummaryDetails400JsonSchema.validateAndGetErrors(
        Json.toJson(npsStandardErrorResponse400)
      ) shouldBe Nil
    }
  }

  "ErrorResponse400 (hipFailureResponse)" - {

    val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

    def liabilitySummaryDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
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
      liabilitySummaryDetails400JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse400)
      ) shouldBe Nil
    }
  }

  "ErrorResponse403" - {

    def liabilitySummaryDetails403JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_403_ForbiddenOnly"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[NpsSingleErrorResponse]]

    val errorResponse403_2 =
      NpsSingleErrorResponse(NpsErrorReason("Forbidden"), NpsErrorCode("403.2"))

    val errorResponse403_2JsonString =
      """{
        |  "reason": "Forbidden",
        |  "code": "403.2"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse403_2) shouldBe Json.parse(
        errorResponse403_2JsonString
      )
    }

    "deserialises to the model class" in {
      val _: NpsSingleErrorResponse =
        jsonFormat.reads(Json.parse(errorResponse403_2JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue                           = Json.parse(errorResponse403_2JsonString)
      val errorResponse403_2: NpsSingleErrorResponse = jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue                      = jsonFormat.writes(errorResponse403_2)

      writtenJson2 shouldBe jValue2
    }

    "should match the openapi schema" in {
      liabilitySummaryDetails403JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse403_2)
      ) shouldBe Nil
    }
  }

  "ErrorResponse422" - {

    def liabilitySummaryDetails422JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponseOverrideable"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[NpsErrorResponse422Special]]

    val errorResponse422 = NpsErrorResponse422Special(
      failures = Some(
        List(
          NpsSingleErrorResponse(code = NpsErrorCode("48003"), reason = NpsErrorReason("Invalid Nino entered")),
          NpsSingleErrorResponse(code = NpsErrorCode("48004"), reason = NpsErrorReason("Invalid dob entered"))
        )
      ),
      askUser = Some(true),
      fixRequired = Some(false),
      workItemRaised = Some(true)
    )

    val errorResponse422JsonString =
      """{
        |  "failures": [
        |    {
        |      "reason": "Invalid Nino entered",
        |      "code": "48003"
        |    },
        |    {
        |      "reason": "Invalid dob entered",
        |      "code": "48004"
        |    }
        |  ],
        |  "askUser": true,
        |  "fixRequired": false,
        |  "workItemRaised": true
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(errorResponse422) shouldBe Json.parse(errorResponse422JsonString)
    }

    "deserialises to the model class" in {
      val _: NpsErrorResponse422Special =
        jsonFormat.reads(Json.parse(errorResponse422JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                              = Json.parse(errorResponse422JsonString)
      val errorResponse422: NpsErrorResponse422Special = jsonFormat.reads(jValue).get
      val writtenJson: JsValue                         = jsonFormat.writes(errorResponse422)

      writtenJson shouldBe jValue
    }

    "should match the openapi schema" in {
      liabilitySummaryDetails422JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse422)
      ) shouldBe Nil
    }

  }

  "ErrorResponse500" - {

    val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

    def liabilitySummaryDetails500JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
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

    def liabilitySummaryDetails503JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
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
      liabilitySummaryDetails503JsonSchema.validateAndGetErrors(
        Json.toJson(errorResponse503)
      ) shouldBe Nil
    }
  }

}
