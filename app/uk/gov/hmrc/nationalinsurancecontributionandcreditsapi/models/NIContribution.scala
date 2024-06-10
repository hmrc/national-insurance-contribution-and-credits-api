package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NIContribution(taxYear: Int,
                          numberOfCredits: Int,
                          contributionCreditTypeCode: String,
                          contributionCreditType: String,
                          class2Or3EarningsFactor: BigDecimal,
                          class2NicAmount: BigDecimal,
                          class2Or3CreditStatus: String)

object NIContribution{
  implicit val format: OFormat[NIContribution] = Json.format[NIContribution]
}
