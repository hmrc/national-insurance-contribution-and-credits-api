package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NICCCredit(taxYear: Int,
                      contributionCategoryLetter: String,
                      contributionCategory: String,
                      totalContribution: BigDecimal,
                      primaryContribution: BigDecimal,
                      class1ContributionStatus: String,
                      primaryPaidEarnings: BigDecimal)

object NICCCredit{
  implicit val format: OFormat[NICCCredit] = Json.format[NICCCredit]
}
