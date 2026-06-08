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
import play.api.libs.json.{JsError, JsPath, JsSuccess, JsonValidationError}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.model.response.ErrorReason
import uk.gov.hmrc.app.benefitEligibility.repository.PageTaskId
import uk.gov.hmrc.app.benefitEligibility.util.{
  ContributionCreditTaxWindowCalculator,
  RequestAwareLogger,
  SuccessfulResult
}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID

object RequestHelper {

  private val logger = new RequestAwareLogger(this.getClass)

  def parseAndValidateRequest(
      request: Request[AnyContent]
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, EligibilityCheckDataRequest] =
    request.body.asJson match {
      case None =>
        Left(InvalidRequestJson(ErrorReason("invalid JSON")))
      case Some(json) =>
        json.validate[EligibilityCheckDataRequest] match {
          case JsSuccess(eligibilityRequest, _) =>
            RequestHelper
              .validateRequestData(eligibilityRequest)
              .map(_ => eligibilityRequest)
          case JsError(errors) =>
            val errorMessage = formatJsonErrors(errors)
            Left(
              InvalidRequestJson(ErrorReason(s"incompatible JSON, request body does not match schema - $errorMessage"))
            )
        }
    }

  def validateHeaders(
      request: Request[AnyContent]
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, CorrelationId] = getAndValidateCorrelationId(request)

  def parsePageTaskId(
      request: Request[AnyContent]
  )(implicit headerCarrier: HeaderCarrier): Either[BenefitEligibilityError, PageTaskId] =
    request.queryString
      .get("cursorId")
      .flatMap(_.headOption)
      .map(id => PageTaskId.from(CursorId(id)))
      .toRight(MissingCursorId(ErrorReason("Pagination request sent without cursorId")))
      .flatMap {
        case Some(pageTaskId) => Right(pageTaskId)
        case None =>
          logger.error(s"invalid uuid used as cursor id value")
          Left(InvalidCursorId(ErrorReason(s"invalid cursorId")))

      }

  def addCorrelationIdHeader(
      result: Result,
      request: Request[AnyContent]
  )(implicit hc: HeaderCarrier): Result =
    getAndValidateCorrelationId(request)
      .map(correlationId => result.withHeaders("CorrelationId" -> correlationId.value.toString))
      .getOrElse(result)

  private def validateRequestData(
      request: EligibilityCheckDataRequest
  ): Either[UnprocessableDataError, SuccessfulResult.type] = {

    val shouldPageForContributionsAndCredits =
      PaginationType.from(request).toList.diff(List(PaginationType.MaPagination)).nonEmpty

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
      case Validated.Invalid(e) => Left(UnprocessableDataError(e.toList))
    }
  }

  private def getAndValidateCorrelationId(
      request: Request[AnyContent]
  ): Either[BenefitEligibilityError, CorrelationId] =
    request.headers.get("CorrelationId") match {
      case None                => Left(InvalidOrMissingHeaderError(ErrorReason("Missing Header CorrelationId")))
      case Some(correlationId) => RequestHelper.validateCorrelationId(correlationId)
    }

  private def validateCorrelationId(correlationId: String): Either[InvalidOrMissingHeaderError, CorrelationId] = {
    val uuidRegex = """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".r
    Validated
      .condNel(
        uuidRegex.matches(correlationId),
        CorrelationId(UUID.fromString(correlationId)),
        "Invalid correlationId value found, expected a valid UUID"
      ) match {
      case Validated.Valid(id)      => Right(id)
      case Validated.Invalid(error) => Left(InvalidOrMissingHeaderError(ErrorReason(error.toList.mkString(","))))
    }
  }

  private def formatJsonErrors(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): String =
    errors
      .map { case (path, validationErrors) =>
        if (path.toString.isEmpty) validationErrors.flatMap(_.messages).mkString(",")
        else s"$path ${validationErrors.flatMap(_.messages).mkString(",")}"
      }
      .mkString("[", "; ", "]")

}
