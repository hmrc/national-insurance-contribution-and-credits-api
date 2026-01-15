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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.request

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  BSPEligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest
}
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.language.postfixOps

class MarriageDetailsRequestHelperSpec extends AnyFreeSpec with MockFactory {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  (mockServicesConfig.baseUrl(_: String)).expects("hip").returning("hip")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.originatorId").returning("originatorId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientId").returning("clientId")
  (mockServicesConfig.getString(_: String)).expects("microservice.services.hip.clientSecret").returning("clientSecret")

  val appConfig: AppConfig = new AppConfig(config = mockServicesConfig)

  val underTest = new MarriageDetailsRequestHelper()

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
    sequence = Some(23),
    associatedCalculationSequenceNumber = 1123232,
    benefitType = "SOME BENEFIT",
    pensionProcessingArea = Some("pensionProcessingArea"),
    schemeContractedOutNumber = 32324343,
    schemeMembershipSequenceNumber = Some(4343343),
    schemeMembershipTransferSequenceNumber = Some(435454545),
    schemeMembershipOccurrenceNumber = Some(3289908)
  )

  private val expectedRequestUrl =
    "hip/ni/individual/Identifier(GD379251T)/marriage-cpSome(searchStartYear=2025&searchEndYear=2025&latest=true&?sequence=23)/"

  "MarriageDetailsRequestHelper" - {
    ".buildRequestPath" - {
      "should build request path successfully when given BSP request" in {

        underTest
          .buildRequestPath(appConfig.hipBaseUrl, eligibilityCheckDataRequestBSP) shouldBe expectedRequestUrl
      }

      "should build request path successfully when given GYSP request" in {

        underTest
          .buildRequestPath(appConfig.hipBaseUrl, eligibilityCheckDataRequestGYSP) shouldBe expectedRequestUrl
      }
    }
  }

}
