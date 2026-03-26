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

package uk.gov.hmrc.app.benefitEligibility.model.response

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess._
import uk.gov.hmrc.app.benefitEligibility.model.common.{Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.model.nps.class2MAReceipts.Class2MAReceiptsSuccess

import java.time.LocalDate

class FilteredClass2MaReceiptsSpec extends AnyFreeSpec with Matchers {

  val class2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(
    Some(Identifier("AA000001A")),
    Some(
      List(
        Class2MAReceiptDetails(
          initials = Some(Initials("JP")),
          surname = Some(Surname("van Cholmondley-warner")),
          receivablePayment = Some(ReceivablePayment(10.56)),
          receiptDate = Some(ReceiptDate(LocalDate.parse("2025-12-10"))),
          liabilityStart = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
          liabilityEnd = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
          billAmount = Some(BillAmount(9999.98)),
          billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
          isClosedRecord = Some(IsClosedRecord(true)),
          weeksPaid = Some(WeeksPaid(2))
        ),
        Class2MAReceiptDetails(
          initials = Some(Initials("JP")),
          surname = Some(Surname("van Cholmondley-warner")),
          receivablePayment = Some(ReceivablePayment(10.56)),
          receiptDate = Some(ReceiptDate(LocalDate.parse("2026-11-10"))),
          liabilityStart = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
          liabilityEnd = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
          billAmount = Some(BillAmount(9999.98)),
          billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
          isClosedRecord = Some(IsClosedRecord(true)),
          weeksPaid = Some(WeeksPaid(2))
        ),
        Class2MAReceiptDetails(
          initials = Some(Initials("JP")),
          surname = Some(Surname("van Cholmondley-warner")),
          receivablePayment = Some(ReceivablePayment(10.56)),
          receiptDate = Some(ReceiptDate(LocalDate.parse("2025-08-23"))),
          liabilityStart = Some(Class2MAReceiptsSuccess.LiabilityStartDate(LocalDate.parse("2025-12-10"))),
          liabilityEnd = Some(Class2MAReceiptsSuccess.LiabilityEndDate(LocalDate.parse("2025-12-10"))),
          billAmount = Some(BillAmount(9999.98)),
          billScheduleNumber = Some(Class2MAReceiptsSuccess.BillScheduleNumber(100)),
          isClosedRecord = Some(IsClosedRecord(true)),
          weeksPaid = Some(WeeksPaid(2))
        )
      )
    ),
    callBack = None
  )

  val minimalClass2MAReceiptsSuccessResponse = Class2MAReceiptsSuccessResponse(None, None, None)

  "FilteredClass2MaReceipts" - {
    ".from" - {
      "should construct a filtered object from a class2MAReceiptsSuccessResponse (maximal response)" in {

        val result = FilteredClass2MaReceipts.from(
          class2MAReceiptsSuccessResponse
        )

        result.receiptDates should contain theSameElementsAs
          List(
            ReceiptDate(LocalDate.parse("2025-08-23")),
            ReceiptDate(LocalDate.parse("2026-11-10")),
            ReceiptDate(LocalDate.parse("2025-12-10"))
          )
      }

      "should construct a filtered object from a class2MAReceiptsSuccessResponse (minimal response)" in {

        val result = FilteredClass2MaReceipts.from(
          minimalClass2MAReceiptsSuccessResponse
        )

        result shouldBe FilteredClass2MaReceipts(List())
      }
    }

  }

}
