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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.mapper

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Liabilities
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{ErrorCode422, NpsErrorCode400}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.{
  Callback,
  LiabilitySummaryDetailsSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.ErrorCode403.ErrorCode403_2
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums.ErrorReason403.Forbidden

class LiabilitySummaryDetailsResponseMapperSpec extends AnyFreeSpec with Matchers {

  private val underTest = new LiabilitySummaryDetailsResponseMapper

  "LiabilitySummaryDetailsResponseMapper" - {

    ".toApiResult" - {

      "should map LiabilitySummaryDetailsSuccessResponse to success result" in {
        val successResponse = LiabilitySummaryDetailsSuccessResponse(Some(List()), Some(List()), Some(Callback("")))

        val result = underTest.toApiResult(successResponse)

        val expected = SuccessResult(Liabilities, successResponse)

        result shouldBe expected
      }

      "should map LiabilitySummaryDetailsErrorResponse403 to AccessForbidden error" in {
        val errorResponse =
          LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse403(
            Forbidden,
            ErrorCode403_2
          )

        val result = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.AccessForbidden)

        result shouldBe expected
      }

      "should map LiabilitySummaryDetailsErrorResponse400 to BadRequest error" in {
        val failures =
          List(LiabilitySummaryDetailsError400(Reason(""), NpsErrorCode400.NpsErrorCode400_1))
        val errorResponse = LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse400(Some(failures))

        val result = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.BadRequest)

        result shouldBe expected
      }

      "should map LiabilitySummaryDetailsError422Response to UnprocessableEntity error" in {
        val failures = List(LiabilitySummaryDetailsError422(Reason(""), ErrorCode422("dsds")))
        val errorResponse = LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse422(
          Some(failures),
          Some(true),
          Some(true),
          Some(true)
        )

        val result = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.UnprocessableEntity)

        result shouldBe expected
      }
    }

    "toResult(normalizedErrorStatusCode: NormalizedErrorStatusCode)" - {

      "should create failure result with AccessForbidden error" in {

        val errorResponse =
          LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse403(Forbidden, ErrorCode403_2)
        val result = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.AccessForbidden)

        result shouldBe expected
      }

      "should create failure result with BadRequest error" in {

        val errorResponse = LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse400(Some(List()))
        val result        = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.BadRequest)

        result shouldBe expected
      }

      "should create failure result with UnprocessableEntity error" in {
        val errorResponse = LiabilitySummaryDetailsError.LiabilitySummaryDetailsErrorResponse422(
          Some(List()),
          Some(false),
          Some(false),
          Some(false)
        )
        val result = underTest.toApiResult(errorResponse)

        val expected = FailureResult(Liabilities, NpsNormalizedError.UnprocessableEntity)

        result shouldBe expected
      }
    }
  }

}
