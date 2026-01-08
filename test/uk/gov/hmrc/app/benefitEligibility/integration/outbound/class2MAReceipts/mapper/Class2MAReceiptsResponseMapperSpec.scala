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
import uk.gov.hmrc.app.benefitEligibility.common.{ErrorCode422, Identifier, Reason}
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.{
  AccessForbidden,
  BadRequest,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.Class2MaReceiptsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsNormalizedError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.enums.{
  ErrorCode400,
  ErrorCode403,
  ErrorReason403
}

class Class2MAReceiptsResponseMapperSpec extends AnyFreeSpec with Matchers {

  private val underTest = new Class2MAReceiptsResponseMapper

  "Class2MAReceiptsResponseMapper" - {

    ".toResult" - {

      "should map Class2MAReceiptsSuccessResponse to success result" in {
        val successResponse = Class2MAReceiptsSuccessResponse(Identifier("AB123456C"), List())

        val result = underTest.toResult(successResponse)

        val expected = Class2MaReceiptsResult(
          Success,
          Some(successResponse),
          None
        )

        result shouldBe expected
      }

      "should map Class2MAReceiptsErrorResponse403 to AccessForbidden error" in {
        val errorResponse =
          Class2MAReceiptsError.Class2MAReceiptsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_1)

        val result = underTest.toResult(errorResponse)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(AccessForbidden, "downstream resource cannot be accessed by the calling client", 403))
        )

        result shouldBe expected
      }

      "should map Class2MAReceiptsErrorResponse400 to BadRequest error" in {
        val failures =
          List(Class2MAReceiptsError400(Reason(""), ErrorCode400.Invalid_Destination_Header))
        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse400(failures)

        val result = underTest.toResult(errorResponse)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(BadRequest, "downstream received a malformed request", 400))
        )

        result shouldBe expected
      }

      "should map Class2MAReceiptsError422Response to UnprocessableEntity error" in {
        val failures      = List(Class2MAReceiptsError422(Reason(""), ErrorCode422("dsds")))
        val errorResponse = Class2MAReceiptsError.Class2MAReceiptsErrorResponse422(failures)

        val result = underTest.toResult(errorResponse)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(UnprocessableEntity, "downstream could not process data in request", 422))
        )

        result shouldBe expected
      }
    }

    "toResult(normalizedErrorStatusCode: NormalizedErrorStatusCode)" - {

      "should create failure result with AccessForbidden error" in {
        val result = underTest.toResult(AccessForbidden)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(AccessForbidden, "downstream resource cannot be accessed by the calling client", 403))
        )

        result shouldBe expected
      }

      "should create failure result with BadRequest error" in {
        val result = underTest.toResult(BadRequest)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(BadRequest, "downstream received a malformed request", 400))
        )

        result shouldBe expected
      }

      "should create failure result with UnprocessableEntity error" in {
        val result = underTest.toResult(UnprocessableEntity)

        val expected = Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsNormalizedError(UnprocessableEntity, "downstream could not process data in request", 422))
        )

        result shouldBe expected
      }
    }
  }

}
