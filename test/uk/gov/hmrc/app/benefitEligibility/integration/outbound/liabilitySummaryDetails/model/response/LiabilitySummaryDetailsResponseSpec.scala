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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response

import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.LiabilitySummaryDetailsFormats._
import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{ErrorCode422, NpsErrorCode400}
import uk.gov.hmrc.app.benefitEligibility.common.{Country, Identifier, Reason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.{
  LiabilitySummaryDetailsErrorResponse400,
  LiabilitySummaryDetailsErrorResponse403,
  LiabilitySummaryDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums._
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema

import java.time.LocalDate

class LiabilitySummaryDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val liabilitySummaryDetailsOpenApiSpec =
    "test/resources/schemas/api/liabilitySummaryDetails/liabilitySummaryDetails.json"

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
      Some(
        List(
          LiabilityEmploymentDetailsList(
            employmentStatusForLiability = EmploymentStatusForLiability("EMP"),
            liabilityDetails = List(
              LiabilityDetails(
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
                officeDetails = Some(
                  OfficeDetails(
                    officeLocationDecode = Some(OfficeLocationDecode(1)),
                    officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                    officeIdentifier = Some(EnumOffidtp.None)
                  )
                )
              )
            )
          )
        )
      ),
      Some(Callback("Callback"))
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
        | "liabilityEmploymentDetailsList":[
        |   {
        |     "employmentStatusForLiability":"EMP",
        |     "liabilityDetails":[
        |       {
        |         "identifier":"RN000001A",
        |         "type":"ABROAD",
        |         "occurrenceNumber":1,
        |         "startDateStatus":"START DATE HELD",
        |         "endDateStatus":"END DATE HELD",
        |         "startDate":"2026-01-01",
        |         "endDate":"2026-01-01",
        |         "country":"GREAT BRITAIN",
        |         "trainingCreditApprovalStatus":"NO CREDIT FOR APPROVED TRAINING",
        |         "casepaperReferenceNumber":"SCH/123/4",
        |         "homeResponsibilitiesProtectionBenefitReference":"12345678AB",
        |         "homeResponsibilitiesProtectionRate":10.56,
        |         "lostCardNotificationReason":"NOT APPLICABLE",
        |         "lostCardRulingReason":"NOT APPLICABLE",
        |         "homeResponsibilityProtectionCalculationYear":2022,
        |         "awardAmount":10.56,"resourceGroupIdentifier":789,
        |         "officeDetails":{
        |           "officeLocationDecode":1,
        |           "officeLocationValue":"HQ STATIONARY STORE",
        |           "officeIdentifier":"(NONE)"
        |         }
        |       }
        |     ]
        |   }
        | ],
        | "callback":"Callback"
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

  }

  "LiabilitySummaryDetailsErrorResponse400" - {

    val jsonFormat = implicitly[Format[LiabilitySummaryDetailsErrorResponse400]]

    def liabilitySummaryDetails400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_400"),
        metaSchemaValidation = Some(Valid(()))
      )

    val liabilitySummaryDetailsErrorResponse400 = LiabilitySummaryDetailsErrorResponse400(
      Some(
        List(
          LiabilitySummaryDetailsError.LiabilitySummaryDetailsError400(
            Reason("HTTP message not readable"),
            NpsErrorCode400.NpsErrorCode400_2
          ),
          LiabilitySummaryDetailsError.LiabilitySummaryDetailsError400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            NpsErrorCode400.NpsErrorCode400_1
          )
        )
      )
    )

    val liabilitySummaryDetailsErrorResponse400JsonString =
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

      val invalidResponse = LiabilitySummaryDetailsErrorResponse400(
        Some(
          List(
            LiabilitySummaryDetailsError.LiabilitySummaryDetailsError400(
              Reason(
                "some reason with way to many letters letters letters letters  letters letters  letters letters  letters letters  letters letters  letters letters  letters letters"
              ),
              NpsErrorCode400_2
            ),
            LiabilitySummaryDetailsError.LiabilitySummaryDetailsError400(
              Reason(
                ""
              ),
              NpsErrorCode400_1
            )
          )
        )
      )

      liabilitySummaryDetails400JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.failures[0].reason: must be at most 128 characters long""",
          """$.failures[1].reason: must be at least 1 characters long"""
        )

    }
    "deserialises and serialises successfully" in {
      Json.toJson(liabilitySummaryDetailsErrorResponse400) shouldBe Json.parse(
        liabilitySummaryDetailsErrorResponse400JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LiabilitySummaryDetailsErrorResponse400 =
        jsonFormat.reads(Json.parse(liabilitySummaryDetailsErrorResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(liabilitySummaryDetailsErrorResponse400JsonString)
      val liabilitySummaryDetailsErrorResponse400: LiabilitySummaryDetailsErrorResponse400 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(liabilitySummaryDetailsErrorResponse400)

      writtenJson shouldBe jValue
    }
  }

  "LiabilitySummaryDetailsErrorResponse403" - {

    val jsonFormat = implicitly[Format[LiabilitySummaryDetailsErrorResponse403]]

    val liabilitySummaryDetailsErrorResponse403_2 =
      LiabilitySummaryDetailsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_2)

    val liabilitySummaryDetailsErrorResponse403_2JsonString =
      """{
        |  "reason": "Forbidden",
        |  "code": "403.2"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(liabilitySummaryDetailsErrorResponse403_2) shouldBe Json.parse(
        liabilitySummaryDetailsErrorResponse403_2JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LiabilitySummaryDetailsErrorResponse403 =
        jsonFormat.reads(Json.parse(liabilitySummaryDetailsErrorResponse403_2JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue = Json.parse(liabilitySummaryDetailsErrorResponse403_2JsonString)
      val liabilitySummaryDetailsErrorResponse403_2: LiabilitySummaryDetailsErrorResponse403 =
        jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue = jsonFormat.writes(liabilitySummaryDetailsErrorResponse403_2)

      writtenJson2 shouldBe jValue2
    }
  }

  "LiabilitySummaryDetailsErrorResponse422" - {

    def liabilitySummaryDetails422JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        liabilitySummaryDetailsOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponseOverrideable"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[LiabilitySummaryDetailsErrorResponse422]]

    val liabilitySummaryDetailsErrorResponse422 = LiabilitySummaryDetailsErrorResponse422(
      failures = Some(
        List(
          LiabilitySummaryDetailsError.LiabilitySummaryDetailsError422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      ),
      askUser = Some(true),
      fixRequired = Some(true),
      workItemRaised = Some(true)
    )

    val liabilitySummaryDetailsErrorResponse422JsonString =
      """{
        |  "failures": [
        |    {
        |      "reason": "HTTP message not readable",
        |      "code": "A589"
        |    }
        |  ],
        | "askUser": true,
        | "fixRequired": true,
        | "workItemRaised": true
        |}""".stripMargin

    "should match the openapi schema" in {

      val invalidResponse = LiabilitySummaryDetailsErrorResponse422(
        Some(
          List(
            LiabilitySummaryDetailsError.LiabilitySummaryDetailsError422(
              Reason(
                "some reason with way too many letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters"
              ),
              ErrorCode422("")
            )
          )
        ),
        askUser = Some(true),
        fixRequired = Some(true),
        workItemRaised = Some(true)
      )

      liabilitySummaryDetails422JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.failures[0].code: must be at least 1 characters long""",
          """$.failures[0].reason: must be at most 128 characters long"""
        )

    }
    "deserialises and serialises successfully" in {
      Json.toJson(liabilitySummaryDetailsErrorResponse422) shouldBe Json.parse(
        liabilitySummaryDetailsErrorResponse422JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LiabilitySummaryDetailsErrorResponse422 =
        jsonFormat.reads(Json.parse(liabilitySummaryDetailsErrorResponse422JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(liabilitySummaryDetailsErrorResponse422JsonString)
      val liabilitySummaryDetailsErrorResponse422: LiabilitySummaryDetailsErrorResponse422 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(liabilitySummaryDetailsErrorResponse422)

      writtenJson shouldBe jValue
    }
  }

}
