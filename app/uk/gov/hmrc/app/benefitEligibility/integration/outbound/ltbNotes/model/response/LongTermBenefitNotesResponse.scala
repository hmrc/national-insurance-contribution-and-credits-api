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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response

import play.api.libs.json.{Format, JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

sealed trait LongTermBenefitNotesResponse extends NpsApiResponse

object LongTermBenefitNotesError {

  // region Error400

  sealed trait LongTermBenefitNotesErrorResponse400 extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse400 {

    implicit val longTermBenefitNotesErrorResponse400Reads: Reads[LongTermBenefitNotesErrorResponse400] =
      Reads[LongTermBenefitNotesErrorResponse400] { resp =>
        LongTermBenefitNotesStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
          case JsSuccess(value, path) => JsSuccess(value, path)
          case JsError(errors) =>
            LongTermBenefitNotesHipFailureResponse400.hipFailureResponse400Reads.reads(resp) match {
              case JsSuccess(value, path) => JsSuccess(value, path)
              case JsError(errors)        => JsError(errors)
            }
        }
      }

  }

  case class LongTermBenefitNotesHipFailureResponse400(origin: HipOrigin, response: HipFailureResponse)
      extends LongTermBenefitNotesErrorResponse400

  object LongTermBenefitNotesHipFailureResponse400 {

    implicit val hipFailureResponse400Reads: Reads[LongTermBenefitNotesHipFailureResponse400] =
      Json.reads[LongTermBenefitNotesHipFailureResponse400]

  }

  case class LongTermBenefitNotesErrorItem400(reason: Reason, code: NpsErrorCode400)

  object LongTermBenefitNotesErrorItem400 {

    implicit val longTermBenefitNotesErrorItem400Reads: Reads[LongTermBenefitNotesErrorItem400] =
      Json.reads[LongTermBenefitNotesErrorItem400]

  }

  case class LongTermBenefitNotesError400(failures: List[LongTermBenefitNotesErrorItem400])

  object LongTermBenefitNotesError400 {

    implicit val longTermBenefitNotesError400Reads: Reads[LongTermBenefitNotesError400] =
      Json.reads[LongTermBenefitNotesError400]

  }

  case class LongTermBenefitNotesStandardErrorResponse400(
      origin: HipOrigin,
      response: LongTermBenefitNotesError400
  ) extends LongTermBenefitNotesErrorResponse400

  object LongTermBenefitNotesStandardErrorResponse400 {

    implicit val standardErrorResponse400Reads: Reads[LongTermBenefitNotesStandardErrorResponse400] =
      Json.reads[LongTermBenefitNotesStandardErrorResponse400]

  }

  // endregion Error400

  // region Error403

  case class LongTermBenefitNotesErrorResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse403 {

    implicit val longTermBenefitNotesErrorResponse403Reads: Reads[LongTermBenefitNotesErrorResponse403] =
      Json.reads[LongTermBenefitNotesErrorResponse403]

  }

  // endregion Error403

  // region Error404

  case class LongTermBenefitNotesErrorResponse404(code: NpsErrorCode404, reason: NpsErrorReason404)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse404 {

    implicit val longTermBenefitNotesErrorResponse404Reads: Reads[LongTermBenefitNotesErrorResponse404] =
      Json.reads[LongTermBenefitNotesErrorResponse404]

  }

  // endregion Error404

  // region Error422

  case class LongTermBenefitNotesError422(reason: Reason, code: ErrorCode422)

  object LongTermBenefitNotesError422 {

    implicit val longTermBenefitNotesError422Reads: Reads[LongTermBenefitNotesError422] =
      Json.reads[LongTermBenefitNotesError422]

  }

  case class LongTermBenefitNotesErrorResponse422(failures: Option[List[LongTermBenefitNotesError422]])
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse422 {

    implicit val longTermBenefitNotesErrorResponse422Reads: Reads[LongTermBenefitNotesErrorResponse422] =
      Json.reads[LongTermBenefitNotesErrorResponse422]

  }

  // endregion Error422

  // region Error500

  case class LongTermBenefitNotesHipFailureResponse500(origin: HipOrigin, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesHipFailureResponse500 {

    implicit val hipFailureResponse500Reads: Reads[LongTermBenefitNotesHipFailureResponse500] =
      Json.reads[LongTermBenefitNotesHipFailureResponse500]

  }

  // endregion Error500

  // region Error503

  case class LongTermBenefitNotesHipFailureResponse503(origin: HipOrigin, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesHipFailureResponse503 {

    implicit val hipFailureResponse503Reads: Reads[LongTermBenefitNotesHipFailureResponse503] =
      Json.reads[LongTermBenefitNotesHipFailureResponse503]

  }

  // endregion Error503

}

object LongTermBenefitNotesSuccess {

  case class Note(value: String) extends AnyVal

  object Note {
    implicit val noteReads: Reads[Note] = Json.valueReads[Note]
  }

  case class LongTermBenefitNotesSuccessResponse(longTermBenefitNotes: List[Note])
      extends LongTermBenefitNotesResponse
      with NpsSuccessfulApiResponse

  object LongTermBenefitNotesSuccessResponse {

    implicit val longTermBenefitNotesSuccessResponseReads: Reads[LongTermBenefitNotesSuccessResponse] =
      Json.reads[LongTermBenefitNotesSuccessResponse]

  }

}
