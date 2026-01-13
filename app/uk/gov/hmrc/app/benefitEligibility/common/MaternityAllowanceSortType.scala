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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class MaternityAllowanceSortType(override val entryName: String) extends EnumEntry

object MaternityAllowanceSortType
    extends Enum[MaternityAllowanceSortType]
    with PlayJsonEnum[MaternityAllowanceSortType] {
  val values: immutable.IndexedSeq[MaternityAllowanceSortType] = findValues
  case object NinoAscending                extends MaternityAllowanceSortType("NINO ASCENDING")
  case object NinoDescending               extends MaternityAllowanceSortType("NINO DESCENDING")
  case object DateOfFinalPaymentDescending extends MaternityAllowanceSortType("DATE OF FINAL PAYMENT DESCENDING")
  case object DateOfFinalPaymentAscending  extends MaternityAllowanceSortType("DATE OF FINAL PAYMENT ASCENDING")
}
