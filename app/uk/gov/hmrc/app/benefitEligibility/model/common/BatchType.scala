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

sealed abstract class BatchType(override val entryName: String) extends EnumEntry

object BatchType extends Enum[BatchType] with PlayJsonEnum[BatchType] {
  val values: immutable.IndexedSeq[BatchType] = findValues

  def from(eligibilityCheckDataRequest: EligibilityCheckDataRequest): Option[BatchType] =
    eligibilityCheckDataRequest match {
      case req: BSPEligibilityCheckDataRequest  => Some(BatchType.BspBatch)
      case req: MAEligibilityCheckDataRequest   => Some(BatchType.MaBatch)
      case req: GYSPEligibilityCheckDataRequest => Some(BatchType.GyspBatch)
      case req: SearchlightEligibilityCheckDataRequest if req.benefitType == BenefitType.BSP =>
        Some(BatchType.BspSearchLightBatch)
      case _ => None
    }

  case object MaBatch extends BatchType("MA")

  case object GyspBatch extends BatchType("GYSP")

  case object BspBatch extends BatchType("BSP")

  case object BspSearchLightBatch extends BatchType("BSP_SEARCHLIGHT")

}
