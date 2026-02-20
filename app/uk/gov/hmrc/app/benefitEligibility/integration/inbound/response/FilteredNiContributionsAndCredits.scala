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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound.response

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.TaxYear
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.*

case class FilteredClass1CreditsAndContributions(
    taxYear: Option[TaxYear],
    creditSource: Option[CreditSource],
    primaryPaidEarnings: Option[PrimaryPaidEarnings],
    contributionCategory: Option[ContributionCategory],
    contributionCategoryLetter: Option[ContributionCategoryLetter],
    primaryContribution: Option[PrimaryContribution],
    class1ContributionStatus: Option[Class1ContributionStatus],
    contributionCreditType: Option[NiContributionCreditType],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions],
    numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions],
    employerName: Option[EmployerName]
)

object FilteredClass1CreditsAndContributions {

  implicit val filteredClass1CreditsAndContributionsWrites: Writes[FilteredClass1CreditsAndContributions] =
    Json.writes[FilteredClass1CreditsAndContributions]

}

case class FilteredClass2CreditsAndContributions(
    taxYear: Option[TaxYear],
    class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
    class2NIContributionAmount: Option[Class2NIContributionAmount],
    class2Or3CreditStatus: Option[Class2Or3CreditStatus],
    contributionCreditType: Option[NiContributionCreditType],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions],
    numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions]
)

object FilteredClass2CreditsAndContributions {

  implicit val filteredClass2CreditsAndContributionsWrites: Writes[FilteredClass2CreditsAndContributions] =
    Json.writes[FilteredClass2CreditsAndContributions]

}

case class FilteredNiContributionsAndCredits(
    totalGraduatedPensionUnits: TotalGraduatedPensionUnits,
    class1CreditsAndContributions: List[FilteredClass1CreditsAndContributions],
    class2CreditsAndContributions: List[FilteredClass2CreditsAndContributions]
)

object FilteredNiContributionsAndCredits {

  implicit val filteredNiContributionsAndCreditsWrites: Writes[FilteredNiContributionsAndCredits] =
    Json.writes[FilteredNiContributionsAndCredits]

  def from(
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse
  ): FilteredNiContributionsAndCredits = ???

}
