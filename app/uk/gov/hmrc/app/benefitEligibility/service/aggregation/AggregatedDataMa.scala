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
import uk.gov.hmrc.app.benefitEligibility.common.{BenefitType, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.{
  NiClass1,
  NiClass2,
  NiContributionsAndCreditsSuccessResponse,
  NumberOfCreditsAndContributions,
  PrimaryPaidEarnings,
  TaxYear
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.{
  ContributionCreditType,
  LatePaymentPeriod
}

import java.time.LocalDate

case class NiContributionDataMa(
    taxYear: Option[TaxYear],
    contributionCreditType: Option[ContributionCreditType],
    noOfCreditsAndContributions: Option[NumberOfCreditsAndContributions],
    primaryPaidEarnings: Option[PrimaryPaidEarnings],
    latePaymentPeriod: Option[LatePaymentPeriod]
)

object NiContributionDataMa {

  def apply(niClass1: NiClass1) =
    new NiContributionDataMa(
      niClass1.taxYear,
      niClass1.contributionCreditType,
      None,
      niClass1.primaryPaidEarnings,
      niClass1.latePaymentPeriod
    )

  def apply(niClass2: NiClass2) =
    new NiContributionDataMa(
      niClass2.taxYear,
      niClass2.contributionCreditType,
      niClass2.noOfCreditsAndConts,
      None,
      niClass2.latePaymentPeriod
    )

}

case class LiabilitiesMa(
    startDate: LocalDate,
    endDate: LocalDate
)

case class Class2MaReceiptData(
    receiptDate: ReceiptDate
)

case class AggregatedDataMa(
    niContributionsAndCreditData: List[NiContributionDataMa],
    class2MaReceiptDetails: List[Class2MaReceiptData],
    liabilitiesDetails: List[LiabilitiesMa]
) extends AggregatedData {
  def benefitType: BenefitType = MA
}
