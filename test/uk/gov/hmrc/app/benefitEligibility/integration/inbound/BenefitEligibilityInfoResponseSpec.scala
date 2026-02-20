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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound

import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json
import uk.gov.hmrc.app.benefitEligibility.common._
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{
  Class2MAReceipts,
  IndividualStatePension,
  Liabilities,
  LongTermBenefitCalculationDetails,
  LongTermBenefitNotes,
  MarriageDetails,
  NiContributionAndCredits
}
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{
  InternalServerError,
  ServiceUnavailable,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.common.OverallResultStatus.{Failure, Partial, Success}
import uk.gov.hmrc.app.benefitEligibility.common.npsError._
import uk.gov.hmrc.app.benefitEligibility.common.npsError.HipOrigin.Hip
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.ErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.{
  LongTermBenefitNotesSuccessResponse,
  Note
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.{
  MarriageDetailsSuccessResponse,
  MarriageEndDate,
  MarriageStartDate,
  ReconciliationDate,
  SeparationDate,
  SpouseForename,
  SpouseSurname
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageEndDateStatus.Verified
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStartDateStatus
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStartDateStatus.NotKnown
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.{
  NiContributionsAndCreditsSuccess,
  enums
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums._
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitSchemeMembershipDetailsData,
  LongTermBenefitCalculationDetailsData
}

import java.time.LocalDate
import scala.util.Random

class BenefitEligibilityInfoResponseSpec extends AnyFreeSpec with Matchers with MockFactory with EitherValues {

  private val nationalInsuranceNumber: Identifier = Identifier("AB123456C")

  val filteredClass2MaReceipts = FilteredClass2MaReceipts(
    List(
      ReceiptDate(LocalDate.parse("2025-12-10"))
    )
  )

  val filteredLiabilitySummaryDetails = FilteredLiabilitySummaryDetails(
    List(
      FilteredLiabilitySummaryDetailItem(
        StartDate(LocalDate.parse("2026-01-01")),
        Some(EndDate(LocalDate.parse("2027-01-01")))
      )
    )
  )

  val filteredMarriageDetails =
    FilteredMarriageDetails(
      List(
        FilteredMarriageDetailsItem(
          status = CivilPartner,
          startDate = Some(MarriageStartDate(LocalDate.parse("1999-01-01"))),
          startDateStatus = Some(MarriageStartDateStatus.Verified),
          endDate = Some(MarriageEndDate(LocalDate.parse("2001-01-01"))),
          endDateStatus = Some(Verified),
          spouseIdentifier = Some(Identifier("AB123456C")),
          spouseForename = Some(SpouseForename("Skywalker")),
          spouseSurname = Some(SpouseSurname("Luke"))
        )
      )
    )

  val filteredIndividualStatePensionInfo = FilteredIndividualStatePensionInfo(
    Some(NumberOfQualifyingYears(35)),
    List(
      FilteredIndividualStatePensionContributionsByTaxYear(
        Some(TotalPrimaryPaidEarnings(48500.75)),
        Some(QualifyingTaxYear(true))
      ),
      FilteredIndividualStatePensionContributionsByTaxYear(
        Some(TotalPrimaryPaidEarnings(39875.90)),
        Some(QualifyingTaxYear(false))
      )
    )
  )

  val filteredSchemeMembershipDetails = FilteredSchemeMembershipDetails(
    List(
      FilteredSchemeMembershipDetailsItem(
        None,
        Some(SchemeMembershipStartDate(LocalDate.of(2022, 6, 27))),
        Some(SchemeMembershipEndDate(LocalDate.of(2022, 6, 27))),
        Some(EmployersContractedOutNumberDetails("S3123456B"))
      )
    )
  )

  val filteredLongTermBenefitCalculationDetails = FilteredLongTermBenefitCalculationDetails(
    List(
      FilteredLongTermBenefitCalculationDetailsItem(
        guaranteedMinimumPensionContractedOutDeductionsPre1988 =
          Some(GuaranteedMinimumPensionContractedOutDeductionsPre1988(10.56)),
        guaranteedMinimumPensionContractedOutDeductionsPost1988 =
          Some(GuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
        contractedOutDeductionsPre1988 = Some(ContractedOutDeductionsPre1988(10.56)),
        contractedOutDeductionsPost1988 = Some(ContractedOutDeductionsPost1988(10.56)),
        List(
          Note("Invalid Note Type Encountered."),
          Note(
            "Married Woman's/Widow's Reduced Rate Authority recorded on this account between 07/04/2020 and 07/04/2025."
          ),
          Note("Married Woman's/Widow's Reduced Rate Authority recorded on this account from 07/04/2025"),
          Note("Widow's Benefit Award UNKNOWN  recorded on this account between 07/04/2020 and 07/04/2025."),
          Note("Widow's Benefit Award UNKNOWN  recorded on this account from 07/04/2025."),
          Note("Retirement Position of UNKNOWN recorded on this account between 07/04/2020 and 07/04/2025."),
          Note("Retirement Position of UNKNOWN recorded on this account from 07/04/2025."),
          Note("Retirement Position of UNKNOWN recorded on this account between NOT KNOWN.")
        )
      )
    )
  )

  val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
    Identifier("AA000001A"),
    List(
      Class2MAReceiptDetails(
        initials = Some(Initials("JP")),
        surname = Some(Surname("van Cholmondley-warner")),
        receivablePeriodStartDate =
          Some(Class2MAReceiptsSuccess.ReceivablePeriodStartDate(LocalDate.parse("2025-12-10"))),
        receivablePeriodEndDate = Some(Class2MAReceiptsSuccess.ReceivablePeriodEndDate(LocalDate.parse("2025-12-10"))),
        receivablePayment = Some(ReceivablePayment(10.56)),
        receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
        liabilityStartDate = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
        liabilityEndDate = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
        billAmount = Some(BillAmount(9999.98)),
        billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
        isClosedRecord = Some(IsClosedRecord(true)),
        weeksPaid = Some(WeeksPaid(2))
      )
    )
  )

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
          endDate = Some(EndDate(LocalDate.parse("2027-01-01"))),
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

  val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
    totalGraduatedPensionUnits = Some(TotalGraduatedPensionUnits(53)),
    class1ContributionAndCredits = Some(
      List(
        Class1ContributionAndCredits(
          taxYear = Some(TaxYear(2022)),
          numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
          contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
          contributionCategory = Some(ContributionCategory.None),
          contributionCreditType = Some(NiContributionCreditType.C1),
          primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
          class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
          primaryPaidEarnings =
            Some(NiContributionsAndCreditsSuccess.PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
          creditSource = Some(CreditSource.NotKnown),
          employerName = Some(EmployerName("ipOpMs")),
          latePaymentPeriod = Some(LatePaymentPeriod.L)
        )
      )
    ),
    class2ContributionAndCredits = Some(
      List(
        Class2ContributionAndCredits(
          taxYear = Some(TaxYear(2022)),
          numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
          contributionCreditType = Some(NiContributionCreditType.C1),
          class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
          class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
          class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
          creditSource = Some(CreditSource.NotKnown),
          latePaymentPeriod = Some(LatePaymentPeriod.L)
        )
      )
    )
  )

  val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
    MarriageDetailsSuccess.MarriageDetails(
      MarriageDetailsSuccess.ActiveMarriage(true),
      Some(
        List(
          MarriageDetailsSuccess
            .MarriageDetailsListElement(
              sequenceNumber = MarriageDetailsSuccess.SequenceNumber(2),
              status = CivilPartner,
              startDate = Some(MarriageStartDate(LocalDate.parse("1999-01-01"))),
              startDateStatus = Some(MarriageStartDateStatus.Verified),
              endDate = Some(MarriageEndDate(LocalDate.parse("2001-01-01"))),
              endDateStatus = Some(Verified),
              spouseIdentifier = Some(Identifier("AB123456C")),
              spouseForename = Some(SpouseForename("Skywalker")),
              spouseSurname = Some(SpouseSurname("Luke")),
              separationDate = Some(SeparationDate(LocalDate.parse("2002-01-01"))),
              reconciliationDate = Some(ReconciliationDate(LocalDate.parse("2003-01-01")))
            )
        )
      ),
      Some(
        MarriageDetailsSuccess.Links(
          MarriageDetailsSuccess.SelfLink(
            Some(MarriageDetailsSuccess.Href("")),
            Some(MarriageDetailsSuccess.Methods.get)
          )
        )
      )
    )
  )

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
      reconciliationDate = Some(BenefitSchemeDetailsSuccess.ReconciliationDate("2025-03-31")),
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

  val individualStatePensionInformationSuccessResponse = IndividualStatePensionInformationSuccessResponse(
    identifier = Identifier("AA000001A"),
    numberOfQualifyingYears = Some(NumberOfQualifyingYears(35)),
    nonQualifyingYears = Some(NonQualifyingYears(5)),
    yearsToFinalRelevantYear = Some(YearsToFinalRelevantYear(3)),
    nonQualifyingYearsPayable = Some(NonQualifyingYearsPayable(2)),
    pre1975CCCount = Some(Pre1975CCCount(156)),
    dateOfEntry = Some(DateOfEntry("1975-04-06")),
    contributionsByTaxYear = Some(
      List(
        ContributionsByTaxYear(
          taxYear = Some(TaxYear(2022)),
          qualifyingTaxYear = Some(QualifyingTaxYear(true)),
          payableAccepted = Some(PayableAccepted(false)),
          amountNeeded = Some(AmountNeeded(BigDecimal("1250.50"))),
          classThreePayable = Some(ClassThreePayable(BigDecimal("824.20"))),
          classThreePayableBy = Some(ClassThreePayableBy("2028-04-05")),
          classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2030-04-05")),
          classTwoPayable = Some(ClassTwoPayable(BigDecimal("164.25"))),
          classTwoPayableBy = Some(ClassTwoPayableBy("2028-01-31")),
          classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2030-01-31")),
          classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(12)),
          totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("3456.78"))),
          niEarnings = Some(NiEarnings(BigDecimal("45000.00"))),
          coClassOnePaid = Some(CoClassOnePaid(BigDecimal("1234.56"))),
          coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("52000.00"))),
          niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(25)),
          niEarningsVoluntary = Some(NiEarningsVoluntary(8)),
          underInvestigationFlag = Some(UnderInvestigationFlag(true)),
          totalPrimaryPaidEarnings =
            Some(IndividualStatePensionInformationSuccess.TotalPrimaryPaidEarnings(BigDecimal("48500.75"))),
          otherCredits = Some(
            List(
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1Credit),
                creditSourceType = Some(CreditSourceType.JsaTapeInput),
                contributionCreditCount = Some(ContributionCreditCount(15))
              ),
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class3Credit),
                creditSourceType = Some(CreditSourceType.CarersCredit),
                contributionCreditCount = Some(ContributionCreditCount(52))
              ),
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class2NormalRate),
                creditSourceType = Some(CreditSourceType.ChildBenefit),
                contributionCreditCount = Some(ContributionCreditCount(-5))
              )
            )
          )
        ),
        ContributionsByTaxYear(
          taxYear = Some(TaxYear(2023)),
          qualifyingTaxYear = Some(QualifyingTaxYear(false)),
          payableAccepted = Some(PayableAccepted(true)),
          amountNeeded = Some(AmountNeeded(BigDecimal("2100.75"))),
          classThreePayable = Some(ClassThreePayable(BigDecimal("876.80"))),
          classThreePayableBy = Some(ClassThreePayableBy("2029-04-05")),
          classThreePayableByPenalty = Some(ClassThreePayableByPenalty("2031-04-05")),
          classTwoPayable = Some(ClassTwoPayable(BigDecimal("175.60"))),
          classTwoPayableBy = Some(ClassTwoPayableBy("2029-01-31")),
          classTwoPayableByPenalty = Some(ClassTwoPayableByPenalty("2031-01-31")),
          classTwoOutstandingWeeks = Some(ClassTwoOutstandingWeeks(35)),
          totalPrimaryContributions = Some(TotalPrimaryContributions(BigDecimal("2987.45"))),
          niEarnings = Some(NiEarnings(BigDecimal("38500.25"))),
          coClassOnePaid = Some(CoClassOnePaid(BigDecimal("987.65"))),
          coPrimaryPaidEarnings = Some(CoPrimaryPaidEarnings(BigDecimal("41250.80"))),
          niEarningsSelfEmployed = Some(NiEarningsSelfEmployed(42)),
          niEarningsVoluntary = Some(NiEarningsVoluntary(15)),
          underInvestigationFlag = Some(UnderInvestigationFlag(false)),
          totalPrimaryPaidEarnings =
            Some(IndividualStatePensionInformationSuccess.TotalPrimaryPaidEarnings(BigDecimal("39875.90"))),
          otherCredits = Some(
            List(
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1Credit),
                creditSourceType = Some(CreditSourceType.UniversalCredit),
                contributionCreditCount = Some(ContributionCreditCount(26))
              ),
              OtherCredits(
                contributionCreditType =
                  Some(IndividualStatePensionContributionCreditType.Class2VoluntaryDevelopmentWorkerRateA),
                creditSourceType = Some(CreditSourceType.StatutoryMaternityPayCredit),
                contributionCreditCount = Some(ContributionCreditCount(12))
              ),
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class3RateC),
                creditSourceType = Some(CreditSourceType.ModSpouseCivilPartnersCredits),
                contributionCreditCount = Some(ContributionCreditCount(39))
              ),
              OtherCredits(
                contributionCreditType = Some(IndividualStatePensionContributionCreditType.Class1EmployeeOnly),
                creditSourceType = Some(CreditSourceType.SharedParentalPay),
                contributionCreditCount = Some(ContributionCreditCount(-3))
              )
            )
          )
        )
      )
    )
  )

  val longTermBenefitCalculationDetailsSuccessResponse = LongTermBenefitCalculationDetailsSuccessResponse(
    statePensionAgeBefore2010TaxYear = Some(StatePensionAgeBefore2010TaxYear(true)),
    statePensionAgeAfter2016TaxYear = Some(StatePensionAgeAfter2016TaxYear(true)),
    benefitCalculationDetailsList = Some(
      List(
        BenefitCalculationDetailsList(
          additionalPensionAmountPre1997 = Some(AdditionalPensionAmountPre1997(10.56)),
          additionalPensionAmountPost1997 = Some(AdditionalPensionAmountPost1997(10.56)),
          pre97AgeRelatedAdditionalPension = Some(Pre97AgeRelatedAdditionalPension(10.56)),
          post97AgeRelatedAdditionalPension = Some(Post97AgeRelatedAdditionalPension(10.56)),
          basicPensionIncrementsCashValue = Some(BasicPensionIncrementsCashValue(10.56)),
          additionalPensionIncrementsCashValue = Some(AdditionalPensionIncrementsCashValue(10.56)),
          graduatedRetirementBenefitCashValue = Some(GraduatedRetirementBenefitCashValue(10.56)),
          totalGuaranteedMinimumPension = Some(TotalGuaranteedMinimumPension(10.56)),
          totalNonGuaranteedMinimumPension = Some(TotalNonGuaranteedMinimumPension(10.56)),
          longTermBenefitsIncrementalCashValue = Some(LongTermBenefitsIncrementalCashValue(10.56)),
          greatBritainPaymentAmount = Some(GreatBritainPaymentAmount(10.56)),
          dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
          notionalPost1997AdditionalPension = Some(NotionalPost1997AdditionalPension(10.56)),
          notionalPre1997AdditionalPension = Some(NotionalPre1997AdditionalPension(10.56)),
          inheritableNotionalAdditionalPensionIncrements = Some(InheritableNotionalAdditionalPensionIncrements(10.56)),
          conditionOneSatisfied = Some(ConditionOneSatisfied("H")),
          reasonForFormIssue = Some(ReasonForFormIssue("REQUESTED BENEFIT CALCULATION")),
          longTermBenefitsCategoryACashValue = Some(LongTermBenefitsCategoryACashValue(10.56)),
          longTermBenefitsCategoryBLCashValue = Some(LongTermBenefitsCategoryBLCashValue(10.56)),
          longTermBenefitsUnitValue = Some(LongTermBenefitsUnitValue(10.56)),
          additionalNotionalPensionAmountPost2002 = Some(AdditionalNotionalPensionAmountPost2002(10.56)),
          additionalPensionAmountPost2002 = Some(AdditionalPensionAmountPost2002(10.56)),
          additionalNotionalPensionIncrementsInheritedPost2002 =
            Some(AdditionalNotionalPensionIncrementsInheritedPost2002(10.56)),
          additionalPensionIncrementsInheritedPost2002 = Some(AdditionalPensionIncrementsInheritedPost2002(10.56)),
          post02AgeRelatedAdditionalPension = Some(Post02AgeRelatedAdditionalPension(10.56)),
          pre1975ShortTermBenefits = Some(Pre1975ShortTermBenefits(2)),
          survivingSpouseAge = Some(SurvivingSpouseAge(45)),
          operativeBenefitStartDate = Some(OperativeBenefitStartDate(LocalDate.parse("2022-06-27"))),
          sicknessBenefitStatusForReports = Some(SicknessBenefitStatusForReports("Y")),
          benefitCalculationDetail = Some(
            BenefitCalculationDetail(
              nationalInsuranceNumber = Identifier("AA123456"),
              benefitType = LongTermBenefitType.All,
              associatedCalculationSequenceNumber = AssociatedCalculationSequenceNumber(86),
              calculationStatus = Some(CalculationStatus.Definitive),
              substitutionMethod1 = Some(SubstitutionMethod1(235)),
              substitutionMethod2 = Some(SubstitutionMethod2(235)),
              calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27"))),
              guaranteedMinimumPensionContractedOutDeductionsPre1988 =
                Some(GuaranteedMinimumPensionContractedOutDeductionsPre1988(10.56)),
              guaranteedMinimumPensionContractedOutDeductionsPost1988 =
                Some(GuaranteedMinimumPensionContractedOutDeductionsPost1988(10.56)),
              contractedOutDeductionsPre1988 = Some(ContractedOutDeductionsPre1988(10.56)),
              contractedOutDeductionsPost1988 = Some(ContractedOutDeductionsPost1988(10.56)),
              additionalPensionPercentage = Some(AdditionalPensionPercentage(10.56)),
              basicPensionPercentage = Some(BasicPensionPercentage(86)),
              survivorsBenefitAgeRelatedPensionPercentage = Some(SurvivorsBenefitAgeRelatedPensionPercentage(10.56)),
              additionalAgeRelatedPensionPercentage = Some(AdditionalAgeRelatedPensionPercentage(10.56)),
              inheritedBasicPensionPercentage = Some(InheritedBasicPensionPercentage(10.56)),
              inheritedAdditionalPensionPercentage = Some(InheritedAdditionalPensionPercentage(10.56)),
              inheritedGraduatedPensionPercentage = Some(InheritedGraduatedPensionPercentage(10.56)),
              inheritedGraduatedBenefit = Some(InheritedGraduatedBenefit(10.56)),
              calculationSource = Some(CalculationSource.ApComponentSuspectAprilMayCalc),
              payday = Some(Payday.Friday),
              dateOfBirth = Some(DateOfBirth(LocalDate.parse("2022-06-27"))),
              husbandDateOfDeath = Some(HusbandDateOfDeath(LocalDate.parse("2022-06-27"))),
              additionalPost1997PensionPercentage = Some(AdditionalPost1997PensionPercentage(10.56)),
              additionalPost1997AgeRelatedPensionPercentage =
                Some(AdditionalPost1997AgeRelatedPensionPercentage(10.56)),
              additionalPensionNotionalPercentage = Some(AdditionalPensionNotionalPercentage(10.56)),
              additionalPost1997PensionNotionalPercentage = Some(AdditionalPost1997PensionNotionalPercentage(10.56)),
              inheritedAdditionalPensionNotionalPercentage = Some(InheritedAdditionalPensionNotionalPercentage(10.56)),
              inheritableAdditionalPensionPercentage = Some(InheritableAdditionalPensionPercentage(90)),
              additionalPost2002PensionNotionalPercentage = Some(AdditionalPost2002PensionNotionalPercentage(10.56)),
              additionalPost2002PensionPercentage = Some(AdditionalPost2002PensionPercentage(10.56)),
              inheritedAdditionalPost2002PensionNotionalPercentage =
                Some(InheritedAdditionalPost2002PensionNotionalPercentage(10.56)),
              inheritedAdditionalPost2002PensionPercentage = Some(InheritedAdditionalPost2002PensionPercentage(10.56)),
              additionalPost2002AgeRelatedPensionPercentage =
                Some(AdditionalPost2002AgeRelatedPensionPercentage(10.56)),
              singleContributionConditionRulesApply = Some(SingleContributionConditionRulesApply(true)),
              officeDetails = Some(
                OfficeDetails(
                  officeLocationDecode = Some(OfficeLocationDecode(1)),
                  officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
                  officeIdentifier = Some(EnumOffidtp.None)
                )
              ),
              newStatePensionCalculationDetails = Some(
                NewStatePensionCalculationDetails(
                  netAdditionalPensionPre1997 = Some(NetAdditionalPensionPre1997(10.56)),
                  oldRulesStatePensionEntitlement = Some(OldRulesStatePensionEntitlement(10.56)),
                  netRulesAmount = Some(NetRulesAmount(10.56)),
                  derivedRebateAmount = Some(DerivedRebateAmount(10.56)),
                  initialStatePensionAmount = Some(InitialStatePensionAmount(10.56)),
                  protectedPayment2016 = Some(ProtectedPayment2016(10.56)),
                  minimumQualifyingPeriodMet = Some(MinimumQualifyingPeriodMet(true)),
                  qualifyingYearsAfter2016 = Some(QualifyingYearsAfter2016(3)),
                  newStatePensionQualifyingYears = Some(NewStatePensionQualifyingYears(20)),
                  newStatePensionRequisiteYears = Some(NewStatePensionRequisiteYears(35)),
                  newStatePensionEntitlement = Some(NewStatePensionEntitlement(10.56)),
                  protectedPayment = Some(ProtectedPayment(10.56)),
                  pensionSharingOrderContractedOutEmploymentsGroup =
                    Some(PensionSharingOrderContractedOutEmploymentsGroup(true)),
                  pensionSharingOrderStateEarningsRelatedPensionScheme =
                    Some(PensionSharingOrderStateEarningsRelatedPensionScheme(true)),
                  considerReducedRateElection = Some(ConsiderReducedRateElection(true)),
                  weeklyBudgetingLoanAmount = Some(WeeklyBudgetingLoanAmount(10.56)),
                  calculationDate = Some(CalculationDate(LocalDate.parse("2022-06-27")))
                )
              )
            )
          )
        )
      )
    )
  )

  val longTermBenefitNotesSuccessResponse = LongTermBenefitNotesSuccessResponse(
    List(
      Note("Invalid Note Type Encountered."),
      Note(
        "Married Woman's/Widow's Reduced Rate Authority recorded on this account between 07/04/2020 and 07/04/2025."
      ),
      Note("Married Woman's/Widow's Reduced Rate Authority recorded on this account from 07/04/2025"),
      Note("Widow's Benefit Award UNKNOWN  recorded on this account between 07/04/2020 and 07/04/2025."),
      Note("Widow's Benefit Award UNKNOWN  recorded on this account from 07/04/2025."),
      Note("Retirement Position of UNKNOWN recorded on this account between 07/04/2020 and 07/04/2025."),
      Note("Retirement Position of UNKNOWN recorded on this account from 07/04/2025."),
      Note("Retirement Position of UNKNOWN recorded on this account between NOT KNOWN.")
    )
  )

  "BenefitEligibilityInfoSuccessResponseMa" - {
    ".from" - {
      "should convert to a BenefitEligibilityInfoSuccessResponseMa" in {

        val result = BenefitEligibilityInfoSuccessResponseMa.from(
          nationalInsuranceNumber,
          EligibilityCheckDataResultMA(
            NpsApiResult.SuccessResult(Class2MAReceipts, class2MAReceiptsSuccessResponse),
            NpsApiResult.SuccessResult(Liabilities, liabilitySummaryDetailsSuccessResponse),
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

        val expected = BenefitEligibilityInfoSuccessResponseMa(
          nationalInsuranceNumber,
          filteredClass2MaReceipts,
          filteredLiabilitySummaryDetails,
          niContributionsAndCreditsSuccessResponse
        )

        result.value shouldBe expected

      }
      "should serialize to json correctly" in {

        val benefitEligibilityInfoSuccessResponseMa = BenefitEligibilityInfoSuccessResponseMa(
          nationalInsuranceNumber,
          filteredClass2MaReceipts,
          filteredLiabilitySummaryDetails,
          niContributionsAndCreditsSuccessResponse
        )

        val expectedJson =
          """{
            |   "benefitType":"MA",
            |   "nationalInsuranceNumber":"AB123456C",
            |   "class2MAReceiptsResult":{
            |      "receiptDates":[
            |         "2025-12-10"
            |      ]
            |   },
            |   "liabilitySummaryDetailsResult":{
            |      "liabilityDetails":[
            |         {
            |            "startDate":"2026-01-01",
            |            "endDate":"2027-01-01"
            |         }
            |      ]
            |   },
            |   "niContributionsAndCreditsResult":{
            |      "totalGraduatedPensionUnits":53,
            |      "class1ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCategoryLetter":"U",
            |            "contributionCategory":"(NONE)",
            |            "contributionCreditType":"C1",
            |            "primaryContribution":99999999999999.98,
            |            "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
            |            "primaryPaidEarnings":99999999999999.98,
            |            "creditSource":"NOT KNOWN",
            |            "employerName":"ipOpMs",
            |            "latePaymentPeriod":"L"
            |         }
            |      ],
            |      "class2ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCreditType":"C1",
            |            "class2Or3EarningsFactor":99999999999999.98,
            |            "class2NIContributionAmount":99999999999999.98,
            |            "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
            |            "creditSource":"NOT KNOWN",
            |            "latePaymentPeriod":"L"
            |         }
            |      ]
            |   }
            |}""".stripMargin

        Json.toJson(benefitEligibilityInfoSuccessResponseMa) shouldBe Json.parse(expectedJson)
      }
    }
  }

  "BenefitEligibilityInfoSuccessResponseBsp" - {
    ".from" - {
      "should convert to a BenefitEligibilityInfoSuccessResponseBsp" in {

        val result = BenefitEligibilityInfoSuccessResponseBsp.from(
          nationalInsuranceNumber,
          EligibilityCheckDataResultBSP(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse),
            NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse)
          )
        )

        val expected = BenefitEligibilityInfoSuccessResponseBsp(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse,
          filteredMarriageDetails
        )

        result.value shouldBe expected

      }
      "should serialize to json correctly" in {

        val benefitEligibilityInfoSuccessResponseBsp = BenefitEligibilityInfoSuccessResponseBsp(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse,
          filteredMarriageDetails
        )

        val expectedJson =
          """{
            |   "benefitType":"BSP",
            |   "nationalInsuranceNumber":"AB123456C",
            |   "niContributionsAndCreditsResult":{
            |      "totalGraduatedPensionUnits":53,
            |      "class1ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCategoryLetter":"U",
            |            "contributionCategory":"(NONE)",
            |            "contributionCreditType":"C1",
            |            "primaryContribution":99999999999999.98,
            |            "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
            |            "primaryPaidEarnings":99999999999999.98,
            |            "creditSource":"NOT KNOWN",
            |            "employerName":"ipOpMs",
            |            "latePaymentPeriod":"L"
            |         }
            |      ],
            |      "class2ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCreditType":"C1",
            |            "class2Or3EarningsFactor":99999999999999.98,
            |            "class2NIContributionAmount":99999999999999.98,
            |            "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
            |            "creditSource":"NOT KNOWN",
            |            "latePaymentPeriod":"L"
            |         }
            |      ]
            |   },
            |   "marriageDetailsResult":{
            |      "marriageDetails":[
            |         {
            |            "status":"CIVIL PARTNER",
            |            "startDate":"1999-01-01",
            |            "startDateStatus":"VERIFIED",
            |            "endDate":"2001-01-01",
            |            "endDateStatus":"VERIFIED",
            |            "spouseIdentifier":"AB123456C",
            |            "spouseForename":"Skywalker",
            |            "spouseSurname":"Luke"
            |         }
            |      ]
            |   }
            |}""".stripMargin

        Json.toJson(benefitEligibilityInfoSuccessResponseBsp) shouldBe Json.parse(expectedJson)
      }
    }
  }

  "BenefitEligibilityInfoSuccessResponseGysp" - {
    ".from" - {
      "should convert to a BenefitEligibilityInfoSuccessResponseGysp" in {

        val result = BenefitEligibilityInfoSuccessResponseGysp.from(
          nationalInsuranceNumber,
          EligibilityCheckDataResultGYSP(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse),
            BenefitSchemeMembershipDetailsData(
              NpsApiResult.SuccessResult(ApiName.SchemeMembershipDetails, schemeMembershipDetailsSuccessResponse),
              List(NpsApiResult.SuccessResult(ApiName.BenefitSchemeDetails, benefitSchemeDetailsSuccessResponse))
            ),
            LongTermBenefitCalculationDetailsData(
              NpsApiResult
                .SuccessResult(LongTermBenefitCalculationDetails, longTermBenefitCalculationDetailsSuccessResponse),
              List(NpsApiResult.SuccessResult(LongTermBenefitNotes, longTermBenefitNotesSuccessResponse))
            ),
            NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse),
            NpsApiResult.SuccessResult(IndividualStatePension, individualStatePensionInformationSuccessResponse)
          )
        )

        val expected = BenefitEligibilityInfoSuccessResponseGysp(
          nationalInsuranceNumber,
          filteredMarriageDetails,
          filteredLongTermBenefitCalculationDetails,
          filteredSchemeMembershipDetails,
          filteredIndividualStatePensionInfo,
          niContributionsAndCreditsSuccessResponse
        )

        result.value shouldBe expected

      }
      "should serialize to json correctly" in {
        val json =
          """{
            |   "benefitType":"GYSP",
            |   "nationalInsuranceNumber":"AB123456C",
            |   "marriageDetailsResult":{
            |      "marriageDetails":[
            |         {
            |            "status":"CIVIL PARTNER",
            |            "startDate":"1999-01-01",
            |            "startDateStatus":"VERIFIED",
            |            "endDate":"2001-01-01",
            |            "endDateStatus":"VERIFIED",
            |            "spouseIdentifier":"AB123456C",
            |            "spouseForename":"Skywalker",
            |            "spouseSurname":"Luke"
            |         }
            |      ]
            |   },
            |   "longTermBenefitCalculationDetailsResult":{
            |      "benefitCalculationDetails":[
            |         {
            |            "guaranteedMinimumPensionContractedOutDeductionsPre1988":10.56,
            |            "guaranteedMinimumPensionContractedOutDeductionsPost1988":10.56,
            |            "contractedOutDeductionsPre1988":10.56,
            |            "contractedOutDeductionsPost1988":10.56,
            |            "longTermBenefitNotes":[
            |               "Invalid Note Type Encountered.",
            |               "Married Woman's/Widow's Reduced Rate Authority recorded on this account between 07/04/2020 and 07/04/2025.",
            |               "Married Woman's/Widow's Reduced Rate Authority recorded on this account from 07/04/2025",
            |               "Widow's Benefit Award UNKNOWN  recorded on this account between 07/04/2020 and 07/04/2025.",
            |               "Widow's Benefit Award UNKNOWN  recorded on this account from 07/04/2025.",
            |               "Retirement Position of UNKNOWN recorded on this account between 07/04/2020 and 07/04/2025.",
            |               "Retirement Position of UNKNOWN recorded on this account from 07/04/2025.",
            |               "Retirement Position of UNKNOWN recorded on this account between NOT KNOWN."
            |            ]
            |         }
            |      ]
            |   },
            |   "schemeMembershipDetailsResult":{
            |      "schemeMembershipDetails":[
            |         {
            |            "schemeMembershipStartDate":"2022-06-27",
            |            "schemeMembershipEndDate":"2022-06-27",
            |            "employersContractedOutNumberDetails":"S3123456B"
            |         }
            |      ]
            |   },
            |   "individualStatePensionInfoResult":{
            |      "numberOfQualifyingYears":35,
            |      "contributionsByTaxYear":[
            |         {
            |            "totalPrimaryPaidEarnings":48500.75,
            |            "qualifyingTaxYear":true
            |         },
            |         {
            |            "totalPrimaryPaidEarnings":39875.9,
            |            "qualifyingTaxYear":false
            |         }
            |      ]
            |   },
            |   "niContributionsAndCreditsResult":{
            |      "totalGraduatedPensionUnits":53,
            |      "class1ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCategoryLetter":"U",
            |            "contributionCategory":"(NONE)",
            |            "contributionCreditType":"C1",
            |            "primaryContribution":99999999999999.98,
            |            "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
            |            "primaryPaidEarnings":99999999999999.98,
            |            "creditSource":"NOT KNOWN",
            |            "employerName":"ipOpMs",
            |            "latePaymentPeriod":"L"
            |         }
            |      ],
            |      "class2ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCreditType":"C1",
            |            "class2Or3EarningsFactor":99999999999999.98,
            |            "class2NIContributionAmount":99999999999999.98,
            |            "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
            |            "creditSource":"NOT KNOWN",
            |            "latePaymentPeriod":"L"
            |         }
            |      ]
            |   }
            |}""".stripMargin

        val benefitEligibilityInfoSuccessResponseGysp = BenefitEligibilityInfoSuccessResponseGysp(
          nationalInsuranceNumber,
          filteredMarriageDetails,
          filteredLongTermBenefitCalculationDetails,
          filteredSchemeMembershipDetails,
          filteredIndividualStatePensionInfo,
          niContributionsAndCreditsSuccessResponse
        )

        Json.toJson(benefitEligibilityInfoSuccessResponseGysp) shouldBe Json.parse(json)
      }
    }
  }

  "BenefitEligibilityInfoSuccessResponseEsa" - {
    ".from" - {
      "should convert to a BenefitEligibilityInfoSuccessResponseEsa" in {

        val result = BenefitEligibilityInfoSuccessResponseEsa.from(
          nationalInsuranceNumber,
          EligibilityCheckDataResultESA(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

        val expected = BenefitEligibilityInfoSuccessResponseEsa(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse
        )

        result.value shouldBe expected

      }
      "should serialize to json correctly" in {

        val benefitEligibilityInfoSuccessResponseEsa = BenefitEligibilityInfoSuccessResponseEsa(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse
        )

        val expectedJson =
          """{
            |   "benefitType":"ESA",
            |   "nationalInsuranceNumber":"AB123456C",
            |   "niContributionsAndCreditsResult":{
            |      "totalGraduatedPensionUnits":53,
            |      "class1ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCategoryLetter":"U",
            |            "contributionCategory":"(NONE)",
            |            "contributionCreditType":"C1",
            |            "primaryContribution":99999999999999.98,
            |            "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
            |            "primaryPaidEarnings":99999999999999.98,
            |            "creditSource":"NOT KNOWN",
            |            "employerName":"ipOpMs",
            |            "latePaymentPeriod":"L"
            |         }
            |      ],
            |      "class2ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCreditType":"C1",
            |            "class2Or3EarningsFactor":99999999999999.98,
            |            "class2NIContributionAmount":99999999999999.98,
            |            "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
            |            "creditSource":"NOT KNOWN",
            |            "latePaymentPeriod":"L"
            |         }
            |      ]
            |   }
            |}""".stripMargin

        Json.toJson(benefitEligibilityInfoSuccessResponseEsa) shouldBe Json.parse(expectedJson)
      }
    }
  }

  "BenefitEligibilityInfoSuccessResponseJsa" - {
    ".from" - {
      "should convert to a BenefitEligibilityInfoSuccessResponseJsa" in {

        val result = BenefitEligibilityInfoSuccessResponseJsa.from(
          nationalInsuranceNumber,
          EligibilityCheckDataResultJSA(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

        val expected = BenefitEligibilityInfoSuccessResponseJsa(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse
        )

        result.value shouldBe expected

      }
      "should serialize to json correctly" in {

        val benefitEligibilityInfoSuccessResponseJsa = BenefitEligibilityInfoSuccessResponseJsa(
          nationalInsuranceNumber,
          niContributionsAndCreditsSuccessResponse
        )

        val expectedJson =
          """{
            |   "benefitType":"JSA",
            |   "nationalInsuranceNumber":"AB123456C",
            |   "niContributionsAndCreditsResult":{
            |      "totalGraduatedPensionUnits":53,
            |      "class1ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCategoryLetter":"U",
            |            "contributionCategory":"(NONE)",
            |            "contributionCreditType":"C1",
            |            "primaryContribution":99999999999999.98,
            |            "class1ContributionStatus":"COMPLIANCE & YIELD INCOMPLETE",
            |            "primaryPaidEarnings":99999999999999.98,
            |            "creditSource":"NOT KNOWN",
            |            "employerName":"ipOpMs",
            |            "latePaymentPeriod":"L"
            |         }
            |      ],
            |      "class2ContributionAndCredits":[
            |         {
            |            "taxYear":2022,
            |            "numberOfContributionsAndCredits":53,
            |            "contributionCreditType":"C1",
            |            "class2Or3EarningsFactor":99999999999999.98,
            |            "class2NIContributionAmount":99999999999999.98,
            |            "class2Or3CreditStatus":"NOT KNOWN/NOT APPLICABLE",
            |            "creditSource":"NOT KNOWN",
            |            "latePaymentPeriod":"L"
            |         }
            |      ]
            |   }
            |}""".stripMargin

        Json.toJson(benefitEligibilityInfoSuccessResponseJsa) shouldBe Json.parse(expectedJson)
      }
    }
  }

  "BenefitEligibilityInfoErrorResponse" - {
    ".from" - {
      "should convert an EligibilityCheckDataResult into a BenefitEligibilityInfoErrorResponse (All success results) " in {

        case class DummySuccessResponse(i: Int) extends NpsSuccessfulApiResponse
        val dummySuccessResponse: DummySuccessResponse = DummySuccessResponse(2)
        val randomApiName: ApiName                     = Random.shuffle(ApiName.values.toList).head
        val benefitTypes                               = Table("benefitTypes", BenefitType.values.toList: _*)

        val allSuccessResults: List[ApiResult] = List(
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse),
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse),
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse)
        )

        val mockEligibilityCheckDataResult = mock[EligibilityCheckDataResult]
        forAll(benefitTypes) { benefitType =>
          (() => mockEligibilityCheckDataResult.benefitType).expects().returning(benefitType)
          (() => mockEligibilityCheckDataResult.allResults).expects().returning(allSuccessResults)
          BenefitEligibilityInfoErrorResponse.from(
            nationalInsuranceNumber,
            mockEligibilityCheckDataResult
          ) shouldBe BenefitEligibilityInfoErrorResponse(
            overallResultStatus = Success,
            nationalInsuranceNumber = nationalInsuranceNumber,
            benefitType = benefitType,
            summary = OverallResultSummary(totalCalls = 3, successful = 3, failed = 0),
            downStreams = List(
              SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None),
              SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None),
              SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None)
            )
          )
        }
      }

      "should convert an EligibilityCheckDataResult into a BenefitEligibilityInfoErrorResponse (Mixed results) " in {

        case class DummySuccessResponse(i: Int) extends NpsSuccessfulApiResponse
        val dummySuccessResponse: DummySuccessResponse = DummySuccessResponse(2)
        val randomApiName: ApiName                     = Random.shuffle(ApiName.values.toList).head
        val benefitTypes                               = Table("benefitTypes", BenefitType.values.toList: _*)

        val mixedResults: List[ApiResult] = List(
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              UnprocessableEntity,
              Some(NpsSingleErrorResponse(NpsErrorReason("error reason"), NpsErrorCode("code")))
            )
          ),
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse)
        )

        val mockEligibilityCheckDataResult = mock[EligibilityCheckDataResult]
        forAll(benefitTypes) { benefitType =>
          (() => mockEligibilityCheckDataResult.benefitType).expects().returning(benefitType)
          (() => mockEligibilityCheckDataResult.allResults).expects().returning(mixedResults)
          BenefitEligibilityInfoErrorResponse.from(
            nationalInsuranceNumber,
            mockEligibilityCheckDataResult
          ) shouldBe
            BenefitEligibilityInfoErrorResponse(
              overallResultStatus = Partial,
              nationalInsuranceNumber = nationalInsuranceNumber,
              benefitType = benefitType,
              summary = OverallResultSummary(totalCalls = 3, successful = 2, failed = 1),
              downStreams = List(
                SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None),
                SanitizedApiResult(
                  apiName = randomApiName,
                  status = NpsApiResponseStatus.Failure,
                  error = Some(UnprocessableEntity)
                ),
                SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None)
              )
            )
        }
      }
      "should convert an EligibilityCheckDataResult into a BenefitEligibilityInfoErrorResponse (All failure results) " in {

        case class DummySuccessResponse(i: Int) extends NpsSuccessfulApiResponse
        val randomApiName: ApiName = Random.shuffle(ApiName.values.toList).head
        val benefitTypes           = Table("benefitTypes", BenefitType.values.toList: _*)

        val allFailureResults: List[ApiResult] = List(
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              UnprocessableEntity,
              Some(NpsSingleErrorResponse(NpsErrorReason("error reason 1"), NpsErrorCode("code 1")))
            )
          ),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              InternalServerError,
              Some(
                NpsMultiErrorResponse(
                  Some(
                    List(
                      NpsSingleErrorResponse(NpsErrorReason("error reason 2"), NpsErrorCode("code 2")),
                      NpsSingleErrorResponse(NpsErrorReason("error reason 3"), NpsErrorCode("code 4"))
                    )
                  )
                )
              )
            )
          ),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              ServiceUnavailable,
              Some(
                NpsErrorResponseHipOrigin(
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
              )
            )
          )
        )

        val mockEligibilityCheckDataResult = mock[EligibilityCheckDataResult]
        forAll(benefitTypes) { benefitType =>
          (() => mockEligibilityCheckDataResult.benefitType).expects().returning(benefitType)
          (() => mockEligibilityCheckDataResult.allResults).expects().returning(allFailureResults)
          BenefitEligibilityInfoErrorResponse.from(
            nationalInsuranceNumber,
            mockEligibilityCheckDataResult
          ) shouldBe BenefitEligibilityInfoErrorResponse(
            overallResultStatus = Failure,
            nationalInsuranceNumber = nationalInsuranceNumber,
            benefitType = benefitType,
            summary = OverallResultSummary(totalCalls = 3, successful = 0, failed = 3),
            downStreams = List(
              SanitizedApiResult(
                apiName = randomApiName,
                status = NpsApiResponseStatus.Failure,
                error = Some(UnprocessableEntity)
              ),
              SanitizedApiResult(
                apiName = randomApiName,
                status = NpsApiResponseStatus.Failure,
                error = Some(InternalServerError)
              ),
              SanitizedApiResult(
                apiName = randomApiName,
                status = NpsApiResponseStatus.Failure,
                error = Some(ServiceUnavailable)
              )
            )
          )
        }
      }

    }
  }

  "BenefitEligibilityInfoResponse" - {
    ".from" - {
      "should return an error response if some of the api results in the data result are failures" in {
        case class DummySuccessResponse(i: Int) extends NpsSuccessfulApiResponse
        val dummySuccessResponse: DummySuccessResponse = DummySuccessResponse(2)
        val randomApiName: ApiName                     = Random.shuffle(ApiName.values.toList).head

        val mixedResults: List[ApiResult] = List(
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              UnprocessableEntity,
              Some(NpsSingleErrorResponse(NpsErrorReason("error reason"), NpsErrorCode("code")))
            )
          ),
          NpsApiResult.SuccessResult[ErrorReport, DummySuccessResponse](randomApiName, dummySuccessResponse)
        )

        val mockEligibilityCheckDataResultMa   = mock[EligibilityCheckDataResultMA]
        val mockEligibilityCheckDataResultEsa  = mock[EligibilityCheckDataResultESA]
        val mockEligibilityCheckDataResultJsa  = mock[EligibilityCheckDataResultJSA]
        val mockEligibilityCheckDataResultGysp = mock[EligibilityCheckDataResultGYSP]
        val mockEligibilityCheckDataResultBsp  = mock[EligibilityCheckDataResultBSP]

        (() => mockEligibilityCheckDataResultMa.benefitType).expects().returning(BenefitType.MA)
        (() => mockEligibilityCheckDataResultMa.allResults).expects().returning(mixedResults)

        (() => mockEligibilityCheckDataResultEsa.benefitType).expects().returning(BenefitType.ESA)
        (() => mockEligibilityCheckDataResultEsa.allResults).expects().returning(mixedResults)

        (() => mockEligibilityCheckDataResultJsa.benefitType).expects().returning(BenefitType.JSA)
        (() => mockEligibilityCheckDataResultJsa.allResults).expects().returning(mixedResults)

        (() => mockEligibilityCheckDataResultGysp.benefitType).expects().returning(BenefitType.GYSP)
        (() => mockEligibilityCheckDataResultGysp.allResults).expects().returning(mixedResults).twice()

        (() => mockEligibilityCheckDataResultBsp.benefitType).expects().returning(BenefitType.BSP)
        (() => mockEligibilityCheckDataResultBsp.allResults).expects().returning(mixedResults)

        val testParams = Map(
          BenefitType.MA   -> mockEligibilityCheckDataResultMa,
          BenefitType.ESA  -> mockEligibilityCheckDataResultEsa,
          BenefitType.JSA  -> mockEligibilityCheckDataResultJsa,
          BenefitType.GYSP -> mockEligibilityCheckDataResultGysp,
          BenefitType.BSP  -> mockEligibilityCheckDataResultBsp
        )

        testParams.foreach { case (benefitType, mockResult) =>
          BenefitEligibilityInfoResponse.from(
            nationalInsuranceNumber,
            mockResult
          ) shouldBe Left(
            BenefitEligibilityInfoErrorResponse(
              overallResultStatus = Partial,
              nationalInsuranceNumber = nationalInsuranceNumber,
              benefitType = benefitType,
              summary = OverallResultSummary(totalCalls = 3, successful = 2, failed = 1),
              downStreams = List(
                SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None),
                SanitizedApiResult(
                  apiName = randomApiName,
                  status = NpsApiResponseStatus.Failure,
                  error = Some(UnprocessableEntity)
                ),
                SanitizedApiResult(apiName = randomApiName, status = NpsApiResponseStatus.Success, error = None)
              )
            )
          )
        }
      }
      "should return an error response if all of the api results in the data result are failures" in {
        case class DummySuccessResponse(i: Int) extends NpsSuccessfulApiResponse
        val randomApiName: ApiName = Random.shuffle(ApiName.values.toList).head

        val allFailureResults: List[ApiResult] = List(
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              UnprocessableEntity,
              Some(NpsSingleErrorResponse(NpsErrorReason("error reason 1"), NpsErrorCode("code 1")))
            )
          ),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              InternalServerError,
              Some(
                NpsMultiErrorResponse(
                  Some(
                    List(
                      NpsSingleErrorResponse(NpsErrorReason("error reason 2"), NpsErrorCode("code 2")),
                      NpsSingleErrorResponse(NpsErrorReason("error reason 3"), NpsErrorCode("code 4"))
                    )
                  )
                )
              )
            )
          ),
          NpsApiResult.FailureResult[ErrorReport, DummySuccessResponse](
            randomApiName,
            ErrorReport(
              ServiceUnavailable,
              Some(
                NpsErrorResponseHipOrigin(
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
              )
            )
          )
        )

        val mockEligibilityCheckDataResultMa   = mock[EligibilityCheckDataResultMA]
        val mockEligibilityCheckDataResultEsa  = mock[EligibilityCheckDataResultESA]
        val mockEligibilityCheckDataResultJsa  = mock[EligibilityCheckDataResultJSA]
        val mockEligibilityCheckDataResultGysp = mock[EligibilityCheckDataResultGYSP]
        val mockEligibilityCheckDataResultBsp  = mock[EligibilityCheckDataResultBSP]

        (() => mockEligibilityCheckDataResultMa.benefitType).expects().returning(BenefitType.MA)
        (() => mockEligibilityCheckDataResultMa.allResults).expects().returning(allFailureResults)

        (() => mockEligibilityCheckDataResultEsa.benefitType).expects().returning(BenefitType.ESA)
        (() => mockEligibilityCheckDataResultEsa.allResults).expects().returning(allFailureResults)

        (() => mockEligibilityCheckDataResultJsa.benefitType).expects().returning(BenefitType.JSA)
        (() => mockEligibilityCheckDataResultJsa.allResults).expects().returning(allFailureResults)

        (() => mockEligibilityCheckDataResultGysp.benefitType).expects().returning(BenefitType.GYSP)
        (() => mockEligibilityCheckDataResultGysp.allResults).expects().returning(allFailureResults).twice()

        (() => mockEligibilityCheckDataResultBsp.benefitType).expects().returning(BenefitType.BSP)
        (() => mockEligibilityCheckDataResultBsp.allResults).expects().returning(allFailureResults)

        val testParams = Map(
          BenefitType.MA   -> mockEligibilityCheckDataResultMa,
          BenefitType.ESA  -> mockEligibilityCheckDataResultEsa,
          BenefitType.JSA  -> mockEligibilityCheckDataResultJsa,
          BenefitType.GYSP -> mockEligibilityCheckDataResultGysp,
          BenefitType.BSP  -> mockEligibilityCheckDataResultBsp
        )

        testParams.foreach { (benefitType, mockResult) =>
          BenefitEligibilityInfoResponse.from(
            nationalInsuranceNumber,
            mockResult
          ) shouldBe Left(
            BenefitEligibilityInfoErrorResponse(
              overallResultStatus = Failure,
              nationalInsuranceNumber = nationalInsuranceNumber,
              benefitType = benefitType,
              summary = OverallResultSummary(totalCalls = 3, successful = 0, failed = 3),
              downStreams = List(
                SanitizedApiResult(
                  apiName = randomApiName,
                  status = NpsApiResponseStatus.Failure,
                  error = Some(UnprocessableEntity)
                ),
                SanitizedApiResult(
                  apiName = randomApiName,
                  status = NpsApiResponseStatus.Failure,
                  error = Some(InternalServerError)
                ),
                SanitizedApiResult(
                  apiName = randomApiName,
                  status = NpsApiResponseStatus.Failure,
                  error = Some(ServiceUnavailable)
                )
              )
            )
          )
        }
      }
      "should return a success response if all of the api results in the data result are successful (MA)" in {

        val eligibilityCheckDataResultMA = EligibilityCheckDataResultMA(
          NpsApiResult.SuccessResult[ErrorReport, Class2MAReceiptsSuccessResponse](
            Class2MAReceipts,
            class2MAReceiptsSuccessResponse
          ),
          NpsApiResult.SuccessResult[ErrorReport, LiabilitySummaryDetailsSuccessResponse](
            Liabilities,
            liabilitySummaryDetailsSuccessResponse
          ),
          NpsApiResult.SuccessResult[ErrorReport, NiContributionsAndCreditsSuccessResponse](
            NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          )
        )

        BenefitEligibilityInfoResponse.from(
          nationalInsuranceNumber,
          eligibilityCheckDataResultMA
        ) shouldBe
          Right(
            BenefitEligibilityInfoSuccessResponseMa(
              nationalInsuranceNumber,
              filteredClass2MaReceipts,
              filteredLiabilitySummaryDetails,
              niContributionsAndCreditsSuccessResponse
            )
          )

      }
      "should return a success response if all of the api results in the data result are successful (JSA)" in {

        val eligibilityCheckDataResultJSA = EligibilityCheckDataResultJSA(
          NpsApiResult.SuccessResult[ErrorReport, NiContributionsAndCreditsSuccessResponse](
            NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          )
        )

        BenefitEligibilityInfoResponse.from(
          nationalInsuranceNumber,
          eligibilityCheckDataResultJSA
        ) shouldBe
          Right(
            BenefitEligibilityInfoSuccessResponseJsa(
              nationalInsuranceNumber,
              niContributionsAndCreditsSuccessResponse
            )
          )

      }
      "should return a success response if all of the api results in the data result are successful (ESA)" in {

        val eligibilityCheckDataResultESA = EligibilityCheckDataResultESA(
          NpsApiResult.SuccessResult[ErrorReport, NiContributionsAndCreditsSuccessResponse](
            NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          )
        )

        BenefitEligibilityInfoResponse.from(
          nationalInsuranceNumber,
          eligibilityCheckDataResultESA
        ) shouldBe
          Right(
            BenefitEligibilityInfoSuccessResponseEsa(
              nationalInsuranceNumber,
              niContributionsAndCreditsSuccessResponse
            )
          )

      }
      "should return a success response if all of the api results in the data result are successful (BSP)" in {

        val eligibilityCheckDataResultBSP = EligibilityCheckDataResultBSP(
          NpsApiResult.SuccessResult[ErrorReport, NiContributionsAndCreditsSuccessResponse](
            NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          ),
          NpsApiResult.SuccessResult[ErrorReport, MarriageDetailsSuccessResponse](
            MarriageDetails,
            marriageDetailsSuccessResponse
          )
        )

        BenefitEligibilityInfoResponse.from(
          nationalInsuranceNumber,
          eligibilityCheckDataResultBSP
        ) shouldBe
          Right(
            BenefitEligibilityInfoSuccessResponseBsp(
              nationalInsuranceNumber,
              niContributionsAndCreditsSuccessResponse,
              filteredMarriageDetails
            )
          )

      }
      "should return a success response if all of the api results in the data result are successful (GYSP)" in {

        val eligibilityCheckDataResultGYSP = EligibilityCheckDataResultGYSP(
          NpsApiResult.SuccessResult[ErrorReport, NiContributionsAndCreditsSuccessResponse](
            NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          ),
          BenefitSchemeMembershipDetailsData(
            NpsApiResult.SuccessResult[ErrorReport, SchemeMembershipDetailsSuccessResponse](
              ApiName.SchemeMembershipDetails,
              schemeMembershipDetailsSuccessResponse
            ),
            List(
              NpsApiResult.SuccessResult[ErrorReport, BenefitSchemeDetailsSuccessResponse](
                ApiName.BenefitSchemeDetails,
                benefitSchemeDetailsSuccessResponse
              )
            )
          ),
          LongTermBenefitCalculationDetailsData(
            NpsApiResult.SuccessResult[ErrorReport, LongTermBenefitCalculationDetailsSuccessResponse](
              LongTermBenefitCalculationDetails,
              longTermBenefitCalculationDetailsSuccessResponse
            ),
            List(
              NpsApiResult.SuccessResult[ErrorReport, LongTermBenefitNotesSuccessResponse](
                LongTermBenefitNotes,
                longTermBenefitNotesSuccessResponse
              )
            )
          ),
          NpsApiResult.SuccessResult[ErrorReport, MarriageDetailsSuccessResponse](
            MarriageDetails,
            marriageDetailsSuccessResponse
          ),
          NpsApiResult.SuccessResult[ErrorReport, IndividualStatePensionInformationSuccessResponse](
            IndividualStatePension,
            individualStatePensionInformationSuccessResponse
          )
        )

        BenefitEligibilityInfoResponse.from(
          nationalInsuranceNumber,
          eligibilityCheckDataResultGYSP
        ) shouldBe
          Right(
            BenefitEligibilityInfoSuccessResponseGysp(
              nationalInsuranceNumber,
              filteredMarriageDetails,
              filteredLongTermBenefitCalculationDetails,
              filteredSchemeMembershipDetails,
              filteredIndividualStatePensionInfo,
              niContributionsAndCreditsSuccessResponse
            )
          )

      }
    }
  }

}
