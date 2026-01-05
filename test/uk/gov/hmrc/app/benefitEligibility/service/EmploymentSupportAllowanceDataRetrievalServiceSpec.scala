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

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.ESAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultESA

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class EmploymentSupportAllowanceDataRetrievalServiceSpec extends AnyFreeSpec {

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  private val underTest = new EmploymentSupportAllowanceDataRetrievalService()

  private val eligibilityCheckDataRequest = ESAEligibilityCheckDataRequest(
    nationalInsuranceNumber = "GD379251T",
    dateOfBirth = LocalDate.parse("1992-08-23"),
    startTaxYear = 2025,
    endTaxYear = 2025
  )

  "EmploymentSupportAllowanceDataRetrievalService" - {
    ".fetchEligibilityData" - {
      "should retrieve data successfully" in {
        underTest
          .fetchEligibilityData(eligibilityCheckDataRequest)
          .value
          .futureValue shouldBe Right(
          EligibilityCheckDataResultESA()
        )

      }
    }
  }

}
