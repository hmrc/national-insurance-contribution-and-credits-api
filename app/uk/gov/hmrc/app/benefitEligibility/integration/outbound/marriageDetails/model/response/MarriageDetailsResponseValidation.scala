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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsError.{
  MarriageDetailsErrorResponse400,
  MarriageDetailsErrorResponse403,
  MarriageDetailsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult.SuccessfulResult
import uk.gov.hmrc.app.benefitEligibility.util.validation.{
  NumberValidation,
  StringLengthValidation,
  StringPatternValidation
}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object MarriageDetailsResponseValidation {

  implicit val marriageDetailsSuccessResponseValidator: NpsResponseValidator[MarriageDetailsSuccessResponse] =
    (response: MarriageDetailsSuccessResponse) =>
      response.marriageDetailsList
        .map { marriageDetailsList =>
          List(
            marriageDetailsList.spouseForename.map(forename =>
              StringPatternValidation.validate(forename, "^([A-Za-z '-]{1,99})+$".r)
            ),
            marriageDetailsList.spouseSurname.map(surname =>
              StringPatternValidation.validate(surname, "^([A-Za-z '-]{2,99})+$".r)
            ),
            marriageDetailsList.sequenceNumber.map(number =>
              NumberValidation.validate(number, minValue = 1, maxValue = 126)
            )
          ).flatten

        }
        .map(_.sequence_.as(SuccessfulResult))
        .getOrElse(Validated.validNel[String, SuccessfulResult](SuccessfulResult))

  implicit val marriageDetailsError400Validator: NpsResponseValidator[MarriageDetailsErrorResponse400] =
    (t: MarriageDetailsErrorResponse400) =>
      t.failures
        .map(f => StringLengthValidation.validate(f.reason, minLength = 1, maxLength = 120))
        .sequence_
        .as(SuccessfulResult)

  implicit val marriageDetailsErrorResponse403Validator: NpsResponseValidator[MarriageDetailsErrorResponse403] =
    (t: MarriageDetailsErrorResponse403) => Validated.validNel(SuccessfulResult)

  implicit val marriageDetailsError422Validator: NpsResponseValidator[MarriageDetailsErrorResponse422] =
    (t: MarriageDetailsErrorResponse422) =>
      t.failures
        .map(f => StringLengthValidation.validate(f.reason, minLength = 1, maxLength = 120))
        .sequence_
        .as(SuccessfulResult)

}
