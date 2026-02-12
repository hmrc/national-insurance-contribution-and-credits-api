package uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.error

import play.api.libs.json.{Format, Json}

case class ErrorResponse(code: ErrorCode, reason: ErrorReason)

object ErrorResponse {
  implicit val errorResponseFormat: Format[ErrorResponse] = Json.format[ErrorResponse]
}
