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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.mapper

import uk.gov.hmrc.app.benefitEligibility.common.ApiName.IndividualStatePension
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, ServiceUnavailable}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.{
  IndividualStatePensionInformationError,
  IndividualStatePensionInformationResponse,
  IndividualStatePensionInformationSuccess
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{IndividualStatePensionResult, NpsResponseMapper}

class IndividualStatePensionInformationResponseMapper
    extends NpsResponseMapper[IndividualStatePensionInformationResponse, IndividualStatePensionResult] {

  def toApiResult(response: IndividualStatePensionInformationResponse): IndividualStatePensionResult =
    response match {
      case _: IndividualStatePensionInformationError.IndividualStatePensionInformationErrorResponse400 =>
        FailureResult(IndividualStatePension, BadRequest)
      case _: IndividualStatePensionInformationError.IndividualStatePensionInformationErrorResponse403 =>
        FailureResult(IndividualStatePension, AccessForbidden)
      case _: IndividualStatePensionInformationError.IndividualStatePensionInformationErrorResponse503 =>
        FailureResult(IndividualStatePension, ServiceUnavailable)
      case response: IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse =>
        SuccessResult(IndividualStatePension, response)
    }

}
