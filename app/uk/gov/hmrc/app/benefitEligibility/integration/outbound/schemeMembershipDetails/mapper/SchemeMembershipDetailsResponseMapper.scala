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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.mapper

import io.scalaland.chimney.dsl.into
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.SchemeMembershipDetails
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError
import uk.gov.hmrc.app.benefitEligibility.common.NpsNormalizedError.{AccessForbidden, BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.{
  SchemeMembershipDetailsError,
  SchemeMembershipDetailsResponse
}

class SchemeMembershipDetailsResponseMapper
    extends NpsResponseMapper[SchemeMembershipDetailsResponse, SchemeMembershipDetailsResult] {

  def toApiResult(
      response: SchemeMembershipDetailsResponse
  ): SchemeMembershipDetailsResult =
    response match {
      case SchemeMembershipDetailsError.SchemeMembershipDetailsErrorResponse400(failures) =>
        FailureResult(SchemeMembershipDetails, BadRequest)
      case SchemeMembershipDetailsError.SchemeMembershipDetailsErrorResponse403(reason, code) =>
        FailureResult(SchemeMembershipDetails, AccessForbidden)
      case SchemeMembershipDetailsError.SchemeMembershipDetailsErrorResponse422(failures) =>
        FailureResult(SchemeMembershipDetails, UnprocessableEntity)
      case response: SchemeMembershipDetailsSuccessResponse =>
        SuccessResult(SchemeMembershipDetails, response)
    }

}
