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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model

import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.validation.{MoneyValidation, NumberValidation}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object IndividualStatePensionInformationResponseValidation {

  implicit val individualStatePensionInformationResponseValidator
      : NpsResponseValidator[IndividualStatePensionInformationSuccessResponse] =
    (_, response: IndividualStatePensionInformationSuccessResponse) =>
      (List(response.numberOfQualifyingYears.map(n => NumberValidation.validate(n, 0, 100))).flatten ++
        (response.contributionsByTaxYear match {
          case Some(contributionsByTaxYear) =>
            contributionsByTaxYear.flatMap { el =>
              List(
                el.primaryPaidEarnings.map(p =>
                  MoneyValidation.validate(p, MoneyValidation.Defaults.signedMin, 2, BigDecimal("0.01"))
                )
              ).flatten
            }
          case None => List()
        })).sequence_.as(SuccessfulResult)

}
