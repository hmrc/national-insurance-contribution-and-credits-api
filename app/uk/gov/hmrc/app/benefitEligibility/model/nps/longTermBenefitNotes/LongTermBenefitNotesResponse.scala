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

package uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitNotes

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.{NpsApiResponse, NpsSuccessfulApiResponse}

sealed trait LongTermBenefitNotesResponse extends NpsApiResponse

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
