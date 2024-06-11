package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

case class NICCContribution(taxYear: Int,
                            numberOfCredits: Int,
                            contributionCreditTypeCode: String,
                            contributionCreditType: String,
                            class2Or3EarningsFactor: BigDecimal,
                            class2NicAmount: BigDecimal,
                            class2Or3CreditStatus: String)

object NICCContribution{
  implicit val format: OFormat[NICCContribution] = Json.format[NICCContribution]
}
