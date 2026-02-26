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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.{
  IndividualStatePensionInformationSuccessResponse,
  NumberOfQualifyingYears,
  QualifyingTaxYear,
  TotalPrimaryPaidEarnings
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*

case class FilteredIndividualStatePensionContributionsByTaxYear(
    totalPrimaryPaidEarnings: Option[TotalPrimaryPaidEarnings],
    qualifyingTaxYear: Option[QualifyingTaxYear]
)

object FilteredIndividualStatePensionContributionsByTaxYear {

  implicit val FilteredIndividualStatePensionContributionsByTaxYearWrites
      : Writes[FilteredIndividualStatePensionContributionsByTaxYear] =
    Json.writes[FilteredIndividualStatePensionContributionsByTaxYear]

}

case class FilteredIndividualStatePensionInfo(
    numberOfQualifyingYears: Option[NumberOfQualifyingYears],
    contributionsByTaxYear: List[FilteredIndividualStatePensionContributionsByTaxYear]
)

object FilteredIndividualStatePensionInfo {

  implicit val filteredIndividualStatePensionInfoWrites: Writes[FilteredIndividualStatePensionInfo] =
    Json.writes[FilteredIndividualStatePensionInfo]

  def from(
      individualStatePensionInformationSuccessResponse: IndividualStatePensionInformationSuccessResponse
  ): FilteredIndividualStatePensionInfo = FilteredIndividualStatePensionInfo(
    individualStatePensionInformationSuccessResponse.numberOfQualifyingYears,
    individualStatePensionInformationSuccessResponse.contributionsByTaxYear match {
      case Some(contributionsByTaxYear) =>
        contributionsByTaxYear.map { contributionByTaxYear =>
          FilteredIndividualStatePensionContributionsByTaxYear(
            contributionByTaxYear.totalPrimaryPaidEarnings,
            contributionByTaxYear.qualifyingTaxYear
          )
        }
      case None => Nil
    }
  )

}
