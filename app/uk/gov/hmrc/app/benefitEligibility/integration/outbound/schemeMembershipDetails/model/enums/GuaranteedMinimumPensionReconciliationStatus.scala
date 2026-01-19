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

sealed abstract class GuaranteedMinimumPensionReconciliationStatus(override val entryName: String) extends EnumEntry

object GuaranteedMinimumPensionReconciliationStatus
    extends Enum[GuaranteedMinimumPensionReconciliationStatus]
    with PlayJsonEnum[GuaranteedMinimumPensionReconciliationStatus] {
  val values: immutable.IndexedSeq[GuaranteedMinimumPensionReconciliationStatus] = findValues

  case object GmpReconciled extends GuaranteedMinimumPensionReconciliationStatus("GMP RECONCILED")
  case object NotApplicable extends GuaranteedMinimumPensionReconciliationStatus("NOT APPLICABLE")
  case object NotKnown      extends GuaranteedMinimumPensionReconciliationStatus("NOT KNOWN")

  case object ReconciliationActionNotTaken
      extends GuaranteedMinimumPensionReconciliationStatus("RECONCILIATION ACTION NOT TAKEN")

  case object ReconciliationFailure extends GuaranteedMinimumPensionReconciliationStatus("RECONCILIATION FAILURE")
}
