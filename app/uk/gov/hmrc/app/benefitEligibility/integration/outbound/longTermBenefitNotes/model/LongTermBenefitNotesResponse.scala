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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.*
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

    implicit val hipFailureResponse400Reads: Format[LongTermBenefitNotesHipFailureResponse400] =
      Json.format[LongTermBenefitNotesHipFailureResponse400]

  }

  case class LongTermBenefitNotesErrorItem400(reason: NpsErrorReason, code: NpsErrorCode400)

  object LongTermBenefitNotesErrorItem400 {

    implicit val longTermBenefitNotesErrorItem400Reads: Format[LongTermBenefitNotesErrorItem400] =
      Json.format[LongTermBenefitNotesErrorItem400]

  }

  case class LongTermBenefitNotesError400(failures: List[LongTermBenefitNotesErrorItem400])

  object LongTermBenefitNotesError400 {

    implicit val longTermBenefitNotesError400Reads: Format[LongTermBenefitNotesError400] =
      Json.format[LongTermBenefitNotesError400]

  }

  case class LongTermBenefitNotesStandardErrorResponse400(
      origin: HipOrigin,
      response: LongTermBenefitNotesError400
  ) extends LongTermBenefitNotesErrorResponse400

  object LongTermBenefitNotesStandardErrorResponse400 {

    implicit val standardErrorResponse400Reads: Format[LongTermBenefitNotesStandardErrorResponse400] =
      Json.format[LongTermBenefitNotesStandardErrorResponse400]

  }

  // endregion Error400

  // region Error403

  case class LongTermBenefitNotesErrorResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse403 {

    implicit val longTermBenefitNotesErrorResponse403Reads: Format[LongTermBenefitNotesErrorResponse403] =
      Json.format[LongTermBenefitNotesErrorResponse403]

  }

  // endregion Error403

  // region Error404

  case class LongTermBenefitNotesErrorResponse404(code: NpsErrorCode404, reason: NpsErrorReason404)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse404 {

    implicit val longTermBenefitNotesErrorResponse404Reads: Format[LongTermBenefitNotesErrorResponse404] =
      Json.format[LongTermBenefitNotesErrorResponse404]

  }

  // endregion Error404

  // region Error422

  case class LongTermBenefitNotesError422(reason: NpsErrorReason, code: ErrorCode422)

  object LongTermBenefitNotesError422 {

    implicit val longTermBenefitNotesError422Reads: Format[LongTermBenefitNotesError422] =
      Json.format[LongTermBenefitNotesError422]

  }

  case class LongTermBenefitNotesErrorResponse422(failures: Option[List[LongTermBenefitNotesError422]])
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesErrorResponse422 {

    implicit val longTermBenefitNotesErrorResponse422Reads: Format[LongTermBenefitNotesErrorResponse422] =
      Json.format[LongTermBenefitNotesErrorResponse422]

  }

  // endregion Error422

  // region Error500

  case class LongTermBenefitNotesHipFailureResponse500(origin: HipOrigin, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesHipFailureResponse500 {

    implicit val hipFailureResponse500Reads: Format[LongTermBenefitNotesHipFailureResponse500] =
      Json.format[LongTermBenefitNotesHipFailureResponse500]

  }

  // endregion Error500

  // region Error503

  case class LongTermBenefitNotesHipFailureResponse503(origin: HipOrigin, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object LongTermBenefitNotesHipFailureResponse503 {

    implicit val hipFailureResponse503Reads: Format[LongTermBenefitNotesHipFailureResponse503] =
      Json.format[LongTermBenefitNotesHipFailureResponse503]

  }

  // endregion Error503

}

object LongTermBenefitNotesSuccess {

  case class Note(value: String) extends AnyVal

  object Note {
    implicit val noteReads: Format[Note] = Json.valueFormat[Note]
  }

  case class LongTermBenefitNotesSuccessResponse(longTermBenefitNotes: List[Note])
      extends LongTermBenefitNotesResponse
      with NpsSuccessfulApiResponse

  object LongTermBenefitNotesSuccessResponse {

    implicit val longTermBenefitNotesSuccessResponseReads: Format[LongTermBenefitNotesSuccessResponse] =
      Json.format[LongTermBenefitNotesSuccessResponse]

  }

}
