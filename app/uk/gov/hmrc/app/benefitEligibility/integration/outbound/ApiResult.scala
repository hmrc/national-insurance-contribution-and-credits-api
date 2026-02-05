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
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsError
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, NpsNormalizedError}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.ErrorReport
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitCalculationDetails.model.BenefitCalculationDetailsSuccess.BenefitCalculationDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.BenefitSchemeDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse

import scala.collection.immutable

sealed abstract class NpsApiResponseStatus(override val entryName: String) extends EnumEntry

object NpsApiResponseStatus extends Enum[NpsApiResponseStatus] with PlayJsonEnum[NpsApiResponseStatus] {
  val values: immutable.IndexedSeq[NpsApiResponseStatus] = findValues

  case object Success extends NpsApiResponseStatus("SUCCESS")

  case object Failure extends NpsApiResponseStatus("FAILURE")
}

type ApiResult                       = NpsApiResult[ErrorReport, NpsSuccessfulApiResponse]
type Class2MaReceiptsResult          = NpsApiResult[ErrorReport, Class2MAReceiptsSuccessResponse]
type ContributionCreditResult        = NpsApiResult[ErrorReport, NiContributionsAndCreditsSuccessResponse]
type SchemeMembershipDetailsResult   = NpsApiResult[ErrorReport, SchemeMembershipDetailsSuccessResponse]
type MarriageDetailsResult           = NpsApiResult[ErrorReport, MarriageDetailsSuccessResponse]
type LiabilityResult                 = NpsApiResult[ErrorReport, LiabilitySummaryDetailsSuccessResponse]
type IndividualStatePensionResult    = NpsApiResult[ErrorReport, IndividualStatePensionInformationSuccessResponse]
type LongTermBenefitNotesResult      = NpsApiResult[ErrorReport, LongTermBenefitNotesSuccessResponse]
type BenefitSchemeDetailsResult      = NpsApiResult[ErrorReport, BenefitSchemeDetailsSuccessResponse]
type BenefitCalculationDetailsResult = NpsApiResult[ErrorReport, BenefitCalculationDetailsSuccessResponse]

//TODO - change type constraints to NpsApiResult[+A <: ErrorReport, +B <: NpsSuccessfulApiResponse] after we move all connectors to extend NpsResponseMapperV2
sealed trait NpsApiResult[+A <: ErrorReport, +B] {
  def apiName: ApiName
  def isSuccess: Boolean
  def isFailure: Boolean
  def getSuccess: Option[B] = None
  def getFailure: Option[A] = None
}

object NpsApiResult {

  final case class ErrorReport(normalizedError: NpsNormalizedError, npsError: Option[NpsError])

  final case class FailureResult[+A <: ErrorReport, +B](
      apiName: ApiName,
      result: A
  ) extends NpsApiResult[A, Nothing] {
    val isSuccess = false
    val isFailure = true

    override def getFailure: Option[A] = Some(result)
  }

  final case class SuccessResult[+A <: ErrorReport, +B](apiName: ApiName, result: B) extends NpsApiResult[Nothing, B] {
    val isSuccess = true
    val isFailure = false

    override def getSuccess: Option[B] = Some(result)
  }

}
