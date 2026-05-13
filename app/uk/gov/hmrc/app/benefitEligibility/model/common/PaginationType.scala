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

package uk.gov.hmrc.app.benefitEligibility.model.common

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import uk.gov.hmrc.app.benefitEligibility.model.request.*

import scala.collection.immutable

sealed abstract class PaginationType(override val entryName: String) extends EnumEntry

object PaginationType extends Enum[PaginationType] with PlayJsonEnum[PaginationType] {
  val values: immutable.IndexedSeq[PaginationType] = findValues

  def from(eligibilityCheckDataRequest: EligibilityCheckDataRequest): Option[PaginationType] =
    eligibilityCheckDataRequest match {
      case req: BSPEligibilityCheckDataRequest  => Some(PaginationType.BspPagination)
      case req: MAEligibilityCheckDataRequest   => Some(PaginationType.MaPagination)
      case req: GYSPEligibilityCheckDataRequest => Some(PaginationType.GyspPagination)
      case req: SearchlightEligibilityCheckDataRequest if req.benefitType == BenefitType.BSP =>
        Some(PaginationType.BspSearchLightPagination)
      case _ => None
    }

  case object MaPagination extends PaginationType("MA")

  case object GyspPagination extends PaginationType("GYSP")

  case object BspPagination extends PaginationType("BSP")

  case object BspSearchLightPagination extends PaginationType("BSP_SEARCHLIGHT")

}
