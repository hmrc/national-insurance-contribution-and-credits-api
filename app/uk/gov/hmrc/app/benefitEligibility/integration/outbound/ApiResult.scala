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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse

import scala.collection.immutable

sealed abstract class NpsApiResponseStatus(override val entryName: String) extends EnumEntry

object NpsApiResponseStatus extends Enum[NpsApiResponseStatus] with PlayJsonEnum[NpsApiResponseStatus] {
  val values: immutable.IndexedSeq[NpsApiResponseStatus] = findValues

  case object Success extends NpsApiResponseStatus("SUCCESS")

  case object Failure extends NpsApiResponseStatus("FAILURE")
}

type ApiResult                     = NpsApiResult[NpsNormalizedError, NpsSuccessfulApiResponse]
type Class2MaReceiptsResult        = NpsApiResult[NpsNormalizedError, Class2MAReceiptsSuccessResponse]
type ContributionCreditResult      = NpsApiResult[NpsNormalizedError, NiContributionsAndCreditsSuccessResponse]
type SchemeMembershipDetailsResult = NpsApiResult[NpsNormalizedError, SchemeMembershipDetailsSuccessResponse]
type MarriageDetailsResult         = NpsApiResult[NpsNormalizedError, MarriageDetailsSuccessResponse]
type LiabilityResult               = NpsApiResult[NpsNormalizedError, LiabilitySummaryDetailsSuccessResponse]

sealed trait NpsApiResult[+A, +B] {
  def apiName: ApiName
  def isSuccess: Boolean
  def isFailure: Boolean
  def getSuccess: Option[B] = None
  def getFailure: Option[A] = None
}

object NpsApiResult {

  final case class DownstreamErrorReport(
      apiName: ApiName,
      value: NpsNormalizedError
  ) extends NpsApiResult[NpsNormalizedError, Nothing] {
    val isSuccess = false
    val isFailure = true

    override def getFailure: Option[NpsNormalizedError] = Some(value)
  }

  final case class DownstreamSuccessResponse[A, B <: NpsSuccessfulApiResponse](apiName: ApiName, value: B)
      extends NpsApiResult[Nothing, B] {
    val isSuccess = true
    val isFailure = false

    override def getSuccess: Option[B] = Some(value)
  }

}
