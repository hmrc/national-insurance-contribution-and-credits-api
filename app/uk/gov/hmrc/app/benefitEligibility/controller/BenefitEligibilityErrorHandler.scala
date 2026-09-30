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

/*
 * Copyright 2026 HM Revenue & Customs
 * ...
 */
package uk.gov.hmrc.app.benefitEligibility.controller

import play.api.libs.json.*
import play.api.mvc.Results.*
import play.api.mvc.{Headers, Result}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.response.*
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier

object BenefitEligibilityErrorHandler {

  private val logger = new RequestAwareLogger(this.getClass)

  def handleError(
      error: BenefitEligibilityError,
      requestHeaders: Headers
  )(implicit hc: HeaderCarrier): Result = {

    val response = error match {
      case UnprocessableDataError(errors) =>
        UnprocessableEntity(
          Json.toJson(
            ErrorResponse(
              ErrorCode.UnprocessableEntity,
              ErrorReason(errors.mkString(","))
            )
          )
        )
      case InvalidCursorId(errorReason) =>
        BadRequest(
          Json.toJson(ErrorResponse(ErrorCode.BadRequest, errorReason))
        )
      case InvalidOriginatorId(errorReason) =>
        BadRequest(
          Json.toJson(ErrorResponse(ErrorCode.BadRequest, errorReason))
        )
      case MissingCursorId(errorReason) =>
        BadRequest(
          Json.toJson(ErrorResponse(ErrorCode.BadRequest, errorReason))
        )
      case InvalidOrMissingHeaderError(errorReason) =>
        BadRequest(
          Json.toJson(ErrorResponse(ErrorCode.BadRequest, errorReason))
        )
      case InvalidRequestJson(errorReason) =>
        BadRequest(
          Json.toJson(ErrorResponse(ErrorCode.BadRequest, errorReason))
        )
      case RecordNotFound(cursorId) =>
        NotFound(
          Json.toJson(
            ErrorResponse(ErrorCode.NotFound, ErrorReason(s"record not found for cursorId: ${cursorId.value}"))
          )
        )
      case FeatureDisabled(message) =>
        logger.error(s"could not process request, $message")
        NotFound
      case err =>
        logger.error(s"Error processing request", err)
        InternalServerError(
          Json.toJson(
            ErrorResponse(ErrorCode.InternalServerError, ErrorReason("Unexpected internal failure"))
          )
        )
    }

    RequestHelper.addCorrelationIdHeader(response, requestHeaders)

  }

}
