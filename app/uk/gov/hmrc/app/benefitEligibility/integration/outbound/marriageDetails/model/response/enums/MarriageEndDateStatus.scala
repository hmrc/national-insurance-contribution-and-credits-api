/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class MarriageEndDateStatus(override val entryName: String) extends EnumEntry

object MarriageEndDateStatus extends Enum[MarriageEndDateStatus] with PlayJsonEnum[MarriageEndDateStatus] {
  val values: immutable.IndexedSeq[MarriageEndDateStatus] = findValues

  case object InEffect   extends MarriageEndDateStatus("IN EFFECT")
  case object NotKnown   extends MarriageEndDateStatus("NOT KNOWN")
  case object Unverified extends MarriageEndDateStatus("UNVERIFIED")
  case object Verified   extends MarriageEndDateStatus("VERIFIED")
}
