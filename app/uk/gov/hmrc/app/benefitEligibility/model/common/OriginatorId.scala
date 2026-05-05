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

import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.model.request.*
import uk.gov.hmrc.app.config.AppConfig

case class OriginatorId(value: String)

object OriginatorId {

  def from(
      eligibilityCheckDataRequest: EligibilityCheckDataRequest,
      appConfig: AppConfig
  ): Option[OriginatorId] =
    eligibilityCheckDataRequest match {
      case req: ESAEligibilityCheckDataRequest  => Some(OriginatorId(appConfig.hipOriginatorIdEsa.standardId))
      case req: JSAEligibilityCheckDataRequest  => Some(OriginatorId(appConfig.hipOriginatorIdJsa.standardId))
      case req: BSPEligibilityCheckDataRequest  => Some(OriginatorId(appConfig.hipOriginatorIdBsp.standardId))
      case req: MAEligibilityCheckDataRequest   => Some(OriginatorId(appConfig.hipOriginatorIdMa.standardId))
      case req: GYSPEligibilityCheckDataRequest => Some(OriginatorId(appConfig.hipOriginatorIdGysp.standardId))
      case req: SearchlightEligibilityCheckDataRequest =>
        if (req.benefitType == BSP) Some(OriginatorId(appConfig.hipOriginatorIdBsp.searchlightId))
        else None
    }

  def from(
      value: String,
      appConfig: AppConfig
  ): Option[OriginatorId] =
    value match {
      case appConfig.hipOriginatorIdEsa.standardId    => Some(OriginatorId(appConfig.hipOriginatorIdEsa.standardId))
      case appConfig.hipOriginatorIdJsa.standardId    => Some(OriginatorId(appConfig.hipOriginatorIdJsa.standardId))
      case appConfig.hipOriginatorIdBsp.standardId    => Some(OriginatorId(appConfig.hipOriginatorIdBsp.standardId))
      case appConfig.hipOriginatorIdMa.standardId     => Some(OriginatorId(appConfig.hipOriginatorIdMa.standardId))
      case appConfig.hipOriginatorIdGysp.standardId   => Some(OriginatorId(appConfig.hipOriginatorIdGysp.standardId))
      case appConfig.hipOriginatorIdBsp.searchlightId => Some(OriginatorId(appConfig.hipOriginatorIdBsp.searchlightId))
      case _                                          => None
    }

}
