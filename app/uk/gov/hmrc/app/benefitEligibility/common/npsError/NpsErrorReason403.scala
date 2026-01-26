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

import scala.collection.immutable

sealed abstract class NpsErrorReason403(override val entryName: String) extends EnumEntry

object NpsErrorReason403 extends Enum[NpsErrorReason403] with PlayJsonEnum[NpsErrorReason403] {
  val values: immutable.IndexedSeq[NpsErrorReason403] = findValues
  case object Forbidden        extends NpsErrorReason403("Forbidden")
  case object UserUnauthorised extends NpsErrorReason403("User Not Authorised")
}
