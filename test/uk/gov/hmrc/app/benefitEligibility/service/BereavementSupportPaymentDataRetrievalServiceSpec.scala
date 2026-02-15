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
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  BSPEligibilityCheckDataRequest,
  ContributionsAndCreditsRequestParams
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultBSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStatus.CivilPartner
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

class BereavementSupportPaymentDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockNiContributionsAndCreditsConnector: NiContributionsAndCreditsConnector =
    mock[NiContributionsAndCreditsConnector]

  val mockMarriageDetailsConnector: MarriageDetailsConnector =
    mock[MarriageDetailsConnector]

  val underTest = new BereavementSupportPaymentDataRetrievalService(
    mockNiContributionsAndCreditsConnector,
    mockMarriageDetailsConnector
  )

  val identifier = Identifier("GD379251T")

  private val niContributionsAndCreditsRequest = NiContributionsAndCreditsRequest(
    nationalInsuranceNumber = identifier,
    dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2025)
  )

  private val eligibilityCheckDataRequest = BSPEligibilityCheckDataRequest(
    nationalInsuranceNumber = Identifier("GD379251T"),
    niContributionsAndCredits = ContributionsAndCreditsRequestParams(
      dateOfBirth = DateOfBirth(LocalDate.parse("2025-10-10")),
      startTaxYear = StartTaxYear(2025),
      endTaxYear = EndTaxYear(2025)
    ),
    marriageDetails = None
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

  "BereavementSupportPaymentDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should return an EligibilityCheckDataResult (all successful nps calls)" in {
        val niContributionAndCreditsResult = SuccessResult(
          ApiName.NiContributionAndCredits,
          niContributionsAndCreditsSuccessResponse
        )

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[StartYear],
            _: Option[FilterLatest],
            _: Option[Int]
          )(_: HeaderCarrier))
          .expects(BenefitType.BSP, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultBSP(niContributionAndCreditsResult, marriageDetailsResult)
        )

      }
      "should return an EligibilityCheckDataResult (both successful and unsuccessful nps call)" in {

        val niContributionAndCreditsResult = FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(BadRequest, None)
        )

        val marriageDetailsResult = SuccessResult(
          ApiName.MarriageDetails,
          marriageDetailsSuccessResponse
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[StartYear],
            _: Option[FilterLatest],
            _: Option[Int]
          )(_: HeaderCarrier))
          .expects(BenefitType.BSP, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultBSP(niContributionAndCreditsResult, marriageDetailsResult)
        )

      }
      "should return an EligibilityCheckDataResult (all unsuccessful nps call)" in {

        val niContributionAndCreditsResult = FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(BadRequest, None)
        )

        val marriageDetailsResult = FailureResult(
          ApiName.MarriageDetails,
          ErrorReport(UnprocessableEntity, None)
        )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[StartYear],
            _: Option[FilterLatest],
            _: Option[Int]
          )(_: HeaderCarrier))
          .expects(BenefitType.BSP, identifier, None, None, None, *)
          .returning(
            EitherT.rightT(marriageDetailsResult)
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultBSP(niContributionAndCreditsResult, marriageDetailsResult)
        )

      }
      "should return a DataRetrievalServiceError if the service fails to retrieve results for a subset of the NPS APIs called" in {

        val niContributionAndCreditsResult: SuccessResult[Nothing, NiContributionsAndCreditsSuccessResponse] =
          SuccessResult(
            ApiName.NiContributionAndCredits,
            niContributionsAndCreditsSuccessResponse
          )

        (mockNiContributionsAndCreditsConnector
          .fetchContributionsAndCredits(_: BenefitType, _: NiContributionsAndCreditsRequest)(_: HeaderCarrier))
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.rightT(niContributionAndCreditsResult)
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[StartYear],
            _: Option[FilterLatest],
            _: Option[Int]
          )(_: HeaderCarrier))
          .expects(BenefitType.BSP, identifier, None, None, None, *)
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
          .expects(BenefitType.BSP, niContributionsAndCreditsRequest, *)
          .returning(
            EitherT.leftT(NpsClientError(new RuntimeException("error")))
          )

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(
            _: BenefitType,
            _: Identifier,
            _: Option[StartYear],
            _: Option[FilterLatest],
            _: Option[Int]
          )(_: HeaderCarrier))
          .expects(BenefitType.BSP, identifier, None, None, None, *)
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
