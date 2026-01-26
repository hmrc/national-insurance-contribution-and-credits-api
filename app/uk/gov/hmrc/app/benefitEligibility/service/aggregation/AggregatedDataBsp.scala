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

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.BSP
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, Identifier, TaxYear}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.{
  MarriageEndDate,
  MarriageStartDate,
  SpouseForename,
  SpouseSurname
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus,
  MarriageStatus
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  ContributionCreditType
}

case class Class1CreditsAndContributionsDataBsp(
    taxYear: Option[TaxYear],
    primaryPaidEarnings: Option[PrimaryPaidEarnings],
    contributionCategory: Option[ContributionCategory],
    contributionCategoryLetter: Option[ContributionCategoryLetter],
    primaryContribution: Option[PrimaryContribution],
    class1ContributionStatus: Option[Class1ContributionStatus],
    contributionCreditType: Option[ContributionCreditType],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions]
)

case class Class2CreditsAndContributionsDataBsp(
    taxYear: Option[TaxYear],
    class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
    class2NIContributionAmount: Option[Class2NIContributionAmount],
    class2Or3CreditStatus: Option[Class2Or3CreditStatus],
    contributionCreditType: Option[ContributionCreditType],
    numberOfCreditsAndContributions: Option[NumberOfCreditsAndContributions]
)

case class MarriageDetailsBsp(
    status: Option[MarriageStatus],
    startDate: Option[MarriageStartDate],
    startDateStatus: Option[MarriageStartDateStatus],
    endDate: Option[MarriageEndDate],
    endDateStatus: Option[MarriageEndDateStatus],
    spouseIdentifier: Option[Identifier],
    spouseForename: Option[SpouseForename],
    spouseSurname: Option[SpouseSurname]
)

case class AggregatedDataBsp(
    class1CreditsAnsContributions: List[Class1CreditsAndContributionsDataBsp],
    class2CreditsAnsContributions: List[Class2CreditsAndContributionsDataBsp],
    marriageDetails: List[MarriageDetailsBsp]
) extends AggregatedData {
  def benefitType: BenefitType = BSP
}
