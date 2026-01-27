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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*

import scala.util.Random

class NpsResponseMapperV2Spec extends AnyFreeSpec with MockFactory {

  val hipFailureResponse = NpsErrorResponseHipOrigin(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse503 = NpsErrorResponse503(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse500 = NpsErrorResponse500(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val standardErrorResponse400 = NpsStandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = ErrorResponse400(
      failures = List(
        ErrorResourceObj400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
        ErrorResourceObj400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
      )
    )
  )

  val npsErrorResponse403 =
    NpsErrorResponse403(NpsErrorCode403.NpsErrorCode403_2, NpsErrorReason403.Forbidden)

  val npsErrorResponse404 =
    NpsErrorResponse404(NpsErrorCode("some code"), Reason("some reason"))

  val npsErrorResponse422 =
    NpsErrorResponse422(
      List(
        NpsError422(NpsErrorCode("some code 1"), Reason("some reason 2")),
        NpsError422(NpsErrorCode("some code 2"), Reason("some reason 2"))
      )
    )

  val randomApiName: ApiName = Random.shuffle(ApiName.values.toList).head

  val underTest: NpsResponseMapperV2 = new NpsResponseMapperV2 {
    val apiName: ApiName = randomApiName
  }

  "NpsResponseMapperV2" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        case class MockSuccessResponse() extends NpsSuccessfulApiResponse
        val mockSuccessResponse = MockSuccessResponse()

        underTest.toApiResult(mockSuccessResponse) shouldBe
          DownstreamSuccessResponse(randomApiName, mockSuccessResponse)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (HipFailureResponse)" in {
        underTest.toApiResult(hipFailureResponse) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (StandardErrorResponse400)" in {

        underTest.toApiResult(standardErrorResponse400) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 403" in {
        underTest.toApiResult(npsErrorResponse403) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a DownstreamErrorReport result when handling a 404" in {
        underTest.toApiResult(npsErrorResponse404) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.NotFound)
      }

      "should successfully return a DownstreamErrorReport result when handling a 422" in {
        underTest.toApiResult(npsErrorResponse422) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.UnprocessableEntity)
      }

      "should successfully return a DownstreamErrorReport result when when handling a 500" in {
        underTest.toApiResult(npsErrorResponse500) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.InternalServerError)
      }

      "should successfully return a DownstreamErrorReport result when when handling a 503" in {
        underTest.toApiResult(npsErrorResponse503) shouldBe
          DownstreamErrorReport(randomApiName, NpsNormalizedError.ServiceUnavailable)
      }

    }
  }

}
