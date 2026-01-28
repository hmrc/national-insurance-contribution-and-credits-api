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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

import scala.collection.immutable

sealed abstract class ContributionCreditType(override val entryName: String) extends EnumEntry

object ContributionCreditType extends Enum[ContributionCreditType] with PlayJsonEnum[ContributionCreditType] {
  val values: immutable.IndexedSeq[ContributionCreditType] = findValues

  case object NotKnown extends ContributionCreditType("NOT KNOWN")

  case object Class1NormalDeductionCard extends ContributionCreditType("CLASS 1 - NORMAL DEDUCTION CARD")

  case object Class1Hmf extends ContributionCreditType("CLASS 1- HMF")

  case object Class1Mariner extends ContributionCreditType("CLASS 1- MARINER")

  case object Class1CivilServantFringeBody extends ContributionCreditType("CLASS 1- CIVIL SERVANT/FRINGE BODY")

  case object Class1EmployeeOnly extends ContributionCreditType("CLASS 1- EMPLOYEE ONLY")

  case object Class2NormalRate extends ContributionCreditType("CLASS 2 - NORMAL RATE")

  case object Class2WomansRate extends ContributionCreditType("CLASS 2 - WOMAN'S RATE")

  case object Class2SharefishermansRate extends ContributionCreditType("CLASS 2 - SHAREFISHERMAN'S RATE")

  case object Class2NormalRateA extends ContributionCreditType("CLASS 2 - NORMAL RATE A")

  case object Class2NormalRateB extends ContributionCreditType("CLASS 2 - NORMAL RATE B")

  case object Class2NormalRateC extends ContributionCreditType("CLASS 2 - NORMAL RATE C")

  case object Class2NormalRateD extends ContributionCreditType("CLASS 2 - NORMAL RATE D")

  case object Class2SharefishermansRateA extends ContributionCreditType("CLASS 2 - SHAREFISHERMAN'S RATE A")

  case object Class2SharefishermansRateB extends ContributionCreditType("CLASS 2 - SHAREFISHERMAN'S RATE B")

  case object Class2SharefishermansRateC extends ContributionCreditType("CLASS 2 - SHAREFISHERMAN'S RATE C")

  case object Class2SharefishermansRateD extends ContributionCreditType("CLASS 2 - SHAREFISHERMAN'S RATE D")

  case object Class2VoluntaryDevelopmentWorkerRateA
      extends ContributionCreditType("CLASS 2 - VOLUNTARY DEVELOPMENT WORKER'S RATE A")

  case object Class3Pre85Rate extends ContributionCreditType("CLASS 3 - PRE 85 RATE")

  case object Class3RateA extends ContributionCreditType("CLASS 3 - RATE A")

  case object Class3RateB extends ContributionCreditType("CLASS 3 - RATE B")

  case object Class3RateC extends ContributionCreditType("CLASS 3 - RATE C")

  case object Class3RateD extends ContributionCreditType("CLASS 3 - RATE D")

  case object Class1Credit extends ContributionCreditType("CLASS 1 CREDIT")

  case object Class3Credit extends ContributionCreditType("CLASS 3 CREDIT")

  case object NotApplicable extends ContributionCreditType("NOT APPLICABLE")

  case object Class2VoluntaryDevelopmentWorkerRateB
      extends ContributionCreditType("CLASS 2 - VOLUNTARY DEVELOPMENT WORKER'S RATE B")

  case object Class2VoluntaryDevelopmentWorkerRateC
      extends ContributionCreditType("CLASS 2 - VOLUNTARY DEVELOPMENT WORKER'S RATE C")

  case object Class2VoluntaryDevelopmentWorkerRateD
      extends ContributionCreditType("CLASS 2 - VOLUNTARY DEVELOPMENT WORKER'S RATE D")

  case object Class2ContributionConvertedFromNirs1
      extends ContributionCreditType("CLASS 2 - CONTRIBUTION CONVERTED FROM NIRS1")

  case object Class3ContributionConvertedFromNirs1
      extends ContributionCreditType("CLASS 3 - CONTRIBUTION CONVERTED FROM NIRS1")

  case object CreditConvertedFromNirs1 extends ContributionCreditType("CREDIT CONVERTED FROM NIRS1")
}
