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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model

import cats.data.{Validated, ValidatedNel}
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult.SuccessfulResult
import uk.gov.hmrc.app.benefitEligibility.util.validation.{
  MoneyValidation,
  StringLengthValidation,
  StringPatternValidation
}
import uk.gov.hmrc.app.benefitEligibility.util.{NpsResponseValidator, SuccessfulResult}

object Class2MAReceiptsResponseValidation {

  implicit val class2MAReceiptsSuccessResponseValidator: NpsResponseValidator[Class2MAReceiptsSuccessResponse] =
    (_, response: Class2MAReceiptsSuccessResponse) =>
      (response.class2MAReceiptDetails.flatMap { el =>
        List(
          el.surname.map(s => StringPatternValidation.validate(s, "^([A-Za-z '-])+$".r)),
          el.surname.map(s => StringLengthValidation.validate(s, 2, 99)),
          el.initials.map(i => StringPatternValidation.validate(i, "^([A-Za-z '-])+$".r)),
          el.initials.map(i => StringLengthValidation.validate(i, 1, 2)),
          el.billAmount.map(MoneyValidation.validateSigned),
          el.receivablePayment.map(MoneyValidation.validateSigned)
        ).flatten
      } ++
        List(
          StringPatternValidation.validate(response.identifier, Identifier.pattern)
        )).sequence_.as(SuccessfulResult)

}
