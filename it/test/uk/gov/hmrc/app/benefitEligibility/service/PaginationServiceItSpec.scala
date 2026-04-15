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

import cats.data.NonEmptyList
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import play.api.test.Injecting
import play.api.{Application, inject}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{
  Class2MAReceipts,
  Liabilities,
  MarriageDetails,
  NiContributionAndCredits
}
import uk.gov.hmrc.app.benefitEligibility.model.common.CallSystem.SEARCHLIGHT
import uk.gov.hmrc.app.benefitEligibility.model.common.PaginationType.{BspPagination, MaPagination}
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.SuccessResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.*
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.ExecutionContext

class PaginationServiceItSpec
    extends AnyFreeSpec
    with DefaultPlayMongoRepositorySupport[PageTask]
    with EitherValues
    with WireMockHelper
    with Injecting
    with Matchers
    with MockFactory
    with ScalaFutures {

  val uuidOne: UUID   = UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba")
  val uuidTwo: UUID   = UUID.fromString("f678d869-7922-4a11-82e2-5cf4e235cfee")
  val uuidThree: UUID = UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")
  val uuidFour: UUID  = UUID.fromString("e8a00a25-beec-4fc1-aeea-4a03c8dc55ac")

  val mockUuidGenerator: UuidGenerator = mock[UuidGenerator]

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = Instant.parse("2007-12-03T10:15:30.00Z")
  }

  val nationalInsuranceNumber                = Identifier("GD379251T")
  val npsLiabilitySummaryDetailsPath: String = s"/ni/person/${nationalInsuranceNumber.value}/liability-summary/ABROAD"
  val npsCreditsAndContributionsPath         = "/ni/national-insurance/contributions-and-credits"
  val npsIndividualMarriageDetailsPath       = s"/paye/individual/${nationalInsuranceNumber.value}/marriage-cp"
  val benefitSchemeDetailsPath               = s"/ni/benefit-scheme/benefit-scheme-details/S3123456B"
  val schemeMembershipDetailsPath = s"/ni/benefit-scheme/${nationalInsuranceNumber.value}/scheme-membership-details"
  val npsClass2MaReceiptsPath     = s"/class-2/${nationalInsuranceNumber.value}/maternity-allowance/receipts"

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        play.api.inject.bind[UuidGenerator].toInstance(mockUuidGenerator),
        play.api.inject.bind[MongoComponent].toInstance(mongoComponent),
        play.api.inject.bind[CurrentTimeSource].toInstance(currentTimeSource)
      )
      .configure(
        "microservice.services.hip.nps.class2MaReceipts.port"         -> server.port,
        "microservice.services.hip.nps.liabilities.port"              -> server.port,
        "microservice.services.hip.nps.niContributionAndCredits.port" -> server.port,
        "microservice.services.hip.nps.schemeMembershipDetails.port"  -> server.port,
        "microservice.services.hip.nps.marriageDetails.port"          -> server.port
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val service: PaginationService = app.injector.instanceOf[PaginationService]

  // wiremock server must be started before the repo is injected else suite will fail
  // perm fix: declare protected val repository: PlayMongoRepository[A] in PlayMongoRepositorySupport as a def (library update)
  server.start()

  override protected val repository: BenefitEligibilityRepositoryImpl =
    inject[BenefitEligibilityRepositoryImpl]

  override protected def checkTtlIndex = false

  val maPageTask = MaPageTask(
    correlationId,
    PageTaskId(uuidOne),
    List(
      PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath),
      PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath)
    ),
    Some(PaginationSource(Class2MAReceipts, npsClass2MaReceiptsPath)),
    nationalInsuranceNumber,
    currentTimeSource.instantNow()
  )

  val bspPageTask = BspPageTask(
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
  )

  val gyspPageTask = GyspPageTask(
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

  val searchLightPageTask = SearchLightPageTask(
    correlationId,
    PageTaskId(uuidFour),
    PaginationType.BspPagination,
    Some(
      ContributionAndCreditsPaging(
        NonEmptyList
          .one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
        DateOfBirth(LocalDate.parse("2025-10-10"))
      )
    ),
    nationalInsuranceNumber,
    currentTimeSource.instantNow()
  )

  val listOfPages: List[PageTask] = List(
    maPageTask,
    bspPageTask,
    gyspPageTask,
    searchLightPageTask
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    listOfPages.foreach(pageTask => insert(pageTask).futureValue)
  }

  "PaginationService" - {
    ".addTask" - {
      "should successfully add a new task" in {
        deleteAll().futureValue
        val pageTask: MaPageTask =
          MaPageTask(
            correlationId,
            PageTaskId(UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba")),
            List(),
            None,
            nationalInsuranceNumber,
            Instant.now
          )

        service.addTask(pageTask).value.futureValue shouldBe Right(
          UUID.fromString("839642e0-d985-4c26-bf2f-eea2364042ba")
        )

        findAll().futureValue shouldBe List(pageTask)
      }
      "should return a new uuid if current uuid already exists in database" in {
        val uuidTwo = PageTaskId(UUID.fromString("2db75f56-9975-4a8d-b315-85ef3fac2161"))

        val pageTask = MaPageTask(
          correlationId,
          PageTaskId(uuidOne),
          List(
            PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath),
            PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath)
          ),
          Some(PaginationSource(Class2MAReceipts, npsClass2MaReceiptsPath)),
          nationalInsuranceNumber,
          currentTimeSource.instantNow()
        )
        val newPageTask = MaPageTask(
          correlationId,
          uuidTwo,
          List(
            PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath),
            PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath)
          ),
          Some(PaginationSource(Class2MAReceipts, npsClass2MaReceiptsPath)),
          nationalInsuranceNumber,
          currentTimeSource.instantNow()
        )
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo.value)

        findAll().futureValue should contain theSameElementsAs listOfPages
        service.addTask(pageTask).value.futureValue shouldBe Right(uuidTwo.value)
        findAll().futureValue should contain theSameElementsAs listOfPages :+ newPageTask
      }

    }
    ".paginate" - {
      "should return a BenefitEligibilityError if paginate fails" in {
        server.stubFor(
          get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
            )
        )
        server.stubFor(
          get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
            )
        )

        service.paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidOne))).value.futureValue shouldBe
          a[Left[BenefitEligibilityError, _]]

      }
      "should process Ma pagination task successfully" in {

        (() => mockUuidGenerator.generate).expects().returning(uuidOne)
        val paginationSource2 =
          PaginationSource(Liabilities, npsLiabilitySummaryDetailsPath)
        val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
          Some(
            List(
              LiabilityDetailsList(
                identifier = nationalInsuranceNumber,
                `type` = EnumLiabtp.Abroad,
                occurrenceNumber = OccurrenceNumber(1),
                startDateStatus = None,
                endDateStatus = None,
                startDate = StartDate(LocalDate.parse("2026-01-01")),
                endDate = None,
                country = None,
                trainingCreditApprovalStatus = None,
                casepaperReferenceNumber = None,
                homeResponsibilitiesProtectionBenefitReference = None,
                homeResponsibilitiesProtectionRate = None,
                lostCardNotificationReason = None,
                lostCardRulingReason = None,
                homeResponsibilityProtectionCalculationYear = None,
                awardAmount = None,
                resourceGroupIdentifier = None,
                homeResponsibilitiesProtectionIndicator = None,
                officeDetails = None
              )
            )
          ),
          Some(Callback(Some(CallbackUrl(paginationSource2.callBackURL))))
        )
        val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
          Some(nationalInsuranceNumber),
          Some(
            List(
              Class2MAReceiptDetails(
                initials = None,
                surname = None,
                receivablePayment = None,
                receiptDate = None,
                liabilityStart = None,
                liabilityEnd = None,
                billAmount = None,
                billScheduleNumber = None,
                isClosedRecord = None,
                weeksPaid = None
              )
            )
          ),
          callBack = Some(Callback(Some(CallbackUrl(paginationSource2.callBackURL))))
        )

        server.stubFor(
          get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(liabilitySummaryDetailsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(class2MAReceiptsSuccessResponse).toString)
            )
        )

        service.paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidOne))).value.futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.MaPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(
                NpsApiResult.SuccessResult(Liabilities, liabilitySummaryDetailsSuccessResponse),
                NpsApiResult.SuccessResult(Liabilities, liabilitySummaryDetailsSuccessResponse)
              ),
              class2MaReceiptsResult =
                Some(NpsApiResult.SuccessResult(Class2MAReceipts, class2MAReceiptsSuccessResponse)),
              marriageDetailsResult = None,
              contributionCreditResult = ContributionCreditPagingResult(None, None),
              benefitSchemeMembershipDetailsData = None,
              callSystem = None,
              nextCursor = Some(PaginationCursor(PaginationType.MaPagination, PageTaskId(uuidOne)))
            )
          )

      }
      "should process Bsp pagination task successfully" in {
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo)

        val paginationSource1 = PaginationSource(MarriageDetails, "/CallBackUrl1")
        val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
          MarriageDetailsSuccess.MarriageDetails(
            MarriageDetailsSuccess.ActiveMarriage(true),
            None,
            Some(
              MarriageDetailsSuccess.Links(
                MarriageDetailsSuccess.SelfLink(
                  Some(MarriageDetailsSuccess.Href(paginationSource1.callBackURL.toString)),
                  Some(MarriageDetailsSuccess.Methods.get)
                )
              )
            )
          )
        )

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
              )
            )
          )
        )

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )

        server.stubFor(
          get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(marriageDetailsSuccessResponse).toString)
            )
        )
        service.paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidTwo))).value.futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.BspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = Some(NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse)),
              contributionCreditResult = ContributionCreditPagingResult(
                Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                None
              ),
              benefitSchemeMembershipDetailsData = None,
              callSystem = None,
              nextCursor = Some(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidTwo)))
            )
          )
      }
      "should process Searchlight pagination task successfully" in {
        deleteAll().futureValue

        val searchLightPageTask = SearchLightPageTask(
          correlationId,
          PageTaskId(uuidFour),
          PaginationType.BspPagination,
          Some(
            ContributionAndCreditsPaging(
              NonEmptyList
                .of(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)), TaxWindow(StartTaxYear(2020), EndTaxYear(2022))),
              DateOfBirth(LocalDate.parse("2025-10-10"))
            )
          ),
          nationalInsuranceNumber,
          currentTimeSource.instantNow()
        )
        List(maPageTask, bspPageTask, searchLightPageTask, gyspPageTask).foreach(pageTask =>
          insert(pageTask).futureValue
        )
        (() => mockUuidGenerator.generate).expects().returning(uuidFour)

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
              )
            )
          )
        )

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )

        service
          .paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidFour)))
          .value
          .futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.BspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = None,
              contributionCreditResult = ContributionCreditPagingResult(
                Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                Some(
                  ContributionAndCreditsPaging(
                    NonEmptyList(TaxWindow(StartTaxYear(2020), EndTaxYear(2022)), List()),
                    DateOfBirth(LocalDate.parse("2025-10-10"))
                  )
                )
              ),
              benefitSchemeMembershipDetailsData = None,
              callSystem = Some(SEARCHLIGHT),
              nextCursor = Some(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidFour)))
            )
          )
      }
      "should process Gysp pagination task successfully" in {
        (() => mockUuidGenerator.generate).expects().returning(uuidThree)

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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
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
            reconciliationDate = Some(ReconciliationDate("2025-03-31")),
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

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(marriageDetailsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(schemeMembershipDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(schemeMembershipDetailsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(benefitSchemeDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(benefitSchemeDetailsSuccessResponse).toString)
            )
        )

        val benefitSchemeMembershipDetailsData = BenefitSchemeMembershipDetailsData(
          NpsApiResult.SuccessResult(ApiName.SchemeMembershipDetails, schemeMembershipDetailsSuccessResponse),
          List(NpsApiResult.SuccessResult(ApiName.BenefitSchemeDetails, benefitSchemeDetailsSuccessResponse))
        )

        service
          .paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidThree)))
          .value
          .futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.GyspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = Some(NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse)),
              contributionCreditResult = ContributionCreditPagingResult(
                Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                None
              ),
              benefitSchemeMembershipDetailsData = Some(benefitSchemeMembershipDetailsData),
              callSystem = None,
              nextCursor = Some(PaginationCursor(PaginationType.GyspPagination, PageTaskId(uuidThree)))
            )
          )
      }
      "should process Ma pagination task successfully and delete MA page" in {
        val newListOfPages = List(
          bspPageTask,
          gyspPageTask,
          searchLightPageTask
        )

        (() => mockUuidGenerator.generate).expects().returning(uuidOne)
        val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
          Some(
            List(
              LiabilityDetailsList(
                identifier = nationalInsuranceNumber,
                `type` = EnumLiabtp.Abroad,
                occurrenceNumber = OccurrenceNumber(1),
                startDateStatus = None,
                endDateStatus = None,
                startDate = StartDate(LocalDate.parse("2026-01-01")),
                endDate = None,
                country = None,
                trainingCreditApprovalStatus = None,
                casepaperReferenceNumber = None,
                homeResponsibilitiesProtectionBenefitReference = None,
                homeResponsibilitiesProtectionRate = None,
                lostCardNotificationReason = None,
                lostCardRulingReason = None,
                homeResponsibilityProtectionCalculationYear = None,
                awardAmount = None,
                resourceGroupIdentifier = None,
                homeResponsibilitiesProtectionIndicator = None,
                officeDetails = None
              )
            )
          ),
          None
        )
        val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
          Some(nationalInsuranceNumber),
          Some(
            List(
              Class2MAReceiptDetails(
                initials = None,
                surname = None,
                receivablePayment = None,
                receiptDate = None,
                liabilityStart = None,
                liabilityEnd = None,
                billAmount = None,
                billScheduleNumber = None,
                isClosedRecord = None,
                weeksPaid = None
              )
            )
          ),
          callBack = None
        )

        server.stubFor(
          get(urlEqualTo(npsLiabilitySummaryDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(liabilitySummaryDetailsSuccessResponse).toString)
            )
        )

        server.stubFor(
          get(urlEqualTo(npsClass2MaReceiptsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(class2MAReceiptsSuccessResponse).toString)
            )
        )

        service.paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidOne))).value.futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.MaPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(
                NpsApiResult.SuccessResult(Liabilities, liabilitySummaryDetailsSuccessResponse),
                NpsApiResult.SuccessResult(Liabilities, liabilitySummaryDetailsSuccessResponse)
              ),
              class2MaReceiptsResult =
                Some(NpsApiResult.SuccessResult(Class2MAReceipts, class2MAReceiptsSuccessResponse)),
              marriageDetailsResult = None,
              contributionCreditResult = ContributionCreditPagingResult(None, None),
              benefitSchemeMembershipDetailsData = None,
              callSystem = None,
              nextCursor = None
            )
          )

        findAll().futureValue should contain theSameElementsAs newListOfPages

      }
      "should process Bsp pagination task successfully and delete BSP page" in {
        val newListOfPages = List(
          maPageTask,
          gyspPageTask,
          searchLightPageTask
        )

        (() => mockUuidGenerator.generate).expects().returning(uuidTwo)

        val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
          MarriageDetailsSuccess.MarriageDetails(
            MarriageDetailsSuccess.ActiveMarriage(true),
            None,
            None
          )
        )

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
              )
            )
          )
        )

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )

        server.stubFor(
          get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(marriageDetailsSuccessResponse).toString)
            )
        )
        service.paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidTwo))).value.futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.BspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = Some(NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse)),
              contributionCreditResult = ContributionCreditPagingResult(
                Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                None
              ),
              benefitSchemeMembershipDetailsData = None,
              callSystem = None,
              nextCursor = None
            )
          )

        findAll().futureValue should contain theSameElementsAs newListOfPages
      }
      "should process Gysp pagination task successfully and delete GYSP page" in {
        val newListOfPages = List(
          bspPageTask,
          maPageTask,
          searchLightPageTask
        )

        (() => mockUuidGenerator.generate).expects().returning(uuidThree)

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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
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
            None
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
            reconciliationDate = Some(ReconciliationDate("2025-03-31")),
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

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(npsIndividualMarriageDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(marriageDetailsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(schemeMembershipDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(schemeMembershipDetailsSuccessResponse).toString)
            )
        )
        server.stubFor(
          get(urlEqualTo(benefitSchemeDetailsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(benefitSchemeDetailsSuccessResponse).toString)
            )
        )

        val benefitSchemeMembershipDetailsData = BenefitSchemeMembershipDetailsData(
          NpsApiResult.SuccessResult(ApiName.SchemeMembershipDetails, schemeMembershipDetailsSuccessResponse),
          List(NpsApiResult.SuccessResult(ApiName.BenefitSchemeDetails, benefitSchemeDetailsSuccessResponse))
        )

        service
          .paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidThree)))
          .value
          .futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.GyspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = Some(NpsApiResult.SuccessResult(MarriageDetails, marriageDetailsSuccessResponse)),
              contributionCreditResult = ContributionCreditPagingResult(
                Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                None
              ),
              benefitSchemeMembershipDetailsData = Some(benefitSchemeMembershipDetailsData),
              callSystem = None,
              nextCursor = None
            )
          )

        findAll().futureValue should contain theSameElementsAs newListOfPages
      }
      "should process Searchlight pagination task successfully and delete old Searchlight page" in {
        val newListOfPages = List(
          maPageTask,
          gyspPageTask,
          bspPageTask
        )

        val newId = UUID.fromString("27d18297-bfc9-47b6-b2ab-4ac8964e6027")
        (() => mockUuidGenerator.generate).expects().returning(newId)

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(BigDecimal("100"))),
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
                class2Or3NIContributionAmount = Some(Class2Or3NIContributionAmount(BigDecimal("99999999999999.98"))),
                class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
                creditSource = Some(CreditSource.NotKnown),
                latePaymentPeriod = Some(LatePaymentPeriod.L),
                receiptDate = Some(ReceiptDate(LocalDate.parse("2025-10-10")))
              )
            )
          )
        )

        server.stubFor(
          post(urlEqualTo(npsCreditsAndContributionsPath))
            .willReturn(
              aResponse()
                .withStatus(OK)
                .withHeader("Content-Type", "application/json")
                .withBody(Json.toJson(niContributionsAndCreditsSuccessResponse).toString)
            )
        )

        service
          .paginate(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuidFour)))
          .value
          .futureValue shouldBe
          Right(
            PaginationResult(
              correlationId = correlationId,
              paginationType = PaginationType.BspPagination,
              nationalInsuranceNumber = nationalInsuranceNumber,
              liabilitiesResult = List(),
              class2MaReceiptsResult = None,
              marriageDetailsResult = None,
              contributionCreditResult = ContributionCreditPagingResult(
                contributionCreditResult =
                  Some(NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)),
                contributionAndCreditsPaging = None
              ),
              benefitSchemeMembershipDetailsData = None,
              callSystem = Some(SEARCHLIGHT),
              nextCursor = None
            )
          )

        findAll().futureValue should contain theSameElementsAs newListOfPages
      }
    }
  }

}
