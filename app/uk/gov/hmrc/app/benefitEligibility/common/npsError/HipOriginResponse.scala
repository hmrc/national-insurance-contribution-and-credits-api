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

package uk.gov.hmrc.app.benefitEligibility.common.npsError

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*

import scala.collection.immutable

sealed abstract class HipOrigin(override val entryName: String) extends EnumEntry

object HipOrigin extends Enum[HipOrigin] with PlayJsonEnum[HipOrigin] {
  val values: immutable.IndexedSeq[HipOrigin] = findValues

  case object Hip extends HipOrigin("HIP")

  case object HoD extends HipOrigin("HoD")
}

case class FailureType(value: String) extends AnyVal

object FailureType {
  implicit val reads: Format[FailureType] = Json.valueFormat[FailureType]
}

case class HipFailureItem(
    `type`: FailureType,
    reason: NpsErrorReason
)

object HipFailureItem {
  implicit val reads: Format[HipFailureItem] = Json.format[HipFailureItem]
}

case class HipFailureResponse(
    failures: List[HipFailureItem]
)

object HipFailureResponse {
  implicit val reads: Format[HipFailureResponse] = Json.format[HipFailureResponse]
}
