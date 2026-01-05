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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.mapper

import io.scalaland.chimney.dsl.into
import uk.gov.hmrc.app.benefitEligibility.common.TextualErrorStatusCode
import uk.gov.hmrc.app.benefitEligibility.common.TextualErrorStatusCode.{
  AccessForbidden,
  BadRequest,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.Class2MaReceiptsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.{
  Class2MAReceiptsError,
  Class2MAReceiptsResponse,
  Class2MAReceiptsSuccess
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  NpsApiResponseStatus,
  NpsApiResult,
  NpsError,
  NpsResponseMapper
}

class Class2MAReceiptsResponseMapper extends NpsResponseMapper[Class2MAReceiptsResponse, Class2MaReceiptsResult] {

  def toResult(response: Class2MAReceiptsResponse): Class2MaReceiptsResult =
    response match {

      case response: Class2MAReceiptsSuccessResponse =>
        Class2MaReceiptsResult(Success, Some(response), None)

      case Class2MAReceiptsError.Class2MAReceiptsErrorResponse403(reason, code) =>
        Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsError(code = AccessForbidden, message = reason.entryName, downstreamStatus = 403))
        )
      case Class2MAReceiptsError.Class2MAReceiptsErrorResponse400(failures) =>
        Class2MaReceiptsResult(
          Failure,
          None,
          Some(NpsError(code = BadRequest, message = failures.map(_.reason).mkString(","), downstreamStatus = 400))
        )
      case Class2MAReceiptsError.Class2MAReceiptsError422Response(failures) =>
        Class2MaReceiptsResult(
          Failure,
          None,
          Some(
            NpsError(
              code = UnprocessableEntity,
              message = failures.map(_.reason).mkString(","),
              downstreamStatus = 422
            )
          )
        )
    }

  def toResult(textualErrorStatusCode: TextualErrorStatusCode) =
    Class2MaReceiptsResult(
      Failure,
      None,
      Some(
        NpsError(
          textualErrorStatusCode,
          textualErrorStatusCode.message,
          textualErrorStatusCode.code
        )
      )
    )

}
