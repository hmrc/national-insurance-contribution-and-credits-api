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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  ErrorCode422,
  FailureType,
  HipFailureItem,
  HipFailureResponse,
  HipOrigin,
  NpsErrorCode403,
  NpsErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.response.LongTermBenefitNotesError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.response.LongTermBenefitNotesSuccess.*

class LongTermBenefitNotesResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val longTermBenefitNotesSuccessResponse = LongTermBenefitNotesSuccessResponse(
    longTermBenefitNotes = List.empty
  )

  val longTermBenefitNotesHipFailureResponse400 = LongTermBenefitNotesHipFailureResponse400(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val longTermBenefitNotesStandardErrorResponse400 = LongTermBenefitNotesStandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = LongTermBenefitNotesError400(
      failures = List(
        LongTermBenefitNotesErrorItem400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
        LongTermBenefitNotesErrorItem400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
      )
    )
  )

  val longTermBenefitNotesErrorResponse403 =
    LongTermBenefitNotesErrorResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

  val longTermBenefitNotesErrorResponse404 =
    LongTermBenefitNotesErrorResponse404(NpsErrorCode404.ErrorCode404, NpsErrorReason404.NotFound)

  val longTermBenefitNotesErrorResponse422 =
    LongTermBenefitNotesErrorResponse422(
      Some(List(LongTermBenefitNotesError422(Reason("Reason"), ErrorCode422("Code"))))
    )

  val longTermBenefitNotesHipFailureResponse500 = LongTermBenefitNotesHipFailureResponse500(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val longTermBenefitNotesHipFailureResponse503 = LongTermBenefitNotesHipFailureResponse503(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val underTest = new LongTermBenefitNotesResponseMapper

  "LongTermBenefitNotesResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {
        underTest.toApiResult(longTermBenefitNotesSuccessResponse) shouldBe
          DownstreamSuccessResponse(ApiName.LongTermBenefitNotes, longTermBenefitNotesSuccessResponse)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (LongTermBenefitNotesHipFailureResponse400)" in {
        underTest.toApiResult(longTermBenefitNotesHipFailureResponse400) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (LongTermBenefitNotesStandardErrorResponse400)" in {

        underTest.toApiResult(longTermBenefitNotesStandardErrorResponse400) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.BadRequest)
      }

      "should successfully return a DownstreamErrorReport result when handling a 403" in {
        underTest.toApiResult(longTermBenefitNotesErrorResponse403) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a DownstreamErrorReport result when handling a 404" in {
        underTest.toApiResult(longTermBenefitNotesErrorResponse404) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.NotFound)
      }

      "should successfully return a DownstreamErrorReport result when handling a 422" in {
        underTest.toApiResult(longTermBenefitNotesErrorResponse422) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.UnprocessableEntity)
      }

      "should successfully return a DownstreamErrorReport result when handling a 500" in {
        underTest.toApiResult(longTermBenefitNotesHipFailureResponse500) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.InternalServerError)
      }

      "should successfully return a DownstreamErrorReport result when when handling a 503" in {
        underTest.toApiResult(longTermBenefitNotesHipFailureResponse503) shouldBe
          DownstreamErrorReport(ApiName.LongTermBenefitNotes, NpsNormalizedError.ServiceUnavailable)
      }
    }
  }

}
