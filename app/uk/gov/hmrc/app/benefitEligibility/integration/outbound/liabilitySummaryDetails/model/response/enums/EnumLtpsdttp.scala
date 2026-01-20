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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class EnumLtpsdttp(override val entryName: String) extends EnumEntry

object EnumLtpsdttp extends Enum[EnumLtpsdttp] with PlayJsonEnum[EnumLtpsdttp] {
  val values: immutable.IndexedSeq[EnumLtpsdttp] = findValues

  case object PseudoStartDate  extends EnumLtpsdttp("PSEUDO START DATE")
  case object StartDateHeld    extends EnumLtpsdttp("START DATE HELD")
  case object StartDateNotHeld extends EnumLtpsdttp("START DATE NOT HELD")
  case object StartDatePre1982 extends EnumLtpsdttp("START DATE PRE 1982")
  case object StartDatePreRni  extends EnumLtpsdttp("START DATE PRE RNI")
}
