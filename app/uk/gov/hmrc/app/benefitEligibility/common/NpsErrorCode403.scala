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

package uk.gov.hmrc.app.benefitEligibility.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class NpsErrorCode403(override val entryName: String) extends EnumEntry

object NpsErrorCode403 extends Enum[NpsErrorCode403] with PlayJsonEnum[NpsErrorCode403] {
  val values: immutable.IndexedSeq[NpsErrorCode403] = findValues

  case object NpsErrorCode403_2 extends NpsErrorCode403("403.2")

  case object NpsErrorCode403_1 extends NpsErrorCode403("403.1")
}
