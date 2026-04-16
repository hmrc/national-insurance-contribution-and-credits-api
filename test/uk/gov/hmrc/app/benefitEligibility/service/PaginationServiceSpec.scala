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

import cats.data.{EitherT, NonEmptyList}
import com.mongodb.{ServerAddress, WriteConcernResult}
import org.mongodb.scala.bson.BsonDocument
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import uk.gov.hmrc.app.benefitEligibility.connectors.*
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.model.common.CallSystem.SEARCHLIGHT
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.SuccessResult
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.marriageDetails.MarriageDetailsSuccess.{
  ActiveMarriage,
  MarriageDetails,
  MarriageDetailsSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.enums.*
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
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDate}
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class PaginationServiceSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  private val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = Instant.parse("2007-12-03T10:15:30.00Z")
  }

  val mockUuidGenerator: UuidGenerator                                       = mock[UuidGenerator]
  val mockLiabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector = mock[LiabilitySummaryDetailsConnector]

  val mockClass2MAReceiptsConnector: Class2MAReceiptsConnector = mock[Class2MAReceiptsConnector]

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val mockMarriageDetailsConnector: MarriageDetailsConnector = mock[MarriageDetailsConnector]

  val mockSchemeMembershipDetailsConnector: SchemeMembershipDetailsConnector = mock[SchemeMembershipDetailsConnector]

  val mockBenefitSchemeDetailsConnector: BenefitSchemeDetailsConnector = mock[BenefitSchemeDetailsConnector]

  val mockBenefitEligibilityRepository: BenefitEligibilityRepository = mock[BenefitEligibilityRepository]

  val nationalInsuranceNumber = Identifier("AB123456C")

  val underTest = new PaginationService(
    liabilitySummaryDetailsConnector = mockLiabilitySummaryDetailsConnector,
    class2MAReceiptsConnector = mockClass2MAReceiptsConnector,
    niContributionsAndCreditsConnector = mockNiContributionsAndCreditsConnector,
    marriageDetailsConnector = mockMarriageDetailsConnector,
    schemeMembershipDetailsConnector = mockSchemeMembershipDetailsConnector,
    benefitSchemeDetailsConnector = mockBenefitSchemeDetailsConnector,
    pageTaskRepo = mockBenefitEligibilityRepository,
    currentTime = currentTimeSource,
    uuidGenerator = mockUuidGenerator
  )

  "PaginationService" - {
    ".addTask" - {
      "should successfully add a new pageTask" in {
        val pageTaskId1       = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val paginationSource3 = List(PaginationSource(ApiName.MarriageDetails, "SomeCallBackURLThree"))

        val pageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId1,
          liabilitiesPaging = paginationSource3,
          None,
          nationalInsuranceNumber,
          Instant.now
        )

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.rightT(pageTask.pageTaskId.value))

        underTest.addTask(pageTask).value.futureValue shouldBe Right(
          UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        )
      }
      "should return a Benefit eligibility error if upsert fails" in {
        val pageTaskId1       = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val paginationSource3 = List(PaginationSource(ApiName.MarriageDetails, "SomeCallBackURLThree"))

        val pageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId1,
          liabilitiesPaging = paginationSource3,
          None,
          nationalInsuranceNumber,
          Instant.now
        )

        val error = new RuntimeException()
        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.leftT(DatabaseError(error)))

        underTest.addTask(pageTask).value.futureValue shouldBe Left(DatabaseError(error))

      }
      "should return a new uuid if current uuid already exists in database for MA" in {
        val uuidOne           = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val uuidTwo           = PageTaskId(UUID.fromString("2db75f56-9975-4a8d-b315-85ef3fac2161"))
        val paginationSource3 = List(PaginationSource(ApiName.MarriageDetails, "SomeCallBackURLThree"))

        val pageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidOne,
          liabilitiesPaging = paginationSource3,
          None,
          nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val newPageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidTwo,
          liabilitiesPaging = paginationSource3,
          None,
          nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val serverAddress              = new ServerAddress()
        val errorDetails: BsonDocument = new BsonDocument()
        val writeConcernResult         = WriteConcernResult.acknowledged(1, true, null)
        val error = new com.mongodb.DuplicateKeyException(errorDetails, serverAddress, writeConcernResult)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.leftT(DatabaseError(error)))
          .noMoreThanOnce()

        // Note: This verifies that a new uuid is being generated if we get a duplicate key error
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo.value)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(newPageTask, *)
          .returning(EitherT.rightT(newPageTask.pageTaskId.value))
          .noMoreThanOnce()

        underTest.addTask(pageTask).value.futureValue
      }
      "should return a new uuid if current uuid already exists in database for BSP" in {
        val uuidOne           = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val uuidTwo           = PageTaskId(UUID.fromString("2db75f56-9975-4a8d-b315-85ef3fac2161"))
        val paginationSource3 = Some(PaginationSource(ApiName.MarriageDetails, "SomeCallBackURLThree"))

        val pageTask = BspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidOne,
          marriageDetailsPaging = paginationSource3,
          contributionAndCreditsPaging = None,
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val newPageTask = BspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidTwo,
          marriageDetailsPaging = paginationSource3,
          contributionAndCreditsPaging = None,
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val serverAddress              = new ServerAddress()
        val errorDetails: BsonDocument = new BsonDocument()
        val writeConcernResult         = WriteConcernResult.acknowledged(1, true, null)
        val error = new com.mongodb.DuplicateKeyException(errorDetails, serverAddress, writeConcernResult)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.leftT(DatabaseError(error)))
          .noMoreThanOnce()

        // Note: This verifies that a new uuid is being generated if we get a duplicate key error
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo.value)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(newPageTask, *)
          .returning(EitherT.rightT(newPageTask.pageTaskId.value))
          .noMoreThanOnce()

        underTest.addTask(pageTask).value.futureValue
      }
      "should return a new uuid if current uuid already exists in database for GYSP" in {
        val uuidOne           = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val uuidTwo           = PageTaskId(UUID.fromString("2db75f56-9975-4a8d-b315-85ef3fac2161"))
        val paginationSource3 = Some(PaginationSource(ApiName.MarriageDetails, "SomeCallBackURLThree"))

        val pageTask = GyspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidOne,
          benefitSchemeMembershipDetailsPaging = paginationSource3,
          marriageDetailsPaging = None,
          contributionAndCreditsPaging = None,
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val newPageTask = GyspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidTwo,
          benefitSchemeMembershipDetailsPaging = paginationSource3,
          marriageDetailsPaging = None,
          contributionAndCreditsPaging = None,
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val serverAddress              = new ServerAddress()
        val errorDetails: BsonDocument = new BsonDocument()
        val writeConcernResult         = WriteConcernResult.acknowledged(1, true, null)
        val error = new com.mongodb.DuplicateKeyException(errorDetails, serverAddress, writeConcernResult)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.leftT(DatabaseError(error)))
          .noMoreThanOnce()

        // Note: This verifies that a new uuid is being generated if we get a duplicate key error
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo.value)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(newPageTask, *)
          .returning(EitherT.rightT(newPageTask.pageTaskId.value))
          .noMoreThanOnce()

        underTest.addTask(pageTask).value.futureValue
      }
      "should return a new uuid if current uuid already exists in database for SearchLight" in {
        val uuidOne = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val uuidTwo = PageTaskId(UUID.fromString("2db75f56-9975-4a8d-b315-85ef3fac2161"))
        val dob     = DateOfBirth(LocalDate.parse("2025-10-10"))
        val paginationSource2 =
          ContributionAndCreditsPaging(
            NonEmptyList
              .of(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)), TaxWindow(StartTaxYear(2021), EndTaxYear(2025))),
            dob
          )
        val pageTask = SearchLightPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidOne,
          paginationType = PaginationType.MaPagination,
          contributionAndCreditsPaging = Some(paginationSource2),
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val newPageTask = SearchLightPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = uuidTwo,
          paginationType = PaginationType.MaPagination,
          contributionAndCreditsPaging = Some(paginationSource2),
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = currentTimeSource.instantNow()
        )
        val serverAddress              = new ServerAddress()
        val errorDetails: BsonDocument = new BsonDocument()
        val writeConcernResult         = WriteConcernResult.acknowledged(1, true, null)
        val error = new com.mongodb.DuplicateKeyException(errorDetails, serverAddress, writeConcernResult)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(pageTask, *)
          .returning(EitherT.leftT(DatabaseError(error)))
          .noMoreThanOnce()

        // Note: This verifies that a new uuid is being generated if we get a duplicate key error
        (() => mockUuidGenerator.generate).expects().returning(uuidTwo.value)

        (mockBenefitEligibilityRepository
          .insert(_: PageTask)(_: HeaderCarrier))
          .expects(newPageTask, *)
          .returning(EitherT.rightT(newPageTask.pageTaskId.value))
          .noMoreThanOnce()

        underTest.addTask(pageTask).value.futureValue
      }
    }
    ".paginate" - {
      "should return pagination result for MA" in {
        val uuid                        = UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        val pageTaskId                  = PageTaskId(uuid)
        val liabilitiesCallBackUrl      = "SomeCallBackURL1"
        val class2MAReceiptsCallBackUrl = "SomeCallBackURL2"
        val paginationSource1           = List(PaginationSource(ApiName.Liabilities, liabilitiesCallBackUrl))
        val paginationSource2           = PaginationSource(Class2MAReceipts, class2MAReceiptsCallBackUrl)
        val nationalInsuranceNumber     = Identifier("GD379251T")

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val pageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId,
          liabilitiesPaging = paginationSource1,
          class2MaReceipts = Some(paginationSource2),
          nationalInsuranceNumber = nationalInsuranceNumber,
          createdAt = Instant.now
        )

        (() => mockUuidGenerator.generate).expects().returning(uuid)
        val liabilitiesSuccessResponse =
          LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl(liabilitiesCallBackUrl)))))

        val class2MAReceiptsSuccessResponse =
          Class2MAReceiptsSuccessResponse(None, None, Some(Callback(Some(CallbackUrl(class2MAReceiptsCallBackUrl)))))

        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId), *)
          .returning(EitherT.rightT(pageTask))

        (mockLiabilitySummaryDetailsConnector
          .fetchData(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.MA, liabilitiesCallBackUrl, *)
          .returning(EitherT.rightT(NpsApiResult.SuccessResult(ApiName.Liabilities, liabilitiesSuccessResponse)))

        (mockClass2MAReceiptsConnector
          .fetchData(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.MA, class2MAReceiptsCallBackUrl, *)
          .returning(
            EitherT.rightT(NpsApiResult.SuccessResult(ApiName.Class2MAReceipts, class2MAReceiptsSuccessResponse))
          )

        (mockBenefitEligibilityRepository
          .upsert(_: Option[UUID], _: PageTask)(_: HeaderCarrier))
          .expects(Some(pageTask.pageTaskId.value), *, *)
          .returning(EitherT.rightT(uuid))

        val expectedResult = PaginationResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          paginationType = PaginationType.MaPagination,
          nationalInsuranceNumber = nationalInsuranceNumber,
          liabilitiesResult = List(
            SuccessResult(
              ApiName.Liabilities,
              LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl(liabilitiesCallBackUrl)))))
            )
          ),
          class2MaReceiptsResult = Some(
            SuccessResult(
              ApiName.Class2MAReceipts,
              Class2MAReceiptsSuccessResponse(
                None,
                None,
                Some(Callback(Some(CallbackUrl(class2MAReceiptsCallBackUrl))))
              )
            )
          ),
          marriageDetailsResult = None,
          contributionCreditResult = ContributionCreditPagingResult(None, None),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          nextCursor = Some(
            PaginationCursor(
              PaginationType.MaPagination,
              PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
            )
          )
        )
        underTest
          .paginate(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId))
          .value
          .futureValue shouldBe Right(
          expectedResult
        )
      }
      "should return pagination result for BSP" in {
        val uuid                               = UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        val pageTaskId                         = PageTaskId(uuid)
        val marriageDetailsCallBackUrl: String = "SomeCallBackURL1"
        val nationalInsuranceNumber            = Identifier("GD379251T")
        val dob                                = DateOfBirth(LocalDate.parse("2025-10-10"))

        val paginationSource1 = PaginationSource(ApiName.MarriageDetails, marriageDetailsCallBackUrl)
        val paginationSource2 =
          ContributionAndCreditsPaging(
            NonEmptyList
              .of(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)), TaxWindow(StartTaxYear(2021), EndTaxYear(2025))),
            dob
          )

        val marriageDetailsSuccessResponse =
          MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))

        val niContributionsAndCreditsRequest: NiContributionsAndCreditsRequest =
          NiContributionsAndCreditsRequest(nationalInsuranceNumber, dob, StartTaxYear(2015), EndTaxYear(2020))

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(None, None, None)

        val pageTask = BspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId,
          marriageDetailsPaging = Some(paginationSource1),
          contributionAndCreditsPaging = Some(paginationSource2),
          nationalInsuranceNumber,
          Instant.now
        )

        (() => mockUuidGenerator.generate).expects().returning(uuid)
        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId), *)
          .returning(EitherT.rightT(pageTask))
        (mockBenefitEligibilityRepository
          .upsert(_: Option[UUID], _: PageTask)(_: HeaderCarrier))
          .expects(Some(pageTask.pageTaskId.value), *, *)
          .returning(EitherT.rightT(uuid))

        (mockMarriageDetailsConnector
          .fetchMarriageDetailsData(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.from(pageTask.paginationType), marriageDetailsCallBackUrl, *)
          .returning(
            EitherT.rightT(NpsApiResult.SuccessResult(ApiName.MarriageDetails, marriageDetailsSuccessResponse))
          )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest, _: Option[CallSystem])(
            _: HeaderCarrier
          ))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, None, *)
          .returning(
            EitherT.rightT(
              NpsApiResult.SuccessResult(ApiName.NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
            )
          )

        val expected = PaginationResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          paginationType = PaginationType.BspPagination,
          nationalInsuranceNumber = nationalInsuranceNumber,
          liabilitiesResult = List(),
          class2MaReceiptsResult = None,
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))
            )
          ),
          contributionCreditResult = ContributionCreditPagingResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(ContributionAndCreditsPaging(NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2025))), dob))
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          nextCursor = Some(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuid)))
        )

        underTest
          .paginate(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId))
          .value
          .futureValue shouldBe Right(expected)
      }
      "should return pagination result for SEARCHLIGHT" in {
        val uuid                    = UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        val pageTaskId              = PageTaskId(uuid)
        val nationalInsuranceNumber = Identifier("GD379251T")
        val dob                     = DateOfBirth(LocalDate.parse("2025-10-10"))

        val paginationSource =
          ContributionAndCreditsPaging(
            NonEmptyList
              .of(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)), TaxWindow(StartTaxYear(2021), EndTaxYear(2025))),
            dob
          )

        val niContributionsAndCreditsRequest: NiContributionsAndCreditsRequest =
          NiContributionsAndCreditsRequest(nationalInsuranceNumber, dob, StartTaxYear(2015), EndTaxYear(2020))

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(None, None, None)

        val pageTask = SearchLightPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId,
          paginationType = PaginationType.BspPagination,
          contributionAndCreditsPaging = Some(paginationSource),
          nationalInsuranceNumber,
          Instant.now
        )

        (() => mockUuidGenerator.generate).expects().returning(uuid)
        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId), *)
          .returning(EitherT.rightT(pageTask))
        (mockBenefitEligibilityRepository
          .upsert(_: Option[UUID], _: PageTask)(_: HeaderCarrier))
          .expects(Some(pageTask.pageTaskId.value), *, *)
          .returning(EitherT.rightT(uuid))

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest, _: Option[CallSystem])(
            _: HeaderCarrier
          ))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, None, *)
          .returning(
            EitherT.rightT(
              NpsApiResult.SuccessResult(ApiName.NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
            )
          )

        val expected = PaginationResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          paginationType = PaginationType.BspPagination,
          nationalInsuranceNumber = nationalInsuranceNumber,
          liabilitiesResult = List(),
          class2MaReceiptsResult = None,
          marriageDetailsResult = None,
          contributionCreditResult = ContributionCreditPagingResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(ContributionAndCreditsPaging(NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2025))), dob))
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = Some(SEARCHLIGHT),
          nextCursor = Some(PaginationCursor(PaginationType.BspPagination, PageTaskId(uuid)))
        )

        underTest
          .paginate(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId))
          .value
          .futureValue shouldBe Right(expected)
      }
      "should return pagination result for GYSP" in {
        val uuid       = UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        val pageTaskId = PageTaskId(uuid)

        val marriageDetailsCallBackUrl: String = "SomeCallBackURL1"
        val BenefitSchemeCallBackUrl: String   = "SomeCallBackURL3"
        val nationalInsuranceNumber            = Identifier("GD379251T")
        val dob                                = DateOfBirth(LocalDate.parse("2025-10-10"))

        val schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")

        val paginationSource1 = PaginationSource(ApiName.MarriageDetails, marriageDetailsCallBackUrl)

        val paginationSource3 = PaginationSource(ApiName.BenefitSchemeDetails, BenefitSchemeCallBackUrl)

        val marriageDetailsSuccessResponse =
          MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))

        val niContributionsAndCreditsRequest: NiContributionsAndCreditsRequest =
          NiContributionsAndCreditsRequest(nationalInsuranceNumber, dob, StartTaxYear(2015), EndTaxYear(2020))

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(None, None, None)

        val schemeMembershipDetails =
          SchemeMembershipDetails(
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
            employersContractedOutNumberDetails =
              Some(EmployersContractedOutNumberDetails(schemeContractedOutNumberDetails.value)),
            schemeCreatingContractedOutNumberDetails = Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
            schemeTerminatingContractedOutNumberDetails =
              Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
            importingAppropriateSchemeNumberDetails = Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
            apparentUnnotifiedTerminationDestinationDetails =
              Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
          )

        val benefitSchemeDetails: BenefitSchemeDetails =
          BenefitSchemeDetails(
            currentOptimisticLock = CurrentOptimisticLock(4),
            schemeContractedOutNumberDetails = schemeContractedOutNumberDetails,
            magneticTapeNumber = None,
            schemeName = None,
            schemeStartDate = None,
            schemeCessationDate = None,
            contractedOutDeductionExtinguishedDate = None,
            paymentSuspensionDate = None,
            recoveriesSuspendedDate = None,
            paymentRestartDate = None,
            recoveriesRestartedDate = None,
            schemeNature = None,
            benefitSchemeInstitution = None,
            accruedGMPLiabilityServiceDate = None,
            rerouteToSchemeCessation = None,
            statementInhibitor = None,
            certificateCancellationDate = None,
            suspendedDate = None,
            isleOfManInterest = None,
            schemeWindingUp = None,
            revaluationRateSequenceNumber = None,
            benefitSchemeStatus = None,
            dateFormallyCertified = None,
            privatePensionSchemeSanctionDate = None,
            schemeConversionDate = None,
            reconciliationDate = None
          )

        val schemeMembershipDetailsSuccessResponse =
          SchemeMembershipDetailsSuccessResponse(
            Some(
              List(
                SchemeMembershipDetailsSummary(
                  StakeholderPensionSchemeType.NonStakeholderPension,
                  schemeMembershipDetails
                )
              )
            ),
            None
          )

        val benefitSchemeDetailsSuccessResponse =
          BenefitSchemeDetailsSuccessResponse(benefitSchemeDetails, List())

        val pageTask = GyspPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId,
          benefitSchemeMembershipDetailsPaging = Some(paginationSource3),
          marriageDetailsPaging = Some(paginationSource1),
          contributionAndCreditsPaging = Some(
            ContributionAndCreditsPaging(
              NonEmptyList
                .of(TaxWindow(StartTaxYear(2015), EndTaxYear(2020)), TaxWindow(StartTaxYear(2021), EndTaxYear(2025))),
              DateOfBirth(LocalDate.parse("2025-10-10"))
            )
          ),
          nationalInsuranceNumber,
          Instant.now
        )

        (() => mockUuidGenerator.generate).expects().returning(uuid)
        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId), *)
          .returning(EitherT.rightT(pageTask))
        (mockBenefitEligibilityRepository
          .upsert(_: Option[UUID], _: PageTask)(_: HeaderCarrier))
          .expects(Some(pageTask.pageTaskId.value), *, *)
          .returning(EitherT.rightT(uuid))

        (mockMarriageDetailsConnector
          .fetchMarriageDetailsData(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.from(pageTask.paginationType), marriageDetailsCallBackUrl, *)
          .returning(
            EitherT.rightT(NpsApiResult.SuccessResult(ApiName.MarriageDetails, marriageDetailsSuccessResponse))
          )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest, _: Option[CallSystem])(
            _: HeaderCarrier
          ))
          .expects(BenefitType.from(pageTask.paginationType), niContributionsAndCreditsRequest, None, *)
          .returning(
            EitherT.rightT(
              NpsApiResult.SuccessResult(ApiName.NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
            )
          )

        (mockSchemeMembershipDetailsConnector
          .fetchData(_: BenefitType, _: String)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, *, *)
          .returning(
            EitherT.rightT(
              NpsApiResult.SuccessResult(ApiName.NiContributionAndCredits, schemeMembershipDetailsSuccessResponse)
            )
          )

        (mockBenefitSchemeDetailsConnector
          .fetchBenefitSchemeDetails(_: BenefitType, _: Identifier, _: SchemeContractedOutNumberDetails)(
            _: HeaderCarrier
          ))
          .expects(BenefitType.GYSP, nationalInsuranceNumber, schemeContractedOutNumberDetails, *)
          .returning(
            EitherT.rightT(
              NpsApiResult.SuccessResult(ApiName.NiContributionAndCredits, benefitSchemeDetailsSuccessResponse)
            )
          )

        val expected = PaginationResult(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          paginationType = PaginationType.GyspPagination,
          nationalInsuranceNumber = nationalInsuranceNumber,
          liabilitiesResult = List(),
          class2MaReceiptsResult = None,
          marriageDetailsResult = Some(
            SuccessResult(
              ApiName.MarriageDetails,
              MarriageDetailsSuccessResponse(MarriageDetails(ActiveMarriage(true), None, None))
            )
          ),
          contributionCreditResult = ContributionCreditPagingResult(
            Some(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                NiContributionsAndCreditsSuccessResponse(None, None, None)
              )
            ),
            Some(ContributionAndCreditsPaging(NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2025))), dob))
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
          nextCursor = Some(PaginationCursor(PaginationType.GyspPagination, pageTaskId))
        )

        underTest
          .paginate(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId))
          .value
          .futureValue shouldBe Right(expected)
      }
      "should error if connector call fails" in {
        val uuid                    = UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")
        val pageTaskId              = PageTaskId(uuid)
        val liabilitiesCallBackUrl  = "SomeCallBackURL1"
        val paginationSource1       = List(PaginationSource(ApiName.Liabilities, liabilitiesCallBackUrl))
        val nationalInsuranceNumber = Identifier("GD379251T")

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val pageTask = MaPageTask(
          correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
          pageTaskId = pageTaskId,
          liabilitiesPaging = paginationSource1,
          None,
          nationalInsuranceNumber,
          Instant.now
        )

        val error = new RuntimeException()
        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId), *)
          .returning(EitherT.rightT(pageTask))

        (mockLiabilitySummaryDetailsConnector
          .fetchData(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.MA, liabilitiesCallBackUrl, *)
          .returning(EitherT.leftT(NpsClientError(error)))

        underTest
          .paginate(PaginationCursor(pageTask.paginationType, pageTask.pageTaskId))
          .value
          .futureValue shouldBe Left(
          NpsClientError(error)
        )
      }
      "should error if database returns error" in {
        val paginationCursor =
          PaginationCursor(
            PaginationType.BspPagination,
            PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
          )

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val error = new RuntimeException()
        (mockBenefitEligibilityRepository
          .getItem(_: PaginationCursor)(_: HeaderCarrier))
          .expects(paginationCursor, *)
          .returning(EitherT.leftT(DatabaseError(error)))

        underTest.paginate(paginationCursor).value.futureValue shouldBe Left(
          DatabaseError(error)
        )
      }
    }
  }

}
