/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NICCContribution(taxYear: Int,
                            niContributionCategory: String,
                            niContributionCategoryName: String,
                            niContributionType: String,
                            totalPrimaryContribution: BigDecimal,
                            contributionStatus: String,
                            totalEarningsFactor: BigDecimal) {
  def this(niClass1: NICCClass1) = {
    this(niClass1.taxYear,
      niClass1.contributionCategoryLetter,
      niClass1.contributionCategory,
      niClass1.contributionCreditType,
      niClass1.primaryContribution,
      niClass1.class1ContributionStatus,
      niClass1.primaryPaidEarnings)
  }
}

object NICCContribution {
  implicit val format: OFormat[NICCContribution] = Json.format[NICCContribution]
}
