package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import play.api.libs.json.{Json, OFormat}

final case class RequestPayload(dateOfBirth: String)

object RequestPayload {

  implicit lazy val format: OFormat[RequestPayload] = Json.format

}
