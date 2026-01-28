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

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  ErrorCode422,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait Class2MAReceiptsResponse extends NpsApiResponse

object Class2MAReceiptsError {

  // 400 start
  case class Class2MAReceiptsError400(reason: Reason, code: NpsErrorCode400)

  object Class2MAReceiptsError400 {
    implicit val npsErrorResponse400Reads: Reads[Class2MAReceiptsError400] = Json.reads[Class2MAReceiptsError400]
  }

  case class Class2MAReceiptsErrorResponse400(failures: List[Class2MAReceiptsError400]) extends Class2MAReceiptsResponse

  object Class2MAReceiptsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[Class2MAReceiptsErrorResponse400] =
      Json.reads[Class2MAReceiptsErrorResponse400]

  }

  // 400 end

  // 403 start
  case class Class2MAReceiptsErrorResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends Class2MAReceiptsResponse
      with NpsApiResponse

  object Class2MAReceiptsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[Class2MAReceiptsErrorResponse403] =
      Json.reads[Class2MAReceiptsErrorResponse403]

  }
  // 403 end

  // 422 start
  case class Class2MAReceiptsError422(reason: Reason, code: ErrorCode422)

  object Class2MAReceiptsError422 {
    implicit val NpsErrorResponse422Reads: Reads[Class2MAReceiptsError422] = Json.reads[Class2MAReceiptsError422]
  }

  case class Class2MAReceiptsErrorResponse422(failures: List[Class2MAReceiptsError422]) extends Class2MAReceiptsResponse

  object Class2MAReceiptsErrorResponse422 {

    implicit val npsFailureResponse422Reads: Reads[Class2MAReceiptsErrorResponse422] =
      Json.reads[Class2MAReceiptsErrorResponse422]

  }
  // 422 end

}

object Class2MAReceiptsSuccess {

  final case class Initials(value: String) extends AnyVal

  object Initials {
    implicit val initialsFormat: Format[Initials] = Json.valueFormat[Initials]
  }

  final case class Surname(value: String) extends AnyVal

  object Surname {
    implicit val surnameFormat: Format[Surname] = Json.valueFormat[Surname]
  }

  case class ReceivablePayment(value: BigDecimal) extends AnyVal

  object ReceivablePayment {
    implicit val receivablePaymentFormat: Format[ReceivablePayment] = Json.valueFormat[ReceivablePayment]
  }

  case class BillAmount(value: BigDecimal) extends AnyVal

  object BillAmount {
    implicit val billAmountFormats: Format[BillAmount] = Json.valueFormat[BillAmount]
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

    implicit val class2MAReceiptDetailsFormat: Format[Class2MAReceiptDetails] =
      Json.format[Class2MAReceiptDetails]

  }

  case class Class2MAReceiptsSuccessResponse(
      identifier: Identifier,
      class2MAReceiptDetails: List[Class2MAReceiptDetails]
  ) extends Class2MAReceiptsResponse
      with NpsSuccessfulApiResponse

  object Class2MAReceiptsSuccessResponse {

    implicit val getClass2MAReceiptsResponseWrites: Format[Class2MAReceiptsSuccessResponse] =
      Json.format[Class2MAReceiptsSuccessResponse]

  }

}
