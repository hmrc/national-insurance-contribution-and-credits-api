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
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode
import uk.gov.hmrc.app.benefitEligibility.common.NormalizedErrorStatusCode.{
  AccessForbidden,
  BadRequest,
  UnprocessableEntity
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResponseStatus.{Failure, Success}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.Class2MaReceiptsResult
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.{
  Class2MAReceiptsErrorResponse400,
  Class2MAReceiptsErrorResponse403,
  Class2MAReceiptsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.{
  Class2MAReceiptsError,
  Class2MAReceiptsResponse,
  Class2MAReceiptsSuccess
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  NpsApiResponseStatus,
  NpsApiResult,
  NpsNormalizedError,
  NpsResponseMapper
}

class Class2MAReceiptsResponseMapper extends NpsResponseMapper[Class2MAReceiptsResponse, Class2MaReceiptsResult] {

  def toResult(response: Class2MAReceiptsResponse): Class2MaReceiptsResult =
    response match {
      case resp: Class2MAReceiptsSuccessResponse => Class2MaReceiptsResult(Success, Some(resp), None)
      case _: Class2MAReceiptsErrorResponse403   => toResult(AccessForbidden)
      case _: Class2MAReceiptsErrorResponse400   => toResult(BadRequest)
      case _: Class2MAReceiptsErrorResponse422   => toResult(UnprocessableEntity)
    }

  def toResult(normalizedErrorStatusCode: NormalizedErrorStatusCode) =
    Class2MaReceiptsResult(
      Failure,
      None,
      Some(
        NpsNormalizedError(normalizedErrorStatusCode, normalizedErrorStatusCode.message, normalizedErrorStatusCode.code)
      )
    )

}
