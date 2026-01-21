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

package uk.gov.hmrc.app.benefitEligibility.service.aggregation

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.ESA
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.{
  Class2NIContributionAmount,
  Class2Or3EarningsFactor,
  ContributionCategoryLetter,
  NumberOfCreditsAndContributions,
  PrimaryContribution,
  PrimaryPaidEarnings
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  ContributionCreditType,
  CreditSource
}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.domain.TaxYear

case class Class1CreditsAndContributionsDataEsa(
    taxYear: Option[TaxYear],
    primaryPaidEarnings: Option[PrimaryPaidEarnings],
    contributionCreditType: Option[ContributionCreditType],
    creditSource: Option[CreditSource],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions]
)

case class Class2CreditsAndContributionsDataEsa(
    taxYear: Option[TaxYear],
    class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
    contributionCreditType: Option[ContributionCreditType],
    creditSource: Option[CreditSource],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions]
)

case class AggregatedDataEsa(
    class1CreditsAnsContributions: List[Class1CreditsAndContributionsDataEsa],
    class2CreditsAnsContributions: List[Class2CreditsAndContributionsDataEsa]
) extends AggregatedData {
  def benefitType: BenefitType = ESA
}
