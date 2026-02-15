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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model

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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

class BenefitSchemeDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val benefitSchemeDetailsResponseOpenApiSpec =
    "test/resources/schemas/api/benefitSchemeDetails/benefitSchemeDetails.yaml"

  "BenefitSchemeDetailsResponse" - {

    "BenefitSchemeDetailsSuccessResponse" - {

      def benefitSchemeDetailsSuccessResponseJsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("GetBenefitSchemeDetailsResponse"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[BenefitSchemeDetailsSuccessResponse]]

      val benefitSchemeDetailsSuccessResponse = BenefitSchemeDetailsSuccessResponse(
        benefitSchemeDetails = BenefitSchemeDetails(
          magneticTapeNumber = Some(MagneticTapeNumber(54321)),
          schemeName = Some(BenefitSchemeName("EXAMPLE PENSION SCHEME")),
          schemeStartDate = Some(SchemeStartDate("1985-04-06")),
          schemeCessationDate = Some(SchemeCessationDate("2024-12-31")),
          contractedOutDeductionExtinguishedDate = Some(ContractedOutDeductionExtinguishedDate("2024-12-31")),
          paymentSuspensionDate = Some(PaymentSuspensionDate("2024-10-01")),
          recoveriesSuspendedDate = Some(RecoveriesSuspendedDate("2024-10-01")),
          paymentRestartDate = Some(PaymentRestartDate("2024-10-01")),
          recoveriesRestartedDate = Some(RecoveriesRestartedDate("2024-10-01")),
          schemeNature = Some(UnitTrusts),
          benefitSchemeInstitution = Some(BenefitSchemeInstitutionType.UnitTrust),
          accruedGMPLiabilityServiceDate = Some(AccruedGMPLiabilityServiceDate("1990-04-06")),
          rerouteToSchemeCessation = Some(RerouteToSchemeCessation.ReRouteToCessation),
          statementInhibitor = Some(StatementInhibitor.Set),
          certificateCancellationDate = Some(CertificateCancellationDate("2024-12-31")),
          suspendedDate = Some(SuspendedDate("2024-10-01")),
          isleOfManInterest = Some(IsleOfManInterest(false)),
          schemeWindingUp = Some(SchemeWindingUp(true)),
          revaluationRateSequenceNumber = Some(RevaluationRateSequenceNumber(12)),
          benefitSchemeStatus = Some(BenefitSchemeStatus.BlockOnProvider),
          dateFormallyCertified = Some(DateFormallyCertified("1985-04-06")),
          privatePensionSchemeSanctionDate = Some(PrivatePensionSchemeSanctionDate("1985-04-06")),
          currentOptimisticLock = CurrentOptimisticLock(4),
          schemeConversionDate = Some(SchemeConversionDate("2024-12-31")),
          schemeInhibitionStatus = SchemeInhibitionStatus.ConvertedStakeholderPension,
          reconciliationDate = Some(ReconciliationDate("2025-03-31")),
          schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
        ),
        schemeAddressDetailsList = List(
          SchemeAddressDetails(
            schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
            schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
            schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
            schemeAddressEndDate = Some(SchemeAddressEndDate("2024-12-31")),
            country = Some(Country.Scotland),
            areaDiallingCode = Some(AreaDiallingCode.Code0131), // Note: This would need to be added to the enum
            schemeTelephoneNumber = Some(SchemeTelephoneNumber("0131 000 0000")),
            schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C"),
            benefitSchemeAddressDetails = Some(
              BenefitSchemeAddressDetails(
                schemeAddressLine1 = Some(SchemeAddressLine1("1 Sample Road")),
                schemeAddressLine2 = Some(SchemeAddressLine2("Unit 2")),
                schemeAddressLocality = Some(SchemeAddressLocality("Old Quarter")),
                schemeAddressPostalTown = Some(SchemeAddressPostalTown("Exampleburgh")),
                schemePostcode = Some(SchemePostcode("EX2 2EX"))
              )
            )
          )
        )
      )

      val jsonString =
        """{
          |  "benefitSchemeDetails": {
          |    "magneticTapeNumber": 54321,
          |    "schemeName": "EXAMPLE PENSION SCHEME",
          |    "schemeStartDate": "1985-04-06",
          |    "schemeCessationDate": "2024-12-31",
          |    "contractedOutDeductionExtinguishedDate": "2024-12-31",
          |    "paymentSuspensionDate": "2024-10-01",
          |    "recoveriesSuspendedDate": "2024-10-01",
          |    "paymentRestartDate": "2024-10-01",
          |    "recoveriesRestartedDate": "2024-10-01",
          |    "schemeNature": "UNIT TRUSTS",
          |    "benefitSchemeInstitution": "UNIT TRUST",
          |    "accruedGMPLiabilityServiceDate": "1990-04-06",
          |    "rerouteToSchemeCessation": "RE-ROUTE TO CESSATION",
          |    "statementInhibitor": "SET",
          |    "certificateCancellationDate": "2024-12-31",
          |    "suspendedDate": "2024-10-01",
          |    "isleOfManInterest": false,
          |    "schemeWindingUp": true,
          |    "revaluationRateSequenceNumber": 12,
          |    "benefitSchemeStatus": "BLOCK ON PROVIDER",
          |    "dateFormallyCertified": "1985-04-06",
          |    "privatePensionSchemeSanctionDate": "1985-04-06",
          |    "currentOptimisticLock": 4,
          |    "schemeConversionDate": "2024-12-31",
          |    "schemeInhibitionStatus": "Converted Stakeholder Pension",
          |    "reconciliationDate": "2025-03-31",
          |    "schemeContractedOutNumberDetails": "S2345678C"
          |  },
          |  "schemeAddressDetailsList": [
          |    {
          |      "schemeAddressType": "GENERAL CORRESPONDENCE",
          |      "schemeAddressSequenceNumber": 5,
          |      "schemeAddressStartDate": "2010-01-01",
          |      "schemeAddressEndDate": "2024-12-31",
          |      "country": "SCOTLAND",
          |      "areaDiallingCode": "0131 (99)",
          |      "schemeTelephoneNumber": "0131 000 0000",
          |      "schemeContractedOutNumberDetails": "S2345678C",
          |      "benefitSchemeAddressDetails": {
          |        "schemeAddressLine1": "1 Sample Road",
          |        "schemeAddressLine2": "Unit 2",
          |        "schemeAddressLocality": "Old Quarter",
          |        "schemeAddressPostalTown": "Exampleburgh",
          |        "schemePostcode": "EX2 2EX"
          |      }
          |    }
          |  ]
          |}""".stripMargin

      "should match the openapi schema validation for the fields required for a given benefit Type (MA)" in {

        val invalidResponse = BenefitSchemeDetailsSuccessResponse(
          benefitSchemeDetails = BenefitSchemeDetails(
            magneticTapeNumber = Some(MagneticTapeNumber(54321)),
            schemeName = Some(BenefitSchemeName("")), // invalid field
            schemeStartDate = Some(SchemeStartDate("1985-04-06")),
            schemeCessationDate = Some(SchemeCessationDate("2024-12-31")),
            contractedOutDeductionExtinguishedDate = Some(ContractedOutDeductionExtinguishedDate("2024-12-31")),
            paymentSuspensionDate = Some(PaymentSuspensionDate("2024-10-01")),
            recoveriesSuspendedDate = Some(RecoveriesSuspendedDate("2024-10-01")),
            paymentRestartDate = Some(PaymentRestartDate("2024-10-01")),
            recoveriesRestartedDate = Some(RecoveriesRestartedDate("2024-10-01")),
            schemeNature = Some(UnitTrusts),
            benefitSchemeInstitution = Some(BenefitSchemeInstitutionType.UnitTrust),
            accruedGMPLiabilityServiceDate = Some(AccruedGMPLiabilityServiceDate("1990-04-06")),
            rerouteToSchemeCessation = Some(RerouteToSchemeCessation.ReRouteToCessation),
            statementInhibitor = Some(StatementInhibitor.Set),
            certificateCancellationDate = Some(CertificateCancellationDate("2024-12-31")),
            suspendedDate = Some(SuspendedDate("2024-10-01")),
            isleOfManInterest = Some(IsleOfManInterest(false)),
            schemeWindingUp = Some(SchemeWindingUp(true)),
            revaluationRateSequenceNumber = Some(RevaluationRateSequenceNumber(12)),
            benefitSchemeStatus = Some(BenefitSchemeStatus.BlockOnProvider),
            dateFormallyCertified = Some(DateFormallyCertified("1985-04-06")),
            privatePensionSchemeSanctionDate = Some(PrivatePensionSchemeSanctionDate("1985-04-06")),
            currentOptimisticLock = CurrentOptimisticLock(4),
            schemeConversionDate = Some(SchemeConversionDate("2024-12-31")),
            schemeInhibitionStatus = SchemeInhibitionStatus.ConvertedStakeholderPension,
            reconciliationDate = Some(ReconciliationDate("2025-03-31")),
            schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
          ),
          schemeAddressDetailsList = List(
            SchemeAddressDetails(
              schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
              schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
              schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
              schemeAddressEndDate = Some(SchemeAddressEndDate("2024-12-31")),
              country = Some(Country.Scotland),
              areaDiallingCode = Some(AreaDiallingCode.Code0131), // Note: This would need to be added to the enum
              schemeTelephoneNumber = Some(SchemeTelephoneNumber("0131 000 0000")),
              schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C"),
              benefitSchemeAddressDetails = Some(
                BenefitSchemeAddressDetails(
                  schemeAddressLine1 = Some(SchemeAddressLine1("1 Sample Road")),
                  schemeAddressLine2 = Some(SchemeAddressLine2("Unit 2")),
                  schemeAddressLocality = Some(SchemeAddressLocality("Old Quarter")),
                  schemeAddressPostalTown = Some(SchemeAddressPostalTown("Exampleburgh")),
                  schemePostcode = Some(SchemePostcode("EX2 2EX"))
                )
              )
            )
          )
        )

        BenefitSchemeDetailsResponseValidation.benefitSchemeDetailsResponseValidationValidator.validate(
          MA,
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """BenefitSchemeName value is below the minimum character limit of 1""",
            """BenefitSchemeName value does not match regex pattern: ^[a-zA-Z0-9\\/,'.&() -]+$"""
          )
        )

        benefitSchemeDetailsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.benefitSchemeDetails.schemeName: does not match the regex pattern ^[a-zA-Z0-9\/,'.&() -]+$""",
            """$.benefitSchemeDetails.schemeName: must be at least 1 characters long"""
          )
      }

      "should match the openapi schema for a full response" in {
        benefitSchemeDetailsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(benefitSchemeDetailsSuccessResponse)
        ) shouldBe Nil
      }

      "deserialises and serialises successfully" in {
        Json.toJson(benefitSchemeDetailsSuccessResponse) shouldBe Json.parse(jsonString)
      }

      "deserialises to the model class" in {
        val _: BenefitSchemeDetailsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonString)
        val benefitSchemeDetailsSuccessResponse: BenefitSchemeDetailsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(benefitSchemeDetailsSuccessResponse)

        writtenJson shouldBe jValue
      }

    }

    "ErrorResponse400 (standard)" - {

      val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

      def benefitSchemeDetails400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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
        benefitSchemeDetails400JsonSchema.validateAndGetErrors(
          Json.toJson(npsStandardErrorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse400 (hipFailureResponse)" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def benefitSchemeDetails400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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
        benefitSchemeDetails400JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse403" - {

      def benefitSchemeDetails403JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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
        benefitSchemeDetails403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_1)
        ) shouldBe Nil

        benefitSchemeDetails403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_2)
        ) shouldBe Nil
      }
    }

    "ErrorResponse422" - {

      def benefitSchemeDetails422JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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
        benefitSchemeDetails422JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse422)
        ) shouldBe Nil
      }

    }

    "ErrorResponse500" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def liabilitySummaryDetails500JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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

      def benefitSchemeDetails503JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          benefitSchemeDetailsResponseOpenApiSpec,
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
        benefitSchemeDetails503JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse503)
        ) shouldBe Nil
      }
    }
  }

}
