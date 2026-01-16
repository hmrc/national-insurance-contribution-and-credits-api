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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class ErrorCode403(override val entryName: String) extends EnumEntry

object ErrorCode403 extends Enum[ErrorCode403] with PlayJsonEnum[ErrorCode403] {
  val values: immutable.IndexedSeq[ErrorCode403] = findValues

  case object ErrorCode403_2 extends ErrorCode403("403.2")

  case object ErrorCode403_1 extends ErrorCode403("403.1")
}
