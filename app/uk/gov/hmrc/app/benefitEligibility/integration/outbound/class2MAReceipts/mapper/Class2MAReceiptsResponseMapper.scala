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
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.Class2MAReceipts
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{
  DownstreamErrorReport,
  DownstreamSuccessResponse
}
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
  ApiResult,
  Class2MaReceiptsResult,
  NpsResponseMapper,
  NpsSuccessfulApiResponse
}

class Class2MAReceiptsResponseMapper extends NpsResponseMapper[Class2MAReceiptsResponse, Class2MaReceiptsResult] {

  def toApiResult(response: Class2MAReceiptsResponse): Class2MaReceiptsResult =
    response match {
      case Class2MAReceiptsErrorResponse400(failures) =>
        DownstreamErrorReport(Class2MAReceipts, BadRequest)
      case Class2MAReceiptsErrorResponse403(reason, code) =>
        DownstreamErrorReport(Class2MAReceipts, AccessForbidden)
      case Class2MAReceiptsErrorResponse422(failures) =>
        DownstreamErrorReport(Class2MAReceipts, UnprocessableEntity)
      case response: Class2MAReceiptsSuccessResponse => DownstreamSuccessResponse(Class2MAReceipts, response)
    }

}
