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
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.BadRequest
import uk.gov.hmrc.app.benefitEligibility.connectors.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.ContributionsAndCreditsRequestParams
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultSearchLight
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.{
  Class1ContributionAndCredits,
  Class2Or3EarningsFactor,
  Class2Or3NIContributionAmount,
  Class2or3ContributionAndCredits,
  ContributionCategoryLetter,
  EmployerName,
  NiContributionsAndCreditsSuccessResponse,
  NumberOfCreditsAndContributions,
  PrimaryContribution,
  PrimaryPaidEarnings,
  TotalGraduatedPensionUnits
}
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  ApiName,
  BenefitType,
  CorrelationId,
  DateOfBirth,
  EndTaxYear,
  Identifier,
  InvalidJsonError,
  JsonValidationError,
  NpsClientError,
  ReceiptDate,
  StartTaxYear,
  TaxYear
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  CreditSource,
  LatePaymentPeriod,
  NiContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.model.request.SearchlightEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDate}
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class SearchlightDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val mockPaginationService: PaginationService = mock[PaginationService]
  val mockUUIDService: UuidGeneratorService    = mock[UuidGeneratorService]

  val testInstant: Instant = Instant.parse("2007-12-03T10:15:30.00Z")

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = testInstant
  }

  val underTest = new SearchlightDataRetrievalService(
    mockNiContributionsAndCreditsConnector,
    mockPaginationService,
    mockUUIDService,
    currentTimeSource
  )

  private val niContributionsAndCreditsRequest = NiContributionsAndCreditsRequest(
    nationalInsuranceNumber = Identifier("GD379251T"),
    dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2025)
  )

  private val eligibilityCheckDataRequest = SearchlightEligibilityCheckDataRequest(
    benefitType = BSP,
    nationalInsuranceNumber = Identifier("GD379251T"),
    niContributionsAndCredits = ContributionsAndCreditsRequestParams(
      dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
      startTaxYear = StartTaxYear(2025),
      endTaxYear = EndTaxYear(2025)
    )
  )

  "SearchlightDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should return an EligibilityCheckDataResult (successful nps call)" in {

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

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(
              SuccessResult(
                ApiName.NiContributionAndCredits,
                niContributionsAndCreditsSuccessResponse
              )
            )
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultSearchLight(
            BenefitType.BSP,
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse),
            None
          )
        )

      }
      "should return an EligibilityCheckDataResult (unsuccessful nps call)" in {

        val result = FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(BadRequest, None)
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(result)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultSearchLight(BenefitType.BSP, result, None)
        )

      }

      "should propagate the error returned from the connector (ValidationError)" in {
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(JsonValidationError(List.empty))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          JsonValidationError(List.empty)
        )
      }

      "should propagate the error returned from the connector (ParsingError)" in {

        val error = new RuntimeException()
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(InvalidJsonError(error))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          InvalidJsonError(error)
        )
      }

      "should propagate the error returned from the connector (NpsClientError)" in {
        val error = new RuntimeException()
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(error))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          NpsClientError(error)
        )
      }
    }
  }

}
