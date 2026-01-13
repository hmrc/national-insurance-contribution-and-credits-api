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

sealed abstract class ContributionCategory(override val entryName: String) extends EnumEntry

object ContributionCategory extends Enum[ContributionCategory] with PlayJsonEnum[ContributionCategory] {
  val values: immutable.IndexedSeq[ContributionCategory] = findValues

  case object None             extends ContributionCategory("(NONE)")
  case object Class1A          extends ContributionCategory("CLASS 1A - PAYE EMPLOYER ONLY CONTRIBUTIONS")
  case object ConvertedAccount extends ContributionCategory("CONVERTED ACCOUNT - CONTRACTED OUT")

  case object MarinerRebateReducedContractedOut
      extends ContributionCategory("MARINER FOREIGN GOING REBATE ( REDUCED CONTRACTED-OUT )")

  case object MarinerRebateStandardContractedOut
      extends ContributionCategory("MARINER FOREIGN GOING REBATE ( STANDARD CONTRACTED-OUT )")

  case object MarinerRebateEquivalentReduced
      extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (REDUCED)")

  case object MarinerRebateEquivalentSecondaryOnly
      extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (SECONDARY ONLY)")

  case object MarinerRebateEquivalentStandard
      extends ContributionCategory("MARINER FOREIGN GOING REBATE EQUIVALENT (STANDARD)")

  case object MarinerRedundancyFundAndForeignRebateReducedContractedOut
      extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE ( REDUCED C-OUT)")

  case object MarinerRedundancyFundAndForeignRebateStandardContractedOut
      extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE ( STANDARD C-OUT)")

  case object MarinerRedundancyFundAndForeignRebateEquivalentSecondaryOnly
      extends ContributionCategory("MARINER REDUNDANCY FUND & FOREIGN GOING REBATE EQUIV.(SECONDARY ONLY)")

  case object MarinerRedundancyFundAndForeignRebateEquivalentStd
      extends ContributionCategory("MARINER REDUNDANCY FUND & FRGN GNG REBATE EQUIV.(STD)")

  case object MarinerRedundancyFundAndForeignRebateEquivalentRdcd
      extends ContributionCategory("MARINER REDUNDANCY FUND & FRGN GOING REBATE EQUIV.(RDCD)")

  case object MarinerRedundancyFundRebateReducedContractedOut
      extends ContributionCategory("MARINER REDUNDANCY FUND REBATE ( REDUCED CONTRACTED-OUT )")

  case object MarinerRedundancyFundRebateStandardContractedOut
      extends ContributionCategory("MARINER REDUNDANCY FUND REBATE ( STANDARD CONTRACTED-OUT )")

  case object MarinerRedundancyFundRebateEquivalentRdcd
      extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIV. (RDCD)")

  case object MarinerRedundancyFundRebateEquivalentSecondaryOnly
      extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIVALENT (SECONDARY ONLY)")

  case object MarinerRedundancyFundRebateEquivalentStandard
      extends ContributionCategory("MARINER REDUNDANCY FUND REBATE EQUIVALENT (STANDARD)")

  case object MarriedWomanReducedRateElection extends ContributionCategory("MARRIED WOMAN'S REDUCED RATE ELECTION")
  case object NoLiability                     extends ContributionCategory("NO LIABILITY")
  case object OfficeHolders                   extends ContributionCategory("OFFICE HOLDERS")
  case object ReducedRate                     extends ContributionCategory("REDUCED RATE")
  case object ReducedRateContractedOut        extends ContributionCategory("REDUCED RATE CONTRACTED-OUT")
  case object StandardRate                    extends ContributionCategory("STANDARD RATE")
  case object StandardRateContractedOut       extends ContributionCategory("STANDARD RATE CONTRACTED-OUT")
  case object Unallocated                     extends ContributionCategory("UNALLOCATED")
  case object ZeroRate                        extends ContributionCategory("ZERO RATE")

}
