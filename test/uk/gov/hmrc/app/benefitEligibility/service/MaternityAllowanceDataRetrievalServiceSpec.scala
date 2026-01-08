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
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultMA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.Failure
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  Class2MaReceiptsResult,
  ContributionCreditResult,
  LiabilityResult
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.connector.Class2MAReceiptsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.reqeust.Class2MAReceiptsRequestHelper
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.reqeust.MaternityAllowanceSortType.NinoDescending
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{EligibilityCheckDataResult, NpsNormalizedError}
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

  val mockClass2MAReceiptsConnector: Class2MAReceiptsConnector         = mock[Class2MAReceiptsConnector]
  val mockClass2MAReceiptsRequestHelper: Class2MAReceiptsRequestHelper = mock[Class2MAReceiptsRequestHelper]

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  (mockServicesConfig.baseUrl(_: String)).expects("hip").returning("hip")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.originatorId").returning("originatorId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientId").returning("clientId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientSecret").returning("clientSecret")

  val appConfig: AppConfig = new AppConfig(config = mockServicesConfig)

  val underTest = new MaternityAllowanceDataRetrievalService(
    mockClass2MAReceiptsConnector,
    mockClass2MAReceiptsRequestHelper,
    appConfig
  )

  private val eligibilityCheckDataRequest = MAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2026,
    identifier = Identifier("GD379251T"),
    liabilitySearchCategoryHyphenated = true,
    liabilityOccurrenceNumber = Some(233232323),
    liabilityType = Some("FOOD"),
    earliestLiabilityStartDate = Some(LocalDate.parse("1992-08-23")),
    liabilityStart = Some(LocalDate.parse("1992-08-23")),
    liabilityEnd = Some(LocalDate.parse("1992-08-23")),
    archived = Some(true),
    receiptDate = Some(ReceiptDate(LocalDate.parse("1992-08-23"))),
    sortBy = Some(NinoDescending)
  )

  private val class2MaReceiptsResult =
    Class2MaReceiptsResult(Failure, None, Some(NpsNormalizedError(NormalizedErrorStatusCode.AccessForbidden, "", 403)))

  private val liabilityResult =
    LiabilityResult(Failure, None, Some(NpsNormalizedError(NormalizedErrorStatusCode.AccessForbidden, "", 403)))

  private val contributionCreditResult = List(
    ContributionCreditResult(
      Failure,
      None,
      Some(NpsNormalizedError(NormalizedErrorStatusCode.AccessForbidden, "", 403))
    )
  )

  "MaternityAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should retrieve data successfully" in {

        (mockClass2MAReceiptsConnector
          .fetchClass2MAReceipts(_: String)(_: HeaderCarrier))
          .expects("some url path", *)
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](
              Class2MaReceiptsResult(
                Failure,
                None,
                Some(NpsNormalizedError(NormalizedErrorStatusCode.AccessForbidden, "", 403))
              )
            )
          )

        (mockClass2MAReceiptsRequestHelper
          .buildRequestPath(_: String, _: MAEligibilityCheckDataRequest))
          .expects("hip", eligibilityCheckDataRequest)
          .returning("some url path")

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultMA(class2MaReceiptsResult, liabilityResult, contributionCreditResult)
        )

      }
    }
  }

}
