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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class NiContributionCreditType(override val entryName: String) extends EnumEntry

object NiContributionCreditType extends Enum[NiContributionCreditType] with PlayJsonEnum[NiContributionCreditType] {
  val values: immutable.IndexedSeq[NiContributionCreditType] = findValues

  case object C1 extends NiContributionCreditType("C1")

  case object Hmf extends NiContributionCreditType("HMF")

  case object Mar extends NiContributionCreditType("MAR")

  case object Cs extends NiContributionCreditType("CS")

  case object Eon extends NiContributionCreditType("EON")

  case object Cr1 extends NiContributionCreditType("CR1")

  case object C2 extends NiContributionCreditType("C2")

  case object C2w extends NiContributionCreditType("C2W")

  case object Sf extends NiContributionCreditType("SF")

  case object TwoA extends NiContributionCreditType("2A")

  case object TwoB extends NiContributionCreditType("2B")

  case object TwoC extends NiContributionCreditType("2C")

  case object TwoD extends NiContributionCreditType("2D")

  case object Sfa extends NiContributionCreditType("SFA")

  case object Sfb extends NiContributionCreditType("SFB")

  case object Sfc extends NiContributionCreditType("SFC")

  case object Sfd extends NiContributionCreditType("SFD")

  case object Vda extends NiContributionCreditType("VDA")

  case object Vdb extends NiContributionCreditType("VDB")

  case object Vdc extends NiContributionCreditType("VDC")

  case object Vdd extends NiContributionCreditType("VDD")

  case object TwoN extends NiContributionCreditType("2N")
}
