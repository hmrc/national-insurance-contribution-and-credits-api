/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.util.validation

import cats.data.{Validated, ValidatedNel}
import cats.implicits.catsSyntaxTuple2Semigroupal
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult.SuccessfulResult

object StringLengthValidation {

  def validate[T <: Product & AnyVal](t: T, minLength: Int, maxLength: Int): ValidatedNel[String, SuccessfulResult] =
    (
      Validated.condNel(
        t.productElement(0).toString.length >= minLength,
        SuccessfulResult,
        s"""${t.getClass().getSimpleName} value is below the minimum character limit of $minLength"""
      ),
      Validated.condNel(
        t.productElement(0).toString.length <= maxLength,
        SuccessfulResult,
        s"""${t.getClass().getSimpleName} value exceeds the max character limit of $maxLength"""
      )
    ).mapN((_, _) => SuccessfulResult)

}
