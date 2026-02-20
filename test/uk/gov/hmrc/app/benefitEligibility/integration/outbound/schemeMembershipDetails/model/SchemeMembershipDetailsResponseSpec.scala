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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.{Callback, CallbackUrl, Identifier, NpsErrorReason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*

class SchemeMembershipDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val schemeMembershipDetailsOpenApiSpec =
    "test/resources/schemas/api/schemeMembershipDetails/schemeMembershipDetails.yaml"

  "SchemeMembershipDetailsResponse" - {

    "SchemeMembershipDetailsSuccessResponse" - {

      def schemeMembershipDetailsSuccessResponseJsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
          SpecVersion.VersionFlag.V7,
          Some("GetSchemeMembershipDetailsResponse"),
          metaSchemaValidation = Some(Valid(()))
        )

      val jsonFormat = implicitly[Format[SchemeMembershipDetailsSuccessResponse]]

      import java.time.LocalDate

      val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
        schemeMembershipDetailsSummaryList = Some(
          List(
            SchemeMembershipDetailsSummary(
              stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
              schemeMembershipDetails = SchemeMembershipDetails(
                nationalInsuranceNumber = Identifier("AA123456"),
                schemeMembershipSequenceNumber = SchemeMembershipSequenceNumber(123),
                schemeMembershipOccurrenceNumber = SchemeMembershipOccurrenceNumber(1),
                schemeMembershipStartDate = Some(SchemeMembershipStartDate(LocalDate.of(2022, 6, 27))),
                contractedOutEmployerIdentifier = Some(ContractedOutEmployerIdentifier(789)),
                schemeMembershipEndDate = Some(SchemeMembershipEndDate(LocalDate.of(2022, 6, 27))),
                methodOfPreservationType = Some(MethodOfPreservation.NotApplicable0),
                totalLinkedGuaranteedMinimumPensionContractedOutDeductions =
                  Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(BigDecimal("10.56"))),
                accruedPensionContractedOutDeductionsValue =
                  Some(AccruedPensionContractedOutDeductionsValue(BigDecimal("10.56"))),
                totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988 =
                  Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(BigDecimal("10.56"))),
                accruedPensionContractedOutDeductionsValuePost1988 =
                  Some(AccruedPensionContractedOutDeductionsValuePost1988(BigDecimal("10.56"))),
                revaluationRate = Some(RevaluationRate.None),
                guaranteedMinimumPensionReconciliationStatus =
                  Some(GuaranteedMinimumPensionReconciliationStatus.NotApplicable),
                employeesReference = Some(EmployeesReference("123/456/ABC")),
                finalYearEarnings = Some(FinalYearEarnings(BigDecimal("10.56"))),
                penultimateYearEarnings = Some(PenultimateYearEarnings(BigDecimal("10.56"))),
                retrospectiveEarnings = Some(RetrospectiveEarnings(BigDecimal("10.56"))),
                furtherPaymentsConfirmation = Some(FurtherPaymentsConfirmation.FurtherPaymentAllowed),
                survivorStatus = Some(SurvivorStatus.NotApplicable),
                transferPremiumElectionDate = Some(TransferPremiumElectionDate(LocalDate.of(2022, 6, 27))),
                revaluationApplied = Some(RevaluationApplied(true)),
                stateEarningsRelatedPensionsSchemeNonRestorationValue =
                  Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(BigDecimal("10.56"))),
                stateEarningsRelatedPensionsSchemeValuePost1988 =
                  Some(StateEarningsRelatedPensionsSchemeValuePost1988(BigDecimal("10.56"))),
                apparentUnnotifiedTerminationStatus =
                  Some(ApparentUnnotifiedTerminationStatus.NoApparentUnnotifiedTermination),
                terminationMicrofilmNumber = Some(TerminationMicrofilmNumber(789)),
                debitVoucherMicrofilmNumber = Some(DebitVoucherMicrofilmNumber(40599123)),
                creationMicrofilmNumber = Some(CreationMicrofilmNumber(40599123)),
                inhibitSchemeProcessing = Some(InhibitSchemeProcessing(true)),
                extensionDate = Some(ExtensionDate(LocalDate.of(2022, 6, 27))),
                guaranteedMinimumPensionContractedOutDeductionsRevalued =
                  Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(BigDecimal("10.56"))),
                clericalCalculationInvolved = Some(Clercalc.NoClericalCalculationInvolved),
                clericallyControlledTotal = Some(ClericallyControlledTotal(BigDecimal("10.56"))),
                clericallyControlledTotalPost1988 = Some(ClericallyControlledTotalPost1988(BigDecimal("10.56"))),
                certifiedAmount = Some(CertifiedAmount(BigDecimal("10.56"))),
                enforcementStatus = Some(Enfcment.NotEnforced),
                stateSchemePremiumDeemed = Some(SspDeem.SspTypeReceivablesToBeTreatAsDeemed),
                transferTakeUpDate = Some(TransferTakeUpDate(LocalDate.of(2022, 6, 27))),
                schemeMembershipTransferSequenceNumber = Some(SchemeMembershipTransferSequenceNumber(123)),
                contributionCategoryFinalYear = Some(ContCatLetter.A),
                contributionCategoryPenultimateYear = Some(ContCatLetter.A),
                contributionCategoryRetrospectiveYear = Some(ContCatLetter.A),
                protectedRightsStartDate = Some(ProtectedRightsStartDate(LocalDate.of(2022, 6, 27))),
                schemeMembershipDebitReason = Some(SchemeMembershipDebitReason.NotApplicable),
                technicalAmount = Some(TechnicalAmount(BigDecimal("10.56"))),
                minimumFundTransferAmount = Some(MinimumFundTransferAmount(BigDecimal("10.56"))),
                actualTransferValue = Some(ActualTransferValue(BigDecimal("10.56"))),
                schemeSuspensionType = Some(SchemeSuspensionType.NoSuspension),
                guaranteedMinimumPensionConversionApplied = Some(GuaranteedMinimumPensionConversionApplied(true)),
                employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S3123456B")),
                schemeCreatingContractedOutNumberDetails = Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
                schemeTerminatingContractedOutNumberDetails =
                  Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                importingAppropriateSchemeNumberDetails = Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                apparentUnnotifiedTerminationDestinationDetails =
                  Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
              )
            )
          )
        ),
        callback = Some(
          Callback(
            callbackURL = Some(
              CallbackUrl(
                "some-url"
              )
            )
          )
        )
      )

      val jsonString =
        """{
          |  "callback": {
          |    "callbackURL": "some-url"
          |  },
          |  "schemeMembershipDetailsSummaryList": [
          |    {
          |      "schemeMembershipDetails": {
          |        "accruedPensionContractedOutDeductionsValue": 10.56,
          |        "accruedPensionContractedOutDeductionsValuePost1988": 10.56,
          |        "actualTransferValue": 10.56,
          |        "apparentUnnotifiedTerminationStatus": "No Apparent Unnotified Termination",
          |        "apparentUnnotifiedTerminationDestinationDetails": "S2123456B",
          |        "certifiedAmount": 10.56,
          |        "clericalCalculationInvolved": "NO CLERICAL CALCULATION INVOLVED",
          |        "clericallyControlledTotal": 10.56,
          |        "clericallyControlledTotalPost1988": 10.56,
          |        "contractedOutEmployerIdentifier": 789,
          |        "contributionCategoryFinalYear": "A",
          |        "contributionCategoryPenultimateYear": "A",
          |        "contributionCategoryRetrospectiveYear": "A",
          |        "creationMicrofilmNumber": 40599123,
          |        "debitVoucherMicrofilmNumber": 40599123,
          |        "employeesReference": "123/456/ABC",
          |        "employersContractedOutNumberDetails": "S3123456B",
          |        "enforcementStatus": "NOT ENFORCED",
          |        "extensionDate": "2022-06-27",
          |        "finalYearEarnings": 10.56,
          |        "furtherPaymentsConfirmation": "FURTHER PAYMENT ALLOWED",
          |        "guaranteedMinimumPensionContractedOutDeductionsRevalued": 10.56,
          |        "guaranteedMinimumPensionConversionApplied": true,
          |        "guaranteedMinimumPensionReconciliationStatus": "NOT APPLICABLE",
          |        "importingAppropriateSchemeNumberDetails": "S2123456B",
          |        "inhibitSchemeProcessing": true,
          |        "methodOfPreservationType": "NOT APPLICABLE (0)",
          |        "minimumFundTransferAmount": 10.56,
          |        "nationalInsuranceNumber": "AA123456",
          |        "penultimateYearEarnings": 10.56,
          |        "protectedRightsStartDate": "2022-06-27",
          |        "retrospectiveEarnings": 10.56,
          |        "revaluationApplied": true,
          |        "revaluationRate": "(NONE)",
          |        "schemeCreatingContractedOutNumberDetails": "A7123456Q",
          |        "schemeMembershipDebitReason": "NOT APPLICABLE",
          |        "schemeMembershipEndDate": "2022-06-27",
          |        "schemeMembershipOccurrenceNumber": 1,
          |        "schemeMembershipSequenceNumber": 123,
          |        "schemeMembershipStartDate": "2022-06-27",
          |        "schemeMembershipTransferSequenceNumber": 123,
          |        "schemeSuspensionType": "NO SUSPENSION",
          |        "stateEarningsRelatedPensionsSchemeNonRestorationValue": 10.56,
          |        "stateEarningsRelatedPensionsSchemeValuePost1988": 10.56,
          |        "stateSchemePremiumDeemed": "SSP TYPE RECEIVABLES TO BE TREAT AS DEEMED",
          |        "survivorStatus": "NOT APPLICABLE",
          |        "technicalAmount": 10.56,
          |        "schemeTerminatingContractedOutNumberDetails": "S2123456B",
          |        "terminationMicrofilmNumber": 789,
          |        "totalLinkedGuaranteedMinimumPensionContractedOutDeductions": 10.56,
          |        "totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988": 10.56,
          |        "transferPremiumElectionDate": "2022-06-27",
          |        "transferTakeUpDate": "2022-06-27"
          |      },
          |      "stakeholderPensionSchemeType": "Non-Stakeholder Pension"
          |    }
          |  ]
          |}
          |""".stripMargin

      "should match the openapi schema validation for the fields required for a given benefit Type (MA)" in {

        val invalidResponse = SchemeMembershipDetailsSuccessResponse(
          schemeMembershipDetailsSummaryList = Some(
            List(
              SchemeMembershipDetailsSummary(
                stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
                schemeMembershipDetails = SchemeMembershipDetails(
                  nationalInsuranceNumber = Identifier("AA123456"),
                  schemeMembershipSequenceNumber = SchemeMembershipSequenceNumber(123),
                  schemeMembershipOccurrenceNumber = SchemeMembershipOccurrenceNumber(1),
                  schemeMembershipStartDate = Some(SchemeMembershipStartDate(LocalDate.of(2022, 6, 27))),
                  contractedOutEmployerIdentifier = Some(ContractedOutEmployerIdentifier(789)),
                  schemeMembershipEndDate = Some(SchemeMembershipEndDate(LocalDate.of(2022, 6, 27))),
                  methodOfPreservationType = Some(MethodOfPreservation.NotApplicable0),
                  totalLinkedGuaranteedMinimumPensionContractedOutDeductions =
                    Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(BigDecimal("10.56"))),
                  accruedPensionContractedOutDeductionsValue =
                    Some(AccruedPensionContractedOutDeductionsValue(BigDecimal("10.56"))),
                  totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988 =
                    Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(BigDecimal("10.56"))),
                  accruedPensionContractedOutDeductionsValuePost1988 =
                    Some(AccruedPensionContractedOutDeductionsValuePost1988(BigDecimal("10.56"))),
                  revaluationRate = Some(RevaluationRate.None),
                  guaranteedMinimumPensionReconciliationStatus =
                    Some(GuaranteedMinimumPensionReconciliationStatus.NotApplicable),
                  employeesReference = Some(EmployeesReference("123/456/ABC")),
                  finalYearEarnings = Some(FinalYearEarnings(BigDecimal("10.56"))),
                  penultimateYearEarnings = Some(PenultimateYearEarnings(BigDecimal("10.56"))),
                  retrospectiveEarnings = Some(RetrospectiveEarnings(BigDecimal("10.56"))),
                  furtherPaymentsConfirmation = Some(FurtherPaymentsConfirmation.FurtherPaymentAllowed),
                  survivorStatus = Some(SurvivorStatus.NotApplicable),
                  transferPremiumElectionDate = Some(TransferPremiumElectionDate(LocalDate.of(2022, 6, 27))),
                  revaluationApplied = Some(RevaluationApplied(true)),
                  stateEarningsRelatedPensionsSchemeNonRestorationValue =
                    Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(BigDecimal("10.56"))),
                  stateEarningsRelatedPensionsSchemeValuePost1988 =
                    Some(StateEarningsRelatedPensionsSchemeValuePost1988(BigDecimal("10.56"))),
                  apparentUnnotifiedTerminationStatus =
                    Some(ApparentUnnotifiedTerminationStatus.NoApparentUnnotifiedTermination),
                  terminationMicrofilmNumber = Some(TerminationMicrofilmNumber(789)),
                  debitVoucherMicrofilmNumber = Some(DebitVoucherMicrofilmNumber(40599123)),
                  creationMicrofilmNumber = Some(CreationMicrofilmNumber(40599123)),
                  inhibitSchemeProcessing = Some(InhibitSchemeProcessing(true)),
                  extensionDate = Some(ExtensionDate(LocalDate.of(2022, 6, 27))),
                  guaranteedMinimumPensionContractedOutDeductionsRevalued =
                    Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(BigDecimal("10.56"))),
                  clericalCalculationInvolved = Some(Clercalc.NoClericalCalculationInvolved),
                  clericallyControlledTotal = Some(ClericallyControlledTotal(BigDecimal("10.56"))),
                  clericallyControlledTotalPost1988 = Some(ClericallyControlledTotalPost1988(BigDecimal("10.56"))),
                  certifiedAmount = Some(CertifiedAmount(BigDecimal("10.56"))),
                  enforcementStatus = Some(Enfcment.NotEnforced),
                  stateSchemePremiumDeemed = Some(SspDeem.SspTypeReceivablesToBeTreatAsDeemed),
                  transferTakeUpDate = Some(TransferTakeUpDate(LocalDate.of(2022, 6, 27))),
                  schemeMembershipTransferSequenceNumber = Some(SchemeMembershipTransferSequenceNumber(123)),
                  contributionCategoryFinalYear = Some(ContCatLetter.A),
                  contributionCategoryPenultimateYear = Some(ContCatLetter.A),
                  contributionCategoryRetrospectiveYear = Some(ContCatLetter.A),
                  protectedRightsStartDate = Some(ProtectedRightsStartDate(LocalDate.of(2022, 6, 27))),
                  schemeMembershipDebitReason = Some(SchemeMembershipDebitReason.NotApplicable),
                  technicalAmount = Some(TechnicalAmount(BigDecimal("10.56"))),
                  minimumFundTransferAmount = Some(MinimumFundTransferAmount(BigDecimal("10.56"))),
                  actualTransferValue = Some(ActualTransferValue(BigDecimal("10.56"))),
                  schemeSuspensionType = Some(SchemeSuspensionType.NoSuspension),
                  guaranteedMinimumPensionConversionApplied = Some(GuaranteedMinimumPensionConversionApplied(true)),
                  employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("INVALID_VALUE")),
                  schemeCreatingContractedOutNumberDetails =
                    Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
                  schemeTerminatingContractedOutNumberDetails =
                    Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                  importingAppropriateSchemeNumberDetails = Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                  apparentUnnotifiedTerminationDestinationDetails =
                    Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                )
              )
            )
          ),
          callback = Some(
            Callback(
              callbackURL = Some(
                CallbackUrl(
                  "some-url"
                )
              )
            )
          )
        )

        SchemeMembershipDetailsResponseValidation.schemeMembershipDetailsSuccessResponseValidator.validate(
          MA,
          invalidResponse
        ) shouldBe Validated.Invalid(
          NonEmptyList.of(
            """EmployersContractedOutNumberDetails value does not match regex pattern: ^([A-Z]{0,1}[3]\d{6}[A-Z ^GIO SUVZ]{0,1})$""",
            """EmployersContractedOutNumberDetails value exceeds the max character limit of 9"""
          )
        )

        schemeMembershipDetailsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(invalidResponse)
        ) shouldBe
          List(
            """$.schemeMembershipDetailsSummaryList[0].schemeMembershipDetails.employersContractedOutNumberDetails: does not match the regex pattern ^([A-Z]{0,1}[3]\d{6}[A-Z ^GIO SUVZ]{0,1})$""",
            """$.schemeMembershipDetailsSummaryList[0].schemeMembershipDetails.employersContractedOutNumberDetails: must be at most 9 characters long"""
          )
      }

      "should match the openapi schema for a full response" in {
        schemeMembershipDetailsSuccessResponseJsonSchema.validateAndGetErrors(
          Json.toJson(schemeMembershipDetailsSuccessResponse)
        ) shouldBe Nil

      }

      "deserialises and serialises successfully" in {
        Json.toJson(schemeMembershipDetailsSuccessResponse) shouldBe Json.parse(jsonString)
      }

      "deserialises to the model class" in {
        val _: SchemeMembershipDetailsSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(jsonString)
        val schemeMembershipDetailsSuccessResponse: SchemeMembershipDetailsSuccessResponse =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(schemeMembershipDetailsSuccessResponse)

        writtenJson shouldBe jValue
      }

    }

    "ErrorResponse400 (standard)" - {

      val jsonFormat = implicitly[Format[NpsStandardErrorResponse400]]

      def schemeMembershipDetails400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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
        schemeMembershipDetails400JsonSchema.validateAndGetErrors(
          Json.toJson(npsStandardErrorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse400 (hipFailureResponse)" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def schemeMembershipDetails400JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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
        schemeMembershipDetails400JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse400)
        ) shouldBe Nil
      }
    }

    "ErrorResponse403" - {

      def schemeMembershipDetails403JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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
        schemeMembershipDetails403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_1)
        ) shouldBe Nil

        schemeMembershipDetails403JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse403_2)
        ) shouldBe Nil
      }
    }

    "ErrorResponse422" - {

      def schemeMembershipDetails422JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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
        schemeMembershipDetails422JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse422)
        ) shouldBe Nil
      }

    }

    "ErrorResponse500" - {

      val jsonFormat = implicitly[Format[NpsErrorResponseHipOrigin]]

      def liabilitySummaryDetails500JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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

      def schemeMembershipDetails503JsonSchema: SimpleJsonSchema =
        SimpleJsonSchema(
          schemeMembershipDetailsOpenApiSpec,
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
        schemeMembershipDetails503JsonSchema.validateAndGetErrors(
          Json.toJson(errorResponse503)
        ) shouldBe Nil
      }
    }
  }

}
