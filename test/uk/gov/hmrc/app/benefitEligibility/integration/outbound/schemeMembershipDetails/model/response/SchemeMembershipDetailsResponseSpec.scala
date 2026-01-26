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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response

import cats.data.Validated.Valid
import cats.data.{NonEmptyList, Validated}
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.{
  ErrorCode422,
  Identifier,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorReason403,
  Reason
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.SchemeMembership.*

class SchemeMembershipDetailsResponseSpec extends AnyFreeSpec with Matchers {

  val schemeMembershipDetailsOpenApiSpec =
    "test/resources/schemas/api/schemeMembershipDetails/schemeMembershipDetails.json"

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
              CallbackURL(
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
                CallbackURL(
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
    "SchemeMembershipDetailsErrorResponse400" - {

      val jsonFormat = implicitly[Format[SchemeMembershipDetailsErrorResponse400]]

      val schemeMembershipDetailsErrorResponse400 = SchemeMembershipDetailsErrorResponse400(
        List(
          SchemeMembershipDetailsError.SchemeMembershipDetailsError400(
            Reason("HTTP message not readable"),
            NpsErrorCode400.NpsErrorCode400_2
          ),
          SchemeMembershipDetailsError.SchemeMembershipDetailsError400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            NpsErrorCode400.NpsErrorCode400_1
          )
        )
      )

      val schemeMembershipDetailsErrorResponse400JsonString =
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
        Json.toJson(schemeMembershipDetailsErrorResponse400) shouldBe Json.parse(
          schemeMembershipDetailsErrorResponse400JsonString
        )
      }

      "deserialises to the model class" in {
        val _: SchemeMembershipDetailsErrorResponse400 =
          jsonFormat.reads(Json.parse(schemeMembershipDetailsErrorResponse400JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(schemeMembershipDetailsErrorResponse400JsonString)
        val schemeMembershipDetailsErrorResponse400: SchemeMembershipDetailsErrorResponse400 =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(schemeMembershipDetailsErrorResponse400)

        writtenJson shouldBe jValue
      }
    }
    "SchemeMembershipDetailsErrorResponse403" - {

      val jsonFormat = implicitly[Format[SchemeMembershipDetailsErrorResponse403]]

      val schemeMembershipDetailsErrorResponse403_1 =
        SchemeMembershipDetailsErrorResponse403(NpsErrorReason403.UserUnauthorised, NpsErrorCode403.NpsErrorCode403_1)

      val schemeMembershipDetailsErrorResponse403_2 =
        SchemeMembershipDetailsErrorResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

      val schemeMembershipDetailsErrorResponse403_1JsonString =
        """{
          |  "reason": "User Not Authorised",
          |  "code": "403.1"
          |}""".stripMargin

      val schemeMembershipDetailsErrorResponse403_2JsonString =
        """{
          |  "reason": "Forbidden",
          |  "code": "403.2"
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(schemeMembershipDetailsErrorResponse403_1) shouldBe Json.parse(
          schemeMembershipDetailsErrorResponse403_1JsonString
        )
        Json.toJson(schemeMembershipDetailsErrorResponse403_2) shouldBe Json.parse(
          schemeMembershipDetailsErrorResponse403_2JsonString
        )
      }

      "deserialises to the model class" in {
        val _: SchemeMembershipDetailsErrorResponse403 =
          jsonFormat.reads(Json.parse(schemeMembershipDetailsErrorResponse403_1JsonString)).get

        val _: SchemeMembershipDetailsErrorResponse403 =
          jsonFormat.reads(Json.parse(schemeMembershipDetailsErrorResponse403_2JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue1: JsValue = Json.parse(schemeMembershipDetailsErrorResponse403_1JsonString)
        val schemeMembershipDetailsErrorResponse403_1: SchemeMembershipDetailsErrorResponse403 =
          jsonFormat.reads(jValue1).get
        val writtenJson1: JsValue = jsonFormat.writes(schemeMembershipDetailsErrorResponse403_1)

        val jValue2: JsValue = Json.parse(schemeMembershipDetailsErrorResponse403_2JsonString)
        val schemeMembershipDetailsErrorResponse403_2: SchemeMembershipDetailsErrorResponse403 =
          jsonFormat.reads(jValue2).get
        val writtenJson2: JsValue = jsonFormat.writes(schemeMembershipDetailsErrorResponse403_2)

        writtenJson1 shouldBe jValue1
        writtenJson2 shouldBe jValue2
      }
    }
    "SchemeMembershipDetailsErrorResponse422" - {

      val jsonFormat = implicitly[Format[SchemeMembershipDetailsErrorResponse422]]

      val schemeMembershipDetailsErrorResponse422 = SchemeMembershipDetailsErrorResponse422(
        failures = List(
          SchemeMembershipDetailsError.SchemeMembershipDetailsError422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      )

      val schemeMembershipDetailsErrorResponse422JsonString =
        """{
          |  "failures": [
          |    {
          |      "reason": "HTTP message not readable",
          |      "code": "A589"
          |    }
          |  ]
          |}""".stripMargin

      "deserialises and serialises successfully" in {
        Json.toJson(schemeMembershipDetailsErrorResponse422) shouldBe Json.parse(
          schemeMembershipDetailsErrorResponse422JsonString
        )
      }

      "deserialises to the model class" in {
        val _: SchemeMembershipDetailsErrorResponse422 =
          jsonFormat.reads(Json.parse(schemeMembershipDetailsErrorResponse422JsonString)).get
      }

      "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
        val jValue: JsValue = Json.parse(schemeMembershipDetailsErrorResponse422JsonString)
        val schemeMembershipDetailsErrorResponse422: SchemeMembershipDetailsErrorResponse422 =
          jsonFormat.reads(jValue).get
        val writtenJson: JsValue = jsonFormat.writes(schemeMembershipDetailsErrorResponse422)

        writtenJson shouldBe jValue
      }
    }
  }

}
