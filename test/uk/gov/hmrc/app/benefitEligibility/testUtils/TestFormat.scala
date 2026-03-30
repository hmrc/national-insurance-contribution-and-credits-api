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

package uk.gov.hmrc.app.benefitEligibility.testUtils

import play.api.libs.json.{JsError, JsNumber, JsObject, JsString, JsSuccess, Json, OWrites, Reads, Writes}
import uk.gov.hmrc.app.benefitEligibility.model.common.MaternityAllowanceSortType
import uk.gov.hmrc.app.benefitEligibility.model.nps.npsError.*
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.*
import uk.gov.hmrc.app.benefitEligibility.model.request.*
import uk.gov.hmrc.app.benefitEligibility.model.response.BenefitEligibilityInfoResponse
import uk.gov.hmrc.app.benefitEligibility.model.response.BenefitEligibilityInfoErrorResponse
import uk.gov.hmrc.app.benefitEligibility.model.response.OverallResultSummary
import uk.gov.hmrc.app.benefitEligibility.model.response.SanitizedApiResult
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError
import uk.gov.hmrc.app.benefitEligibility.model.common.NpsNormalizedError.{
  AccessForbidden,
  BadRequest,
  InternalServerError,
  NotFound,
  ServiceUnavailable,
  UnexpectedStatus,
  UnprocessableEntity
}

object TestFormat {

  implicit val npsErrorResponse422SpecialWrites: Writes[NpsErrorResponse422Special] =
    Json.writes[NpsErrorResponse422Special]

  implicit val npsSingleErrorResponseWrites: Writes[NpsSingleErrorResponse] = Json.writes[NpsSingleErrorResponse]
  implicit val npsMultiErrorResponseWrites: OWrites[NpsMultiErrorResponse]  = Json.writes[NpsMultiErrorResponse]

  implicit val npsStandardErrorResponse400Writes: Writes[NpsStandardErrorResponse400] =
    Json.writes[NpsStandardErrorResponse400]

  implicit val hipFailureResponseWrites: Writes[HipFailureResponse] = Json.writes[HipFailureResponse]

  implicit val npsErrorResponseHipOriginResponseWrites: Writes[NpsErrorResponseHipOrigin] =
    Json.writes[NpsErrorResponseHipOrigin]

  implicit val contributionCreditWrites: Writes[ContributionsAndCreditsRequestParams] =
    Json.writes[ContributionsAndCreditsRequestParams]

  implicit val liabilitiesWrites: Writes[LiabilitiesRequestParams] = Json.writes[LiabilitiesRequestParams]

  implicit val maternityAllowanceSortTypeWrites: Writes[MaternityAllowanceSortType] =
    Json.writes[MaternityAllowanceSortType]

  implicit val longTermBenefitCalculationWrites: Writes[LongTermBenefitCalculationRequestParams] =
    Json.writes[LongTermBenefitCalculationRequestParams]

  implicit val esaEligibilityCheckDataRequestWrites: Writes[ESAEligibilityCheckDataRequest] =
    Json.writes[ESAEligibilityCheckDataRequest]

  implicit val searchlightEligibilityCheckDataRequestWrites: Writes[SearchlightEligibilityCheckDataRequest] =
    Json.writes[SearchlightEligibilityCheckDataRequest]

  implicit val maEligibilityCheckDataRequestWrites: Writes[MAEligibilityCheckDataRequest] =
    Json.writes[MAEligibilityCheckDataRequest]

  implicit val jsaEligibilityCheckDataRequestWrites: Writes[JSAEligibilityCheckDataRequest] =
    Json.writes[JSAEligibilityCheckDataRequest]

  implicit val bspEligibilityCheckDataRequestWrites: Writes[BSPEligibilityCheckDataRequest] =
    Json.writes[BSPEligibilityCheckDataRequest]

  implicit val gypEligibilityCheckDataRequestWrites: Writes[GYSPEligibilityCheckDataRequest] =
    Json.writes[GYSPEligibilityCheckDataRequest]

  implicit val overallResultSummaryReads: Reads[OverallResultSummary] = Json.reads[OverallResultSummary]
  implicit val sanitizedApiResultReads: Reads[SanitizedApiResult]     = Json.reads[SanitizedApiResult]

  implicit val npsNormalizedErrorReads: Reads[NpsNormalizedError] = Reads {
    case JsObject(underlying) =>
      val code             = underlying.getOrElse("code", JsError("code missing"))
      val message          = underlying.getOrElse("message", JsError("message missing"))
      val downstreamStatus = underlying.getOrElse("downstreamStatus", JsError("downstreamStatus missing"))

      (code, message, downstreamStatus) match {
        case (
              JsString(AccessForbidden.code),
              JsString(AccessForbidden.message),
              JsNumber(AccessForbidden.downstreamStatus)
            ) =>
          JsSuccess(AccessForbidden)

        case (JsString(BadRequest.code), JsString(BadRequest.message), JsNumber(BadRequest.downstreamStatus)) =>
          JsSuccess(BadRequest)

        case (JsString(NotFound.code), JsString(NotFound.message), JsNumber(NotFound.downstreamStatus)) =>
          JsSuccess(NotFound)

        case (
              JsString(UnprocessableEntity.code),
              JsString(UnprocessableEntity.message),
              JsNumber(UnprocessableEntity.downstreamStatus)
            ) =>
          JsSuccess(UnprocessableEntity)

        case (
              JsString(ServiceUnavailable.code),
              JsString(ServiceUnavailable.message),
              JsNumber(ServiceUnavailable.downstreamStatus)
            ) =>
          JsSuccess(ServiceUnavailable)

        case (
              JsString(InternalServerError.code),
              JsString(InternalServerError.message),
              JsNumber(InternalServerError.downstreamStatus)
            ) =>
          JsSuccess(InternalServerError)

        case (
              JsString("UNEXPECTED_STATUS_CODE"),
              JsString("downstream returned an unexpected status"),
              JsNumber(status)
            ) =>
          JsSuccess(UnexpectedStatus(status.toInt))
        case _ => JsError("incompatible json found, does not match a known error")
      }
    case _ => JsError("invalid type, expected JsObject()")
  }

  implicit val benefitEligibilityInfoErrorResponseReads: Reads[BenefitEligibilityInfoErrorResponse] =
    Json.reads[BenefitEligibilityInfoErrorResponse]

}
