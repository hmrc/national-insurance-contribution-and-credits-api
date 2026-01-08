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

package uk.gov.hmrc.app.benefitEligibility.util

import cats.data.Validated
import cats.implicits.catsSyntaxEitherId
import play.api.libs.json.Reads
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitEligibilityError, ParsingError, ValidationError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponse
import uk.gov.hmrc.app.benefitEligibility.util.ValidatorSyntax.ValidatorOps
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.util.{Failure, Success, Try}

object HttpParsing {

  def attemptStrictParse[T <: NpsApiResponse](
      response: HttpResponse
  )(
      implicit reads: Reads[T],
      headerCarrier: HeaderCarrier,
      validator: NpsResponseValidator[T]
  ): Either[BenefitEligibilityError, T] =
    attemptParse(response).flatMap { value =>
      value.validate match {
        case Validated.Valid(a)   => Right(value)
        case Validated.Invalid(e) => Left(ValidationError(e.toList))
      }
    }

  def attemptParse[T <: NpsApiResponse](
      response: HttpResponse
  )(
      implicit reads: Reads[T],
      headerCarrier: HeaderCarrier
  ): Either[BenefitEligibilityError, T] =
    Try(response.json) match {
      case Success(value) =>
        value
          .validate[T]
          .fold(
            errors => ValidationError(errors.flatMap(_._2.flatMap(_.messages)).toList).asLeft[T],
            value => Right(value)
          )
      case Failure(exception) => ParsingError(exception).asLeft[T]
    }

}
