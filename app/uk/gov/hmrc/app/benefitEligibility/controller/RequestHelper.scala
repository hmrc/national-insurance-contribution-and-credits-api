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
import uk.gov.hmrc.app.benefitEligibility.repository.PaginationCursor
import uk.gov.hmrc.app.benefitEligibility.util.{
  ContributionCreditTaxWindowCalculator,
  RequestAwareLogger,
  SuccessfulResult
}
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.util.{Failure, Success}

object RequestHelper {

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
      request: Request[AnyContent],
      appConfig: AppConfig
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, (CorrelationId, OriginatorId)] =
    for {
      _             <- validateAcceptHeader(request)
      originatorId  <- getAndValidateOriginatorId(request, appConfig)
      correlationId <- getAndValidateCorrelationId(request)
    } yield (correlationId, originatorId)

  def parsePaginationCursor(
      request: Request[AnyContent]
  ): Either[BenefitEligibilityError, PaginationCursor] =
    request.queryString
      .get("cursorId")
      .flatMap(_.headOption)
      .map(id => PaginationCursor.from(CursorId(id)))
      .toRight(MissingCursorId(ErrorReason("Pagination request sent without cursorId")))
      .flatMap {
        case Failure(e) => Left(InvalidCursorId(ErrorReason(s"invalid cursorId - ${e.getMessage}")))
        case Success(cursor) =>
          Right(cursor)
      }

  def validateOriginatorMatch(
      eligibilityRequest: EligibilityCheckDataRequest,
      originatorId: OriginatorId,
      appConfig: AppConfig
  ): Either[BenefitEligibilityError, Unit] =
    if (OriginatorId.from(eligibilityRequest, appConfig).contains(originatorId)) {
      Right(())
    } else {
      Left(InvalidOriginatorId(ErrorReason("Invalid Originator Id")))
    }

  def validatePaginationOriginatorMatch(
      cursor: PaginationCursor,
      originatorId: OriginatorId,
      appConfig: AppConfig
  ): Either[BenefitEligibilityError, Unit] =
    if (OriginatorId.from(cursor.paginationType, appConfig).contains(originatorId)) {
      Right(())
    } else {
      Left(InvalidOriginatorId(ErrorReason("Invalid Originator Id")))
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
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, CorrelationId] =
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

  private def validateAcceptHeader(
      acceptHeader: Option[String]
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, SuccessfulResult.type] =
    acceptHeader match {
      case None =>
        Left(InvalidOrMissingHeaderError(ErrorReason("Accept header is required")))
      case Some(header) if header.trim.nonEmpty =>
        Right(SuccessfulResult)
      case Some(_) =>
        Left(InvalidOrMissingHeaderError(ErrorReason("Accept header cannot be empty")))
    }

  private def validateOriginatorId(
      originatorId: Option[String],
      appConfig: AppConfig
  )(implicit hc: HeaderCarrier): Either[InvalidOrMissingHeaderError, OriginatorId] =
    originatorId.flatMap(id => OriginatorId.from(id, appConfig)) match {
      case None =>
        Left(InvalidOrMissingHeaderError(ErrorReason("Originator Id is missing or invalid")))
      case Some(success) => Right(success)
    }

  private def validateAcceptHeader(
      request: Request[AnyContent]
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, Unit] =
    request.headers.get("Accept") match {
      case None => Left(InvalidOrMissingHeaderError(ErrorReason("Missing Header Accept")))
      case Some(acceptHeader) =>
        RequestHelper.validateAcceptHeader(Some(acceptHeader)).map(_ => ())
    }

  private def getAndValidateOriginatorId(
      request: Request[AnyContent],
      appConfig: AppConfig
  )(implicit hc: HeaderCarrier): Either[BenefitEligibilityError, OriginatorId] =
    request.headers.get("gov-uk-originator-id") match {
      case None               => Left(InvalidOrMissingHeaderError(ErrorReason("Missing header 'gov-uk-originator-id'")))
      case Some(originatorId) => validateOriginatorId(Some(originatorId), appConfig)
    }

  private def formatJsonErrors(errors: collection.Seq[(JsPath, collection.Seq[JsonValidationError])]): String =
    errors
      .map { case (path, validationErrors) =>
        if (path.toString.isEmpty) validationErrors.flatMap(_.messages).mkString(",")
        else s"$path ${validationErrors.flatMap(_.messages).mkString(",")}"
      }
      .mkString("[", "; ", "]")

}
