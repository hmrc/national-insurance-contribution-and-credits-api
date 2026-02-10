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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  ContributionsAndCredits,
  JSAEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultJSA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.{
  Class1ContributionAndCredits,
  Class2ContributionAndCredits,
  Class2NIContributionAmount,
  Class2Or3EarningsFactor,
  ContributionCategoryLetter,
  EmployerName,
  NiContributionsAndCreditsSuccessResponse,
  NumberOfCreditsAndContributions,
  PrimaryContribution,
  PrimaryPaidEarnings,
  TotalGraduatedPensionUnits
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  CreditSource,
  LatePaymentPeriod,
  NiContributionCreditType
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{EligibilityCheckDataResult, NpsApiResult}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class JobSeekersAllowanceDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val underTest = new JobSeekersAllowanceDataRetrievalService(mockNiContributionsAndCreditsConnector)

  private val niContributionsAndCreditsRequest = NiContributionsAndCreditsRequest(
    nationalInsuranceNumber = Identifier("GD379251T"),
    dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2025)
  )

  private val eligibilityCheckDataRequest = JSAEligibilityCheckDataRequest(
    nationalInsuranceNumber = Identifier("GD379251T"),
    niContributionsAndCredits = ContributionsAndCredits(
      dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
      startTaxYear = StartTaxYear(2025),
      endTaxYear = EndTaxYear(2025)
    )
  )

  private val emptyThrowable = Throwable()

  "JobSeekersAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should retrieve class 1 data successfully" in {

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
          class2ContributionAndCredits = None
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
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
          EligibilityCheckDataResultJSA(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

      }

      "should retrieve class 2 data successfully" in {

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          Some(TotalGraduatedPensionUnits(53)),
          class1ContributionAndCredits = None,
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

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
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
          EligibilityCheckDataResultJSA(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

      }

      "should retrieve empty data successfully" in {

        val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
          totalGraduatedPensionUnits = None,
          class1ContributionAndCredits = None,
          class2ContributionAndCredits = None
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
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
          EligibilityCheckDataResultJSA(
            NpsApiResult.SuccessResult(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
          )
        )

      }

      "should propagate the error returned from the connector (ValidationError)" in {
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(ValidationError(List.empty))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          ValidationError(List.empty)
        )
      }

      "should propagate the error returned from the connector (ParsingError)" in {
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(ParsingError(emptyThrowable))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          ParsingError(emptyThrowable)
        )
      }

      "should propagate the error returned from the connector (NpsClientError)" in {
        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.JSA, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(emptyThrowable))
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Left(
          NpsClientError(emptyThrowable)
        )
      }
    }
  }

}
