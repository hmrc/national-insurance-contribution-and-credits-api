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

package uk.gov.hmrc.app.benefitEligibility.repository

import cats.data.NonEmptyList
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.SuccessResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.{
  BenefitSchemeDetails,
  BenefitSchemeDetailsSuccessResponse,
  CurrentOptimisticLock,
  SchemeContractedOutNumberDetails
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.Methods.get
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.ApparentUnnotifiedTerminationStatus.NoApparentUnnotifiedTermination
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.Clercalc.NoClericalCalculationInvolved
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.ContCatLetter.A
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.Enfcment.NotEnforced
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.FurtherPaymentsConfirmation.FurtherPaymentAllowed
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.MethodOfPreservation.NotApplicable0
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.SchemeSuspensionType.NoSuspension
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.SspDeem.SspTypeReceivablesToBeTreatAsDeemed
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.StakeholderPensionSchemeType.NonStakeholderPension
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.SurvivorStatus.NotApplicable
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.{
  GuaranteedMinimumPensionReconciliationStatus,
  RevaluationRate,
  SchemeMembershipDebitReason
}
import uk.gov.hmrc.app.benefitEligibility.repository.Batch.createBatchDocument
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitSchemeMembershipDetailsData,
  BatchWithTaxWindowsResult,
  BatchResult
}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource

import java.time.{Instant, LocalDate}
import java.util.UUID

class BatchSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = Instant.parse("2007-12-03T10:15:30.00Z")
  }

  val nationalInsuranceNumber = Identifier("AB123456C")

  "Batch" - {
    ".createBatchDocument" - {
      "should return batch if MA batch result with next cursor" in {
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.MaBatch,
          nationalInsuranceNumber = nationalInsuranceNumber,
          liabilitiesResult = List(
            SuccessResult(
              ApiName.Liabilities,
              LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl1")))))
            )
          ),
          marriageDetailsResult = None,
          contributionCreditResult = BatchWithTaxWindowsResult(None, None),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe Some(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")),
            Json
              .toJson(
                MaBatch(
                  List(BatchWithCallback(Liabilities, "SomeUrl1")),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )
      }
      "should return batch if BSP batch result with next cursor" in {
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.BspBatch,
          nationalInsuranceNumber,
          liabilitiesResult = List(),
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(
                MarriageDetails(ActiveMarriage(true), None, Some(Links(SelfLink(Some(Href("SomeURL1")), Some(get)))))
              )
            )
          ),
          contributionCreditResult = BatchWithTaxWindowsResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob))
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe Some(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")),
            Json
              .toJson(
                BspBatch(
                  marriageDetails = Some(BatchWithCallback(ApiName.MarriageDetails, "SomeURL1")),
                  contributionsAndCredits = Some(
                    BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob)
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )
      }
      "should return batch if SEARCHLIGHT batch result with next cursor" in {
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.BspBatch,
          nationalInsuranceNumber,
          liabilitiesResult = List(),
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(
                MarriageDetails(ActiveMarriage(true), None, Some(Links(SelfLink(Some(Href("SomeURL1")), Some(get)))))
              )
            )
          ),
          contributionCreditResult = BatchWithTaxWindowsResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob))
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe Some(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")),
            Json
              .toJson(
                BspBatch(
                  marriageDetails = Some(BatchWithCallback(ApiName.MarriageDetails, "SomeURL1")),
                  contributionsAndCredits = Some(
                    BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob)
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )
      }
      "should return batch if GYSP batch result with next cursor" in {
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.GyspBatch,
          nationalInsuranceNumber,
          liabilitiesResult = List(),
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(
                MarriageDetails(ActiveMarriage(true), None, Some(Links(SelfLink(Some(Href("SomeURL1")), Some(get)))))
              )
            )
          ),
          contributionCreditResult = BatchWithTaxWindowsResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob))
          ),
          benefitSchemeMembershipDetailsData = Some(
            BenefitSchemeMembershipDetailsData(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                SchemeMembershipDetailsSuccessResponse(
                  Some(
                    List(
                      SchemeMembershipDetailsSummary(
                        NonStakeholderPension,
                        SchemeMembershipDetails(
                          Identifier("GD379251T"),
                          SchemeMembershipSequenceNumber(123),
                          SchemeMembershipOccurrenceNumber(1),
                          Some(SchemeMembershipStartDate(LocalDate.parse("2022-06-27"))),
                          Some(ContractedOutEmployerIdentifier(789)),
                          Some(SchemeMembershipEndDate(LocalDate.parse("2022-06-27"))),
                          Some(NotApplicable0),
                          Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(10.56)),
                          Some(AccruedPensionContractedOutDeductionsValue(10.56)),
                          Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
                          Some(AccruedPensionContractedOutDeductionsValuePost1988(10.56)),
                          Some(RevaluationRate.None),
                          Some(
                            GuaranteedMinimumPensionReconciliationStatus.NotApplicable
                          ),
                          Some(EmployeesReference("123/456/ABC")),
                          Some(FinalYearEarnings(10.56)),
                          Some(PenultimateYearEarnings(10.56)),
                          Some(RetrospectiveEarnings(10.56)),
                          Some(FurtherPaymentAllowed),
                          Some(NotApplicable),
                          Some(TransferPremiumElectionDate(LocalDate.parse("2022-06-27"))),
                          Some(RevaluationApplied(true)),
                          Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(10.56)),
                          Some(StateEarningsRelatedPensionsSchemeValuePost1988(10.56)),
                          Some(NoApparentUnnotifiedTermination),
                          Some(TerminationMicrofilmNumber(789)),
                          Some(DebitVoucherMicrofilmNumber(40599123)),
                          Some(CreationMicrofilmNumber(40599123)),
                          Some(InhibitSchemeProcessing(true)),
                          Some(ExtensionDate(LocalDate.parse("2022-06-27"))),
                          Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(10.56)),
                          Some(NoClericalCalculationInvolved),
                          Some(ClericallyControlledTotal(10.56)),
                          Some(ClericallyControlledTotalPost1988(10.56)),
                          Some(CertifiedAmount(10.56)),
                          Some(NotEnforced),
                          Some(SspTypeReceivablesToBeTreatAsDeemed),
                          Some(TransferTakeUpDate(LocalDate.parse("2022-06-27"))),
                          Some(SchemeMembershipTransferSequenceNumber(123)),
                          Some(A),
                          Some(A),
                          Some(A),
                          Some(ProtectedRightsStartDate(LocalDate.parse("2022-06-27"))),
                          Some(
                            SchemeMembershipDebitReason.NotApplicable
                          ),
                          Some(TechnicalAmount(10.56)),
                          Some(MinimumFundTransferAmount(10.56)),
                          Some(ActualTransferValue(10.56)),
                          Some(NoSuspension),
                          Some(GuaranteedMinimumPensionConversionApplied(true)),
                          Some(EmployersContractedOutNumberDetails("S2345678C")),
                          Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
                          Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                          Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                          Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                        )
                      )
                    )
                  ),
                  Some(Callback(Some(CallbackUrl("SomeURL2"))))
                )
              ),
              List(
                SuccessResult(
                  ApiName.NiContributionAndCredits,
                  BenefitSchemeDetailsSuccessResponse(
                    BenefitSchemeDetails(
                      CurrentOptimisticLock(4),
                      SchemeContractedOutNumberDetails("S2345678C"),
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None
                    ),
                    List()
                  )
                )
              )
            )
          ),
          callSystem = None,
          batchId = Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe Some(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")),
            Json
              .toJson(
                GyspBatch(
                  benefitSchemeMembershipDetails =
                    Some(BatchWithCallback(ApiName.BenefitSchemeDetails, "SomeURL2")),
                  marriageDetails = Some(BatchWithCallback(ApiName.MarriageDetails, "SomeURL1")),
                  contributionsAndCredits = Some(
                    BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob)
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )
      }
      "should return None if MA batch result without next cursor" in {
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.MaBatch,
          nationalInsuranceNumber,
          liabilitiesResult =
            List(SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None))),
          marriageDetailsResult = None,
          contributionCreditResult = BatchWithTaxWindowsResult(None, None),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = None
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe None
      }
      "should return None if BSP batch result without next cursor" in {
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.BspBatch,
          nationalInsuranceNumber,
          liabilitiesResult = List(),
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))
            )
          ),
          contributionCreditResult = BatchWithTaxWindowsResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob))
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = None
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe None
      }
      "should return None if GYSP batch result without next cursor" in {
        val dob = DateOfBirth(LocalDate.parse("2025-10-10"))
        val batchResult = BatchResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          batchType = BatchType.GyspBatch,
          nationalInsuranceNumber,
          liabilitiesResult = List(),
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))
            )
          ),
          contributionCreditResult = BatchWithTaxWindowsResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(BatchWithTaxWindows(NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))), dob))
          ),
          benefitSchemeMembershipDetailsData = Some(
            BenefitSchemeMembershipDetailsData(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                SchemeMembershipDetailsSuccessResponse(
                  Some(
                    List(
                      SchemeMembershipDetailsSummary(
                        NonStakeholderPension,
                        SchemeMembershipDetails(
                          Identifier("GD379251T"),
                          SchemeMembershipSequenceNumber(123),
                          SchemeMembershipOccurrenceNumber(1),
                          Some(SchemeMembershipStartDate(LocalDate.parse("2022-06-27"))),
                          Some(ContractedOutEmployerIdentifier(789)),
                          Some(SchemeMembershipEndDate(LocalDate.parse("2022-06-27"))),
                          Some(NotApplicable0),
                          Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(10.56)),
                          Some(AccruedPensionContractedOutDeductionsValue(10.56)),
                          Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
                          Some(AccruedPensionContractedOutDeductionsValuePost1988(10.56)),
                          Some(RevaluationRate.None),
                          Some(
                            GuaranteedMinimumPensionReconciliationStatus.NotApplicable
                          ),
                          Some(EmployeesReference("123/456/ABC")),
                          Some(FinalYearEarnings(10.56)),
                          Some(PenultimateYearEarnings(10.56)),
                          Some(RetrospectiveEarnings(10.56)),
                          Some(FurtherPaymentAllowed),
                          Some(NotApplicable),
                          Some(TransferPremiumElectionDate(LocalDate.parse("2022-06-27"))),
                          Some(RevaluationApplied(true)),
                          Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(10.56)),
                          Some(StateEarningsRelatedPensionsSchemeValuePost1988(10.56)),
                          Some(NoApparentUnnotifiedTermination),
                          Some(TerminationMicrofilmNumber(789)),
                          Some(DebitVoucherMicrofilmNumber(40599123)),
                          Some(CreationMicrofilmNumber(40599123)),
                          Some(InhibitSchemeProcessing(true)),
                          Some(ExtensionDate(LocalDate.parse("2022-06-27"))),
                          Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(10.56)),
                          Some(NoClericalCalculationInvolved),
                          Some(ClericallyControlledTotal(10.56)),
                          Some(ClericallyControlledTotalPost1988(10.56)),
                          Some(CertifiedAmount(10.56)),
                          Some(NotEnforced),
                          Some(SspTypeReceivablesToBeTreatAsDeemed),
                          Some(TransferTakeUpDate(LocalDate.parse("2022-06-27"))),
                          Some(SchemeMembershipTransferSequenceNumber(123)),
                          Some(A),
                          Some(A),
                          Some(A),
                          Some(ProtectedRightsStartDate(LocalDate.parse("2022-06-27"))),
                          Some(
                            SchemeMembershipDebitReason.NotApplicable
                          ),
                          Some(TechnicalAmount(10.56)),
                          Some(MinimumFundTransferAmount(10.56)),
                          Some(ActualTransferValue(10.56)),
                          Some(NoSuspension),
                          Some(GuaranteedMinimumPensionConversionApplied(true)),
                          Some(EmployersContractedOutNumberDetails("S2345678C")),
                          Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
                          Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                          Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                          Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                        )
                      )
                    )
                  ),
                  None
                )
              ),
              List(
                SuccessResult(
                  ApiName.NiContributionAndCredits,
                  BenefitSchemeDetailsSuccessResponse(
                    BenefitSchemeDetails(
                      CurrentOptimisticLock(4),
                      SchemeContractedOutNumberDetails("S2345678C"),
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None
                    ),
                    List()
                  )
                )
              )
            )
          ),
          callSystem = None,
          batchId = None
        )

        val result = createBatchDocument(batchResult, currentTimeSource)
        result shouldBe None
      }
    }
    ".BatchId" - {
      ".from" - {
        "should return a batchId if a valid UUID is used as cursorId" in {
          val uuidOne = UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")
          val result  = BatchId.from(CursorId(uuidOne.toString))

          result shouldBe Some(BatchId(uuidOne))
        }

        "should return none if an invalid UUID is used as cursorId" in {
          val invalidId = "some-invalid-cursor-id"
          val result    = BatchId.from(CursorId(invalidId))

          result shouldBe None
        }
      }
    }
  }

}
