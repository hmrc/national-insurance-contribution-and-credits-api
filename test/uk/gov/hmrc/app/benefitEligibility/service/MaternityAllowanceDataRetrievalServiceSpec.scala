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
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{Class2MAReceipts, Liabilities, NiContributionAndCredits}
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, UnexpectedStatus}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector.Class2MAReceiptsConnector
import MaternityAllowanceSortType.NinoDescending
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.OccurrenceNumber
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.LiabilitySearchCategoryHyphenated.AllLiabilities
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.reqeust.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class MaternityAllowanceDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  val mockClass2MAReceiptsConnector: Class2MAReceiptsConnector = mock[Class2MAReceiptsConnector]

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  (mockServicesConfig.baseUrl(_: String)).expects("hip").returning("hip")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.originatorId").returning("originatorId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientId").returning("clientId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientSecret").returning("clientSecret")

  val appConfig: AppConfig = new AppConfig(config = mockServicesConfig)

  lazy val underTest = new MaternityAllowanceDataRetrievalService(
    mockClass2MAReceiptsConnector
  )

  private val eligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = DateOfBirth(LocalDate.parse("1992-08-23")),
    startTaxYear = StartTaxYear(2025),
    endTaxYear = EndTaxYear(2026),
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

//  private val class2MaReceiptsResult =
//    DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.AccessForbidden)
//
//  private val liabilityResult =
//    DownstreamErrorReport(Liabilities, NpsNormalizedError.AccessForbidden)
//
//  private val contributionCreditResult = List(
//    DownstreamErrorReport(ContributionCredit, NpsNormalizedError.AccessForbidden)
//  )

  "MaternityAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should retrieve data successfully" in {
        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(
            _: Identifier,
            _: Option[Boolean],
            _: Option[ReceiptDate],
            _: Option[MaternityAllowanceSortType]
          )(_: HeaderCarrier))
          .expects(
            Identifier("GD379251T"),
            Some(true),
            Some(ReceiptDate(LocalDate.parse("1992-08-23"))),
            Some(NinoDescending),
            *
          )
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](
              DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.AccessForbidden)
            )
          )

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe
          Right(
            EligibilityCheckDataResultMA(
              DownstreamErrorReport(Class2MAReceipts, AccessForbidden),
              DownstreamErrorReport(Liabilities, UnexpectedStatus(207)),
              List(DownstreamErrorReport(NiContributionAndCredits, UnexpectedStatus(207)))
            )
          )

      }
    }
  }

}
