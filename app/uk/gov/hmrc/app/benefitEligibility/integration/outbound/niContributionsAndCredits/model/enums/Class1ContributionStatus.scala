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

sealed abstract class Class1ContributionStatus(override val entryName: String) extends EnumEntry

object Class1ContributionStatus extends Enum[Class1ContributionStatus] with PlayJsonEnum[Class1ContributionStatus] {
  val values: immutable.IndexedSeq[Class1ContributionStatus] = findValues

  case object ComplianceAndYieldIncomplete extends Class1ContributionStatus("COMPLIANCE & YIELD INCOMPLETE")

  case object Current extends Class1ContributionStatus("CURRENT")

  case object HistoricAmend extends Class1ContributionStatus("HISTORIC AMENDED")

  case object HistoricCancelled extends Class1ContributionStatus("HISTORIC CANCELLED")

  case object InvalidNoRcf extends Class1ContributionStatus("INVALID - NO RCF")

  case object InvalidRcf extends Class1ContributionStatus("INVALID - RCF")

  case object InvalidCompatibilityCheck extends Class1ContributionStatus("INVALID COMPATIBILITY CHECK")

  case object NotKnownOrNotApplicable extends Class1ContributionStatus("NOT KNOWN / NOT APPLICABLE")

  case object Potential extends Class1ContributionStatus("POTENTIAL")

  case object Valid extends Class1ContributionStatus("VALID")

  case object ValidRcf extends Class1ContributionStatus("VALID RCF")
}
