/*
 * Copyright 2025 HM Revenue & Customs
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
import cats.syntax.all.*
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.{
  EligibilityCheckDataResultBSP,
  EligibilityCheckDataResultESA,
  EligibilityCheckDataResultGYSP,
  EligibilityCheckDataResultJSA,
  EligibilityCheckDataResultMA
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.Failure
import MaternityAllowanceSortType.NinoDescending
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{
  Class2MAReceipts,
  Liabilities,
  MarriageDetails,
  NiContributionAndCredits
}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  BSPEligibilityCheckDataRequest,
  ESAEligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest,
  JSAEligibilityCheckDataRequest,
  MAEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.OccurrenceNumber
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.LiabilitySearchCategoryHyphenated.AllLiabilities

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class BenefitEligibilityDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  private val mockMaternityAllowanceDataRetrievalService: MaternityAllowanceDataRetrievalService =
    mock[MaternityAllowanceDataRetrievalService]

  private val mockEmploymentSupportAllowanceDataRetrievalService: EmploymentSupportAllowanceDataRetrievalService =
    mock[EmploymentSupportAllowanceDataRetrievalService]

  private val mockJobSeekersAllowanceDataRetrievalService: JobSeekersAllowanceDataRetrievalService =
    mock[JobSeekersAllowanceDataRetrievalService]

  private val mockGetYourStatePensionDataRetrievalService: GetYourStatePensionDataRetrievalService =
    mock[GetYourStatePensionDataRetrievalService]

  private val mockBspDataRetrievalService: BspDataRetrievalService = mock[BspDataRetrievalService]

  private val underTest = new BenefitEligibilityDataRetrievalService(
    mockMaternityAllowanceDataRetrievalService,
    mockEmploymentSupportAllowanceDataRetrievalService,
    mockJobSeekersAllowanceDataRetrievalService,
    mockGetYourStatePensionDataRetrievalService,
    mockBspDataRetrievalService
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  private val marriageDetailsResult =
    DownstreamErrorReport(MarriageDetails, NpsNormalizedError.AccessForbidden)

  private val class2MaReceiptsResult =
    DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.AccessForbidden)

  private val liabilityResult =
    DownstreamErrorReport(Liabilities, NpsNormalizedError.AccessForbidden)

  private val contributionCreditResult = List(
    DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.AccessForbidden)
  )

  private val eligibilityCheckDataRequestMA = MAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = DateOfBirth(LocalDate.parse("1992-08-23")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2025),
    identifier = Identifier("GD379251T"),
    liabilitySearchCategoryHyphenated = AllLiabilities,
    liabilityOccurrenceNumber = Some(OccurrenceNumber(233232323)),
    liabilityType = Some(AllLiabilities),
    earliestLiabilityStartDate = Some(LocalDate.parse("1992-08-23")),
    liabilityStart = Some(LocalDate.parse("1992-08-23")),
    liabilityEnd = Some(LocalDate.parse("1992-08-23")),
    archived = Some(true),
    receiptDate = Some(ReceiptDate(LocalDate.parse("1992-08-23"))),
    sortBy = Some(NinoDescending)
  )

  private val eligibilityCheckDataRequestJSA = JSAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2025
  )

  private val eligibilityCheckDataRequestBSP = BSPEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2025,
    identifier = Identifier("GD379251T"),
    searchStartYear = Some(2025),
    searchEndYear = Some(2025),
    latest = Some(true),
    sequence = Some(23)
  )

  private val eligibilityCheckDataRequestGYSP = GYSPEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2025,
    identifier = Identifier("GD379251T"),
    searchStartYear = Some(2025),
    searchEndYear = Some(2025),
    latest = Some(true),
    sequence = Some(12),
    associatedCalculationSequenceNumber = 1123232,
    benefitType = LongTermBenefitType.All,
    pensionProcessingArea = Some("pensionProcessingArea"),
    schemeContractedOutNumber = 32324343,
    schemeMembershipSequenceNumber = Some(4343343),
    schemeMembershipTransferSequenceNumber = Some(435454545),
    schemeMembershipOccurrenceNumber = Some(3289908)
  )

  private val eligibilityCheckDataRequestESA = ESAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2025
  )

  "BenefitEligibilityDataRetrievalService" - {
    ".getEligibilityData" - {
      "should retrieve eligibility data for MA" in {

        (mockMaternityAllowanceDataRetrievalService
          .fetchEligibilityData(_: MAEligibilityCheckDataRequest)(_: HeaderCarrier))
          .expects(eligibilityCheckDataRequestMA, *)
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](
              EligibilityCheckDataResultMA(
                class2MaReceiptsResult,
                liabilityResult,
                contributionCreditResult
              )
            )
          )

        underTest.getEligibilityData(eligibilityCheckDataRequestMA).value.futureValue shouldBe Right(
          EligibilityCheckDataResultMA(
            class2MaReceiptsResult,
            liabilityResult,
            contributionCreditResult
          )
        )
      }
      "should retrieve eligibility data for JSA" in {
        (mockJobSeekersAllowanceDataRetrievalService
          .fetchEligibilityData(_: JSAEligibilityCheckDataRequest))
          .expects(eligibilityCheckDataRequestJSA)
          .returning(EitherT.pure[Future, BenefitEligibilityError](EligibilityCheckDataResultJSA()))

        underTest.getEligibilityData(eligibilityCheckDataRequestJSA).value.futureValue shouldBe Right(
          EligibilityCheckDataResultJSA()
        )
      }
      "should retrieve eligibility data for ESA" in {
        (mockEmploymentSupportAllowanceDataRetrievalService
          .fetchEligibilityData(_: ESAEligibilityCheckDataRequest))
          .expects(eligibilityCheckDataRequestESA)
          .returning(EitherT.pure[Future, BenefitEligibilityError](EligibilityCheckDataResultESA()))

        underTest.getEligibilityData(eligibilityCheckDataRequestESA).value.futureValue shouldBe Right(
          EligibilityCheckDataResultESA()
        )
      }
      "should retrieve eligibility data for GYSP" in {
        (mockGetYourStatePensionDataRetrievalService
          .fetchEligibilityData(_: GYSPEligibilityCheckDataRequest)(_: HeaderCarrier))
          .expects(eligibilityCheckDataRequestGYSP, *)
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](
              EligibilityCheckDataResultGYSP(
                marriageDetailsResult
              )
            )
          )

        underTest.getEligibilityData(eligibilityCheckDataRequestGYSP).value.futureValue shouldBe Right(
          EligibilityCheckDataResultGYSP(
            marriageDetailsResult
          )
        )
      }
      "should retrieve eligibility data for BSP" in {
        (mockBspDataRetrievalService
          .fetchEligibilityData(_: BSPEligibilityCheckDataRequest)(_: HeaderCarrier))
          .expects(eligibilityCheckDataRequestBSP, *)
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](EligibilityCheckDataResultBSP(marriageDetailsResult))
          )

        underTest.getEligibilityData(eligibilityCheckDataRequestBSP).value.futureValue shouldBe Right(
          EligibilityCheckDataResultBSP(marriageDetailsResult)
        )
      }
    }
  }

}

//
// GO LIVE PLAN
// CREDENTIALS
// PRA
// CIP
// CAB
//
// NetCompany docs => go live
//
