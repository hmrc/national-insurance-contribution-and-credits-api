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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class ContCatLetter(override val entryName: String) extends EnumEntry

object ContCatLetter extends Enum[ContCatLetter] with PlayJsonEnum[ContCatLetter] {
  val values: immutable.IndexedSeq[ContCatLetter] = findValues

  case object None extends ContCatLetter("(NONE)")
  case object Y    extends ContCatLetter("Y")
  case object J    extends ContCatLetter("J")
  case object O    extends ContCatLetter("O")
  case object N    extends ContCatLetter("N")
  case object T    extends ContCatLetter("T")
  case object W    extends ContCatLetter("W")
  case object R    extends ContCatLetter("R")
  case object K    extends ContCatLetter("K")
  case object H    extends ContCatLetter("H")
  case object V    extends ContCatLetter("V")
  case object I    extends ContCatLetter("I")
  case object P    extends ContCatLetter("P")
  case object G    extends ContCatLetter("G")
  case object F    extends ContCatLetter("F")
  case object M    extends ContCatLetter("M")
  case object S    extends ContCatLetter("S")
  case object L    extends ContCatLetter("L")
  case object Q    extends ContCatLetter("Q")
  case object X    extends ContCatLetter("X")
  case object Z    extends ContCatLetter("Z")
  case object B    extends ContCatLetter("B")
  case object E    extends ContCatLetter("E")
  case object A    extends ContCatLetter("A")
  case object D    extends ContCatLetter("D")
  case object U    extends ContCatLetter("U")
  case object C    extends ContCatLetter("C")
}
