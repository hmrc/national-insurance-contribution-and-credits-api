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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class MarriageStatus(override val entryName: String) extends EnumEntry

object MarriageStatus extends Enum[MarriageStatus] with PlayJsonEnum[MarriageStatus] {
  val values: immutable.IndexedSeq[MarriageStatus] = findValues

  case object CivilPartner               extends MarriageStatus("CIVIL PARTNER")
  case object CivilPartnershipAnnulled   extends MarriageStatus("CIVIL PARTNERSHIP ANNULLED")
  case object CivilPartnershipDissolved  extends MarriageStatus("CIVIL PARTNERSHIP DISSOLVED")
  case object CivilPartnershipTerminated extends MarriageStatus("CIVIL PARTNERSHIP TERMINATED - REASON N/K")
  case object Divorced                   extends MarriageStatus("DIVORCED")
  case object MarriageAnnulled           extends MarriageStatus("MARRIAGE ANNULLED")
  case object MarriageTerminated         extends MarriageStatus("MARRIAGE TERMINATED - REASON NOT KNOWN")
  case object Married                    extends MarriageStatus("MARRIED")
  case object Single                     extends MarriageStatus("SINGLE")
  case object SurvivingCivilPartner      extends MarriageStatus("SURVIVING CIVIL PARTNER")
  case object Void                       extends MarriageStatus("VOID")
  case object Widowed                    extends MarriageStatus("WIDOWED")
}
