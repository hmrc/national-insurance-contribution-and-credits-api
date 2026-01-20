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

sealed abstract class EnumLtpedttp(override val entryName: String) extends EnumEntry

object EnumLtpedttp extends Enum[EnumLtpedttp] with PlayJsonEnum[EnumLtpedttp] {
  val values: immutable.IndexedSeq[EnumLtpedttp] = findValues

  case object EndDateHeld    extends EnumLtpedttp("END DATE HELD")
  case object EndDateNotHeld extends EnumLtpedttp("END DATE NOT HELD")
  case object EndDatePreRni  extends EnumLtpedttp("END DATE PRE RNI")

  case object InputRemovalOfContributionCreditOfRreLapsedByLo
      extends EnumLtpedttp("INPUT/REMOVAL OF CONTRIBUTION/CREDIT OF RRE LAPSED BY LO")

  case object PeriodClosedDueToClaimForBenefit  extends EnumLtpedttp("PERIOD CLOSED DUE TO CLAIM FOR BENEFIT")
  case object PseudoEndDate                     extends EnumLtpedttp("PSEUDO END DATE")
  case object PsuedoReturnFromAbroad            extends EnumLtpedttp("PSUEDO RETURN FROM ABROAD")
  case object RreEndedForMpa                    extends EnumLtpedttp("RRE ENDED FOR MPA")
  case object RreLapsedByChangeInMaritalStatus  extends EnumLtpedttp("RRE LAPSED BY CHANGE IN MARITAL STATUS")
  case object RreLapsedByCredits                extends EnumLtpedttp("RRE LAPSED BY CREDITS")
  case object RreLapsedByRevocation             extends EnumLtpedttp("RRE LAPSED BY REVOCATION")
  case object RreLapsedByRpfa                   extends EnumLtpedttp("RRE LAPSED BY RPFA")
  case object RreLapsedByVpaApplication         extends EnumLtpedttp("RRE LAPSED BY VPA APPLICATION")
  case object RreLapsedDueToFailureOf2YearCheck extends EnumLtpedttp("RRE LAPSED DUE TO FAILURE OF 2 YEAR CHECK")
}
