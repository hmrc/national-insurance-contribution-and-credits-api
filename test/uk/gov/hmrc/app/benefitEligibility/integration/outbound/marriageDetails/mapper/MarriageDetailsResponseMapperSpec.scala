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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.MarriageDetails
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError.{
  MarriageDetailsErrorResponse400,
  MarriageDetailsErrorResponse403,
  MarriageDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums.{
  ErrorCode403,
  ErrorReason403
}

class MarriageDetailsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  MarriageDetailsSuccessResponse(None, None, None)

  val mockMarriageDetailsSuccessResponse: MarriageDetailsSuccessResponse =
    MarriageDetailsSuccessResponse(None, None, None)

  val mockMarriageDetailsErrorResponse400: MarriageDetailsErrorResponse400 = MarriageDetailsErrorResponse400(List())

  val mockMarriageDetailsErrorResponse403: MarriageDetailsErrorResponse403 =
    MarriageDetailsErrorResponse403(ErrorReason403.Forbidden, ErrorCode403.ErrorCode403_2)

  val mockMarriageDetailsErrorResponse422: MarriageDetailsErrorResponse422 = MarriageDetailsErrorResponse422(List())

  val underTest = new MarriageDetailsResponseMapper

  "MarriageDetailsResponseMapper" - {
    ".toResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        underTest.toApiResult(mockMarriageDetailsSuccessResponse) shouldBe
          SuccessResult(ApiName.MarriageDetails, mockMarriageDetailsSuccessResponse)
      }

      "should successfully return a Failure result when given an ErrorResponse400" in {

        underTest.toApiResult(mockMarriageDetailsErrorResponse400) shouldBe
          FailureResult(MarriageDetails, NpsNormalizedError.BadRequest)
      }

      "should successfully return a Failure result when given a ErrorResponse403" in {

        underTest.toApiResult(mockMarriageDetailsErrorResponse403) shouldBe
          FailureResult(MarriageDetails, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a Failure result when given a ErrorResponse422" in {

        underTest.toApiResult(mockMarriageDetailsErrorResponse422) shouldBe
          FailureResult(MarriageDetails, NpsNormalizedError.UnprocessableEntity)
      }
    }
  }

}
