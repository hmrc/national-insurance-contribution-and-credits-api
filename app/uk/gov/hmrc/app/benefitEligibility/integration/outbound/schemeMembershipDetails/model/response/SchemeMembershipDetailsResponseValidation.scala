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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.validation.{StringLengthValidation, StringPatternValidation}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object SchemeMembershipDetailsResponseValidation {

  implicit val schemeMembershipDetailsSuccessResponseValidator
      : NpsResponseValidator[SchemeMembershipDetailsSuccessResponse] =
    (_, response: SchemeMembershipDetailsSuccessResponse) =>
      response.schemeMembershipDetailsSummaryList
        .getOrElse(Nil)
        .flatMap { el =>
          List(
            el.schemeMembershipDetails.employersContractedOutNumberDetails.map(
              StringPatternValidation.validate(_, "^([A-Z]{0,1}[3]\\d{6}[A-Z ^GIO SUVZ]{0,1})$".r)
            ),
            el.schemeMembershipDetails.employersContractedOutNumberDetails.map(StringLengthValidation.validate(_, 9, 7))
          ).flatten
        }
        .sequence_
        .as(SuccessfulResult)

}
