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
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.MarriageDetails
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, BenefitType, Identifier, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.GYSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.DownstreamErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.request.MarriageDetailsRequestHelper
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class GetYourStatePensionDataRetrievalServiceSpec extends AnyFreeSpec with MockFactory {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  val mockMarriageDetailsConnector: MarriageDetailsConnector         = mock[MarriageDetailsConnector]
  val mockMarriageDetailsRequestHelper: MarriageDetailsRequestHelper = mock[MarriageDetailsRequestHelper]

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  (mockServicesConfig.baseUrl(_: String)).expects("hip").returning("hip")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.originatorId").returning("originatorId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientId").returning("clientId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientSecret").returning("clientSecret")

  val appConfig: AppConfig = new AppConfig(config = mockServicesConfig)

  val underTest = new GetYourStatePensionDataRetrievalService(
    mockMarriageDetailsConnector,
    mockMarriageDetailsRequestHelper,
    appConfig
  )

  private val eligibilityCheckDataRequest = GYSPEligibilityCheckDataRequest(
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
    benefitType = "SOME BENEFIT",
    pensionProcessingArea = Some("pensionProcessingArea"),
    schemeContractedOutNumber = 32324343,
    schemeMembershipSequenceNumber = Some(4343343),
    schemeMembershipTransferSequenceNumber = Some(435454545),
    schemeMembershipOccurrenceNumber = Some(3289908)
  )

  private val marriageDetailsResult =
    DownstreamErrorReport(MarriageDetails, NpsNormalizedError.AccessForbidden)

  "GetYourStatePensionDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should retrieve data successfully" in {

        (mockMarriageDetailsConnector
          .fetchMarriageDetails(_: BenefitType, _: String)(_: HeaderCarrier))
          .expects(BenefitType.GYSP, "some url path", *)
          .returning(
            EitherT.pure[Future, BenefitEligibilityError](
              DownstreamErrorReport(MarriageDetails, NpsNormalizedError.AccessForbidden)
            )
          )

        (mockMarriageDetailsRequestHelper
          .buildRequestPath(_: String, _: GYSPEligibilityCheckDataRequest))
          .expects("hip", eligibilityCheckDataRequest)
          .returning("some url path")

        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(EligibilityCheckDataResultGYSP(marriageDetailsResult))

      }
    }
  }

}
