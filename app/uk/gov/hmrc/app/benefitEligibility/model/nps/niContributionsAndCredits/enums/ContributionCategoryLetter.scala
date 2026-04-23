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

package uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class ContributionCategoryLetter(override val entryName: String) extends EnumEntry

object ContributionCategoryLetter
    extends Enum[ContributionCategoryLetter]
    with PlayJsonEnum[ContributionCategoryLetter] {

  val values: immutable.IndexedSeq[ContributionCategoryLetter] = findValues

  case object None extends ContributionCategoryLetter("(NONE)")

  case object C extends ContributionCategoryLetter("C")
  case object U extends ContributionCategoryLetter("U")
  case object D extends ContributionCategoryLetter("D")
  case object A extends ContributionCategoryLetter("A")
  case object E extends ContributionCategoryLetter("E")
  case object B extends ContributionCategoryLetter("B")
  case object Z extends ContributionCategoryLetter("Z")
  case object X extends ContributionCategoryLetter("X")
  case object Q extends ContributionCategoryLetter("Q")
  case object L extends ContributionCategoryLetter("L")
  case object S extends ContributionCategoryLetter("S")
  case object M extends ContributionCategoryLetter("M")
  case object F extends ContributionCategoryLetter("F")
  case object G extends ContributionCategoryLetter("G")
  case object P extends ContributionCategoryLetter("P")
  case object I extends ContributionCategoryLetter("I")
  case object V extends ContributionCategoryLetter("V")
  case object H extends ContributionCategoryLetter("H")
  case object K extends ContributionCategoryLetter("K")
  case object R extends ContributionCategoryLetter("R")
  case object W extends ContributionCategoryLetter("W")
  case object T extends ContributionCategoryLetter("T")
  case object N extends ContributionCategoryLetter("N")
  case object O extends ContributionCategoryLetter("O")
  case object J extends ContributionCategoryLetter("J")
  case object Y extends ContributionCategoryLetter("Y")

}
