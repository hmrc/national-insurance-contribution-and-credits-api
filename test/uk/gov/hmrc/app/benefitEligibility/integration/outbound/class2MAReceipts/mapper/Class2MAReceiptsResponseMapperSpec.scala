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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.mapper

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.enums.ErrorCode403.ErrorCode403_1
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.enums.{
  ErrorCode403,
  ErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse

class Class2MAReceiptsResponseMapperSpec extends AnyFreeSpec with Matchers {

  private val underTest = new Class2MAReceiptsResponseMapper

  "Class2MAReceiptsResponseMapper" - {

    ".toApiResult" - {

      "should map Class2MAReceiptsSuccessResponse to success result" in {
        val successResponse = Class2MAReceiptsSuccessResponse(Identifier("AB123456C"), List())

        val result = underTest.toApiResult(successResponse)

        val expected = DownstreamSuccessResponse(Class2MAReceipts, successResponse)

        result shouldBe expected
      }

      "should map Class2MAReceiptsErrorResponse403 to AccessForbidden error" in {
        val errorResponse =
          Class2MAReceiptsError.Class2MAReceiptsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_1)

        val result = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.AccessForbidden)

        result shouldBe expected
      }

      "should map Class2MAReceiptsErrorResponse400 to BadRequest error" in {
        val failures =
          List(Class2MAReceiptsError400(Reason(""), ErrorCode400.ErrorCode400_1))
        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse400(failures)

        val result = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.BadRequest)

        result shouldBe expected
      }

      "should map Class2MAReceiptsError422Response to UnprocessableEntity error" in {
        val failures      = List(Class2MAReceiptsError422(Reason(""), ErrorCode422("dsds")))
        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse422(failures)

        val result = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.UnprocessableEntity)

        result shouldBe expected
      }
    }

    "toResult(normalizedErrorStatusCode: NormalizedErrorStatusCode)" - {

      "should create failure result with AccessForbidden error" in {

        val errorResponse =
          Class2MAReceiptsError.Class2MAReceiptsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403_1)
        val result = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.AccessForbidden)

        result shouldBe expected
      }

      "should create failure result with BadRequest error" in {

        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse400(List())
        val result        = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.BadRequest)

        result shouldBe expected
      }

      "should create failure result with UnprocessableEntity error" in {
        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse422(List())
        val result        = underTest.toApiResult(errorResponse)

        val expected = DownstreamErrorReport(Class2MAReceipts, NpsNormalizedError.UnprocessableEntity)

        result shouldBe expected
      }
    }
  }

}
