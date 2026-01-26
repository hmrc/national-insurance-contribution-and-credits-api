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

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.MA
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, ReceiptDate, TaxYear}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.{
  NumberOfCreditsAndContributions,
  PrimaryPaidEarnings
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.{
  ContributionCreditType,
  LatePaymentPeriod
}

import java.time.LocalDate

case class Class1CreditsAnsContributionsDataMa(
    taxYear: Option[TaxYear],
    contributionCreditType: Option[ContributionCreditType],
    noOfCreditsAndContributions: Option[NumberOfCreditsAndContributions],
    primaryPaidEarnings: Option[PrimaryPaidEarnings],
    latePaymentPeriod: Option[LatePaymentPeriod]
)

case class Class2CreditsAnsContributionsMa(
    taxYear: Option[TaxYear],
    contributionCreditType: Option[ContributionCreditType],
    noOfCreditsAndContributions: Option[NumberOfCreditsAndContributions],
    latePaymentPeriod: Option[LatePaymentPeriod]
)

case class LiabilitiesMa(
    startDate: LocalDate,
    endDate: LocalDate
)

case class Class2MaReceiptData(
    receiptDate: ReceiptDate
)

case class AggregatedDataMa(
    class1CreditsAnsContributions: List[Class1CreditsAnsContributionsDataMa],
    class2CreditsAnsContributions: List[Class2CreditsAnsContributionsMa],
    class2MaReceiptDetails: List[Class2MaReceiptData],
    liabilitiesDetails: List[LiabilitiesMa]
) extends AggregatedData {
  def benefitType: BenefitType = MA
}
