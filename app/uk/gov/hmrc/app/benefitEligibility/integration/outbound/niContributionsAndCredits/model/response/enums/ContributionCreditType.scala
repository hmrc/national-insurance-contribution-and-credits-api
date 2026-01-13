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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class ContributionCreditType(override val entryName: String) extends EnumEntry

object ContributionCreditType extends Enum[ContributionCreditType] with PlayJsonEnum[ContributionCreditType] {
  val values: immutable.IndexedSeq[ContributionCreditType] = findValues

  case object C1 extends ContributionCreditType("C1")

  case object Hmf extends ContributionCreditType("HMF")

  case object Mar extends ContributionCreditType("MAR")

  case object Cs extends ContributionCreditType("CS")

  case object Eon extends ContributionCreditType("EON")

  case object Cr1 extends ContributionCreditType("CR1")

  case object C2 extends ContributionCreditType("C2")

  case object C2w extends ContributionCreditType("C2W")

  case object Sf extends ContributionCreditType("SF")

  case object TwoA extends ContributionCreditType("2A")

  case object TwoB extends ContributionCreditType("2B")

  case object TwoC extends ContributionCreditType("2C")

  case object TwoD extends ContributionCreditType("2D")

  case object Sfa extends ContributionCreditType("SFA")

  case object Sfb extends ContributionCreditType("SFB")

  case object Sfc extends ContributionCreditType("SFC")

  case object Sfd extends ContributionCreditType("SFD")

  case object Vda extends ContributionCreditType("VDA")

  case object Vdb extends ContributionCreditType("VDB")

  case object Vdc extends ContributionCreditType("VDC")

  case object Vdd extends ContributionCreditType("VDD")

  case object TwoN extends ContributionCreditType("2N")
}
