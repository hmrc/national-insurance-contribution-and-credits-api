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
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.validation.{MoneyValidation, NumberValidation}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object NiContributionsAndCreditsResponseValidation {

  implicit val niContributionsAndCreditsSuccessResponseValidator
      : NpsResponseValidator[NiContributionsAndCreditsSuccessResponse] =
    (benefitType, response: NiContributionsAndCreditsSuccessResponse) =>
      benefitType match {
        case BenefitType.MA =>
          (if (response.niClass1.isDefined) {
             response.niClass1.get.flatMap { el =>
               List(
                 el.taxYear.map(NumberValidation.validate(_, 1900, 2099)),
                 el.primaryPaidEarnings.map(MoneyValidation.validateUnsigned)
               ).flatten
             }
           } else {
             response.niClass2.get.flatMap { el =>
               List(
                 el.taxYear.map(NumberValidation.validate(_, 1900, 2099)),
                 el.noOfCreditsAndConts.map(NumberValidation.validate(_, 0, 53))
               ).flatten
             }
           }).sequence_.as(SuccessfulResult)
        case _ => Validated.validNel(SuccessfulResult)
      }

}
