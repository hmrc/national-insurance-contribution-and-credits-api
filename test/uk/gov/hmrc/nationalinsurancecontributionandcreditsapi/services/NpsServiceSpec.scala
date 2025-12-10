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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services

import cats.data.EitherT
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.api.EligibilityCheckDataRequest
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.{BenefitType, EligibilityCheckDataResult}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services.benefitEligibilityDataRetrieval.{
  EmploymentSupportAllowanceDataRetrievalService,
  GetYourStatePensionDataRetrievalService,
  JobSeekersAllowanceDataRetrievalService,
  MaternityAllowanceDataRetrievalService
}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class NpsServiceSpec extends AnyFreeSpec with MockFactory {

  val mockMaternityAllowanceDataRetrievalService: MaternityAllowanceDataRetrievalService =
    mock[MaternityAllowanceDataRetrievalService]

  val mockEmploymentSupportAllowanceDataRetrievalService: EmploymentSupportAllowanceDataRetrievalService =
    mock[EmploymentSupportAllowanceDataRetrievalService]

  val mockJobSeekersAllowanceDataRetrievalService: JobSeekersAllowanceDataRetrievalService =
    mock[JobSeekersAllowanceDataRetrievalService]

  val mockGetYourStatePensionDataRetrievalService: GetYourStatePensionDataRetrievalService =
    mock[GetYourStatePensionDataRetrievalService]

  val underTest = new NpsService(
    mockMaternityAllowanceDataRetrievalService,
    mockEmploymentSupportAllowanceDataRetrievalService,
    mockJobSeekersAllowanceDataRetrievalService,
    mockGetYourStatePensionDataRetrievalService
  )

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  "NpsService" - {
    ".getEligibilityData" - {
      "should retrieve eligibility data for MA" in {
        (mockMaternityAllowanceDataRetrievalService
          .fetchEligibilityData(_: EligibilityCheckDataRequest))
          .expects(EligibilityCheckDataRequest(BenefitType.MA))
          .returning(EitherT.pure[Future, Throwable](EligibilityCheckDataResult("MA result")))

        underTest.getEligibilityData(EligibilityCheckDataRequest(BenefitType.MA)).value.futureValue shouldBe Right(
          EligibilityCheckDataResult("MA result")
        )
      }
      "should retrieve eligibility data for JSA" in {
        (mockJobSeekersAllowanceDataRetrievalService
          .fetchEligibilityData(_: EligibilityCheckDataRequest))
          .expects(EligibilityCheckDataRequest(BenefitType.JSA))
          .returning(EitherT.pure[Future, Throwable](EligibilityCheckDataResult("JSA result")))

        underTest.getEligibilityData(EligibilityCheckDataRequest(BenefitType.JSA)).value.futureValue shouldBe Right(
          EligibilityCheckDataResult("JSA result")
        )
      }
      "should retrieve eligibility data for ESA" in {
        (mockEmploymentSupportAllowanceDataRetrievalService
          .fetchEligibilityData(_: EligibilityCheckDataRequest))
          .expects(EligibilityCheckDataRequest(BenefitType.ESA))
          .returning(EitherT.pure[Future, Throwable](EligibilityCheckDataResult("ESA result")))

        underTest.getEligibilityData(EligibilityCheckDataRequest(BenefitType.ESA)).value.futureValue shouldBe Right(
          EligibilityCheckDataResult("ESA result")
        )
      }
      "should retrieve eligibility data for GYSP" in {
        (mockGetYourStatePensionDataRetrievalService
          .fetchEligibilityData(_: EligibilityCheckDataRequest))
          .expects(EligibilityCheckDataRequest(BenefitType.GYSP))
          .returning(EitherT.pure[Future, Throwable](EligibilityCheckDataResult("GYSP result")))

        underTest.getEligibilityData(EligibilityCheckDataRequest(BenefitType.GYSP)).value.futureValue shouldBe Right(
          EligibilityCheckDataResult("GYSP result")
        )
      }
    }
  }

}
