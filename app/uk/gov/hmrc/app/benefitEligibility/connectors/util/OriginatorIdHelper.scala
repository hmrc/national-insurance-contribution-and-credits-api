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

package uk.gov.hmrc.app.benefitEligibility.connectors.util

import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CallSystem, OriginatorId}
import uk.gov.hmrc.app.config.AppConfig

import javax.inject.Inject

class OriginatorIdHelper  @Inject() (config: AppConfig){

  def getOriginatorId(benefitType: BenefitType, callSystem: Option[CallSystem] = None): OriginatorId =
    callSystem match {
      case Some(_) =>
        benefitType match {
          case BenefitType.MA => OriginatorId(config.hipOriginatorIdMa.searchlightId)
          case BenefitType.ESA => OriginatorId(config.hipOriginatorIdEsa.searchlightId)
          case BenefitType.JSA => OriginatorId(config.hipOriginatorIdJsa.searchlightId)
          case BenefitType.GYSP => OriginatorId(config.hipOriginatorIdGysp.searchlightId)
          case BenefitType.BSP => OriginatorId(config.hipOriginatorIdBsp.searchlightId)
        }
      case None =>
        benefitType match {
          case BenefitType.MA => OriginatorId(config.hipOriginatorIdMa.standardId)
          case BenefitType.ESA => OriginatorId(config.hipOriginatorIdEsa.standardId)
          case BenefitType.JSA => OriginatorId(config.hipOriginatorIdJsa.standardId)
          case BenefitType.GYSP => OriginatorId(config.hipOriginatorIdGysp.standardId)
          case BenefitType.BSP => OriginatorId(config.hipOriginatorIdBsp.standardId)
        }
    }
}
