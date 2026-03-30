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

import uk.gov.hmrc.app.benefitEligibility.model.common.{CallSystem, CorrelationId, Identifier, PaginationType}
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.repository.{
  ContributionAndCreditsPaging,
  PageTaskId,
  PaginationCursor,
  PaginationSource
}

import java.util.UUID

final case class ContributionCreditPagingResult(
    contributionCreditResult: Option[ContributionCreditResult],
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging]
)

final case class PaginationResult(
    correlationId: CorrelationId,
    paginationType: PaginationType,
    nationalInsuranceNumber: Identifier,
    liabilitiesResult: List[LiabilityResult],
    class2MaReceiptsResult: Option[Class2MaReceiptsResult],
    marriageDetailsResult: Option[MarriageDetailsResult],
    contributionCreditResult: ContributionCreditPagingResult,
    benefitSchemeMembershipDetailsData: Option[BenefitSchemeMembershipDetailsData],
    callSystem: Option[CallSystem],
    nextCursor: Option[PaginationCursor]
) {

  private def shouldPage: Boolean =
    (PaginationSource.fromLiabilities(liabilitiesResult) ++ List(
      PaginationSource.fromBenefitSchemeMembershipDetails(benefitSchemeMembershipDetailsData),
      PaginationSource.fromMarriageDetails(marriageDetailsResult),
      PaginationSource.fromClass2MAReceipts(class2MaReceiptsResult)
    ).flatten).nonEmpty || contributionCreditResult.contributionAndCreditsPaging.isDefined

  def setNextCursor(uuid: UUID): PaginationResult = {
    val cursor = if (shouldPage) {
      Some(PaginationCursor(paginationType, PageTaskId(uuid)))
    } else None

    this.copy(nextCursor = cursor)
  }

  def getNextCursor: Option[PaginationCursor] = this.nextCursor

  def allResults: List[ApiResult] = liabilitiesResult ++ List(
    marriageDetailsResult,
    class2MaReceiptsResult,
    contributionCreditResult.contributionCreditResult,
    benefitSchemeMembershipDetailsData.map(_.schemeMembershipDetailsResult)
  ).flatten ++ benefitSchemeMembershipDetailsData.map(_.benefitSchemeDetailsResults).getOrElse(Nil)

}
