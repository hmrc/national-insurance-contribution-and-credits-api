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

package uk.gov.hmrc.app.benefitEligibility.controller

import cats.data.Validated
import cats.implicits.catsSyntaxTuple6Semigroupal
import play.api.libs.json.JsonValidationError
import uk.gov.hmrc.app.benefitEligibility.model.common.{CorrelationId, Identifier, OriginatorIdType, PaginationType}
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.response.ErrorReason
import uk.gov.hmrc.app.benefitEligibility.util.{
  ContributionCreditTaxWindowCalculator,
  RequestAwareLogger,
  SuccessfulResult
}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.util.Try

object RequestValidations {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def validateRequest(
      request: EligibilityCheckDataRequest
  ): Either[JsonValidationError, SuccessfulResult.type] = {

    val shouldPageForContributionsAndCredits =
      PaginationType.from(request.benefitType).toList.diff(List(PaginationType.MaPagination)).nonEmpty

    val hasOneTaxWindow = ContributionCreditTaxWindowCalculator
      .createTaxWindows(
        request.niContributionsAndCredits.startTaxYear,
        request.niContributionsAndCredits.endTaxYear
      )
      .exists(_.size == 1)

    (
      Validated.condNel(
        Identifier.pattern.matches(request.nationalInsuranceNumber.value),
        SuccessfulResult,
        "invalid national insurance number format"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        "Start tax year after CY-1"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.endTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        "End tax year after CY-1"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value <= request.niContributionsAndCredits.endTaxYear.value,
        SuccessfulResult,
        "Start tax year after end tax year"
      ),
      Validated.condNel(
        (!shouldPageForContributionsAndCredits && hasOneTaxWindow) || shouldPageForContributionsAndCredits,
        SuccessfulResult,
        "Tax year range greater than six years"
      ),
      Validated.condNel(
        request.niContributionsAndCredits.startTaxYear.value >= 1975,
        SuccessfulResult,
        "Start tax year before 1975"
      )
    ).mapN((_, _, _, _, _, _) => SuccessfulResult) match {
      case Validated.Valid(_)   => Right(SuccessfulResult)
      case Validated.Invalid(e) => Left(JsonValidationError(e.toList))
    }
  }

  def validateCorrelationId(correlationId: String): Either[ErrorReason, CorrelationId] = {
    val uuidRegex = """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".r
    Validated
      .condNel(
        uuidRegex.matches(correlationId),
        CorrelationId(UUID.fromString(correlationId)),
        "Invalid correlationId value found, expected a valid UUID"
      ) match {
      case Validated.Valid(id)      => Right(id)
      case Validated.Invalid(error) => Left(ErrorReason(error.toList.mkString(",")))
    }
  }

  def validateAcceptHeader(
      acceptHeader: Option[String]
  )(implicit hc: HeaderCarrier): Either[ErrorReason, SuccessfulResult.type] =
    acceptHeader match {
      case None =>
        Left(ErrorReason("Accept header is required"))
      case Some(header) if header.trim.nonEmpty =>
        logger.info("Accept Header Valid")
        Right(SuccessfulResult)
      case Some(_) =>
        Left(ErrorReason("Accept header cannot be empty"))
    }

  def validateOriginatorId(
      originatorId: Option[String]
  )(implicit hc: HeaderCarrier): Either[ErrorReason, OriginatorIdType] =
    originatorId.flatMap(id => OriginatorIdType.withNameOption(id)) match {
      case None =>
        Left(ErrorReason("Originator Id is missing or invalid"))
      case Some(success) => Right(success)
    }

}
