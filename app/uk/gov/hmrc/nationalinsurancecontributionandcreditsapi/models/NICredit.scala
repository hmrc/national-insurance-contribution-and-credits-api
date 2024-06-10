package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NICredit(taxYear: Int,
                    contributionCategoryLetter: String,
                    contributionCategory: String,
                    totalContribution: BigDecimal,
                    primaryContribution: BigDecimal,
                    class1ContributionStatus: String,
                    primaryPaidEarnings: BigDecimal)

object NICredit{
  implicit val format: OFormat[NICredit] = Json.format[NICredit]
}
