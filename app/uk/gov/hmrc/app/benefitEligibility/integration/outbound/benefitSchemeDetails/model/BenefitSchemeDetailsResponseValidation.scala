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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model

import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.BenefitSchemeDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.validation.{
  MoneyValidation,
  NumberValidation,
  StringLengthValidation,
  StringPatternValidation
}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object BenefitSchemeDetailsResponseValidation {

  implicit val benefitSchemeDetailsResponseValidationValidator
      : NpsResponseValidator[BenefitSchemeDetailsSuccessResponse] =
    (_, response: BenefitSchemeDetailsSuccessResponse) =>
      List(
        response.benefitSchemeDetails.schemeName.map(name => StringLengthValidation.validate(name, 1, 54)),
        response.benefitSchemeDetails.schemeName.map(name =>
          StringPatternValidation.validate(name, """^[a-zA-Z0-9\\/,'.&() -]+$""".r)
        )
      ).flatten.sequence_.as(SuccessfulResult)

}
