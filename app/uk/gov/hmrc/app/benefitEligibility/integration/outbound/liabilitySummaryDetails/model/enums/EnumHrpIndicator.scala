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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class EnumHrpIndicator(override val entryName: String) extends EnumEntry

object EnumHrpIndicator extends Enum[EnumHrpIndicator] with PlayJsonEnum[EnumHrpIndicator] {
  val values: immutable.IndexedSeq[EnumHrpIndicator] = findValues

  case object None                           extends EnumHrpIndicator("(NONE)")
  case object HrpAlreadyRecorded             extends EnumHrpIndicator("HRP already recorded")
  case object MissingHrpLetterNotSentAwarded extends EnumHrpIndicator("Missing HRP letter not sent - awarded")
  case object MissingHrpLetterNotSentRefused extends EnumHrpIndicator("Missing HRP letter not sent - refused")
  case object MissingHrpLetterSent           extends EnumHrpIndicator("Missing HRP letter sent")
  case object MissingHrpLetterSentAwarded    extends EnumHrpIndicator("Missing HRP letter sent -awarded")
  case object MissingHrpLetterSentRefused    extends EnumHrpIndicator("Missing HRP letter sent -refused")
}
