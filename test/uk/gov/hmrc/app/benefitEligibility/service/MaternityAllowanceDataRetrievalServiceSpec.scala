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
import org.scalatest.matchers.should.Matchers.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.UnprocessableEntity
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  Class2MaReceipts,
  ContributionsAndCredits,
  MAEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector.Class2MAReceiptsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.connector.LiabilitySummaryDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated.Abroad
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class MaternityAllowanceDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val mockClass2MAReceiptsConnector: Class2MAReceiptsConnector =
    mock[Class2MAReceiptsConnector]

  val mockLiabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector = mock[LiabilitySummaryDetailsConnector]

  val underTest = new MaternityAllowanceDataRetrievalService(
    mockClass2MAReceiptsConnector,
    mockNiContributionsAndCreditsConnector,
    mockLiabilitySummaryDetailsConnector
  )

  val identifier = Identifier("GD379251T")

  private val niContributionsAndCreditsRequest = NiContributionsAndCreditsRequest(
    nationalInsuranceNumber = identifier,
    dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2026)
  )

  private val eligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
    Identifier("GD379251T"),
    ContributionsAndCredits(
      DateOfBirth(LocalDate.parse("2025-10-10")),
      StartTaxYear(2025),
      EndTaxYear(2026)
    ),
    uk.gov.hmrc.app.benefitEligibility.integration.inbound.request
      .Liabilities(Abroad, None, None, None, None, None),
    Class2MaReceipts(None, None, None)
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
          primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
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

  val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
    identifier = Identifier("AB123456C"),
    class2MAReceiptDetails = List()
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
          endDate = Some(EndDate(LocalDate.parse("2026-01-01"))),
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

  "MaternityAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should return an EligibilityCheckDataResult (all successful nps calls)" in {
        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        val class2MAReceiptsResult = SuccessResult(
          ApiName.Class2MAReceipts,
          class2MAReceiptsSuccessResponse
        )

        val liabilitySummaryDetailsResult = SuccessResult(
          ApiName.Liabilities,
          liabilitySummaryDetailsSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.MA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: BenefitType,
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(class2MAReceiptsResult)
          )

        (mockLiabilitySummaryDetailsConnector
          .fetchLiabilitySummaryDetails(
            _: BenefitType,
            _: Identifier,
            _: LiabilitySearchCategoryHyphenated,
            _: Option[LiabilitiesOccurrenceNumber],
            _: Option[LiabilitySearchCategoryHyphenated],
            _: Option[LocalDate],
            _: Option[LocalDate],
            _: Option[LocalDate]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, Abroad, None, None, None, None, None, *)
          .returning(
            EitherT.rightT(liabilitySummaryDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultMA(
            class2MAReceiptsResult,
            liabilitySummaryDetailsResult,
            niContributionAndCreditsResult
          )
        )

      }
      "should return an EligibilityCheckDataResult (both successful and unsuccessful nps call)" in {

        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        val class2MAReceiptsResult = FailureResult(
          ApiName.Class2MAReceipts,
          ErrorReport(UnprocessableEntity, None)
        )

        val liabilitySummaryDetailsResult = SuccessResult(
          ApiName.Liabilities,
          liabilitySummaryDetailsSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.MA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: BenefitType,
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(class2MAReceiptsResult)
          )

        (mockLiabilitySummaryDetailsConnector
          .fetchLiabilitySummaryDetails(
            _: BenefitType,
            _: Identifier,
            _: LiabilitySearchCategoryHyphenated,
            _: Option[LiabilitiesOccurrenceNumber],
            _: Option[LiabilitySearchCategoryHyphenated],
            _: Option[LocalDate],
            _: Option[LocalDate],
            _: Option[LocalDate]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, Abroad, None, None, None, None, None, *)
          .returning(
            EitherT.rightT(liabilitySummaryDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultMA(
            class2MAReceiptsResult,
            liabilitySummaryDetailsResult,
            niContributionAndCreditsResult
          )
        )

      }
      "should return an EligibilityCheckDataResult (all unsuccessful nps call)" in {

        val niContributionAndCreditsResult = FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(UnprocessableEntity, None)
        )

        val class2MAReceiptsResult = FailureResult(
          ApiName.Class2MAReceipts,
          ErrorReport(UnprocessableEntity, None)
        )

        val liabilitySummaryDetailsResult = FailureResult(
          ApiName.Liabilities,
          ErrorReport(UnprocessableEntity, None)
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.MA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: BenefitType,
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(class2MAReceiptsResult)
          )

        (mockLiabilitySummaryDetailsConnector
          .fetchLiabilitySummaryDetails(
            _: BenefitType,
            _: Identifier,
            _: LiabilitySearchCategoryHyphenated,
            _: Option[LiabilitiesOccurrenceNumber],
            _: Option[LiabilitySearchCategoryHyphenated],
            _: Option[LocalDate],
            _: Option[LocalDate],
            _: Option[LocalDate]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, Abroad, None, None, None, None, None, *)
          .returning(
            EitherT.rightT(liabilitySummaryDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultMA(
            class2MAReceiptsResult,
            liabilitySummaryDetailsResult,
            niContributionAndCreditsResult
          )
        )
      }

      "should return a DataRetrievalServiceError if the service fails to retrieve results for a subset the NPS APIs called" in {

        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.MA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: BenefitType,
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, None, None, None, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockLiabilitySummaryDetailsConnector
          .fetchLiabilitySummaryDetails(
            _: BenefitType,
            _: Identifier,
            _: LiabilitySearchCategoryHyphenated,
            _: Option[LiabilitiesOccurrenceNumber],
            _: Option[LiabilitySearchCategoryHyphenated],
            _: Option[LocalDate],
            _: Option[LocalDate],
            _: Option[LocalDate]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, Abroad, None, None, None, None, None, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          DataRetrievalServiceError()
        )
      }

      "should return a DataRetrievalServiceError if the service fails to retrieve results for all the NPS APIs called" in {

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.MA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: BenefitType,
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, None, None, None, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockLiabilitySummaryDetailsConnector
          .fetchLiabilitySummaryDetails(
            _: BenefitType,
            _: Identifier,
            _: LiabilitySearchCategoryHyphenated,
            _: Option[LiabilitiesOccurrenceNumber],
            _: Option[LiabilitySearchCategoryHyphenated],
            _: Option[LocalDate],
            _: Option[LocalDate],
            _: Option[LocalDate]
          )(_: HeaderCarrier))
          .expects(BenefitType.MA, identifier, Abroad, None, None, None, None, None, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          DataRetrievalServiceError()
        )
      }
    }
  }

}
