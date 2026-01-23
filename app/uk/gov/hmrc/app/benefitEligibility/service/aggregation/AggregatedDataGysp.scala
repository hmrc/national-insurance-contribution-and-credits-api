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

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.GYSP
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, Identifier}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.{
  MarriageEndDate,
  MarriageStartDate,
  SpouseForename,
  SpouseSurname
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.{
  ContributionCategory,
  ContributionCreditType
}

import java.time.LocalDate

case class Class1CreditsAnsContributionsDataGysp(
    taxYear: Option[Int],
    contributionCategory: Option[ContributionCategory],
    contributionCategoryLetter: Option[ContributionCategoryLetter],
    contributionCreditType: Option[ContributionCreditType],
    numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions],
    employerName: Option[EmployerName]
)

case class Class2CreditsAnsContributionsGysp(
    taxYear: Option[Int],
    contributionCreditType: Option[ContributionCreditType],
    numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions]
)

case class BenefitCalculationDetailsDataGysp(
    guaranteedMinimumPensionContractedOutDeductionsPre1988: Option[BigDecimal],
    guaranteedMinimumPensionContractedOutDeductionsPost1988: Option[BigDecimal],
    contractedOutDeductionsPre1988: Option[BigDecimal],
    contractedOutDeductionsPost1988: Option[BigDecimal]
)

case class MarriageDetailsGysp(
    startDate: Option[MarriageStartDate],
    startDateStatus: Option[MarriageStartDateStatus],
    endDate: Option[MarriageEndDate],
    endDateStatus: Option[MarriageEndDateStatus],
    spouseIdentifier: Option[Identifier],
    spouseForename: Option[SpouseForename],
    spouseSurname: Option[SpouseSurname]
)

case class StatePensionDataGysp(noQualifyingYears: Int, qualifyingTaxYear: Int)

case class LongTermBenefitNote(value: String)

case class SchemeMembershipDetailsDataGysp(
    schemeMembershipStartDate: LocalDate,
    schemeMembershipEndDate: LocalDate,
    employersContractedOutNumberDetails: String
)

case class BenefitSchemeName(
    schemeName: String
)

case class AggregatedDataGysp(
    benefitSchemeName: Option[BenefitSchemeName],
    totalGraduatedPensionUnits: Option[BigDecimal],
    class1CreditsAnsContributions: List[Class1CreditsAnsContributionsDataGysp],
    class2CreditsAnsContributions: List[Class2CreditsAnsContributionsGysp],
    benefitCalcDetails: List[BenefitCalculationDetailsDataGysp],
    marriageDetails: List[MarriageDetailsGysp],
    longTermBenefitNotes: List[LongTermBenefitNote],
    schemeMembershipDetails: List[SchemeMembershipDetailsDataGysp],
    statePensionData: List[StatePensionDataGysp]
) extends AggregatedData {
  def benefitType: BenefitType = GYSP
}
