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

package uk.gov.hmrc.app.benefitEligibility.model.common

import cats.Semigroup
import uk.gov.hmrc.app.benefitEligibility.model.response.ErrorReason

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
          case (DataRetrievalServiceError(a), DataRetrievalServiceError(b)) => DataRetrievalServiceError(a ++ b)
          case (DataRetrievalServiceError(errors), b)                       => DataRetrievalServiceError(b +: errors)
          case (a, DataRetrievalServiceError(errors))                       => DataRetrievalServiceError(a +: errors)
          case (a, b)                                                       => DataRetrievalServiceError(List(a, b))
        }
    }

}

case class UnprocessableDataError(errors: List[String]) extends BenefitEligibilityError {
  override def getMessage: String = errors.mkString(",")
}

case class InvalidCursorId(errorReason: ErrorReason) extends BenefitEligibilityError {
  override def getMessage: String = errorReason.value
}

case class InvalidOriginatorId(errorReason: ErrorReason) extends BenefitEligibilityError {
  override def getMessage: String = errorReason.value
}

case class MissingCursorId(errorReason: ErrorReason) extends BenefitEligibilityError {
  override def getMessage: String = errorReason.value
}

case class InvalidOrMissingHeaderError(errorReason: ErrorReason) extends BenefitEligibilityError {
  override def getMessage: String = errorReason.value
}

case class JsonParsingError(errors: List[String]) extends BenefitEligibilityError {
  override def getMessage: String = errors.mkString(",")
}

case class InvalidRequestJson(errorReason: ErrorReason) extends BenefitEligibilityError {
  override def getMessage: String = errorReason.value
}

case class InvalidJsonError(throwable: Throwable) extends BenefitEligibilityError {
  override def getMessage: String = throwable.getMessage
}

case class NpsClientError(throwable: Throwable) extends BenefitEligibilityError {
  override def getMessage: String = throwable.getMessage
}

case class DataRetrievalServiceError(errors: List[BenefitEligibilityError]) extends BenefitEligibilityError

case class RecordNotFound(cursorId: CursorId) extends BenefitEligibilityError

case class DatabaseError(throwable: Throwable) extends BenefitEligibilityError {
  override def getMessage: String = throwable.getMessage
}

case class FeatureDisabled(message: String) extends BenefitEligibilityError {
  override def getMessage: String = message
}

case class ContributionCreditTaxWindowCalculatorError(message: String) extends BenefitEligibilityError
