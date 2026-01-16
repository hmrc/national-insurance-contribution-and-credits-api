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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.NiContributionAndCredits
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.*

class NiContributionsAndCreditsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val niContributionsAndCreditsSuccessResponse = NiContributionsAndCreditsSuccessResponse(
    List(
      NicClass1(
        taxYear = Some(TaxYear(2022)),
        contributionCategoryLetter = Some(ContributionCategoryLetter("U")),
        contributionCategory = Some(ContributionCategory.None),
        contributionCreditType = Some(ContributionCreditType.C1),
        primaryContribution = Some(PrimaryContribution(BigDecimal("99999999999999.98"))),
        class1ContributionStatus = Some(Class1ContributionStatus.ComplianceAndYieldIncomplete),
        primaryPaidEarnings = Some(PrimaryPaidEarnings(BigDecimal("99999999999999.98"))),
        creditSource = Some(CreditSource.NotKnown),
        employerName = Some(EmployerName("ipOpMs")),
        latePaymentPeriod = Some(LatePaymentPeriod.L)
      )
    ),
    List(
      NicClass2(
        taxYear = Some(TaxYear(2022)),
        noOfCreditsAndConts = Some(NumberOfCreditsAndConts(53)),
        contributionCreditType = Some(ContributionCreditType.C1),
        class2Or3EarningsFactor = Some(Class2Or3EarningsFactor(BigDecimal("99999999999999.98"))),
        class2NIContributionAmount = Some(Class2NIContributionAmount(BigDecimal("99999999999999.98"))),
        class2Or3CreditStatus = Some(Class2Or3CreditStatus.NotKnowNotApplicable),
        creditSource = Some(CreditSource.NotKnown),
        latePaymentPeriod = Some(LatePaymentPeriod.L)
      )
    )
  )

  val niContributionsAndCreditsResponse400 = NiContributionsAndCreditsResponse400(
    List(
      NiContributionsAndCredits400(
        Reason("HTTP message not readable"),
        ErrorCode400.ErrorCode400_2
      ),
      NiContributionsAndCredits400(
        Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
        ErrorCode400.ErrorCode400_1
      )
    )
  )

  val niContributionsAndCreditsResponse403 =
    NiContributionsAndCreditsResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_2)

  val niContributionsAndCreditsResponse422 = NiContributionsAndCreditsResponse422(
    failures = List(
      NiContributionsAndCredits422(
        Reason("HTTP message not readable"),
        ErrorCode422("A589")
      )
    )
  )

  val underTest = new NiContributionsAndCreditsResponseMapper

  "NiContributionsAndCreditsResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        underTest.toApiResult(niContributionsAndCreditsSuccessResponse) shouldBe
          DownstreamSuccessResponse(NiContributionAndCredits, niContributionsAndCreditsSuccessResponse)
      }

      "should successfully return a Failure result when given an ErrorResponse400" in {

        underTest.toApiResult(niContributionsAndCreditsResponse400) shouldBe
          DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.BadRequest)
      }

      "should successfully return a Failure result when given a ErrorResponse403" in {

        underTest.toApiResult(niContributionsAndCreditsResponse403) shouldBe
          DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a Failure result when given a ErrorResponse422" in {

        underTest.toApiResult(niContributionsAndCreditsResponse422) shouldBe
          DownstreamErrorReport(NiContributionAndCredits, NpsNormalizedError.UnprocessableEntity)
      }
    }
  }

}
