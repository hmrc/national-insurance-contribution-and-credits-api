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

package uk.gov.hmrc.app.benefitEligibility.service.aggregation

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, GYSP, MA}
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.{
  EndDate,
  SpouseForename,
  SpouseSurname,
  StartDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.enums.{
  MarriageEndDateStatus,
  MarriageStartDateStatus,
  MarriageStatus
}

sealed trait AggregatedData {
  def benefitType: BenefitType
}

object AggregatedData {

  case class AggregatedDataMA(
      receiptDate: List[ReceiptDate]
      //      contributionCreditType: String,
      //      taxYear: Int,
      //      numberOfCreditsAndConts: Int,
      //      latePaymentPeriod: Boolean,
      //      startDate: LocalDate,
      //      endDate: LocalDate
  ) extends AggregatedData {
    def benefitType: BenefitType = MA
  }

  case class AggregatedDataBSP(
      // primaryPaidEarnings: BigDecimal,
      // contributionCategory: ContributionCategory,
      // contributionCategoryLetter: ContributionCategoryLetter,
      // primaryContribution: Int,
      // class1ContributionStatus: Class1ContributionStatus,
      // contributionCreditType: ContributionCreditType,
      // taxYear: TaxYear,
      // numberOfCreditsAndConts: Int,
      status: MarriageStatus,
      startDate: StartDate,
      startDateStatus: MarriageStartDateStatus,
      endDate: EndDate,
      endDateStatus: MarriageEndDateStatus,
      // terminationReason: String,
      spouseIdentifier: Identifier,
      spouseForename: SpouseForename,
      spouseSurname: SpouseSurname
  ) extends AggregatedData {
    def benefitType: BenefitType = BSP
  }

  case class AggregatedDataGYSP(
      // guaranteedMinimumPensionContractedOutDeductionsPre1988: Int,
      // guaranteedMinimumPensionContractedOutDeductionsPost1988: Int,
      // contractedOutDeductionsPre1988: Int,
      // contractedOutDeductionsPost1988: Int,
      // contributionCategory: ContributionCategory,
      // contributionCategoryLetter: ContributionCategoryLetter,
      // contributionCreditType: ContributionCreditType,
      // taxYear: TaxYear,
      // numberOfCreditsAndConts: Int,
      // totalGraduatedPensionUnits: Int,
      // employerName: String,
      // longTermBenefitNotes: String,
      status: MarriageStatus,
      startDate: StartDate,
      startDateStatus: MarriageStartDateStatus,
      endDate: EndDate,
      endDateStatus: MarriageEndDateStatus,
      // terminationReason: String,
      spouseIdentifier: Identifier,
      spouseForename: SpouseForename,
      spouseSurname: SpouseSurname
      // schemeMembershipStartDate: StartDate,
      // schemeMembershipEndDate: EndDate,
      // employersContractedOutNumberDetails: Int,
      // qualifyingTaxYear: TaxYear,
      // primaryPainEarnings: BigDecimal
  ) extends AggregatedData {
    def benefitType: BenefitType = GYSP
  }

}
