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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response

import cats.data.Validated
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.{NiContributionsAndCreditsResponse400, NiContributionsAndCreditsResponse403, NiContributionsAndCreditsResponse422}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object NiContributionsAndCreditsResponseValidation {

  implicit val niContributionsAndCreditsSuccessResponseValidator
      : NpsResponseValidator[NiContributionsAndCreditsSuccessResponse] =
    (response: NiContributionsAndCreditsSuccessResponse) =>
      (response.niClass1.flatMap { el =>
        List(
          Some(Validated.validNel(SuccessfulResult)),
          Some(Validated.validNel(SuccessfulResult))
        ).flatten
      } ++
        List(
          Validated.validNel(SuccessfulResult)
        )).sequence_.as(SuccessfulResult)

  implicit val niContributionsAndCreditsResponse400Validator
      : NpsResponseValidator[NiContributionsAndCreditsResponse400] =
    (t: NiContributionsAndCreditsResponse400) => Validated.validNel(SuccessfulResult)

  implicit val niContributionsAndCreditsResponse403Validator
      : NpsResponseValidator[NiContributionsAndCreditsResponse403] =
    (t: NiContributionsAndCreditsResponse403) => Validated.validNel(SuccessfulResult)

  implicit val niContributionsAndCreditsResponse422Validator
      : NpsResponseValidator[NiContributionsAndCreditsResponse422] =
    (t: NiContributionsAndCreditsResponse422) => Validated.validNel(SuccessfulResult)

}
