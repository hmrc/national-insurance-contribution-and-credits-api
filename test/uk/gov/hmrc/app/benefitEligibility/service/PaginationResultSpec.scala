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

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{Liabilities, NiContributionAndCredits}
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.BadRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.repository.{PageTaskId, PaginationCursor}

import java.util.UUID

class PaginationResultSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  val nationalInsuranceNumber = Identifier("AB123456C")

  "PaginationResult" - {
    val paginationResultWithNextCursor = PaginationResult(
      paginationType = PaginationType.MA,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(
          ApiName.Liabilities,
          LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = ContributionCreditPagingResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      nextCursor =
        Some(PaginationCursor(PaginationType.MA, PageTaskId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26"))))
    )

    val paginationResultWithoutNextCursor = PaginationResult(
      paginationType = PaginationType.MA,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(
          ApiName.Liabilities,
          LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = ContributionCreditPagingResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      nextCursor = None
    )

    val paginationResultNoPaging = PaginationResult(
      paginationType = PaginationType.MA,
      nationalInsuranceNumber,
      liabilitiesResult = List(SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None))),
      marriageDetailsResult = None,
      contributionCreditResult = ContributionCreditPagingResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      nextCursor = None
    )

    val paginationResultWithFailure = PaginationResult(
      paginationType = PaginationType.MA,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None)),
        FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(NpsNormalizedError.BadRequest, None)
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = ContributionCreditPagingResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      nextCursor = None
    )

    ".setNextCursor" - {
      "should return pagination result with a next cursor if paging should happen " in {
        val newResult =
          paginationResultWithoutNextCursor.setNextCursor(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26"))

        newResult shouldBe paginationResultWithNextCursor
      }
      "should return pagination result with no next cursor if paging should not happen " in {
        val newResult = paginationResultNoPaging.setNextCursor(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26"))

        newResult shouldBe paginationResultNoPaging
      }
    }
    ".getNextCursor" - {
      "should return next cursor if next cursor exists" in {
        val nextCursor = paginationResultWithNextCursor.getNextCursor

        nextCursor shouldBe Some(
          PaginationCursor(PaginationType.MA, PageTaskId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
        )
      }
      "should return none if no next cursor" in {
        val nextCursor = paginationResultWithoutNextCursor.getNextCursor

        nextCursor shouldBe None
      }
    }
    ".allResults" - {
      "should return all results if given a pagination result" in {
        val newResult = paginationResultWithoutNextCursor.allResults

        newResult shouldBe List(
          SuccessResult(
            Liabilities,
            LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
          )
        )
      }
      "should return all results including failure if given a pagination result with failure" in {
        val newResult = paginationResultWithFailure.allResults

        newResult shouldBe
          List(
            SuccessResult(Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None)),
            FailureResult(NiContributionAndCredits, ErrorReport(BadRequest, None))
          )

      }
    }
  }

}
