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

package uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts

import play.api.libs.json.*
import Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.model.common.{Callback, Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.model.nps.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait Class2MAReceiptsResponse extends NpsApiResponse

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
      receivablePayment: Option[ReceivablePayment],
      receiptDate: Option[ReceiptDate],
      liabilityStart: Option[LiabilityStartDate],
      liabilityEnd: Option[LiabilityEndDate],
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
      nationalInsuranceNumber: Option[Identifier],
      class2MAReceiptDetails: Option[List[Class2MAReceiptDetails]],
      callBack: Option[Callback]
  ) extends Class2MAReceiptsResponse
      with NpsSuccessfulApiResponse

  object Class2MAReceiptsSuccessResponse {

    implicit val getClass2MAReceiptsResponseWrites: Format[Class2MAReceiptsSuccessResponse] =
      Json.format[Class2MAReceiptsSuccessResponse]

  }

}
