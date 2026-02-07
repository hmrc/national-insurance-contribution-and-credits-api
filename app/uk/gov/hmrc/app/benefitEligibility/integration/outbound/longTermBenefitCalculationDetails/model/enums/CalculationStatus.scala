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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class CalculationStatus(override val entryName: String) extends EnumEntry

object CalculationStatus extends Enum[CalculationStatus] with PlayJsonEnum[CalculationStatus] {
  val values: immutable.IndexedSeq[CalculationStatus] = findValues

  case object Definitive    extends CalculationStatus("DEFINITIVE")
  case object Deleted       extends CalculationStatus("DELETED")
  case object Final         extends CalculationStatus("FINAL")
  case object Initial       extends CalculationStatus("INITIAL")
  case object NotApplicable extends CalculationStatus("NOT APPLICABLE")

}
