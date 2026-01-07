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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response

import cats.data.{Validated, ValidatedNel}
import cats.implicits.catsSyntaxTuple2Semigroupal
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.{Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}
import uk.gov.hmrc.app.benefitEligibility.util.{MoneyValidation, SuccessfulResult}

import java.time.LocalDate
import scala.collection.immutable

sealed trait Class2MAReceiptsResponse extends NpsApiResponse

object Class2MAReceiptsError {

  sealed abstract class ErrorCode403(override val entryName: String) extends EnumEntry

  object ErrorCode403 extends Enum[ErrorCode403] with PlayJsonEnum[ErrorCode403] {
    val values: immutable.IndexedSeq[ErrorCode403] = findValues
    case object ErrorCode403_2 extends ErrorCode403("403.2")
    case object ErrorCode403_1 extends ErrorCode403("403.1")
  }

  sealed abstract class ErrorReason403(override val entryName: String) extends EnumEntry

  object ErrorReason403 extends Enum[ErrorReason403] with PlayJsonEnum[ErrorReason403] {
    val values: immutable.IndexedSeq[ErrorReason403] = findValues
    case object Forbidden        extends ErrorReason403("Forbidden")
    case object UserUnauthorised extends ErrorReason403("User Not Authorised")
  }

  case class Class2MAReceiptsErrorResponse403(reason: ErrorReason403, code: ErrorCode403)
      extends Class2MAReceiptsResponse
      with NpsApiResponse

  object Class2MAReceiptsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[Class2MAReceiptsErrorResponse403] =
      Json.reads[Class2MAReceiptsErrorResponse403]

  }

  sealed abstract class ErrorCode400(override val entryName: String) extends EnumEntry

  object ErrorCode400 extends Enum[ErrorCode400] with PlayJsonEnum[ErrorCode400] {
    val values: immutable.IndexedSeq[ErrorCode400] = findValues
    case object Invalid_Destination_Header extends ErrorCode400("400.1")
    case object Invalid_Depth_Header       extends ErrorCode400("400.2")
  }

  case class Class2MAReceiptsError400(reason: Reason, code: ErrorCode400)

  object Class2MAReceiptsError400 {
    implicit val npsErrorResponse400Reads: Reads[Class2MAReceiptsError400] = Json.reads[Class2MAReceiptsError400]
  }

  case class Class2MAReceiptsErrorResponse400(failures: List[Class2MAReceiptsError400]) extends Class2MAReceiptsResponse

  object Class2MAReceiptsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[Class2MAReceiptsErrorResponse400] =
      Json.reads[Class2MAReceiptsErrorResponse400]

  }

  final case class Reason(value: String)

  object Reason {
    private val minLength = 1
    private val maxLength = 128

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

  final case class ErrorCode422(value: String)

  object ErrorCode422 {
    private val minLength = 1
    private val maxLength = 10

    private def from(value: String): ValidatedNel[String, ErrorCode422] =
      (
        Validated.condNel(
          value.length >= minLength,
          SuccessfulResult,
          "provided value is below the minimum length limit"
        ),
        Validated.condNel(value.length <= maxLength, SuccessfulResult, "provided value exceeds the max length limit")
      ).mapN((_, _) => ErrorCode422(value))

    implicit val reads: Reads[ErrorCode422] = {
      case JsString(value) =>
        ErrorCode422
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

  }

  case class Class2MAReceiptsError422(reason: Reason, code: ErrorCode422)

  object Class2MAReceiptsError422 {
    implicit val NpsErrorResponse422Reads: Reads[Class2MAReceiptsError422] = Json.reads[Class2MAReceiptsError422]
  }

  case class Class2MAReceiptsError422Response(failures: List[Class2MAReceiptsError422]) extends Class2MAReceiptsResponse

  object Class2MAReceiptsError422Response {

    implicit val npsFailureResponse422Reads: Reads[Class2MAReceiptsError422Response] =
      Json.reads[Class2MAReceiptsError422Response]

  }

}

object Class2MAReceiptsSuccess {
  final case class Initials private (value: String)

  object Initials {
    private val pattern = "^[A-Za-z' -]{1,2}$".r

    private def from(value: String): ValidatedNel[String, Initials] =
      Validated.condNel(pattern.matches(value), Initials(value), "invalid initials")

    implicit val initialsReads: Reads[Initials] = {
      case JsString(value) =>
        Initials
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

    implicit val initialsWrites: Writes[Initials] =
      Json.writes[Initials]

  }

  final case class Surname private (value: String)

  object Surname {
    private val pattern = "^[A-Za-z' -]{2,99}$".r

    private def from(value: String): ValidatedNel[String, Surname] =
      Validated.condNel(pattern.matches(value), Surname(value), "invalid surname")

    implicit val surnameReads: Reads[Surname] = {
      case JsString(value) =>
        Surname
          .from(value)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected String"))
    }

    implicit val surname: Writes[Surname] =
      Json.writes[Surname]

  }

  case class ReceivablePayment(value: BigDecimal)

  object ReceivablePayment {

    implicit val receivablePaymentReads: Reads[ReceivablePayment] = {
      case JsNumber(value) =>
        MoneyValidation
          .validate(value)(ReceivablePayment.apply)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))
      case _ => JsError(__ -> JsonValidationError("wrong data type, expected Number"))
    }

    implicit val receivablePaymentWrites: Writes[ReceivablePayment] =
      Json.writes[ReceivablePayment]

  }

  case class BillAmount(value: BigDecimal)

  object BillAmount {

    implicit val billAmountReads: Reads[BillAmount] = {
      case JsNumber(value) =>
        MoneyValidation
          .validate(value)(BillAmount.apply)
          .leftMap(errors => JsError(__ -> JsonValidationError(errors.toList.mkString(","))))
          .fold(identity, JsSuccess(_))

      case _ => JsError(__ -> JsonValidationError("wrong data type, expected Number"))
    }

    implicit val billAmountWrites: Writes[BillAmount] =
      Json.writes[BillAmount]

  }

  case class ReceivablePeriodStartDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodStartDate {

    implicit val receivablePeriodStartDateReads: Format[ReceivablePeriodStartDate] =
      Json.valueFormat[ReceivablePeriodStartDate]

  }

  case class ReceivablePeriodEndDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodEndDate {

    implicit val receivablePeriodEndDateReads: Format[ReceivablePeriodEndDate] =
      Json.valueFormat[ReceivablePeriodEndDate]

  }

  case class LiabilityStartDate(value: LocalDate) extends AnyVal

  object LiabilityStartDate {
    implicit val liabilityStartDateReads: Format[LiabilityStartDate] = Json.valueFormat[LiabilityStartDate]
  }

  case class LiabilityEndDate(value: LocalDate) extends AnyVal

  object LiabilityEndDate {
    implicit val liabilityEndDateReads: Format[LiabilityEndDate] = Json.valueFormat[LiabilityEndDate]
  }

  case class BillScheduleNumber(value: Int) extends AnyVal

  object BillScheduleNumber {
    implicit val billScheduleNumberReads: Format[BillScheduleNumber] = Json.valueFormat[BillScheduleNumber]
  }

  case class IsClosedRecord(value: Boolean) extends AnyVal

  object IsClosedRecord {
    implicit val isClosedRecordReads: Format[IsClosedRecord] = Json.valueFormat[IsClosedRecord]
  }

  case class WeeksPaid(value: Int) extends AnyVal

  object WeeksPaid {
    implicit val weeksPaidReads: Format[WeeksPaid] = Json.valueFormat[WeeksPaid]
  }

  case class Class2MAReceiptDetails(
      initials: Option[Initials],
      surname: Option[Surname],
      receivablePeriodStartDate: Option[ReceivablePeriodStartDate],
      receivablePeriodEndDate: Option[ReceivablePeriodEndDate],
      receivablePayment: Option[ReceivablePayment],
      receiptDate: Option[ReceiptDate],
      liabilityStartDate: Option[LiabilityStartDate],
      liabilityEndDate: Option[LiabilityEndDate],
      billAmount: Option[BillAmount],
      billScheduleNumber: Option[BillScheduleNumber],
      isClosedRecord: Option[IsClosedRecord],
      weeksPaid: Option[WeeksPaid]
  )

  object Class2MAReceiptDetails {

    implicit val class2MAReceiptDetailsReads: Reads[Class2MAReceiptDetails] =
      Json.reads[Class2MAReceiptDetails]

    implicit val class2MAReceiptDetailsWrites: Writes[Class2MAReceiptDetails] = Json.writes[Class2MAReceiptDetails]

  }

  case class Class2MAReceiptsSuccessResponse(
      identifier: Identifier,
      class2MAReceiptDetails: List[Class2MAReceiptDetails]
  ) extends Class2MAReceiptsResponse
      with NpsSuccessfulApiResponse

  object Class2MAReceiptsSuccessResponse {

    implicit val getClass2MAReceiptsResponseReads: Reads[Class2MAReceiptsSuccessResponse] =
      Json.reads[Class2MAReceiptsSuccessResponse]

    implicit val getClass2MAReceiptsResponseWrites: Writes[Class2MAReceiptsSuccessResponse] =
      Json.writes[Class2MAReceiptsSuccessResponse]

  }

}
