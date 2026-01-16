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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class Class2Or3CreditStatus(override val entryName: String) extends EnumEntry

object Class2Or3CreditStatus extends Enum[Class2Or3CreditStatus] with PlayJsonEnum[Class2Or3CreditStatus] {
  val values: immutable.IndexedSeq[Class2Or3CreditStatus] = findValues

  case object NotKnowNotApplicable extends Class2Or3CreditStatus("NOT KNOWN/NOT APPLICABLE")

  case object Valid extends Class2Or3CreditStatus("VALID")

  case object Invalid extends Class2Or3CreditStatus("INVALID")

  case object Potential extends Class2Or3CreditStatus("POTENTIAL")

  case object InvalidCompatibilityCheck extends Class2Or3CreditStatus("INVALID COMPATIBILITY CHECK")

}
