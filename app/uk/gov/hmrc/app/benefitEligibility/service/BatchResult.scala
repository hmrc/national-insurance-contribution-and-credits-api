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

package uk.gov.hmrc.app.benefitEligibility.service

import uk.gov.hmrc.app.benefitEligibility.model.common.{CallSystem, CorrelationId, Identifier, BatchType}
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.repository.{ContributionAndCreditsBatching, BatchId, BatchCallback}

import java.util.UUID

final case class ContributionCreditBatchingResult(
    contributionCreditResult: Option[ContributionCreditResult],
    contributionAndCreditsBatching: Option[ContributionAndCreditsBatching]
)

final case class BatchResult(
    correlationId: CorrelationId,
    batchType: BatchType,
    nationalInsuranceNumber: Identifier,
    liabilitiesResult: List[LiabilityResult],
    marriageDetailsResult: Option[MarriageDetailsResult],
    contributionCreditResult: ContributionCreditBatchingResult,
    benefitSchemeMembershipDetailsData: Option[BenefitSchemeMembershipDetailsData],
    callSystem: Option[CallSystem],
    batchId: Option[BatchId]
) {

  private def shouldBatch: Boolean =
    (BatchCallback.fromLiabilities(liabilitiesResult) ++ List(
      BatchCallback.fromBenefitSchemeMembershipDetails(benefitSchemeMembershipDetailsData),
      BatchCallback.fromMarriageDetails(marriageDetailsResult)
    ).flatten).nonEmpty || contributionCreditResult.contributionAndCreditsBatching.isDefined

  def setBatchId(uuid: UUID): BatchResult = {
    val batchId = if (shouldBatch) {
      Some(BatchId(uuid))
    } else None

    this.copy(batchId = batchId)
  }

  def getBatchId: Option[BatchId] = this.batchId

  def allResults: List[ApiResult] = liabilitiesResult ++ List(
    marriageDetailsResult,
    contributionCreditResult.contributionCreditResult,
    benefitSchemeMembershipDetailsData.map(_.schemeMembershipDetailsResult)
  ).flatten ++ benefitSchemeMembershipDetailsData.map(_.benefitSchemeDetailsResults).getOrElse(Nil)

}
