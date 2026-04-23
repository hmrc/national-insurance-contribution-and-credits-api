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

sealed abstract class ContributionCategory(override val entryName: String) extends EnumEntry

object ContributionCategory extends Enum[ContributionCategory] with PlayJsonEnum[ContributionCategory] {
  val values: immutable.IndexedSeq[ContributionCategory] = findValues

  case object None                          extends ContributionCategory("(NONE)")
  case object Class1A                       extends ContributionCategory("CLASS 1 A -PAYE EMPLOYER ONLY CONTRIBUTIONS")
  case object ConvertedAccountContractedOut extends ContributionCategory("CONVERTED ACCOUNT -CONTRACTED OUT")
  case object ContractedOutReducedRate      extends ContributionCategory("CONTRACTED OUT REDUCED RATE")
  case object ContractedOutReducedRateCosr  extends ContributionCategory("CONTRACTED - OUT REDUCED RATE COSR")
  case object ContractedOutReducedRateComp  extends ContributionCategory("CONTRACTED - OUT REDUCED RATE(COMP)")
  case object ContractedOutStandardRate     extends ContributionCategory("CONTRACTED OUT STANDARD RATE COSR")
  case object ContractedOutStandardRateComp extends ContributionCategory("CONTRACTED - OUT STANDARD RATE(COMP)")
  case object NotFeasible                   extends ContributionCategory("NOT FEASIBLE")
  case object NoLiability                   extends ContributionCategory("NO LIABILITY")
  case object OfficeHolders                 extends ContributionCategory("OFFICE HOLDERS")
  case object DeferredRate                  extends ContributionCategory("DEFERRED RATE")
  case object ReducedRate                   extends ContributionCategory("REDUCED RATE")
  case object ReducedRateContractedOut      extends ContributionCategory("REDUCED RATE CONTRACTED - OUT")
  case object ReducedRateContractedOutCosr  extends ContributionCategory("REDUCED RATE CONTRACTED - OUT(COSR)")
  case object StandardRate                  extends ContributionCategory("STANDARD RATE")
  case object StandardRateContractedOut     extends ContributionCategory("STANDARD RATE CONTRACTED OUT")
  case object StandardRateContractedOutDash extends ContributionCategory("STANDARD RATE CONTRACTED - OUT")
  case object StandardRateContractedOutCosr extends ContributionCategory("STANDARD RATE CONTRACTED - OUT(COSR)")
  case object Unallocated                   extends ContributionCategory("UNALLOCATED")
  case object ZeroRate                      extends ContributionCategory("ZERO RATE")
  case object ZeroRateNicHoliday            extends ContributionCategory("ZERO RATE (NIC HOLIDAY)")
}
