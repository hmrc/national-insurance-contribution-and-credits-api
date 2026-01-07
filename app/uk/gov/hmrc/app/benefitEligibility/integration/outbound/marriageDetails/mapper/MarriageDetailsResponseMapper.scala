/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.mapper

import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.{
  AccessForbidden,
  BadRequest,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.MarriageDetailsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.{
  MarriageDetailsError,
  MarriageDetailsResponse
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsNormalizedError, NpsResponseMapper}

class MarriageDetailsResponseMapper extends NpsResponseMapper[MarriageDetailsResponse, MarriageDetailsResult] {

  def toResult(response: MarriageDetailsResponse): MarriageDetailsResult =
    response match {

      case response: MarriageDetailsSuccessResponse =>
        MarriageDetailsResult(Success, Some(response), None)

      case MarriageDetailsError.MarriageDetailsErrorResponse400(failures) =>
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              code = BadRequest,
              message = failures.map(_.reason).mkString(","),
              downstreamStatus = 400
            )
          )
        )

      case MarriageDetailsError.MarriageDetailsErrorResponse403(reason, code) =>
        MarriageDetailsResult(
          Failure,
          None,
          Some(NpsNormalizedError(code = AccessForbidden, message = reason.entryName, downstreamStatus = 403))
        )

      case MarriageDetailsError.MarriageDetailsErrorResponse422(failures) =>
        MarriageDetailsResult(
          Failure,
          None,
          Some(
            NpsNormalizedError(
              code = UnprocessableEntity,
              message = failures.map(_.reason).mkString(","),
              downstreamStatus = 422
            )
          )
        )
    }

  def toResult(textualErrorStatusCode: NormalizedErrorStatusCode) =
    MarriageDetailsResult(
      Failure,
      None,
      Some(
        NpsNormalizedError(
          textualErrorStatusCode,
          textualErrorStatusCode.message,
          textualErrorStatusCode.code
        )
      )
    )

}
