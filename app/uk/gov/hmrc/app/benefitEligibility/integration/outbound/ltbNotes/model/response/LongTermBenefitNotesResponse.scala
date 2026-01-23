package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.{
  ErrorCode422,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorCode404,
  NpsErrorReason403,
  NpsErrorReason404,
  Reason
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.enums.HiporiginEnum
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

sealed trait LongTermBenefitNotesResponse extends NpsApiResponse

object LongTermBenefitNotesError {

  case class HipFailureItem(`type`: String, reason: Reason)

  object HipFailureItem {
    implicit val hipFailureItemReads: Reads[HipFailureItem] = Json.reads[HipFailureItem]
  }

  case class HipFailureResponse(failures: List[HipFailureItem])

  object HipFailureResponse {
    implicit val hipFailureResponseReads: Reads[HipFailureResponse] = Json.reads[HipFailureResponse]
  }

  // region Error400

  sealed trait LongTermBenefitNotesErrorResponse400 extends LongTermBenefitNotesResponse

  case class HipFailureResponse400(origin: HiporiginEnum, response: HipFailureResponse)
      extends LongTermBenefitNotesErrorResponse400

  object HipFailureResponse400 {
    implicit val hipFailureResponse400Reads: Reads[HipFailureResponse400] = Json.reads[HipFailureResponse400]
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

  case class StandardErrorResponse400(
      origin: HiporiginEnum,
      response: LongTermBenefitNotesError400
  ) extends LongTermBenefitNotesErrorResponse400

  object StandardErrorResponse400 {
    implicit val standardErrorResponse400Reads: Reads[StandardErrorResponse400] = Json.reads[StandardErrorResponse400]
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

  case class HipFailureResponse500(origin: HiporiginEnum, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object HipFailureResponse500 {
    implicit val hipFailureResponse500Reads: Reads[HipFailureResponse500] = Json.reads[HipFailureResponse500]
  }

  // endregion Error500

  // region Error503

  case class HipFailureResponse503(origin: HiporiginEnum, response: HipFailureResponse)
      extends LongTermBenefitNotesResponse

  object HipFailureResponse503 {
    implicit val hipFailureResponse503Reads: Reads[HipFailureResponse503] = Json.reads[HipFailureResponse503]
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
