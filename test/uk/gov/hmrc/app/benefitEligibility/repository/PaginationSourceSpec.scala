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

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.NpsStandardErrorResponse400
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationSource.{
  fromBenefitSchemeMembershipDetails,
  fromLiabilities,
  fromMarriageDetails
}
import uk.gov.hmrc.app.benefitEligibility.service.BenefitSchemeMembershipDetailsData

import java.time.LocalDate

class PaginationSourceSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  "PaginationSource" - {
    ".fromBenefitSchemeMembershipDetails" - {
      "should return PaginationSource if successful BenefitSchemeMembershipDetailsData with callback url" in {
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
          callback = Some(Callback(Some(CallbackUrl("SomeURL"))))
        )
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

        val benefitSchemeMembershipDetailsData = BenefitSchemeMembershipDetailsData(
          SuccessResult(
            ApiName.SchemeMembershipDetails,
            schemeMembershipDetailsSuccessResponse
          ),
          List(
            SuccessResult(
              ApiName.BenefitSchemeDetails,
              benefitSchemeDetailsSuccessResponse
            )
          )
        )

        val result = fromBenefitSchemeMembershipDetails(Some(benefitSchemeMembershipDetailsData))
        result shouldBe Some(PaginationSource(ApiName.BenefitSchemeDetails, Some("SomeURL")))
      }
      "should return None if successful BenefitSchemeMembershipDetailsData with No callback url" in {
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
          callback = None
        )
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

        val benefitSchemeMembershipDetailsData = BenefitSchemeMembershipDetailsData(
          SuccessResult(
            ApiName.SchemeMembershipDetails,
            schemeMembershipDetailsSuccessResponse
          ),
          List(
            SuccessResult(
              ApiName.BenefitSchemeDetails,
              benefitSchemeDetailsSuccessResponse
            )
          )
        )

        val result = fromBenefitSchemeMembershipDetails(Some(benefitSchemeMembershipDetailsData))
        result shouldBe None
      }
      "should return None if Failure BenefitSchemeMembershipDetailsData passed in" in {
        val errorResponse =
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
        val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
        val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get
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

        val benefitSchemeMembershipDetailsData = BenefitSchemeMembershipDetailsData(
          FailureResult(
            ApiName.SchemeMembershipDetails,
            ErrorReport(NpsNormalizedError.BadRequest, Some(response))
          ),
          List(
            SuccessResult(
              ApiName.BenefitSchemeDetails,
              benefitSchemeDetailsSuccessResponse
            )
          )
        )

        val result = fromBenefitSchemeMembershipDetails(Some(benefitSchemeMembershipDetailsData))
        result shouldBe None
      }
    }
    ".fromMarriageDetails" - {
      "should return PaginationSource if successful MarriageDetailsResult with callback url" in {
        val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
          MarriageDetailsSuccess.MarriageDetails(
            MarriageDetailsSuccess.ActiveMarriage(true),
            Some(
              List(
                MarriageDetailsSuccess
                  .MarriageDetailsListElement(
                    sequenceNumber = MarriageDetailsSuccess.SequenceNumber(2),
                    status = CivilPartner,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None,
                    None
                  )
              )
            ),
            Some(
              MarriageDetailsSuccess.Links(
                MarriageDetailsSuccess.SelfLink(
                  Some(MarriageDetailsSuccess.Href("SomeUrl")),
                  Some(MarriageDetailsSuccess.Methods.get)
                )
              )
            )
          )
        )

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        val result = fromMarriageDetails(Some(marriageDetailsResult))
        result shouldBe Some(PaginationSource(ApiName.MarriageDetails, Some("SomeUrl")))
      }
      "should return None if successful MarriageDetailsResult with no callback url" in {
        val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
          MarriageDetailsSuccess.MarriageDetails(
            MarriageDetailsSuccess.ActiveMarriage(true),
            None,
            None
          )
        )

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        val result = fromMarriageDetails(Some(marriageDetailsResult))
        result shouldBe None
      }
      "should return None if Failure MarriageDetailsResult passed in" in {
        val errorResponse =
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
        val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
        val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

        val marriageDetailsResult = FailureResult(
          ApiName.MarriageDetails,
          ErrorReport(NpsNormalizedError.BadRequest, Some(response))
        )

        val result = fromMarriageDetails(Some(marriageDetailsResult))
        result shouldBe None
      }
    }
    ".fromLiabilities" - {
      "should return PaginationSource if successful LiabilityResult with callback url" in {

        val liabilitiesResult = List(
          SuccessResult(
            ApiName.Liabilities,
            LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
          )
        )

        val result = fromLiabilities(liabilitiesResult)
        result shouldBe List(PaginationSource(Liabilities, Some("SomeUrl")))
      }
      "should return EmptyList if successful LiabilityResult with no callback url" in {
        val liabilitiesResult =
          List(SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None)))

        val result = fromLiabilities(liabilitiesResult)
        result shouldBe List()

      }
      "should return EmptyList if Failure LiabilityResult passed in" in {
        val errorResponse =
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
        val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
        val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

        val liabilitiesResult =
          List(FailureResult(ApiName.Liabilities, ErrorReport(NpsNormalizedError.BadRequest, Some(response))))

        val result = fromLiabilities(liabilitiesResult)
        result shouldBe List()

      }
    }
  }

}
