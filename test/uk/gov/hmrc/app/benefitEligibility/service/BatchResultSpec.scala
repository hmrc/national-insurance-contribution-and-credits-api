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
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.repository.BatchId

import java.util.UUID

class BatchResultSpec
    extends AnyFreeSpec
    with MockFactory
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  val nationalInsuranceNumber = Identifier("AB123456C")

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  "BatchResult" - {
    val batchResultWithNextCursor = BatchResult(
      correlationId,
      batchType = BatchType.MaBatch,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(
          ApiName.Liabilities,
          LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = BatchWithTaxWindowsResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      callSystem = None,
      batchId = Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
    )

    val batchResultWithoutNextCursor = BatchResult(
      correlationId,
      batchType = BatchType.MaBatch,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(
          ApiName.Liabilities,
          LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = BatchWithTaxWindowsResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      callSystem = None,
      batchId = None
    )

    val batchResultNoBatching = BatchResult(
      correlationId,
      batchType = BatchType.MaBatch,
      nationalInsuranceNumber,
      liabilitiesResult = List(SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None))),
      marriageDetailsResult = None,
      contributionCreditResult = BatchWithTaxWindowsResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      callSystem = None,
      batchId = None
    )

    val batchResultWithFailure = BatchResult(
      correlationId,
      batchType = BatchType.MaBatch,
      nationalInsuranceNumber,
      liabilitiesResult = List(
        SuccessResult(ApiName.Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None)),
        FailureResult(
          ApiName.NiContributionAndCredits,
          ErrorReport(None)
        )
      ),
      marriageDetailsResult = None,
      contributionCreditResult = BatchWithTaxWindowsResult(None, None),
      benefitSchemeMembershipDetailsData = None,
      callSystem = None,
      batchId = None
    )

    ".setNextCursor" - {
      "should return batch result with a next cursor if batching should happen " in {
        val newResult =
          batchResultWithoutNextCursor.setBatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26"))

        newResult shouldBe batchResultWithNextCursor
      }
      "should return batch result with no next cursor if batching should not happen " in {
        val newResult = batchResultNoBatching.setBatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26"))

        newResult shouldBe batchResultNoBatching
      }
    }
    ".getNextCursor" - {
      "should return next cursor if next cursor exists" in {
        val batchId = batchResultWithNextCursor.getBatchId

        batchId shouldBe Some(BatchId(UUID.fromString("9b0de48f-b995-4c61-aeab-8b02273a8f26")))
      }
      "should return none if no next cursor" in {
        val nextCursor = batchResultWithoutNextCursor.getBatchId

        nextCursor shouldBe None
      }
    }
    ".allResults" - {
      "should return all results if given a batch result" in {
        val newResult = batchResultWithoutNextCursor.allResults

        newResult shouldBe List(
          SuccessResult(
            Liabilities,
            LiabilitySummaryDetailsSuccessResponse(None, Some(Callback(Some(CallbackUrl("SomeUrl")))))
          )
        )
      }
      "should return all results including failure if given a batch result with failure" in {
        val newResult = batchResultWithFailure.allResults

        newResult shouldBe
          List(
            SuccessResult(Liabilities, LiabilitySummaryDetailsSuccessResponse(None, None)),
            FailureResult(NiContributionAndCredits, ErrorReport(None))
          )

      }
    }
  }

}
