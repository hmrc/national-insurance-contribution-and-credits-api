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
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.MarriageDetailsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsNormalizedError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError.{
  MarriageDetailsErrorResponse400,
  MarriageDetailsErrorResponse403,
  MarriageDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse

class MarriageDetailsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val mockMarriageDetailsSuccessResponse: MarriageDetailsSuccessResponse   = mock[MarriageDetailsSuccessResponse]
  val mockMarriageDetailsErrorResponse400: MarriageDetailsErrorResponse400 = mock[MarriageDetailsErrorResponse400]
  val mockMarriageDetailsErrorResponse403: MarriageDetailsErrorResponse403 = mock[MarriageDetailsErrorResponse403]
  val mockMarriageDetailsErrorResponse422: MarriageDetailsErrorResponse422 = mock[MarriageDetailsErrorResponse422]

  val underTest = new MarriageDetailsResponseMapper

  "MarriageDetailsResponseMapper" - {
    ".toResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        underTest.toResult(mockMarriageDetailsSuccessResponse) shouldBe
          MarriageDetailsResult(Success, Some(mockMarriageDetailsSuccessResponse), None)
      }

      "should successfully return a Failure result when given an ErrorResponse400" in {

        underTest.toResult(mockMarriageDetailsErrorResponse400) shouldBe
          MarriageDetailsResult(
            Failure,
            None,
            Some(
              NpsNormalizedError(
                code = BadRequest,
                message = "downstream received a malformed request",
                downstreamStatus = 400
              )
            )
          )
      }

      "should successfully return a Failure result when given a ErrorResponse403" in {

        underTest.toResult(mockMarriageDetailsErrorResponse403) shouldBe
          MarriageDetailsResult(
            Failure,
            None,
            Some(
              NpsNormalizedError(
                code = AccessForbidden,
                message = "downstream resource cannot be accessed by the calling client",
                downstreamStatus = 403
              )
            )
          )
      }

      "should successfully return a Failure result when given a ErrorResponse422" in {

        underTest.toResult(mockMarriageDetailsErrorResponse422) shouldBe
          MarriageDetailsResult(
            Failure,
            None,
            Some(
              NpsNormalizedError(
                code = UnprocessableEntity,
                message = "downstream could not process data in request",
                downstreamStatus = 422
              )
            )
          )
      }
    }

    "should successfully return a Failure result when given an UnprocessableEntity TextualErrorStatusCode" in {

      underTest.toResult(UnprocessableEntity) shouldBe
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              UnprocessableEntity,
              UnprocessableEntity.message,
              UnprocessableEntity.code
            )
          )
        )
    }

    "should successfully return a Failure result when given a BadRequest TextualErrorStatusCode" in {

      underTest.toResult(BadRequest) shouldBe
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              BadRequest,
              BadRequest.message,
              BadRequest.code
            )
          )
        )
    }

    "should successfully return a Failure result when given an AccessForbidden TextualErrorStatusCode" in {

      underTest.toResult(AccessForbidden) shouldBe
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              AccessForbidden,
              AccessForbidden.message,
              AccessForbidden.code
            )
          )
        )
    }

    "should successfully return a Failure result when given a NotFound TextualErrorStatusCode" in {

      underTest.toResult(NotFound) shouldBe
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              NotFound,
              NotFound.message,
              NotFound.code
            )
          )
        )
    }

    "should successfully return a Failure result when given an InternalServerError TextualErrorStatusCode" in {

      underTest.toResult(InternalServerError) shouldBe
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              InternalServerError,
              InternalServerError.message,
              InternalServerError.code
            )
          )
        )
    }
  }

}
