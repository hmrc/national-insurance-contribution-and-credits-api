package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response

import cats.data.Validated
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object LongTermBenefitNotesResponseValidation {

  // region Success Validation

  implicit val longTermBenefitNotesResponseValidator
  : NpsResponseValidator[LongTermBenefitNotesSuccessResponse] = {
    (_, response: LongTermBenefitNotesSuccessResponse) => Validated.validNel(SuccessfulResult)
  }

  // endregion Success Validation

}