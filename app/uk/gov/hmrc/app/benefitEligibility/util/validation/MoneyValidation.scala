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
import cats.implicits.{catsSyntaxNestedFoldable, toFunctorOps}
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult.SuccessfulResult

object MoneyValidation {

  object Defaults {

    val max: BigDecimal = BigDecimal("99999999999999.98")

    val signedMin: BigDecimal        = BigDecimal("-99999999999999.98")
    val signedMaxScale: Int          = 4
    val signedMultipleOf: BigDecimal = BigDecimal("0.0001")

    val unsignedMin: BigDecimal        = BigDecimal("0")
    val unsignedMaxScale: Int          = 2
    val unsignedMultipleOf: BigDecimal = BigDecimal("0.01")

  }

  private def validateMultipleOf[T <: Product & AnyVal](
      multipleOf: BigDecimal,
      valueName: String,
      value: BigDecimal
  ): ValidatedNel[String, SuccessfulResult] =
    Validated.condNel(
      value % multipleOf == 0,
      SuccessfulResult,
      s"""$valueName value is not a multiple of ${multipleOf.toString()}"""
    )

  private def validateScale[T <: Product & AnyVal](
      maxScale: Int,
      valueName: String,
      value: BigDecimal
  ): ValidatedNel[String, SuccessfulResult] =
    Validated.condNel(
      value.scale <= maxScale,
      SuccessfulResult,
      s"""$valueName value exceeds the limit of $maxScale decimal places"""
    )

  private def validateMin[T <: Product & AnyVal](
      min: BigDecimal,
      valueName: String,
      value: BigDecimal
  ): ValidatedNel[String, SuccessfulResult] =
    Validated.condNel(
      value >= min,
      SuccessfulResult,
      s"""$valueName value is below the minimum allowed limit of $min"""
    )

  private def validateMax[T <: Product & AnyVal](
      valueName: String,
      value: BigDecimal
  ): ValidatedNel[String, SuccessfulResult] = {
    val max = Defaults.max
    Validated.condNel(
      value <= max,
      SuccessfulResult,
      s"""$valueName value exceeds the maximum allowed limit of $max"""
    )
  }

  def validate[T <: Product & AnyVal](
      value: T,
      min: BigDecimal,
      maxScale: Int,
      multipleOf: BigDecimal
  ): ValidatedNel[String, SuccessfulResult] =
    List(
      validateMultipleOf(multipleOf, value.getClass().getSimpleName, BigDecimal(value.productElement(0).toString)),
      validateScale(maxScale, value.getClass().getSimpleName, BigDecimal(value.productElement(0).toString)),
      validateMin(min, value.getClass().getSimpleName, BigDecimal(value.productElement(0).toString)),
      validateMax(value.getClass().getSimpleName, BigDecimal(value.productElement(0).toString))
    ).sequence_.as(SuccessfulResult)

  def validateUnsigned[T <: Product & AnyVal](
      value: T
  ): ValidatedNel[String, SuccessfulResult] =
    validate(
      value = value,
      min = Defaults.unsignedMin,
      maxScale = Defaults.unsignedMaxScale,
      multipleOf = Defaults.unsignedMultipleOf
    )

  def validateSigned[T <: Product & AnyVal](
      value: T
  ): ValidatedNel[String, SuccessfulResult] =
    validate(
      value = value,
      min = Defaults.signedMin,
      maxScale = Defaults.signedMaxScale,
      multipleOf = Defaults.signedMultipleOf
    )

}
