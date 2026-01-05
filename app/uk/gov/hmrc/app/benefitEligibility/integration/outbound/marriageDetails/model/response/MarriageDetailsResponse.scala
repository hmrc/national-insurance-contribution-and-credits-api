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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.catsSyntaxTuple2Semigroupal
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus,
  MarriageStatus
}
import uk.gov.hmrc.app.benefitEligibility.util.SuccessfulResult

import java.time.LocalDate
import scala.collection.immutable

sealed trait MarriageDetailsResponse extends NpsApiResponse

object MarriageDetailsError {

  // region Common

  final case class Reason private (value: String)

  object Reason {
    private val minLength = 1
    private val maxLength = 120

    private def from(value: String): ValidatedNel[String, Reason] =
      (
        Validated.condNel(
          value.length >= minLength,
          SuccessfulResult,
          "provided value is below the minimum length limit"
        ),
        Validated.condNel(value.length <= maxLength, SuccessfulResult, "provided value exceeds the max length limit")
      ).mapN((_, _) => Reason(value))

    implicit val reads: Reads[Reason] = {
      case JsString(value) =>
        Reason
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

  }

  // endregion Common

  // region Error400

  sealed abstract class ErrorCode400(override val entryName: String) extends EnumEntry

  object ErrorCode400 extends Enum[ErrorCode400] with PlayJsonEnum[ErrorCode400] {
    val values: immutable.IndexedSeq[ErrorCode400] = findValues

    case object Constraint_Violation extends ErrorCode400("400.1")

    case object Message_Not_Readable extends ErrorCode400("400.2")
  }

  case class MarriageDetailsError400(reason: Reason, code: ErrorCode400)

  object MarriageDetailsError400 {
    implicit val npsErrorResponse400Reads: Reads[MarriageDetailsError400] = Json.reads[MarriageDetailsError400]
  }

  case class MarriageDetailsErrorResponse400(failures: List[MarriageDetailsError400]) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[MarriageDetailsErrorResponse400] =
      Json.reads[MarriageDetailsErrorResponse400]

  }

  // endregion Error400

  // region Error403

  sealed abstract class ErrorCode403(override val entryName: String) extends EnumEntry

  object ErrorCode403 extends Enum[ErrorCode403] with PlayJsonEnum[ErrorCode403] {
    val values: immutable.IndexedSeq[ErrorCode403] = findValues

    case object ErrorCode403_2 extends ErrorCode403("403.2")
  }

  sealed abstract class ErrorReason403(override val entryName: String) extends EnumEntry

  object ErrorReason403 extends Enum[ErrorReason403] with PlayJsonEnum[ErrorReason403] {
    val values: immutable.IndexedSeq[ErrorReason403] = findValues

    case object Forbidden extends ErrorReason403("Forbidden")
  }

  case class MarriageDetailsErrorResponse403(reason: ErrorReason403, code: ErrorCode403) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[MarriageDetailsErrorResponse403] =
      Json.reads[MarriageDetailsErrorResponse403]

  }

  // endregion Error403

  // region Error422

  final case class ErrorCode422 private (value: String)

  object ErrorCode422 {

    implicit val reads: Reads[ErrorCode422] = Json.reads[ErrorCode422]

  }

  case class MarriageDetailsError422(reason: Reason, code: ErrorCode422)

  object MarriageDetailsError422 {
    implicit val NpsErrorResponse422Reads: Reads[MarriageDetailsError422] = Json.reads[MarriageDetailsError422]
  }

  case class MarriageDetailsErrorResponse422(failures: List[MarriageDetailsError422]) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse422 {

    implicit val npsFailureResponse422Reads: Reads[MarriageDetailsErrorResponse422] =
      Json.reads[MarriageDetailsErrorResponse422]

  }

  // endregion Error422

}

object MarriageDetailsSuccess {

  // region Active Marriage

  case class ActiveMarriage(value: Boolean) extends AnyVal

  object ActiveMarriage {
    implicit val activeMarriageReads: Format[ActiveMarriage] = Json.valueFormat[ActiveMarriage]
  }

  // endregion Active Marriage

  // region Marriage Details List

  final case class SequenceNumber private (value: Int)

  object SequenceNumber {

    private val minValue = 1
    private val maxValue = 126

    private def from(value: Int): ValidatedNel[String, SequenceNumber] =
      (
        Validated.condNel(value >= minValue, SuccessfulResult, "provided value is below the minimum value"),
        Validated.condNel(value <= maxValue, SuccessfulResult, "provided value exceeds the max value")
      ).mapN((_, _) => SequenceNumber(value))

    implicit val sequenceNumberReads: Reads[SequenceNumber] = {
      case JsNumber(value) =>
        SequenceNumber
          .from(value.toInt)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))

      case _ => JsError(__ -> JsonValidationError("wrong data type, expected Integer"))
    }

    implicit val sequenceNumberWrites: Writes[SequenceNumber] = Json.writes[SequenceNumber]

  }

  case class StartDate(value: LocalDate) extends AnyVal

  object StartDate {
    implicit val startDateReads: Format[StartDate] = Json.valueFormat[StartDate]
  }

  case class EndDate(value: LocalDate) extends AnyVal

  object EndDate {
    implicit val endDateReads: Format[EndDate] = Json.valueFormat[EndDate]
  }

  final case class SpouseForename private (value: String)

  object SpouseForename {
    private val pattern = "^([A-Za-z '-]{1,99})+$".r

    private def from(value: String): ValidatedNel[String, SpouseForename] =
      Validated.condNel(pattern.matches(value), SpouseForename(value), "invalid spouse forename")

    implicit val spouseForenameReads: Reads[SpouseForename] = {
      case JsString(value) =>
        SpouseForename
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

    implicit val spouseForenameWrites: Writes[SpouseForename] = Json.writes[SpouseForename]

  }

  final case class SpouseSurname private (value: String)

  object SpouseSurname {
    private val pattern = "^([A-Za-z '-]{2,99})+$".r

    private def from(value: String): ValidatedNel[String, SpouseSurname] =
      Validated.condNel(pattern.matches(value), SpouseSurname(value), "invalid spouse surname")

    implicit val spouseSurnameReads: Reads[SpouseSurname] = {
      case JsString(value) =>
        SpouseSurname
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

    implicit val spouseSurnameWrites: Writes[SpouseSurname] = Json.writes[SpouseSurname]

  }

  case class SeparationDate(value: LocalDate) extends AnyVal

  object SeparationDate {
    implicit val separationDateReads: Format[SeparationDate] = Json.valueFormat[SeparationDate]
  }

  case class ReconciliationDate(value: LocalDate) extends AnyVal

  object ReconciliationDate {
    implicit val reconciliationDateReads: Format[ReconciliationDate] = Json.valueFormat[ReconciliationDate]
  }

  case class MarriageDetailsList(
      sequenceNumber: Option[SequenceNumber],
      status: Option[MarriageStatus],
      startDate: Option[StartDate],
      startDateStatus: Option[MarriageStartDateStatus],
      endDate: Option[EndDate],
      endDateStatus: Option[MarriageEndDateStatus],
      spouseIdentifier: Option[Identifier],
      spouseForename: Option[SpouseForename],
      spouseSurname: Option[SpouseSurname],
      separationDate: Option[SeparationDate],
      reconciliationDate: Option[ReconciliationDate]
  )

  object MarriageDetailsList {
    implicit val marriageDetailsListFormats: Format[MarriageDetailsList] = Json.format[MarriageDetailsList]
  }

  // endregion MarriageDetailsList

  // region Links

  case class Href(value: String) extends AnyVal

  object Href {
    implicit val hrefReads: Format[Href] = Json.valueFormat[Href]
  }

  sealed abstract class Methods(override val entryName: String) extends EnumEntry

  object Methods extends Enum[Methods] with PlayJsonEnum[Methods] {
    val values: immutable.IndexedSeq[Methods] = findValues

    case object get extends Methods("get")
  }

  case class Self(
      href: Option[Href],
      methods: Option[Methods]
  )

  object Self {
    implicit val selfReads: Reads[Self]   = Json.reads[Self]
    implicit val selfWrites: Writes[Self] = Json.writes[Self]
  }

  case class Links(self: Self)

  object Links {
    implicit val linksReads: Reads[Links]   = Json.reads[Links]
    implicit val linksWrites: Writes[Links] = Json.writes[Links]
  }

  // endregion Links

  // region Marriage Details Success Response

  case class MarriageDetailsSuccessResponse(
      activeMarriage: Option[Boolean],
      marriageDetailsList: Option[MarriageDetailsList],
      marriageDetailsLinks: Option[Links]
  ) extends MarriageDetailsResponse
      with NpsSuccessfulApiResponse

  object MarriageDetailsSuccessResponse {

    implicit val getMarriageDetailsResponseReads: Reads[MarriageDetailsSuccessResponse] =
      Json.reads[MarriageDetailsSuccessResponse]

    implicit val getMarriageDetailsResponseWrites: Writes[MarriageDetailsSuccessResponse] =
      Json.writes[MarriageDetailsSuccessResponse]

  }

  // endregion Marriage Details Success Response

}
