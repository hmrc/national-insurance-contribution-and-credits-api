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

sealed abstract class Enfcment(override val entryName: String) extends EnumEntry

object Enfcment extends Enum[Enfcment] with PlayJsonEnum[Enfcment] {
  val values: immutable.IndexedSeq[Enfcment] = findValues

  case object EnforcedFollowingAut                 extends Enfcment("ENFORCED FOLLOWING AUT")
  case object EnforcedFollowingSspAllowChangeInMop extends Enfcment("ENFORCED FOLLOWING SSP: ALLOW CHANGE IN MOP")

  case object EnforcedFollowingSspInhibitChangeOfMop extends Enfcment("ENFORCED FOLLOWING SSP: INHIBIT CHANGE OF MOP")

  case object EnforcementCarriedForwardFromNirs1 extends Enfcment("ENFORCEMENT CARRIED FORWARD FROM NIRS1")
  case object InhibitGmpSspEnforcement           extends Enfcment("INHIBIT GMP SSP ENFORCEMENT")
  case object NotApplicable                      extends Enfcment("NOT APPLICABLE")
  case object NotEnforced                        extends Enfcment("NOT ENFORCED")
}
