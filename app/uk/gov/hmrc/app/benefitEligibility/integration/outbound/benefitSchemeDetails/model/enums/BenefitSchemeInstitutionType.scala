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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class BenefitSchemeInstitutionType(override val entryName: String) extends EnumEntry

object BenefitSchemeInstitutionType
    extends Enum[BenefitSchemeInstitutionType]
    with PlayJsonEnum[BenefitSchemeInstitutionType] {
  val values: immutable.IndexedSeq[BenefitSchemeInstitutionType] = findValues

  case object None extends BenefitSchemeInstitutionType("(NONE)")

  case object Bank extends BenefitSchemeInstitutionType("BANK")

  case object BuildingSociety extends BenefitSchemeInstitutionType("BUILDING SOCIETY")

  case object FriendlySociety extends BenefitSchemeInstitutionType("FRIENDLY SOCIETY")

  case object InsuranceCompany extends BenefitSchemeInstitutionType("INSURANCE COMPANY")

  case object InvestmentTrust extends BenefitSchemeInstitutionType("INVESTMENT TRUST")

  case object NotKnown extends BenefitSchemeInstitutionType("NOT KNOWN")

  case object Other extends BenefitSchemeInstitutionType("OTHER")

  case object UnitTrust extends BenefitSchemeInstitutionType("UNIT TRUST")
}
