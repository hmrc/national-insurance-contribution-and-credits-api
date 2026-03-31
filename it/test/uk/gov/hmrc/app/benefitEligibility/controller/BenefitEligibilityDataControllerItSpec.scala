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

package uk.gov.hmrc.app.benefitEligibility.controller

import cats.data.NonEmptyList
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsSuccess, JsValue, Json, Writes}
import play.api.mvc.{AnyContent, Result}
import play.api.test.*
import play.api.test.Helpers.*
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{Class2MAReceipts, Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResponseStatus
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.IndividualStatePensionInformationSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.IndividualStatePensionInformationSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.individualStatePensionInformation.enums.{
  CreditSourceType,
  IndividualStatePensionContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.LiabilitySearchCategoryHyphenated.Abroad
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitCalculationDetails.BenefitCalculationDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitCalculationDetails.enums.{
  CalculationSource,
  CalculationStatus,
  Payday
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitNotes.LongTermBenefitNotesSuccess.{
  LongTermBenefitNotesSuccessResponse,
  Note
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageEndDateStatus.Verified
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStartDateStatus
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.{
  HipOrigin,
  NpsErrorCode,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.request.*
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.*
import uk.gov.hmrc.app.benefitEligibility.model.response.*
import uk.gov.hmrc.app.benefitEligibility.model.response.ErrorCode.{
  BadRequest,
  Forbidden,
  InternalServerError,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.service.UuidGenerator
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.*
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.Future

class BenefitEligibilityDataControllerItSpec
    extends AnyFreeSpec
    with EitherValues
    with DefaultPlayMongoRepositorySupport[PageTask]
    with WireMockHelper
    with Injecting
    with Matchers
    with MockFactory {

  val uuidOne: UUID   = UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba")
  val uuidTwo: UUID   = UUID.fromString("f678d869-7922-4a11-82e2-5cf4e235cfee")
  val uuidThree: UUID = UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")

  val mockUuidGenerator: UuidGenerator = mock[UuidGenerator]

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = Instant.parse("2007-12-03T10:15:30.00Z")
  }

  lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        play.api.inject.bind[UuidGenerator].toInstance(mockUuidGenerator),
        play.api.inject.bind[MongoComponent].toInstance(mongoComponent),
        play.api.inject.bind[CurrentTimeSource].toInstance(currentTimeSource)
      )
      .configure(
        "microservice.services.hip.nps.class2MaReceipts.port"           -> server.port,
        "microservice.services.hip.nps.liabilities.port"                -> server.port,
        "microservice.services.hip.nps.niContributionAndCredits.port"   -> server.port,
        "microservice.services.hip.nps.marriageDetails.port"            -> server.port,
        "microservice.services.hip.nps.individualStatePension.port"     -> server.port,
        "microservice.services.hip.nps.schemeMembershipDetails.port"    -> server.port,
        "microservice.services.hip.nps.longTermBenefitCalculation.port" -> server.port,
        "benefitEligibilityInfoEndpointEnabled"                         -> true,
        "microservice.services.auth.port"                               -> server.port
      )
      .build()

  private lazy val underTest: BenefitEligibilityDataController =
    app.injector.instanceOf[BenefitEligibilityDataController]

  server.start()

  override protected val repository: BenefitEligibilityRepositoryImpl =
    inject[BenefitEligibilityRepositoryImpl]

  override protected def checkTtlIndex = false

  private val nationalInsuranceNumber: Identifier  = Identifier("AB123456C")
  private val nationalInsuranceNumber2: Identifier = Identifier("CD345678E")

  val npsLiabilitySummaryDetailsPath: String = s"/person/${nationalInsuranceNumber.value}/liability-summary/ABROAD"
  val npsCreditsAndContributionsPath         = "/national-insurance/contributions-and-credits"
  val npsIndividualMarriageDetailsPath       = s"/individual/${nationalInsuranceNumber.value}/marriage-cp"
  val benefitSchemeDetailsPath    = s"/benefit-scheme/${nationalInsuranceNumber.value}/benefit-scheme-details/S2123456B"
  val schemeMembershipDetailsPath = s"/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"
  val npsClass2MaReceiptsPath     = s"/class-2/${nationalInsuranceNumber.value}/maternity-allowance/receipts"

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    List(
      MaPageTask(
        correlationId,
        PageTaskId(uuidOne),
        List(
          PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath),
          PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath)
        ),
        Some(PaginationSource(Class2MAReceipts, npsClass2MaReceiptsPath)),
        nationalInsuranceNumber,
        currentTimeSource.instantNow()
      ),
      BspPageTask(
        correlationId,
        PageTaskId(uuidTwo),
        Some(
          PaginationSource(MarriageDetails, npsIndividualMarriageDetailsPath)
        ),
        Some(
          ContributionAndCreditsPaging(
            NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2030))),
            DateOfBirth(LocalDate.parse("2025-10-10"))
          )
        ),
        nationalInsuranceNumber,
        currentTimeSource.instantNow()
      ),
      GyspPageTask(
        correlationId,
        PageTaskId(uuidThree),
        Some(
          PaginationSource(
            ApiName.SchemeMembershipDetails,
            schemeMembershipDetailsPath
          )
        ),
        Some(
          PaginationSource(MarriageDetails, npsIndividualMarriageDetailsPath)
        ),
        Some(
          ContributionAndCreditsPaging(
            NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2030))),
            DateOfBirth(LocalDate.parse("2025-10-10"))
          )
        ),
        nationalInsuranceNumber,
        currentTimeSource.instantNow()
      )
    ).foreach(pageTask => insert(pageTask).futureValue)
  }

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
          spouseIdentifier = Some(nationalInsuranceNumber2),
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
        Some(EmployersContractedOutNumberDetails("S2123456B"))
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
    Some(nationalInsuranceNumber),
    Some(
      List(
        Class2MAReceiptDetails(
          initials = Some(Initials("JP")),
          surname = Some(Surname("van Cholmondley-warner")),
          receivablePayment = Some(ReceivablePayment(10.56)),
          receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
          liabilityStart = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
          liabilityEnd = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
          billAmount = Some(BillAmount(9999.98)),
          billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
          isClosedRecord = Some(IsClosedRecord(true)),
          weeksPaid = Some(WeeksPaid(2))
        )
      )
    ),
    callBack = None
  )

  val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
    Some(
      List(
        LiabilityDetailsList(
          identifier = nationalInsuranceNumber,
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
    callback = None
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
              startDate = Some(MarriageStartDate(LocalDate.parse("1999-01-01"))),
              startDateStatus = Some(MarriageStartDateStatus.Verified),
              endDate = Some(MarriageEndDate(LocalDate.parse("2001-01-01"))),
              endDateStatus = Some(Verified),
              spouseIdentifier = Some(nationalInsuranceNumber2),
              spouseForename = Some(SpouseForename("Skywalker")),
              spouseSurname = Some(SpouseSurname("Luke")),
              separationDate = Some(SeparationDate(LocalDate.parse("2002-01-01"))),
              reconciliationDate = Some(MarriageDetailsReconciliationDate(LocalDate.parse("2003-01-01")))
            )
        )
      ),
      None
    )
  )

  val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
    schemeMembershipDetailsSummaryList = Some(
      List(
        SchemeMembershipDetailsSummary(
          stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
          schemeMembershipDetails = SchemeMembershipDetails(
            nationalInsuranceNumber = nationalInsuranceNumber,
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
            employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S2123456B")),
            schemeCreatingContractedOutNumberDetails = Some(SchemeCreatingContractedOutNumberDetails("S2123456B")),
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
      reconciliationDate = Some(BenefitSchemeDetailsSuccess.ReconciliationDate("2025-03-31")),
      schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
    ),
    schemeAddressDetailsList = List(
      SchemeAddressDetails(
        schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
        schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
        schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
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
    nationalInsuranceNumber = nationalInsuranceNumber,
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
              nationalInsuranceNumber = nationalInsuranceNumber,
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

  "BenefitEligibilityController" - {
    ".fetchBenefitEligibilityData" - {
      "ESA" - {
        "should fetch ESA Data correctly" in {

          val successResponse = NiContributionsAndCreditsSuccessResponse(
            Some(TotalGraduatedPensionUnits(BigDecimal("100.0"))),
            Some(
              List(
                Class1ContributionAndCredits(
                  taxYear = Some(TaxYear(2022)),
                  numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                  contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                  contributionCategory = Some(ContributionCategory.None),
                  contributionCreditType = Some(NiContributionCreditType.C1),
                  primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                  class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                  creditSource = Some(CreditSource.NotKnown),
                  employerName = Some(EmployerName("ipOpMs")),
                  latePaymentPeriod = Some(LatePaymentPeriod.L)
                )
              )
            ),
            Some(
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
          val successResponseJson =
            """{
              |  "totalGraduatedPensionUnits": 100,
              |  "class1ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCategoryLetter": "U",
              |      "contributionCategory": "(NONE)",
              |      "contributionCreditType": "C1",
              |      "primaryContribution": 99999999999999.98,
              |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
              |      "primaryPaidEarnings": 99999999999999.98,
              |      "creditSource": "NOT KNOWN",
              |      "employerName": "ipOpMs",
              |      "latePaymentPeriod": "L"
              |    }
              |  ],
              |  "class2Or3ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCreditType": "C1",
              |      "class2Or3EarningsFactor": 99999999999999.98,
              |      "class2NIContributionAmount": 99999999999999.98,
              |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
              |      "creditSource": "NOT KNOWN",
              |      "latePaymentPeriod": "L"
              |    }
              |  ]
              |}""".stripMargin

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(successResponseJson)
              )
          )
          val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] =
            FakeRequest("POST", "/benefit-eligibility-info")
              .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
              .withHeaders(
                "Content-Type"  -> "application/json",
                "Authorization" -> "Bearer token",
                "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
              )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseEsa(nationalInsuranceNumber, successResponse)

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should return a 502 if downstream calls to NPS services fail (ESA)" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
              )
          )
          val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResponse = """{
                                   |   "status":"FAILURE",
                                   |   "nationalInsuranceNumber":"AB123456C",
                                   |   "benefitType":"ESA",
                                   |   "summary":{
                                   |      "totalCalls":1,
                                   |      "successful":0,
                                   |      "failed":1
                                   |   },
                                   |   "downStreams":[
                                   |      {
                                   |         "apiName":"NI Contributions and credits",
                                   |         "status":"FAILURE",
                                   |         "error":{
                                   |            "code":"UNEXPECTED_STATUS_CODE",
                                   |            "message":"downstream returned an unexpected status",
                                   |            "downstreamStatus":502
                                   |         }
                                   |      }
                                   |   ]
                                   |}""".stripMargin
          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResponse)
        }
        "should return 422 if ESA Tax years exceed 6 years" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2015),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] =
            FakeRequest("POST", "/benefit-eligibility-info")
              .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
              .withHeaders(
                "Content-Type"  -> "application/json",
                "Authorization" -> "Bearer token",
                "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
              )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          status(result) shouldBe 422
          contentAsJson(result) shouldBe Json.toJson(
            ErrorResponse(UnprocessableEntity, ErrorReason("Tax year range greater than six years"))
          )
        }

      }

      "JSA" - {
        "should fetch JSA Data correctly" in {

          val successResponse = NiContributionsAndCreditsSuccessResponse(
            Some(TotalGraduatedPensionUnits(BigDecimal("100.0"))),
            Some(
              List(
                Class1ContributionAndCredits(
                  taxYear = Some(TaxYear(2022)),
                  numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                  contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                  contributionCategory = Some(ContributionCategory.None),
                  contributionCreditType = Some(NiContributionCreditType.C1),
                  primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                  class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                  creditSource = Some(CreditSource.NotKnown),
                  employerName = Some(EmployerName("ipOpMs")),
                  latePaymentPeriod = Some(LatePaymentPeriod.L)
                )
              )
            ),
            Some(
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
          val successResponseJson =
            """{
              |  "totalGraduatedPensionUnits": 100,
              |  "class1ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCategoryLetter": "U",
              |      "contributionCategory": "(NONE)",
              |      "contributionCreditType": "C1",
              |      "primaryContribution": 99999999999999.98,
              |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
              |      "primaryPaidEarnings": 99999999999999.98,
              |      "creditSource": "NOT KNOWN",
              |      "employerName": "ipOpMs",
              |      "latePaymentPeriod": "L"
              |    }
              |  ],
              |  "class2Or3ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCreditType": "C1",
              |      "class2Or3EarningsFactor": 99999999999999.98,
              |      "class2NIContributionAmount": 99999999999999.98,
              |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
              |      "creditSource": "NOT KNOWN",
              |      "latePaymentPeriod": "L"
              |    }
              |  ]
              |}""".stripMargin

          server.stubFor(
            WireMock
              .post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(successResponseJson)
              )
          )
          val jsaEligibilityCheckDataRequest = JSAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(jsaEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseJsa(nationalInsuranceNumber, successResponse)

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)

        }
        "should return a 502 if downstream calls to NPS services fail (JSA)" in {

          server.stubFor(
            WireMock
              .post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
              )
          )
          val jsaEligibilityCheckDataRequest = JSAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(jsaEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResponse = """{
                                   |   "status":"FAILURE",
                                   |   "nationalInsuranceNumber":"AB123456C",
                                   |   "benefitType":"JSA",
                                   |   "summary":{
                                   |      "totalCalls":1,
                                   |      "successful":0,
                                   |      "failed":1
                                   |   },
                                   |   "downStreams":[
                                   |      {
                                   |         "apiName":"NI Contributions and credits",
                                   |         "status":"FAILURE",
                                   |         "error":{
                                   |            "code":"UNEXPECTED_STATUS_CODE",
                                   |            "message":"downstream returned an unexpected status",
                                   |            "downstreamStatus":502
                                   |         }
                                   |      }
                                   |   ]
                                   |}""".stripMargin
          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResponse)

        }
        "should return 422 if JSA Tax years exceed 6 years" in {

          server.stubFor(
            WireMock
              .post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          val jsaEligibilityCheckDataRequest = JSAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2015),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(jsaEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          status(result) shouldBe 422
          contentAsJson(result) shouldBe Json.toJson(
            ErrorResponse(UnprocessableEntity, ErrorReason("Tax year range greater than six years"))
          )

        }

      }

      "BSP Searchlight" - {

        "should fetch BSP Searchlight Data correctly" in {

          val successResponse = NiContributionsAndCreditsSuccessResponse(
            Some(TotalGraduatedPensionUnits(BigDecimal("100.0"))),
            Some(
              List(
                Class1ContributionAndCredits(
                  taxYear = Some(TaxYear(2022)),
                  numberOfContributionsAndCredits = Some(NumberOfCreditsAndContributions(53)),
                  contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
                  contributionCategory = Some(ContributionCategory.None),
                  contributionCreditType = Some(NiContributionCreditType.C1),
                  primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
                  class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
                  primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
                  creditSource = Some(CreditSource.NotKnown),
                  employerName = Some(EmployerName("ipOpMs")),
                  latePaymentPeriod = Some(LatePaymentPeriod.L)
                )
              )
            ),
            Some(
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
          val successResponseJson =
            """{
              |  "totalGraduatedPensionUnits": 100,
              |  "class1ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCategoryLetter": "U",
              |      "contributionCategory": "(NONE)",
              |      "contributionCreditType": "C1",
              |      "primaryContribution": 99999999999999.98,
              |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
              |      "primaryPaidEarnings": 99999999999999.98,
              |      "creditSource": "NOT KNOWN",
              |      "employerName": "ipOpMs",
              |      "latePaymentPeriod": "L"
              |    }
              |  ],
              |  "class2Or3ContributionAndCredits": [
              |    {
              |      "taxYear": 2022,
              |      "numberOfContributionsAndCredits": 53,
              |      "contributionCreditType": "C1",
              |      "class2Or3EarningsFactor": 99999999999999.98,
              |      "class2NIContributionAmount": 99999999999999.98,
              |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
              |      "creditSource": "NOT KNOWN",
              |      "latePaymentPeriod": "L"
              |    }
              |  ]
              |}""".stripMargin

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(successResponseJson)
              )
          )
          val bspSearchlightEligibilityCheckDataRequest = SearchlightEligibilityCheckDataRequest(
            BenefitType.BSP,
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] =
            FakeRequest("POST", "/benefit-eligibility-info")
              .withJsonBody(Json.toJson(bspSearchlightEligibilityCheckDataRequest))
              .withHeaders(
                "Content-Type"  -> "application/json",
                "Authorization" -> "Bearer token",
                "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
              )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          println("==== ERROR BODY ====")
          println(contentAsString(result))
          println("====================")

          val expectedResult =
            BenefitEligibilityInfoSuccessResponseSearchLight(
              BenefitType.BSP,
              nationalInsuranceNumber,
              successResponse,
              None
            )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should return a 502 if downstream calls to NPS services fail (BSP Searchlight)" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
              )
          )
          val bspSearchlightEligibilityCheckDataRequest = SearchlightEligibilityCheckDataRequest(
            BenefitType.BSP,
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )
          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(bspSearchlightEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResponse =
            """{
              |   "status":"FAILURE",
              |   "nationalInsuranceNumber":"AB123456C",
              |   "benefitType":"BSP",
              |   "summary":{
              |      "totalCalls":1,
              |      "successful":0,
              |      "failed":1
              |   },
              |   "downStreams":[
              |      {
              |         "apiName":"NI Contributions and credits",
              |         "status":"FAILURE",
              |         "error":{
              |            "code":"UNEXPECTED_STATUS_CODE",
              |            "message":"downstream returned an unexpected status",
              |            "downstreamStatus":502
              |         }
              |      }
              |   ]
              |}""".stripMargin
          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResponse)
        }
      }

      "MA" - {
        "should Fetch MA Correctly" in {

          val npsClass2MaReceiptsPath        = s"/class-2/${nationalInsuranceNumber.value}/maternity-allowance/receipts"
          val npsLiabilitySummaryDetailsPath = s"/person/${nationalInsuranceNumber.value}/liability-summary/ABROAD"

          val class2MAReceiptsSuccessResponseBody = Json.toJson(class2MAReceiptsSuccessResponse).toString()
          val liabilitySummaryDetailsSuccessResponseBody =
            Json.toJson(liabilitySummaryDetailsSuccessResponse).toString()
          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )
          val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            LiabilitiesRequestParams(List(Abroad), None, None, None)
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(maEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseMa(
            nationalInsuranceNumber,
            filteredClass2MaReceipts,
            List(filteredLiabilitySummaryDetails),
            niContributionsAndCreditsSuccessResponse,
            None
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should Fetch MA Correctly and response contains nextCursor" in {
          (() => mockUuidGenerator.generate)
            .expects()
            .returning(UUID.fromString("df94d7bd-7269-4fc8-bcf8-40ae955ac76e"))

          val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
            Some(
              List(
                LiabilityDetailsList(
                  identifier = nationalInsuranceNumber,
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
            callback = Some(Callback(Some(CallbackUrl("someUrl"))))
          )
          val npsClass2MaReceiptsPath        = s"/class-2/${nationalInsuranceNumber.value}/maternity-allowance/receipts"
          val npsLiabilitySummaryDetailsPath = s"/person/${nationalInsuranceNumber.value}/liability-summary/ABROAD"

          val class2MAReceiptsSuccessResponseBody = Json.toJson(class2MAReceiptsSuccessResponse).toString()
          val liabilitySummaryDetailsSuccessResponseBody =
            Json.toJson(liabilitySummaryDetailsSuccessResponse).toString()
          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()
          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(class2MAReceiptsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )
          val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            LiabilitiesRequestParams(List(Abroad), None, None, None)
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(maEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseMa(
            nationalInsuranceNumber,
            filteredClass2MaReceipts,
            List(filteredLiabilitySummaryDetails),
            niContributionsAndCreditsSuccessResponse,
            Some(
              CursorId(
                "eyJwYWdpbmF0aW9uVHlwZSI6Ik1BIiwicGFnZVRhc2tJZCI6ImRmOTRkN2JkLTcyNjktNGZjOC1iY2Y4LTQwYWU5NTVhYzc2ZSJ9"
              )
            )
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should return a 502 if some downstream calls to NPS services fail (MA - Partial Failure)" in {

          val liabilitySummaryDetailsSuccessResponseBody =
            Json.toJson(liabilitySummaryDetailsSuccessResponse).toString()
          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(liabilitySummaryDetailsSuccessResponseBody)
              )
          )
          val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            LiabilitiesRequestParams(List(Abroad), None, None, None)
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(maEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"PARTIAL FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"MA",
                                 |   "summary":{
                                 |      "totalCalls":3,
                                 |      "successful":1,
                                 |      "failed":2
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"Liabilities",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Class2 MA Receipts",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)
        }
        "should return a 502 if all downstream calls to NPS services fail (MA - Failure)" in {

          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsClass2MaReceiptsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            LiabilitiesRequestParams(List(Abroad), None, None, None)
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(maEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"MA",
                                 |   "summary":{
                                 |      "totalCalls":3,
                                 |      "successful":0,
                                 |      "failed":3
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"Liabilities",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Class2 MA Receipts",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)
        }
        "should return 422 if MA Tax years exceed 6 years" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          val maEligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2015),
              EndTaxYear(2025)
            ),
            LiabilitiesRequestParams(List(Abroad), None, None, None)
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(maEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          status(result) shouldBe 422
          contentAsJson(result) shouldBe Json.toJson(
            ErrorResponse(UnprocessableEntity, ErrorReason("Tax year range greater than six years"))
          )
        }

      }

      "BSP" - {
        "should Fetch BSP Correctly" in {
          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()
          val marriageDetailsSuccessResponseBody = Json.toJson(marriageDetailsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
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
            WireMock
              .get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val bspEligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(bspEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseBsp(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse,
            marriageDetailsResult = filteredMarriageDetails,
            nextCursor = None
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should Fetch BSP Correctly and response contains nextCursor" in {
          (() => mockUuidGenerator.generate)
            .expects()
            .returning(UUID.fromString("df94d7bd-7269-4fc8-bcf8-40ae955ac76e"))
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
                      spouseIdentifier = Some(nationalInsuranceNumber2),
                      spouseForename = Some(SpouseForename("Skywalker")),
                      spouseSurname = Some(SpouseSurname("Luke")),
                      separationDate = Some(SeparationDate(LocalDate.parse("2002-01-01"))),
                      reconciliationDate = Some(MarriageDetailsReconciliationDate(LocalDate.parse("2003-01-01")))
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

          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()
          val marriageDetailsSuccessResponseBody = Json.toJson(marriageDetailsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
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
            WireMock
              .get(urlEqualTo(npsIndividualMarriageDetailsPath))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val bspEligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(bspEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseBsp(
            nationalInsuranceNumber = nationalInsuranceNumber,
            niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse,
            marriageDetailsResult = filteredMarriageDetails,
            nextCursor = Some(
              CursorId(
                "eyJwYWdpbmF0aW9uVHlwZSI6IkJTUCIsInBhZ2VUYXNrSWQiOiJkZjk0ZDdiZC03MjY5LTRmYzgtYmNmOC00MGFlOTU1YWM3NmUifQ=="
              )
            )
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)
        }
        "should return a 502 if some downstream calls to NPS services fail (BSP - Partial Failure)" in {

          val marriageDetailsSuccessResponseBody = Json.toJson(marriageDetailsSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          val bspEligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(bspEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"PARTIAL FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"BSP",
                                 |   "summary":{
                                 |      "totalCalls":2,
                                 |      "successful":1,
                                 |      "failed":1
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"Marriage Details",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)
        }
        "should return a 502 if all downstream calls to NPS services fail (BSP - Failure)" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          val bspEligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            )
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(bspEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"BSP",
                                 |   "summary":{
                                 |      "totalCalls":2,
                                 |      "successful":0,
                                 |      "failed":2
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"Marriage Details",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)
        }
      }

      "GSYP" - {
        "should Fetch GYSP Correctly" in {
          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()
          val marriageDetailsSuccessResponseBody      = Json.toJson(marriageDetailsSuccessResponse).toString()

          val longTermBenefitCalculationDetailsSuccessResponseBody =
            Json.toJson(longTermBenefitCalculationDetailsSuccessResponse).toString()

          val longTermBenefitNotesSuccessResponseBody = Json.toJson(longTermBenefitNotesSuccessResponse).toString()

          val schemeMembershipDetailsSuccessResponseBody =
            Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

          val individualStatePensionInformationSuccessResponseBody =
            Json.toJson(individualStatePensionInformationSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/benefit-scheme-details/S2123456B"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation/ALL/notes/86"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/contributions"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )

          val gypEligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            Some(LongTermBenefitCalculationRequestParams(None, None))
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(gypEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseGysp(
            nationalInsuranceNumber,
            filteredMarriageDetails,
            filteredLongTermBenefitCalculationDetails,
            filteredSchemeMembershipDetails,
            filteredIndividualStatePensionInfo,
            niContributionsAndCreditsSuccessResponse,
            None
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)

        }
        "should Fetch GYSP Correctly and response contains nextCursor" in {
          (() => mockUuidGenerator.generate)
            .expects()
            .returning(UUID.fromString("df94d7bd-7269-4fc8-bcf8-40ae955ac76e"))

          val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
            schemeMembershipDetailsSummaryList = Some(
              List(
                SchemeMembershipDetailsSummary(
                  stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
                  schemeMembershipDetails = SchemeMembershipDetails(
                    nationalInsuranceNumber = nationalInsuranceNumber,
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
                    employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S2123456B")),
                    schemeCreatingContractedOutNumberDetails =
                      Some(SchemeCreatingContractedOutNumberDetails("S2123456B")),
                    schemeTerminatingContractedOutNumberDetails =
                      Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                    importingAppropriateSchemeNumberDetails =
                      Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                    apparentUnnotifiedTerminationDestinationDetails =
                      Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                  )
                )
              )
            ),
            callback = Some(Callback(Some(CallbackUrl("SomeUrl"))))
          )

          val niContributionsAndCreditsSuccessResponseBody =
            Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

          val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()
          val marriageDetailsSuccessResponseBody      = Json.toJson(marriageDetailsSuccessResponse).toString()

          val longTermBenefitCalculationDetailsSuccessResponseBody =
            Json.toJson(longTermBenefitCalculationDetailsSuccessResponse).toString()

          val longTermBenefitNotesSuccessResponseBody = Json.toJson(longTermBenefitNotesSuccessResponse).toString()

          val schemeMembershipDetailsSuccessResponseBody =
            Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

          val individualStatePensionInformationSuccessResponseBody =
            Json.toJson(individualStatePensionInformationSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(niContributionsAndCreditsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/benefit-scheme-details/S2123456B"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(marriageDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation/ALL/notes/86"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/contributions"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )

          val gypEligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            Some(LongTermBenefitCalculationRequestParams(None, None))
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(gypEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = BenefitEligibilityInfoSuccessResponseGysp(
            nationalInsuranceNumber,
            filteredMarriageDetails,
            filteredLongTermBenefitCalculationDetails,
            filteredSchemeMembershipDetails,
            filteredIndividualStatePensionInfo,
            niContributionsAndCreditsSuccessResponse,
            Some(
              CursorId(
                "eyJwYWdpbmF0aW9uVHlwZSI6IkdZU1AiLCJwYWdlVGFza0lkIjoiZGY5NGQ3YmQtNzI2OS00ZmM4LWJjZjgtNDBhZTk1NWFjNzZlIn0="
              )
            )
          )

          status(result) shouldBe 200
          contentAsJson(result) shouldBe Json.toJson(expectedResult)

        }
        "should return a 502 if some downstream calls to NPS services fail (GYSP - Partial Failure)" in {

          val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()

          val longTermBenefitCalculationDetailsSuccessResponseBody =
            Json.toJson(longTermBenefitCalculationDetailsSuccessResponse).toString()

          val longTermBenefitNotesSuccessResponseBody = Json.toJson(longTermBenefitNotesSuccessResponse).toString()

          val schemeMembershipDetailsSuccessResponseBody =
            Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

          val individualStatePensionInformationSuccessResponseBody =
            Json.toJson(individualStatePensionInformationSuccessResponse).toString()

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/benefit-scheme-details/S2123456B"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(benefitSchemeDetailsSuccessResponseBody)
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitCalculationDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation/ALL/notes/86"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(longTermBenefitNotesSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(schemeMembershipDetailsSuccessResponseBody)
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/contributions"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody(individualStatePensionInformationSuccessResponseBody)
              )
          )

          val gypEligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            Some(LongTermBenefitCalculationRequestParams(None, None))
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(gypEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"PARTIAL FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"GYSP",
                                 |   "summary":{
                                 |      "totalCalls":7,
                                 |      "successful":5,
                                 |      "failed":2
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"Benefit Scheme Details",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"Long Term Benefit Notes",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Marriage Details",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Scheme Membership Details",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"Long Term Benefit Calculation Details",
                                 |         "status":"SUCCESS"
                                 |      },
                                 |      {
                                 |         "apiName":"Individual State Pension Information",
                                 |         "status":"SUCCESS"
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)

        }
        "should return a 502 if some downstream calls to NPS services fail (GYSP - Failure)" in {

          server.stubFor(
            post(urlEqualTo("/auth/authorise"))
              .willReturn(
                aResponse()
                  .withStatus(OK)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            post(urlEqualTo("/national-insurance/contributions-and-credits"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/benefit-scheme-details/S2123456B"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/individual/${nationalInsuranceNumber.value}/marriage-cp"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/calculation/ALL/notes/86"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )
          server.stubFor(
            WireMock
              .get(urlEqualTo(s"/long-term-benefits/${nationalInsuranceNumber.value}/contributions"))
              .willReturn(
                aResponse()
                  .withStatus(BAD_GATEWAY)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{}")
              )
          )

          val gypEligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
            nationalInsuranceNumber,
            ContributionsAndCreditsRequestParams(
              DateOfBirth(LocalDate.parse("2025-10-10")),
              StartTaxYear(2024),
              EndTaxYear(2025)
            ),
            Some(LongTermBenefitCalculationRequestParams(None, None))
          )

          val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(gypEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "Authorization" -> "Bearer token",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

          val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

          val expectedResult = """{
                                 |   "status":"FAILURE",
                                 |   "nationalInsuranceNumber":"AB123456C",
                                 |   "benefitType":"GYSP",
                                 |   "summary":{
                                 |      "totalCalls":5,
                                 |      "successful":0,
                                 |      "failed":5
                                 |   },
                                 |   "downStreams":[
                                 |      {
                                 |         "apiName":"NI Contributions and credits",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Marriage Details",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Scheme Membership Details",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Long Term Benefit Calculation Details",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      },
                                 |      {
                                 |         "apiName":"Individual State Pension Information",
                                 |         "status":"FAILURE",
                                 |         "error":{
                                 |            "code":"UNEXPECTED_STATUS_CODE",
                                 |            "message":"downstream returned an unexpected status",
                                 |            "downstreamStatus":502
                                 |         }
                                 |      }
                                 |   ]
                                 |}""".stripMargin

          status(result) shouldBe 502
          contentAsJson(result) shouldBe Json.parse(expectedResult)

        }
      }

      "should return a 400 if a request is sent without a CorrelationID" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
        val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
          nationalInsuranceNumber,
          ContributionsAndCreditsRequestParams(
            DateOfBirth(LocalDate.parse("2025-10-10")),
            StartTaxYear(2025),
            EndTaxYear(2026)
          )
        )
        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
          .withHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer token")

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 400

        contentAsJson(result) shouldBe Json.toJson(
          ErrorResponse(BadRequest, ErrorReason("Missing Header CorrelationId"))
        )
      }
      "should return a 400 if a request is sent with an invalid CorrelationID" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
        val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
          nationalInsuranceNumber,
          ContributionsAndCreditsRequestParams(
            DateOfBirth(LocalDate.parse("2025-10-10")),
            StartTaxYear(2025),
            EndTaxYear(2026)
          )
        )
        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "notValidCorrelationId"
          )

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 400

        contentAsJson(result) shouldBe Json.toJson(
          ErrorResponse(BadRequest, ErrorReason("Invalid correlationId value found, expected a valid UUID"))
        )
      }
      "should return a 403 if Bearer token is not supplied" in {

        val successResponseJson =
          """{
            |  "totalGraduatedPensionUnits": 100,
            |  "class1ContributionAndCredits": [
            |    {
            |      "taxYear": 2022,
            |      "numberOfContributionsAndCredits": 53,
            |      "contributionCategoryLetter": "U",
            |      "contributionCategory": "(NONE)",
            |      "contributionCreditType": "C1",
            |      "primaryContribution": 99999999999999.98,
            |      "class1ContributionStatus": "COMPLIANCE & YIELD INCOMPLETE",
            |      "primaryPaidEarnings": 99999999999999.98,
            |      "creditSource": "NOT KNOWN",
            |      "employerName": "ipOpMs",
            |      "latePaymentPeriod": "L"
            |    }
            |  ],
            |  "class2Or3ContributionAndCredits": [
            |    {
            |      "taxYear": 2022,
            |      "numberOfContributionsAndCredits": 53,
            |      "contributionCreditType": "C1",
            |      "class2Or3EarningsFactor": 99999999999999.98,
            |      "class2NIContributionAmount": 99999999999999.98,
            |      "class2Or3CreditStatus": "NOT KNOWN/NOT APPLICABLE",
            |      "creditSource": "NOT KNOWN",
            |      "latePaymentPeriod": "L"
            |    }
            |  ]
            |}""".stripMargin

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
        server.stubFor(
          post(urlEqualTo("/national-insurance/contributions-and-credits"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(successResponseJson)
            )
        )
        val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
          nationalInsuranceNumber,
          ContributionsAndCreditsRequestParams(
            DateOfBirth(LocalDate.parse("2025-10-10")),
            StartTaxYear(2024),
            EndTaxYear(2025)
          )
        )
        val request: FakeRequest[AnyContent] =
          FakeRequest("POST", "/benefit-eligibility-info")
            .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
            .withHeaders(
              "Content-Type"  -> "application/json",
              "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
            )

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 403
        contentAsJson(result) shouldBe Json.toJson(ErrorResponse(Forbidden, ErrorReason("Bearer token not supplied")))
      }
      "should return a 422 if a request is sent with invalid data" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
          nationalInsuranceNumber,
          ContributionsAndCreditsRequestParams(
            DateOfBirth(LocalDate.parse("2025-10-10")),
            StartTaxYear(2025),
            EndTaxYear(2026)
          )
        )
        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 422
        contentAsJson(result) shouldBe Json.toJson(
          ErrorResponse(ErrorCode.UnprocessableEntity, ErrorReason("End tax year after CY-1"))
        )

      }

      "should return a 400 if a request is sent with json that fails domain validation" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withJsonBody(Json.toJson("{}"))
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 400
        contentAsJson(result) shouldBe Json.toJson(
          ErrorResponse(
            BadRequest,
            ErrorReason("incompatible json, request body does not match schema - [\"{}\" is not an object]")
          )
        )
      }

      "should return a 400 if a request is sent with invalid json" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )
          .withTextBody("invalidJson")

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 400
        contentAsJson(result) shouldBe Json.toJson(ErrorResponse(BadRequest, ErrorReason("invalid json")))

      }

      "should return a 500, Internal Server Error if an unexpected error occurs" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
            )
        )

        val esaEligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
          nationalInsuranceNumber,
          ContributionsAndCreditsRequestParams(
            DateOfBirth(LocalDate.parse("2025-10-10")),
            StartTaxYear(2024),
            EndTaxYear(2025)
          )
        )

        val request: FakeRequest[AnyContent] = FakeRequest("POST", "/benefit-eligibility-info")
          .withJsonBody(Json.toJson(esaEligibilityCheckDataRequest))
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.fetchBenefitEligibilityData()(request)

        status(result) shouldBe 500
        contentAsJson(result) shouldBe Json.toJson(
          ErrorResponse(InternalServerError, ErrorReason("unexpected internal failure"))
        )
      }
    }
    ".getNextPage" - {
      "should handle a MA request containing nextCursor successfully (200)" in {
        (() => mockUuidGenerator.generate).expects().returning(uuidOne)
        val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
          Some(
            List(
              LiabilityDetailsList(
                identifier = nationalInsuranceNumber,
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
          callback = Some(Callback(Some(CallbackUrl("SomeUrl"))))
        )

        val liabilitySummaryDetailsSuccessResponseBody =
          Json.toJson(liabilitySummaryDetailsSuccessResponse).toString()
        val class2MAReceiptsSuccessResponseBody = Json.toJson(class2MAReceiptsSuccessResponse).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(liabilitySummaryDetailsSuccessResponseBody)
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(class2MAReceiptsSuccessResponseBody)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6Ik1BIiwicGFnZVRhc2tJZCI6IjgzOTY0MmUwLWQ5ODUtNGMyNi1iZjJmLWVlYTIzNjQwNDJiYSJ9"
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        val expectedResult = BenefitEligibilityInfoSuccessResponseMa(
          nationalInsuranceNumber,
          FilteredClass2MaReceipts(
            List()
          ),
          List(filteredLiabilitySummaryDetails, filteredLiabilitySummaryDetails),
          NiContributionsAndCreditsSuccessResponse(None, None, None),
          Some(
            CursorId(
              "eyJwYWdpbmF0aW9uVHlwZSI6Ik1BIiwicGFnZVRhc2tJZCI6IjgzOTY0MmUwLWQ5ODUtNGMyNi1iZjJmLWVlYTIzNjQwNDJiYSJ9"
            )
          )
        )

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.toJson(expectedResult)
      }
      "should handle a MA request containing nextCursor successfully (502) " in {
        (() => mockUuidGenerator.generate).expects().returning(uuidOne)
        val npsStandardErrorResponse400 = NpsStandardErrorResponse400(
          HipOrigin.Hip,
          NpsMultiErrorResponse(
            Some(
              List(
                NpsSingleErrorResponse(
                  NpsErrorReason("HTTP message not readable"),
                  NpsErrorCode("502")
                ),
                NpsSingleErrorResponse(
                  NpsErrorReason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
                  NpsErrorCode("502")
                )
              )
            )
          )
        )

        val npsStandardErrorResponse400Body =
          Json.toJson(npsStandardErrorResponse400).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(npsStandardErrorResponse400Body)
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(npsStandardErrorResponse400Body)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6Ik1BIiwicGFnZVRhc2tJZCI6IjgzOTY0MmUwLWQ5ODUtNGMyNi1iZjJmLWVlYTIzNjQwNDJiYSJ9"
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        val expectedResult = BenefitEligibilityInfoErrorResponse(
          OverallResultStatus.Failure,
          nationalInsuranceNumber,
          BenefitType.MA,
          OverallResultSummary(3, 0, 3),
          List(
            SanitizedApiResult(ApiName.Liabilities, NpsApiResponseStatus.Failure, Some(NpsNormalizedError.BadRequest)),
            SanitizedApiResult(ApiName.Liabilities, NpsApiResponseStatus.Failure, Some(NpsNormalizedError.BadRequest)),
            SanitizedApiResult(
              ApiName.Class2MAReceipts,
              NpsApiResponseStatus.Failure,
              Some(NpsNormalizedError.BadRequest)
            )
          )
        )

        status(result) shouldBe 502
        contentAsJson(result) shouldBe Json.toJson(expectedResult)
      }

      "should handle a BSP request containing nextCursor successfully (200)" in {

        (() => mockUuidGenerator.generate).expects().returning(uuidTwo)

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
                    spouseIdentifier = Some(nationalInsuranceNumber2),
                    spouseForename = Some(SpouseForename("Skywalker")),
                    spouseSurname = Some(SpouseSurname("Luke")),
                    separationDate = Some(SeparationDate(LocalDate.parse("2002-01-01"))),
                    reconciliationDate = Some(MarriageDetailsReconciliationDate(LocalDate.parse("2003-01-01")))
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

        val niContributionsAndCreditsSuccessResponseBody =
          Json.toJson(niContributionsAndCreditsSuccessResponse).toString()
        val marriageDetailsSuccessResponseBody = Json.toJson(marriageDetailsSuccessResponse).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
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
          WireMock
            .get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(marriageDetailsSuccessResponseBody)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6IkJTUCIsInBhZ2VUYXNrSWQiOiJmNjc4ZDg2OS03OTIyLTRhMTEtODJlMi01Y2Y0ZTIzNWNmZWUifQ=="
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        val expectedResult = BenefitEligibilityInfoSuccessResponseBsp(
          nationalInsuranceNumber = nationalInsuranceNumber,
          niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse,
          marriageDetailsResult = filteredMarriageDetails,
          nextCursor = Some(
            CursorId(
              "eyJwYWdpbmF0aW9uVHlwZSI6IkJTUCIsInBhZ2VUYXNrSWQiOiJmNjc4ZDg2OS03OTIyLTRhMTEtODJlMi01Y2Y0ZTIzNWNmZWUifQ=="
            )
          )
        )

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.toJson(expectedResult)
      }
      "should handle a BSP request containing nextCursor successfully (502)" in {

        (() => mockUuidGenerator.generate).expects().returning(uuidTwo)

        val npsStandardErrorResponse400 = NpsStandardErrorResponse400(
          HipOrigin.Hip,
          NpsMultiErrorResponse(
            Some(
              List(
                NpsSingleErrorResponse(
                  NpsErrorReason("HTTP message not readable"),
                  NpsErrorCode("502")
                ),
                NpsSingleErrorResponse(
                  NpsErrorReason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
                  NpsErrorCode("502")
                )
              )
            )
          )
        )

        val niContributionsAndCreditsSuccessResponseBody =
          Json.toJson(niContributionsAndCreditsSuccessResponse).toString()
        val npsStandardErrorResponse400Body = Json.toJson(npsStandardErrorResponse400).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
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
          WireMock
            .get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(BAD_REQUEST)
                .withHeader("Content-Type", "application/json")
                .withBody(npsStandardErrorResponse400Body)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6IkJTUCIsInBhZ2VUYXNrSWQiOiJmNjc4ZDg2OS03OTIyLTRhMTEtODJlMi01Y2Y0ZTIzNWNmZWUifQ=="
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        status(result) shouldBe 502
        contentAsJson(result)
          .validate[BenefitEligibilityInfoErrorResponse] shouldBe a[JsSuccess[BenefitEligibilityInfoErrorResponse]]
      }

      "should handle a GYSP request containing nextCursor successfully (200)" in {
        (() => mockUuidGenerator.generate).expects().returning(uuidThree)

        val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
          schemeMembershipDetailsSummaryList = Some(
            List(
              SchemeMembershipDetailsSummary(
                stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
                schemeMembershipDetails = SchemeMembershipDetails(
                  nationalInsuranceNumber = nationalInsuranceNumber,
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
                  employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S2123456B")),
                  schemeCreatingContractedOutNumberDetails =
                    Some(SchemeCreatingContractedOutNumberDetails("S2123456B")),
                  schemeTerminatingContractedOutNumberDetails =
                    Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
                  importingAppropriateSchemeNumberDetails = Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
                  apparentUnnotifiedTerminationDestinationDetails =
                    Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
                )
              )
            )
          ),
          callback = Some(Callback(Some(CallbackUrl("SomeUrl"))))
        )

        val niContributionsAndCreditsSuccessResponseBody =
          Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

        val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()
        val marriageDetailsSuccessResponseBody      = Json.toJson(marriageDetailsSuccessResponse).toString()

        val schemeMembershipDetailsSuccessResponseBody =
          Json.toJson(schemeMembershipDetailsSuccessResponse).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
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
          WireMock
            .get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(marriageDetailsSuccessResponseBody)
            )
        )
        server.stubFor(
          WireMock
            .get(urlEqualTo(schemeMembershipDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(schemeMembershipDetailsSuccessResponseBody)
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(benefitSchemeDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(benefitSchemeDetailsSuccessResponseBody)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6IkdZU1AiLCJwYWdlVGFza0lkIjoiOWIwZGU0OGYtYjk5NS00YzYxLWFlYWItOGIwMjI3M2E4ZjI2In0="
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        val expectedResult = BenefitEligibilityInfoSuccessResponseGysp(
          nationalInsuranceNumber = nationalInsuranceNumber,
          marriageDetailsResult = filteredMarriageDetails,
          longTermBenefitCalculationDetailsResult = FilteredLongTermBenefitCalculationDetails(List()),
          schemeMembershipDetailsResult = filteredSchemeMembershipDetails,
          individualStatePensionInfoResult = FilteredIndividualStatePensionInfo(None, List()),
          niContributionsAndCreditsResult = niContributionsAndCreditsSuccessResponse,
          nextCursor = Some(
            CursorId(
              "eyJwYWdpbmF0aW9uVHlwZSI6IkdZU1AiLCJwYWdlVGFza0lkIjoiOWIwZGU0OGYtYjk5NS00YzYxLWFlYWItOGIwMjI3M2E4ZjI2In0="
            )
          )
        )

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.toJson(expectedResult)

      }

      "should handle a GYSP request containing nextCursor successfully (502)" in {
        (() => mockUuidGenerator.generate).expects().returning(uuidThree)

        val npsStandardErrorResponse400 = NpsStandardErrorResponse400(
          HipOrigin.Hip,
          NpsMultiErrorResponse(
            Some(
              List(
                NpsSingleErrorResponse(
                  NpsErrorReason("HTTP message not readable"),
                  NpsErrorCode("502")
                ),
                NpsSingleErrorResponse(
                  NpsErrorReason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
                  NpsErrorCode("502")
                )
              )
            )
          )
        )

        val niContributionsAndCreditsSuccessResponseBody =
          Json.toJson(niContributionsAndCreditsSuccessResponse).toString()

        val benefitSchemeDetailsSuccessResponseBody = Json.toJson(benefitSchemeDetailsSuccessResponse).toString()
        val marriageDetailsSuccessResponseBody      = Json.toJson(marriageDetailsSuccessResponse).toString()

        val npsStandardErrorResponse400Body =
          Json.toJson(npsStandardErrorResponse400).toString()

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )
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
          WireMock
            .get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(marriageDetailsSuccessResponseBody)
            )
        )
        server.stubFor(
          WireMock
            .get(urlEqualTo(schemeMembershipDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(BAD_GATEWAY)
                .withHeader("Content-Type", "application/json")
                .withBody(npsStandardErrorResponse400Body)
            )
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(benefitSchemeDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(benefitSchemeDetailsSuccessResponseBody)
            )
        )

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          "/benefit-eligibility-info?cursorId=eyJwYWdpbmF0aW9uVHlwZSI6IkdZU1AiLCJwYWdlVGFza0lkIjoiOWIwZGU0OGYtYjk5NS00YzYxLWFlYWItOGIwMjI3M2E4ZjI2In0="
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        status(result) shouldBe 502
        contentAsJson(result)
          .validate[BenefitEligibilityInfoErrorResponse] shouldBe a[JsSuccess[BenefitEligibilityInfoErrorResponse]]
      }

      "should return a 404 if the nextCursor value sent does not exist in the db" in {

        server.stubFor(
          post(urlEqualTo("/auth/authorise"))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody("{}")
            )
        )

        val paginationCursor =
          PaginationCursor(
            PaginationType.GyspPagination,
            PageTaskId(UUID.fromString("2e22042b-d1dd-495d-b4b5-36d734b05e02"))
          )

        val cursorId = CursorId.from(paginationCursor)

        val request: FakeRequest[AnyContent] = FakeRequest(
          "GET",
          s"/benefit-eligibility-info?cursorId=${cursorId.value}"
        )
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token",
            "CorrelationID" -> "eba473d1-c34b-498d-925f-af8d2514fa92"
          )

        val result: Future[Result] = underTest.getNextPage()(request)

        status(result) shouldBe 404
        contentAsJson(result) shouldBe Json.parse(
          s"""{
             |   "code":"NOT_FOUND",
             |   "reason":"record not found for cursorId: ${cursorId.value}"
             |}""".stripMargin
        )
      }
    }
  }

}
