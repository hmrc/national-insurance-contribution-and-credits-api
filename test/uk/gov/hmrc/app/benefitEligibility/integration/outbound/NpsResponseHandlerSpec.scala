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
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*

import scala.util.Random

class NpsResponseHandlerSpec extends AnyFreeSpec with MockFactory {

  val hipFailureResponse = NpsErrorResponseHipOrigin(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = NpsErrorReason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = NpsErrorReason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = NpsErrorReason("reason_3"))
      )
    )
  )

  val standardErrorResponse400 = NpsStandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = NpsMultiErrorResponse(
      failures = List(
        NpsSingleErrorResponse(reason = NpsErrorReason("reason_1"), code = NpsErrorCode("some error")),
        NpsSingleErrorResponse(reason = NpsErrorReason("reason_2"), code = NpsErrorCode("some error"))
      )
    )
  )

  val npsSingleErrorResponse =
    NpsSingleErrorResponse(reason = NpsErrorReason("reason_1"), code = NpsErrorCode("some error"))

  val npsMultiErrorResponse =
    NpsMultiErrorResponse(
      List(
        NpsSingleErrorResponse(code = NpsErrorCode("some code 1"), reason = NpsErrorReason("some reason 2")),
        NpsSingleErrorResponse(code = NpsErrorCode("some code 2"), reason = NpsErrorReason("some reason 2"))
      )
    )

  val npsErrorResponse422Special = NpsErrorResponse422Special(
    failures = Some(
      List(
        NpsSingleErrorResponse(code = NpsErrorCode("some code 1"), reason = NpsErrorReason("some reason 2")),
        NpsSingleErrorResponse(code = NpsErrorCode("some code 2"), reason = NpsErrorReason("some reason 2"))
      )
    ),
    askUser = Some(true),
    fixRequired = Some(true),
    workItemRaised = Some(true)
  )

  val randomApiName: ApiName = Random.shuffle(ApiName.values.toList).head

  val underTest: NpsResponseHandler = new NpsResponseHandler {
    val apiName: ApiName = randomApiName
  }

  "NpsResponseHandler" - {
    ".toSuccessResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        case class MockSuccessResponse() extends NpsSuccessfulApiResponse
        val mockSuccessResponse = MockSuccessResponse()

        underTest.toSuccessResult(mockSuccessResponse) shouldBe
          SuccessResult(randomApiName, mockSuccessResponse)
      }
    }
    ".toFailureResult" - {
      "should successfully convert to a FailureResult (with npsError)" in {

        val errors = Table("error", NpsNormalizedError.values.toList: _*)

        forAll(errors) { error =>
          underTest.toFailureResult(error, Some(hipFailureResponse)) shouldBe
            FailureResult(randomApiName, ErrorReport(error, Some(hipFailureResponse)))
        }
      }
      "should successfully convert to a FailureResult (without npsError)" in {

        val errors = Table("error", NpsNormalizedError.values.toList: _*)

        forAll(errors) { error =>
          underTest.toFailureResult(error, None) shouldBe
            FailureResult(randomApiName, ErrorReport(error, None))
        }
      }
    }
  }

}
