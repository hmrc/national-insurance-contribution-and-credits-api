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

import cats.data.EitherT
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.*
import uk.gov.hmrc.app.benefitEligibility.connectors.{
  BenefitSchemeDetailsConnector,
  IndividualStatePensionInformationConnector,
  LiabilitySummaryDetailsConnector,
  LongTermBenefitCalculationDetailsConnector,
  LongTermBenefitNotesConnector,
  MarriageDetailsConnector,
  NiContributionsAndCreditsConnector,
  SchemeMembershipDetailsConnector
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitCalculationDetails.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitNotes.LongTermBenefitNotesSuccess.{
  LongTermBenefitNotesSuccessResponse,
  Note
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  ServiceUnavailable,
  UnexpectedStatus,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.{
  AreaDiallingCode,
  BenefitSchemeInstitutionType,
  BenefitSchemeStatus,
  RerouteToSchemeCessation,
  SchemeAddressType,
  StatementInhibitor
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.IndividualStatePensionInformationSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.EnumOffidtp
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitCalculationDetails.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.{
  NiContributionsAndCreditsRequest,
  NiContributionsAndCreditsSuccess
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  CreditSource,
  LatePaymentPeriod,
  NiContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.{
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
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.*
import uk.gov.hmrc.app.benefitEligibility.model.request.GYSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.{
  GyspPageTask,
  PageTask,
  PageTaskId,
  PaginationCursor,
  PaginationSource
}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDate, LocalDateTime}
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource

class GetYourStatePensionDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val mockBenefitSchemeDetailsConnector: BenefitSchemeDetailsConnector =
    mock[BenefitSchemeDetailsConnector]

  val mockMarriageDetailsConnector: MarriageDetailsConnector =
    mock[MarriageDetailsConnector]

  val mockLongTermBenefitCalculationDetailsConnector: LongTermBenefitCalculationDetailsConnector =
    mock[LongTermBenefitCalculationDetailsConnector]

  val mockLongTermBenefitNotesConnector: LongTermBenefitNotesConnector =
    mock[LongTermBenefitNotesConnector]

  val mockSchemeMembershipDetailsConnector: SchemeMembershipDetailsConnector =
    mock[SchemeMembershipDetailsConnector]

  val mockIndividualStatePensionInformationConnector: IndividualStatePensionInformationConnector =
    mock[IndividualStatePensionInformationConnector]

  val mockLiabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector = mock[LiabilitySummaryDetailsConnector]

  val mockPaginationService = mock[PaginationService]
  val mockUUIDService       = mock[UuidGeneratorService]

  val testInstant: Instant = Instant.parse("2007-12-03T10:15:30.00Z")

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = testInstant
  }

  val underTest = new GetYourStatePensionDataRetrievalService(
    mockNiContributionsAndCreditsConnector,
    mockBenefitSchemeDetailsConnector,
    mockMarriageDetailsConnector,
    mockLongTermBenefitCalculationDetailsConnector,
    mockLongTermBenefitNotesConnector,
    mockSchemeMembershipDetailsConnector,
    mockIndividualStatePensionInformationConnector,
    mockPaginationService,
    mockUUIDService,
    currentTimeSource
  )

  val identifier = Identifier("GD379251T")

  private val niContributionsAndCreditsRequest = NiContributionsAndCreditsRequest(
    nationalInsuranceNumber = identifier,
    dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2026)
  )

  private val eligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
    Identifier("GD379251T"),
    ContributionsAndCreditsRequestParams(
      DateOfBirth(LocalDate.parse("2025-10-10")),
      StartTaxYear(2025),
      EndTaxYear(2026)
    ),
    Some(
      LongTermBenefitCalculationRequestParams(
        Some(LongTermBenefitType.WidowsBenefit),
        Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing)
      )
    )
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
    class2Or3ContributionAndCredits = Some(
      List(
        Class2or3ContributionAndCredits(
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
      reconciliationDate = Some(ReconciliationDate("2025-03-31")),
      schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
    ),
    schemeAddressDetailsList = List(
      SchemeAddressDetails(
        schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
        schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
        schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
        country = Some(Country.Scotland),
        areaDiallingCode = Some(AreaDiallingCode.Code0131),
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

  val paging = GyspPageTask(
    PageTaskId(UUID.fromString("cd0cc67d-4732-4b8e-b103-1535b531307a")),
    Some(PaginationSource(ApiName.SchemeMembershipDetails, Some("some-url"))),
    Some(PaginationSource(ApiName.MarriageDetails, Some(""))),
    None,
    identifier,
    testInstant
  )

  "MaternityAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should return an EligibilityCheckDataResult (all successful nps calls)" in {
        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        val benefitSchemeDetailsResult = SuccessResult(
          ApiName.BenefitSchemeDetails,
          benefitSchemeDetailsSuccessResponse
        )

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        val longTermBenefitCalculationDetailsResult = SuccessResult(
          ApiName.LongTermBenefitCalculationDetails,
          longTermBenefitCalculationDetailsSuccessResponse
        )

        val longTermBenefitNotesResult = SuccessResult(
          ApiName.LongTermBenefitNotes,
          longTermBenefitNotesSuccessResponse
        )

        val schemeMembershipDetailsResult = SuccessResult(
          ApiName.SchemeMembershipDetails,
          schemeMembershipDetailsSuccessResponse
        )

        val individualStatePensionInformationResult = SuccessResult(
          ApiName.IndividualStatePension,
          individualStatePensionInformationSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockBenefitSchemeDetailsConnector
          .fetchBenefitSchemeDetails(_: BenefitType, _: Identifier, _: SchemeContractedOutNumberDetails)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *, *)
          .returning(
            EitherT.rightT(benefitSchemeDetailsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: Identifier
          )(_: HeaderCarrier))
          .expects(identifier, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        (mockLongTermBenefitCalculationDetailsConnector
          .fetchBenefitCalculationDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[LongTermBenefitType],
            _: Option[PensionProcessingArea]
          )(_: HeaderCarrier))
          .expects(
            BenefitType.GYSP,
            identifier,
            Some(LongTermBenefitType.WidowsBenefit),
            Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing),
            *
          )
          .returning(
            EitherT.rightT(longTermBenefitCalculationDetailsResult)
          )

        (mockLongTermBenefitNotesConnector
          .fetchLongTermBenefitNotes(
            _: BenefitType,
            _: Identifier,
            _: LongTermBenefitType,
            _: AssociatedCalculationSequenceNumber
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *, *, *)
          .returning(
            EitherT.rightT(longTermBenefitNotesResult)
          )

        (mockSchemeMembershipDetailsConnector
          .fetchSchemeMembershipDetails(
            _: BenefitType,
            _: Identifier
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(schemeMembershipDetailsResult)
          )

        (mockIndividualStatePensionInformationConnector
          .fetchIndividualStatePensionInformation(_: BenefitType, _: Identifier)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(individualStatePensionInformationResult)
          )

        (() => mockUUIDService.generate).expects().returning(UUID.fromString("cd0cc67d-4732-4b8e-b103-1535b531307a"))

        (mockPaginationService
          .addTask(_: PageTask))
          .expects(paging)
          .returning(EitherT.rightT(UUID.fromString("cd0cc67d-4732-4b8e-b103-1535b531307a")))

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultGYSP(
            niContributionAndCreditsResult,
            BenefitSchemeMembershipDetailsData(
              schemeMembershipDetailsResult,
              List(benefitSchemeDetailsResult)
            ),
            LongTermBenefitCalculationDetailsData(
              longTermBenefitCalculationDetailsResult,
              List(longTermBenefitNotesResult)
            ),
            marriageDetailsResult,
            individualStatePensionInformationResult,
            Some(PaginationCursor(PaginationType.GYSP, paging.pageTaskId))
          )
        )

      }
      "should return an EligibilityCheckDataResult (both successful and unsuccessful nps call)" in {

        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        val benefitSchemeDetailsResult = SuccessResult(
          ApiName.BenefitSchemeDetails,
          benefitSchemeDetailsSuccessResponse
        )

        val marriageDetailsResult = FailureResult(
          ApiName.MarriageDetails,
          ErrorReport(UnprocessableEntity, None)
        )

        val longTermBenefitCalculationDetailsResult = FailureResult(
          ApiName.LongTermBenefitCalculationDetails,
          ErrorReport(BadRequest, None)
        )

        val schemeMembershipDetailsResult = SuccessResult(
          ApiName.SchemeMembershipDetails,
          schemeMembershipDetailsSuccessResponse
        )

        val individualStatePensionInformationResult = SuccessResult(
          ApiName.IndividualStatePension,
          individualStatePensionInformationSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockBenefitSchemeDetailsConnector
          .fetchBenefitSchemeDetails(_: BenefitType, _: Identifier, _: SchemeContractedOutNumberDetails)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *, *)
          .returning(
            EitherT.rightT(benefitSchemeDetailsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: Identifier
          )(_: HeaderCarrier))
          .expects(identifier, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        (mockLongTermBenefitCalculationDetailsConnector
          .fetchBenefitCalculationDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[LongTermBenefitType],
            _: Option[PensionProcessingArea]
          )(_: HeaderCarrier))
          .expects(
            BenefitType.GYSP,
            identifier,
            Some(LongTermBenefitType.WidowsBenefit),
            Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing),
            *
          )
          .returning(
            EitherT.rightT(longTermBenefitCalculationDetailsResult)
          )

        (mockSchemeMembershipDetailsConnector
          .fetchSchemeMembershipDetails(
            _: BenefitType,
            _: Identifier
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(schemeMembershipDetailsResult)
          )

        (mockIndividualStatePensionInformationConnector
          .fetchIndividualStatePensionInformation(_: BenefitType, _: Identifier)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(individualStatePensionInformationResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultGYSP(
            niContributionAndCreditsResult,
            BenefitSchemeMembershipDetailsData(
              schemeMembershipDetailsResult,
              List(benefitSchemeDetailsResult)
            ),
            LongTermBenefitCalculationDetailsData(
              longTermBenefitCalculationDetailsResult,
              Nil
            ),
            marriageDetailsResult,
            individualStatePensionInformationResult,
            None
          )
        )

      }
      "should return an EligibilityCheckDataResult (all unsuccessful nps call)" in {

        val niContributionAndCreditsResult = FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(BadRequest, None)
        )

        val marriageDetailsResult = FailureResult(
          ApiName.LongTermBenefitNotes,
          ErrorReport(UnexpectedStatus(504), None)
        )

        val longTermBenefitCalculationDetailsResult = FailureResult(
          ApiName.LongTermBenefitCalculationDetails,
          ErrorReport(ServiceUnavailable, None)
        )

        val schemeMembershipDetailsResult = FailureResult(
          ApiName.SchemeMembershipDetails,
          ErrorReport(AccessForbidden, None)
        )

        val individualStatePensionInformationResult = FailureResult(
          ApiName.IndividualStatePension,
          ErrorReport(UnprocessableEntity, None)
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: Identifier
          )(_: HeaderCarrier))
          .expects(identifier, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        (mockLongTermBenefitCalculationDetailsConnector
          .fetchBenefitCalculationDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[LongTermBenefitType],
            _: Option[PensionProcessingArea]
          )(_: HeaderCarrier))
          .expects(
            BenefitType.GYSP,
            identifier,
            Some(LongTermBenefitType.WidowsBenefit),
            Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing),
            *
          )
          .returning(
            EitherT.rightT(longTermBenefitCalculationDetailsResult)
          )

        (mockSchemeMembershipDetailsConnector
          .fetchSchemeMembershipDetails(
            _: BenefitType,
            _: Identifier
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(schemeMembershipDetailsResult)
          )

        (mockIndividualStatePensionInformationConnector
          .fetchIndividualStatePensionInformation(_: BenefitType, _: Identifier)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.rightT(individualStatePensionInformationResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultGYSP(
            niContributionAndCreditsResult,
            BenefitSchemeMembershipDetailsData(
              schemeMembershipDetailsResult,
              Nil
            ),
            LongTermBenefitCalculationDetailsData(
              longTermBenefitCalculationDetailsResult,
              Nil
            ),
            marriageDetailsResult,
            individualStatePensionInformationResult,
            None
          )
        )
      }

      "should return a DataRetrievalServiceError if the service fails to retrieve results for a subset of the NPS APIs called" in {

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        val longTermBenefitCalculationDetailsResult = SuccessResult(
          ApiName.LongTermBenefitCalculationDetails,
          longTermBenefitCalculationDetailsSuccessResponse
        )

        val longTermBenefitNotesResult = SuccessResult(
          ApiName.LongTermBenefitNotes,
          longTermBenefitNotesSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockLongTermBenefitNotesConnector
          .fetchLongTermBenefitNotes(
            _: BenefitType,
            _: Identifier,
            _: LongTermBenefitType,
            _: AssociatedCalculationSequenceNumber
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *, *, *)
          .returning(
            EitherT.rightT(longTermBenefitNotesResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: Identifier
          )(_: HeaderCarrier))
          .expects(identifier, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        (mockLongTermBenefitCalculationDetailsConnector
          .fetchBenefitCalculationDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[LongTermBenefitType],
            _: Option[PensionProcessingArea]
          )(_: HeaderCarrier))
          .expects(
            BenefitType.GYSP,
            identifier,
            Some(LongTermBenefitType.WidowsBenefit),
            Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing),
            *
          )
          .returning(
            EitherT.rightT(longTermBenefitCalculationDetailsResult)
          )

        (mockSchemeMembershipDetailsConnector
          .fetchSchemeMembershipDetails(
            _: BenefitType,
            _: Identifier
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockIndividualStatePensionInformationConnector
          .fetchIndividualStatePensionInformation(_: BenefitType, _: Identifier)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(DataRetrievalServiceError())
      }

      "should return a DataRetrievalServiceError if the service fails to retrieve results for all the NPS APIs called" in {
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: Identifier
          )(_: HeaderCarrier))
          .expects(identifier, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockLongTermBenefitCalculationDetailsConnector
          .fetchBenefitCalculationDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[LongTermBenefitType],
            _: Option[PensionProcessingArea]
          )(_: HeaderCarrier))
          .expects(
            BenefitType.GYSP,
            identifier,
            Some(LongTermBenefitType.WidowsBenefit),
            Some(PensionProcessingArea.StandardElectronicEnabledPensionProcessing),
            *
          )
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockSchemeMembershipDetailsConnector
          .fetchSchemeMembershipDetails(
            _: BenefitType,
            _: Identifier
          )(_: HeaderCarrier))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockIndividualStatePensionInformationConnector
          .fetchIndividualStatePensionInformation(_: BenefitType, _: Identifier)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, identifier, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(DataRetrievalServiceError())
      }
    }
  }

}
