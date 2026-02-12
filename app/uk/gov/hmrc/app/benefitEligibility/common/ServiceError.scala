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

package uk.gov.hmrc.app.benefitEligibility.common

import cats.Semigroup
import io.scalaland.chimney.dsl.into
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{ApiResult, EligibilityCheckDataResult}

import scala.collection.immutable

trait WithLoggableDebugString {
  def toStringSafeToLogInProd: String
}

sealed trait ServiceError extends Exception with WithLoggableDebugString {
  this: Product & Serializable & (BenefitEligibilityError | NpsClientError) =>
  def toStringSafeToLogInProd: String = this.toString

  override final def toString: String = {
    val elements: Seq[(String, Any)] = this.productElementNames.zip(this.productIterator).toSeq
    s"${this.getClass.getName}(${elements.map { case (name, value) => s"$name = $value" }.mkString(", ")})"
  }

}

sealed trait BenefitEligibilityError extends ServiceError { this: Product with Serializable => }

object BenefitEligibilityError {

  implicit val benefitEligibilityErrorSemiGroup: Semigroup[BenefitEligibilityError] =
    new Semigroup[BenefitEligibilityError] {
      override def combine(x: BenefitEligibilityError, y: BenefitEligibilityError): BenefitEligibilityError =
        (x, y) match {
          case (_, _) => DataRetrievalServiceError()
        }
    }

}

case class ValidationError(errors: List[String]) extends BenefitEligibilityError {
  override def getMessage: String = errors.mkString(",")
} //TODO - should return as a 500 to DWP

case class ParsingError(throwable: Throwable) extends BenefitEligibilityError {
  override def getMessage: String = throwable.getMessage
} //TODO - should return as a 500 to DWP

case class NpsClientError(throwable: Throwable) extends BenefitEligibilityError {
  override def getMessage: String = throwable.getMessage
} //TODO - should return as a 500 to DWP

case class DataRetrievalServiceError() extends BenefitEligibilityError
