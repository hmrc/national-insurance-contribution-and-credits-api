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

sealed abstract class LongTermBenefitType(override val entryName: String) extends EnumEntry

object LongTermBenefitType extends Enum[LongTermBenefitType] with PlayJsonEnum[LongTermBenefitType] {
  val values: immutable.IndexedSeq[LongTermBenefitType] = findValues

  case object All                      extends LongTermBenefitType("ALL")
  case object AutoPieAutoJsp           extends LongTermBenefitType("AUTOPIE / AUTOJSP")
  case object BereavedNsp              extends LongTermBenefitType("BEREAVED NSP")
  case object BereavementBenefit       extends LongTermBenefitType("BEREAVEMENT BENEFIT")
  case object CaaUs                    extends LongTermBenefitType("CAA/US")
  case object CarersAllowance          extends LongTermBenefitType("CARER'S ALLOWANCE")
  case object CategoryB                extends LongTermBenefitType("CATEGORY B")
  case object ClaimsPaper              extends LongTermBenefitType("CLAIMS PAPER")
  case object DependantsWarPension     extends LongTermBenefitType("DEPENDANT'S WAR PENSION")
  case object EmploymentAndSupport     extends LongTermBenefitType("EMPLOYMENT AND SUPPORT")
  case object EquivalentPensionBenefit extends LongTermBenefitType("EQUIVALENT PENSION BENEFIT")
  case object Gbu                      extends LongTermBenefitType("GBU")
  case object GuardiansAllowance       extends LongTermBenefitType("GUARDIAN'S ALLOWANCE")
  case object HousingBenefit           extends LongTermBenefitType("HOUSING BENEFIT")
  case object IncapacityBenefit        extends LongTermBenefitType("INCAPACITY BENEFIT")
  case object IndustrialDeathBenefit   extends LongTermBenefitType("INDUSTRIAL DEATH BENEFIT")
  case object JobReleaseAllowance      extends LongTermBenefitType("JOB RELEASE ALLOWANCE")
  case object JobSeekersAllowance      extends LongTermBenefitType("JOB SEEKER'S ALLOWANCE")
  case object Ltb                      extends LongTermBenefitType("LTB")
  case object MaternityAllowance       extends LongTermBenefitType("MATERNITY ALLOWANCE")
  case object NotKnown                 extends LongTermBenefitType("NOT KNOWN")
  case object PensionSharingOnDivorce  extends LongTermBenefitType("PENSION SHARING ON DIVORCE")
  case object Pre2001Widower           extends LongTermBenefitType("PRE-2001 WIDOWER")
  case object RetirementPension        extends LongTermBenefitType("RETIREMENT PENSION")
  case object Rpfa                     extends LongTermBenefitType("RPFA")
  case object SicknessBenefitIvb       extends LongTermBenefitType("SICKNESS BENEFIT/IVB")
  case object StbNotUbJsa              extends LongTermBenefitType("STB (NOT UB/JSA)")
  case object UnemploymentBenefit      extends LongTermBenefitType("UNEMPLOYMENT BENEFIT")
  case object WidowsBenefit            extends LongTermBenefitType("WIDOWS BENEFIT")
}
