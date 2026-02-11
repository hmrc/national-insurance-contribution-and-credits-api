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

package uk.gov.hmrc.app.benefitEligibility.service

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.{
  BAD_REQUEST,
  FORBIDDEN,
  INTERNAL_SERVER_ERROR,
  METHOD_NOT_ALLOWED,
  MULTIPLE_CHOICES,
  MULTI_STATUS,
  NOT_FOUND,
  OK,
  SERVICE_UNAVAILABLE,
  UNPROCESSABLE_ENTITY
}
import play.api.test.Injecting
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  Class2MaReceipts,
  ContributionsAndCredits,
  GYSPEligibilityCheckDataRequest,
  LongTermBenefitCalculation
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.{
  AccruedGMPLiabilityServiceDate,
  BenefitSchemeAddressDetails,
  BenefitSchemeDetails,
  BenefitSchemeDetailsSuccessResponse,
  BenefitSchemeName,
  CertificateCancellationDate,
  ContractedOutDeductionExtinguishedDate,
  CurrentOptimisticLock,
  DateFormallyCertified,
  IsleOfManInterest,
  MagneticTapeNumber,
  PaymentRestartDate,
  PaymentSuspensionDate,
  PrivatePensionSchemeSanctionDate,
  ReconciliationDate,
  RecoveriesRestartedDate,
  RecoveriesSuspendedDate,
  RevaluationRateSequenceNumber,
  SchemeAddressDetails,
  SchemeAddressEndDate,
  SchemeAddressLine1,
  SchemeAddressLine2,
  SchemeAddressLocality,
  SchemeAddressPostalTown,
  SchemeAddressSequenceNumber,
  SchemeAddressStartDate,
  SchemeCessationDate,
  SchemeContractedOutNumberDetails,
  SchemeConversionDate,
  SchemePostcode,
  SchemeStartDate,
  SchemeTelephoneNumber,
  SchemeWindingUp,
  SuspendedDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.{
  AreaDiallingCode,
  BenefitSchemeInstitutionType,
  BenefitSchemeStatus,
  RerouteToSchemeCessation,
  SchemeAddressType,
  SchemeInhibitionStatus,
  StatementInhibitor
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.{
  AmountNeeded,
  ClassThreePayable,
  ClassThreePayableBy,
  ClassThreePayableByPenalty,
  ClassTwoOutstandingWeeks,
  ClassTwoPayable,
  ClassTwoPayableBy,
  ClassTwoPayableByPenalty,
  CoClassOnePaid,
  CoPrimaryPaidEarnings,
  ContributionCreditCount,
  ContributionsByTaxYear,
  DateOfEntry,
  IndividualStatePensionInformationSuccessResponse,
  NiEarnings,
  NiEarningsSelfEmployed,
  NiEarningsVoluntary,
  NonQualifyingYears,
  NonQualifyingYearsPayable,
  NumberOfQualifyingYears,
  OtherCredits,
  PayableAccepted,
  Pre1975CCCount,
  QualifyingTaxYear,
  TotalPrimaryContributions,
  UnderInvestigationFlag,
  YearsToFinalRelevantYear
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated.Abroad
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.{
  AdditionalAgeRelatedPensionPercentage,
  AdditionalNotionalPensionAmountPost2002,
  AdditionalNotionalPensionIncrementsInheritedPost2002,
  AdditionalPensionAmountPost1997,
  AdditionalPensionAmountPost2002,
  AdditionalPensionAmountPre1997,
  AdditionalPensionIncrementsCashValue,
  AdditionalPensionIncrementsInheritedPost2002,
  AdditionalPensionNotionalPercentage,
  AdditionalPensionPercentage,
  AdditionalPost1997AgeRelatedPensionPercentage,
  AdditionalPost1997PensionNotionalPercentage,
  AdditionalPost1997PensionPercentage,
  AdditionalPost2002AgeRelatedPensionPercentage,
  AdditionalPost2002PensionNotionalPercentage,
  AdditionalPost2002PensionPercentage,
  BasicPensionIncrementsCashValue,
  BasicPensionPercentage,
  BenefitCalculationDetail,
  BenefitCalculationDetailsList,
  CalculationDate,
  ConditionOneSatisfied,
  ConsiderReducedRateElection,
  ContractedOutDeductionsPost1988,
  ContractedOutDeductionsPre1988,
  DerivedRebateAmount,
  GraduatedRetirementBenefitCashValue,
  GreatBritainPaymentAmount,
  GuaranteedMinimumPensionContractedOutDeductionsPost1988,
  GuaranteedMinimumPensionContractedOutDeductionsPre1988,
  HusbandDateOfDeath,
  InheritableAdditionalPensionPercentage,
  InheritableNotionalAdditionalPensionIncrements,
  InheritedAdditionalPensionNotionalPercentage,
  InheritedAdditionalPensionPercentage,
  InheritedAdditionalPost2002PensionNotionalPercentage,
  InheritedAdditionalPost2002PensionPercentage,
  InheritedBasicPensionPercentage,
  InheritedGraduatedBenefit,
  InheritedGraduatedPensionPercentage,
  InitialStatePensionAmount,
  LongTermBenefitCalculationDetailsSuccessResponse,
  LongTermBenefitsCategoryACashValue,
  LongTermBenefitsCategoryBLCashValue,
  LongTermBenefitsIncrementalCashValue,
  LongTermBenefitsUnitValue,
  MinimumQualifyingPeriodMet,
  NetAdditionalPensionPre1997,
  NetRulesAmount,
  NewStatePensionCalculationDetails,
  NewStatePensionEntitlement,
  NewStatePensionQualifyingYears,
  NewStatePensionRequisiteYears,
  NotionalPost1997AdditionalPension,
  NotionalPre1997AdditionalPension,
  OldRulesStatePensionEntitlement,
  OperativeBenefitStartDate,
  PensionSharingOrderContractedOutEmploymentsGroup,
  PensionSharingOrderStateEarningsRelatedPensionScheme,
  Post02AgeRelatedAdditionalPension,
  Post97AgeRelatedAdditionalPension,
  Pre1975ShortTermBenefits,
  Pre97AgeRelatedAdditionalPension,
  ProtectedPayment,
  ProtectedPayment2016,
  QualifyingYearsAfter2016,
  ReasonForFormIssue,
  SicknessBenefitStatusForReports,
  SingleContributionConditionRulesApply,
  StatePensionAgeAfter2016TaxYear,
  StatePensionAgeBefore2010TaxYear,
  SubstitutionMethod1,
  SubstitutionMethod2,
  SurvivingSpouseAge,
  SurvivorsBenefitAgeRelatedPensionPercentage,
  TotalGuaranteedMinimumPension,
  TotalNonGuaranteedMinimumPension,
  WeeklyBudgetingLoanAmount
}
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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.{
  AccruedPensionContractedOutDeductionsValue,
  AccruedPensionContractedOutDeductionsValuePost1988,
  ActualTransferValue,
  ApparentUnnotifiedTerminationDestinationDetails,
  CertifiedAmount,
  ClericallyControlledTotal,
  ClericallyControlledTotalPost1988,
  ContractedOutEmployerIdentifier,
  CreationMicrofilmNumber,
  DebitVoucherMicrofilmNumber,
  EmployeesReference,
  EmployersContractedOutNumberDetails,
  ExtensionDate,
  FinalYearEarnings,
  GuaranteedMinimumPensionContractedOutDeductionsRevalued,
  GuaranteedMinimumPensionConversionApplied,
  ImportingAppropriateSchemeNumberDetails,
  InhibitSchemeProcessing,
  MinimumFundTransferAmount,
  PenultimateYearEarnings,
  ProtectedRightsStartDate,
  RetrospectiveEarnings,
  RevaluationApplied,
  SchemeCreatingContractedOutNumberDetails,
  SchemeMembershipDetails,
  SchemeMembershipDetailsSuccessResponse,
  SchemeMembershipDetailsSummary,
  SchemeMembershipEndDate,
  SchemeMembershipOccurrenceNumber,
  SchemeMembershipSequenceNumber,
  SchemeMembershipStartDate,
  SchemeMembershipTransferSequenceNumber,
  SchemeTerminatingContractedOutNumberDetails,
  StateEarningsRelatedPensionsSchemeNonRestorationValue,
  StateEarningsRelatedPensionsSchemeValuePost1988,
  TechnicalAmount,
  TerminationMicrofilmNumber,
  TotalLinkedGuaranteedMinimumPensionContractedOutDeductions,
  TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988,
  TransferPremiumElectionDate,
  TransferTakeUpDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.{
  ApparentUnnotifiedTerminationStatus,
  Clercalc,
  ContCatLetter,
  Enfcment,
  FurtherPaymentsConfirmation,
  GuaranteedMinimumPensionReconciliationStatus,
  MethodOfPreservation,
  RevaluationRate,
  SchemeMembershipDebitReason,
  SchemeSuspensionType,
  SspDeem,
  StakeholderPensionSchemeType,
  SurvivorStatus
}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class GetYourStatePensionDataRetrievalServiceItSpec
    extends AnyFreeSpec
    with EitherValues
    with GuiceOneAppPerSuite
    with WireMockHelper
    with Injecting
    with Matchers
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val defaultPatience: PatienceConfig = PatienceConfig(
    timeout = Span(10, Seconds),
    interval = Span(100, Millis)
  )

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.hip.port" -> server.port
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = Seq(("CorrelationId", "testing-correlationId")))

  private lazy val service: GetYourStatePensionDataRetrievalService =
    inject[GetYourStatePensionDataRetrievalService]

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
        MarriageDetailsSuccess
          .MarriageDetailsList(
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

  private val niContributionsAndCreditsSuccessResponseBody =
    Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

  private val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()
  private val marriageDetailsSuccessResponseBody      = Json.toJson(marriageDetailsSuccessResponse).toString()

  private val longTermBenefitCalculationDetailsSuccessResponseBody =
    Json.toJson(longTermBenefitCalculationDetailsSuccessResponse).toString()

  private val longTermBenefitNotesSuccessResponseBody = Json.toJson(longTermBenefitNotesSuccessResponse).toString()

  private val schemeMembershipDetailsSuccessResponseBody =
    Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

  private val individualStatePensionInformationSuccessResponseBody =
    Json.toJson(individualStatePensionInformationSuccessResponse).toString()

  "GetYourStatePensionDataRetrievalService" - {

    ".fetchEligibilityData" - {

      val npsCreditsAndContributionsPath        = "/national-insurance/contributions-and-credits"
      val benefitSchemeDetailsPath              = "/benefit-scheme/GD379251T/benefit-scheme-details/S3123456B"
      val marriageDetailsPath                   = "/individual/GD379251T/marriage-cp"
      val longTermBenefitCalculationDetailsPath = "/long-term-benefits/GD379251T/calculation"
      val longTermBenefitNotesPath              = "/long-term-benefits/GD379251T/calculation/ALL/notes/86"
      val schemeMembershipDetailsPath           = "/benefit-scheme/GD379251T/scheme-membership-details"
      val individualStatePensionInformationPath = "/long-term-benefits/GD379251T/contributions"

      val gyspEligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
        Identifier("GD379251T"),
        ContributionsAndCredits(
          DateOfBirth(LocalDate.parse("2025-10-10")),
          StartTaxYear(2025),
          EndTaxYear(2026)
        ),
        LongTermBenefitCalculation(None, None),
        None
      )

      "when all NPS endpoint returns OK (200) with valid responses" - {
        "should parse responses and map to result successfully" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result = service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                SuccessResult(
                  ApiName.NiContributionAndCredits,
                  niContributionsAndCreditsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )

        }
      }

      "when an NPS endpoint returns BAD_REQUEST (400 - StandardErrorResponse400)" - {
        "should parse error response and map to result" in {

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

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                             = implicitly[Reads[NpsStandardErrorResponse400]]
          val response: NpsStandardErrorResponse400 = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.BadRequest, Some(response))
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns BAD_REQUEST (400 - HipFailureResponse400) " - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |  "origin": "HIP",
              |  "response": {
              |   "failures": [
              |    {
              |      "type": "Type of Failure",
              |      "reason": "Reason for Failure"
              |    },
              |    {
              |      "type": "Type of ';'",
              |      "reason": "Reason for Failure"
              |    }
              |  ]
              | }
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_REQUEST)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.BadRequest, Some(response))
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns FORBIDDEN (403)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |   "reason":"Forbidden",
              |   "code":"403.2"
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(FORBIDDEN)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                        = implicitly[Reads[NpsSingleErrorResponse]]
          val response: NpsSingleErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.AccessForbidden, Some(response))
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns NOT_FOUND (404)" - {
        "should parse error response and map to result" in {

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(NOT_FOUND)
                  .withHeader("Content-Type", "application/json")
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.NotFound, None)
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns UNPROCESSABLE_ENTITY (422)" - {
        "should parse error response and map to result" in {

          val errorResponse =
            """{
              |"failures":[
              | {
              |   "reason":"Some reason",
              |   "code":"fail code"
              | }
              |]
              |}""".stripMargin

          val responseBody = Json.parse(errorResponse).toString()

          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(UNPROCESSABLE_ENTITY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(responseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                       = implicitly[Reads[NpsMultiErrorResponse]]
          val response: NpsMultiErrorResponse = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(response))
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns an INTERNAL_SERVER_ERROR (500)" - {
        "should map to InternalServerError result" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(INTERNAL_SERVER_ERROR)
              )
          )
          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.InternalServerError, None)
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )

        }
      }

      "when an NPS endpoint returns an INTERNAL_SERVER_ERROR (503)" - {

        val errorResponse =
          """{
            |  "origin": "HIP",
            |  "response": {
            |   "failures": [
            |    {
            |      "type": "Type of Failure",
            |      "reason": "Reason for Failure"
            |    },
            |    {
            |      "type": "Type of ';'",
            |      "reason": "Reason for Failure"
            |    }
            |  ]
            | }
            |}""".stripMargin

        "should map to Service unavailable result" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(SERVICE_UNAVAILABLE)
                  .withBody(errorResponse)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          val jsonReads                           = implicitly[Reads[NpsErrorResponseHipOrigin]]
          val response: NpsErrorResponseHipOrigin = jsonReads.reads(Json.parse(errorResponse)).get

          result shouldBe Right(
            EligibilityCheckDataResultGYSP(
              List(
                FailureResult(
                  ApiName.NiContributionAndCredits,
                  ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(response))
                )
              ),
              SuccessResult(
                ApiName.SchemeMembershipDetails,
                schemeMembershipDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.BenefitSchemeDetails,
                  benefitSchemeDetailsSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.LongTermBenefitCalculationDetails,
                longTermBenefitCalculationDetailsSuccessResponse
              ),
              List(
                SuccessResult(
                  ApiName.LongTermBenefitNotes,
                  longTermBenefitNotesSuccessResponse
                )
              ),
              SuccessResult(
                ApiName.MarriageDetails,
                marriageDetailsSuccessResponse
              ),
              SuccessResult(
                ApiName.IndividualStatePension,
                individualStatePensionInformationSuccessResponse
              )
            )
          )

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when an NPS endpoint returns an unexpected statusCode" - {
        "should map to InternalServerError result" in {

          val statusCodes: TableFor1[Int] =
            Table("statusCodes", MULTIPLE_CHOICES, MULTI_STATUS, METHOD_NOT_ALLOWED)

          forAll(statusCodes) { statusCode =>
            server.stubFor(
              post(urlEqualTo(npsCreditsAndContributionsPath))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            server.stubFor(
              get(urlEqualTo(benefitSchemeDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(benefitSchemeDetailsSuccessResponseBody)
                )
            )

            server.stubFor(
              get(urlEqualTo(marriageDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(marriageDetailsSuccessResponseBody)
                )
            )

            server.stubFor(
              get(urlEqualTo(longTermBenefitCalculationDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
                )
            )
            server.stubFor(
              get(urlEqualTo(longTermBenefitNotesPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(longTermBenefitNotesSuccessResponseBody)
                )
            )
            server.stubFor(
              get(urlEqualTo(schemeMembershipDetailsPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(schemeMembershipDetailsSuccessResponseBody)
                )
            )
            server.stubFor(
              get(urlEqualTo(individualStatePensionInformationPath))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(individualStatePensionInformationSuccessResponseBody)
                )
            )

            val result =
              service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

            result shouldBe Right(
              EligibilityCheckDataResultGYSP(
                List(
                  FailureResult(
                    ApiName.NiContributionAndCredits,
                    ErrorReport(NpsNormalizedError.UnexpectedStatus(statusCode), None)
                  )
                ),
                SuccessResult(
                  ApiName.SchemeMembershipDetails,
                  schemeMembershipDetailsSuccessResponse
                ),
                List(
                  SuccessResult(
                    ApiName.BenefitSchemeDetails,
                    benefitSchemeDetailsSuccessResponse
                  )
                ),
                SuccessResult(
                  ApiName.LongTermBenefitCalculationDetails,
                  longTermBenefitCalculationDetailsSuccessResponse
                ),
                List(
                  SuccessResult(
                    ApiName.LongTermBenefitNotes,
                    longTermBenefitNotesSuccessResponse
                  )
                ),
                SuccessResult(
                  ApiName.MarriageDetails,
                  marriageDetailsSuccessResponse
                ),
                SuccessResult(
                  ApiName.IndividualStatePension,
                  individualStatePensionInformationSuccessResponse
                )
              )
            )

            server.verify(
              postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
            )

            server.verify(
              postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
            )

            server.verify(
              getRequestedFor(urlEqualTo(marriageDetailsPath))
            )
          }

        }
      }

      "when an NPS endpoint returns malformed JSON" - {
        "should return parsing error" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{ invalid json structure")
              )
          )
          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )
          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

      "when a request to a downstream fails unexpectedly" - {
        "should return downstream error" in {
          server.stubFor(
            post(urlEqualTo(npsCreditsAndContributionsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
              )
          )

          server.stubFor(
            get(urlEqualTo(benefitSchemeDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(marriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            get(urlEqualTo(longTermBenefitCalculationDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(longTermBenefitNotesPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(schemeMembershipDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            get(urlEqualTo(individualStatePensionInformationPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )

          val result =
            service.fetchEligibilityData(gyspEligibilityCheckDataRequest).value.futureValue

          result shouldBe a[Left[_, _]]
          result.left.value shouldBe a[DataRetrievalServiceError]

          server.verify(
            postRequestedFor(urlEqualTo(npsCreditsAndContributionsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(benefitSchemeDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(schemeMembershipDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitNotesPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(longTermBenefitCalculationDetailsPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(individualStatePensionInformationPath))
          )

          server.verify(
            getRequestedFor(urlEqualTo(marriageDetailsPath))
          )
        }
      }

    }
  }

}
