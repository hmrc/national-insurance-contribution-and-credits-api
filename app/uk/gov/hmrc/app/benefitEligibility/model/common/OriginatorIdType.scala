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
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.model.request.{
  BSPEligibilityCheckDataRequest,
  ESAEligibilityCheckDataRequest,
  EligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest,
  JSAEligibilityCheckDataRequest,
  MAEligibilityCheckDataRequest,
  SearchlightEligibilityCheckDataRequest
}

import scala.collection.immutable

sealed abstract class OriginatorIdType(override val entryName: String) extends EnumEntry

object OriginatorIdType extends Enum[OriginatorIdType] with PlayJsonEnum[OriginatorIdType] {
  val values: immutable.IndexedSeq[OriginatorIdType] = findValues

  case object MaOriginatorId extends OriginatorIdType("DWP-CF-MA-6")

  case object GyspOriginatorId extends OriginatorIdType("DWP-CF-GYSP-6")

  case object BspOriginatorId extends OriginatorIdType("DWP-CF-BSP-6")

  case object BspSearchLightOriginatorId extends OriginatorIdType("DWP-SEARCHLIGHT-CF-BSP-6")

  case object JsaOriginatorId extends OriginatorIdType("DWP-CF-JSA-6")

  case object EsaOriginatorId extends OriginatorIdType("DWP-CF-ESA-6")

  def from(eligibilityCheckDataRequest: EligibilityCheckDataRequest): Option[OriginatorIdType] =
    eligibilityCheckDataRequest match {
      case req: ESAEligibilityCheckDataRequest  => Some(EsaOriginatorId)
      case req: JSAEligibilityCheckDataRequest  => Some(JsaOriginatorId)
      case req: BSPEligibilityCheckDataRequest  => Some(BspOriginatorId)
      case req: MAEligibilityCheckDataRequest   => Some(MaOriginatorId)
      case req: GYSPEligibilityCheckDataRequest => Some(GyspOriginatorId)
      case req: SearchlightEligibilityCheckDataRequest =>
        if (req.benefitType == BSP) Some(BspSearchLightOriginatorId) else None
    }

}
