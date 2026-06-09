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

package uk.gov.hmrc.app.benefitEligibility.repository

import cats.data.NonEmptyList
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{BenefitSchemeDetails, Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.model.common.PaginationType.BspSearchLightPagination
import uk.gov.hmrc.app.benefitEligibility.model.nps.{Class2MaReceiptsResult, LiabilityResult, MarriageDetailsResult}
import uk.gov.hmrc.app.benefitEligibility.service.{BenefitSchemeMembershipDetailsData, PaginationResult}
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.app.benefitEligibility.util.{CurrentTimeSource, NonEmptyListFormat}

import java.time.Instant
import java.util.UUID

final case class PaginationSource(
    apiName: ApiName,
    callBackURL: String
)

object PaginationSource {
  implicit val format: OFormat[PaginationSource] = Json.format[PaginationSource]

  def fromBenefitSchemeMembershipDetails(
      benefitSchemeMembershipDetailsData: Option[BenefitSchemeMembershipDetailsData]
  ): Option[PaginationSource] =
    benefitSchemeMembershipDetailsData.flatMap {
      _.schemeMembershipDetailsResult.getSuccess
        .flatMap(_.callback)
        .flatMap {
          _.callbackURL.map(url => PaginationSource(BenefitSchemeDetails, url.value))
        }
    }

  def fromMarriageDetails(marriageDetailsResult: Option[MarriageDetailsResult]): Option[PaginationSource] =
    marriageDetailsResult.flatMap {
      _.getSuccess
        .map(_.marriageDetails)
        .flatMap(m => m._links.flatMap(_.self.href.map(_.value)))
        .map(url => PaginationSource(MarriageDetails, url))
    }

  def fromLiabilities(liabilitiesResult: List[LiabilityResult]): List[PaginationSource] = liabilitiesResult.flatMap {
    _.getSuccess
      .flatMap(_.callback)
      .flatMap {
        _.callbackURL.map(url => PaginationSource(Liabilities, url.value))
      }
  }

  def fromClass2MAReceipts(class2MaReceiptsResult: Option[Class2MaReceiptsResult]): Option[PaginationSource] =
    class2MaReceiptsResult.flatMap(
      _.getSuccess
        .flatMap(_.callBack)
        .flatMap {
          _.callbackURL.map(url => PaginationSource(Liabilities, url.value))
        }
    )

}

final case class ContributionAndCreditsPaging private (
    apiName: ApiName,
    niContributionAndCreditsTaxWindows: NonEmptyList[TaxWindow],
    dateOfBirth: DateOfBirth
) {

  def tail: Option[ContributionAndCreditsPaging] = niContributionAndCreditsTaxWindows.toList.safeTailNel.map(
    remaining => this.copy(niContributionAndCreditsTaxWindows = remaining)
  )

}

object ContributionAndCreditsPaging {

  implicit val nonEmptyListTawWindowFormat: Format[NonEmptyList[TaxWindow]] =
    NonEmptyListFormat.nonEmptyListFormat[TaxWindow]

  implicit val format: OFormat[ContributionAndCreditsPaging] = Json.format[ContributionAndCreditsPaging]

  def apply(niContributionAndCreditsTaxWindows: NonEmptyList[TaxWindow], dateOfBirth: DateOfBirth) =
    new ContributionAndCreditsPaging(ApiName.NiContributionAndCredits, niContributionAndCreditsTaxWindows, dateOfBirth)

}

case class PageTaskId(value: UUID) extends AnyVal

object PageTaskId {
  implicit val pageTaskIdFormat: Format[PageTaskId] = Json.valueFormat[PageTaskId]

  def from(cursorId: CursorId): Option[PageTaskId] = {
    val uuidRegex = """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".r
    val id        = cursorId.value
    if (uuidRegex.matches(id)) Some(PageTaskId(UUID.fromString(id))) else None
  }

}

sealed trait PageTask {
  def correlationId: CorrelationId
  def pageTaskId: PageTaskId
  def paginationType: PaginationType
  def nationalInsuranceNumber: Identifier
  def createdAt: Instant
}

final case class MaPageTask private (
    correlationId: CorrelationId,
    pageTaskId: PageTaskId,
    paginationType: PaginationType,
    liabilitiesPaging: List[PaginationSource],
    nationalInsuranceNumber: Identifier,
    createdAt: Instant
) extends PageTask

object MaPageTask {

  implicit val maPageTaskformat: OFormat[MaPageTask] = Json.format[MaPageTask]

  def apply(
      correlationId: CorrelationId,
      pageTaskId: PageTaskId,
      liabilitiesPaging: List[PaginationSource],
      nationalInsuranceNumber: Identifier,
      createdAt: Instant
  ) =
    new MaPageTask(
      correlationId,
      pageTaskId,
      PaginationType.MaPagination,
      liabilitiesPaging,
      nationalInsuranceNumber,
      createdAt
    )

}

final case class BspPageTask private (
    correlationId: CorrelationId,
    pageTaskId: PageTaskId,
    paginationType: PaginationType,
    marriageDetailsPaging: Option[PaginationSource],
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
    nationalInsuranceNumber: Identifier,
    createdAt: Instant
) extends PageTask

object BspPageTask {

  implicit val bspPageTaskformat: OFormat[BspPageTask] = Json.format[BspPageTask]

  def apply(
      correlationId: CorrelationId,
      pageTaskId: PageTaskId,
      marriageDetailsPaging: Option[PaginationSource],
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
      nationalInsuranceNumber: Identifier,
      createdAt: Instant
  ) =
    new BspPageTask(
      correlationId,
      pageTaskId,
      PaginationType.BspPagination,
      marriageDetailsPaging,
      contributionAndCreditsPaging,
      nationalInsuranceNumber,
      createdAt
    )

}

final case class SearchLightPageTask private (
    system: CallSystem,
    correlationId: CorrelationId,
    pageTaskId: PageTaskId,
    paginationType: PaginationType,
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
    nationalInsuranceNumber: Identifier,
    createdAt: Instant
) extends PageTask

object SearchLightPageTask {
  implicit val searchLightPageTaskFormat: OFormat[SearchLightPageTask] = Json.format[SearchLightPageTask]

  def apply(
      correlationId: CorrelationId,
      pageTaskId: PageTaskId,
      paginationType: PaginationType,
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
      nationalInsuranceNumber: Identifier,
      createdAt: Instant
  ) =
    new SearchLightPageTask(
      CallSystem.SEARCHLIGHT,
      correlationId,
      pageTaskId,
      paginationType,
      contributionAndCreditsPaging,
      nationalInsuranceNumber,
      createdAt
    )

}

final case class GyspPageTask private (
    correlationId: CorrelationId,
    pageTaskId: PageTaskId,
    paginationType: PaginationType,
    benefitSchemeMembershipDetailsPaging: Option[PaginationSource],
    marriageDetailsPaging: Option[PaginationSource],
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
    nationalInsuranceNumber: Identifier,
    createdAt: Instant
) extends PageTask

object GyspPageTask {

  implicit val gyspPageTaskformat: OFormat[GyspPageTask] = Json.format[GyspPageTask]

  def apply(
      correlationId: CorrelationId,
      pageTaskId: PageTaskId,
      benefitSchemeMembershipDetailsPaging: Option[PaginationSource],
      marriageDetailsPaging: Option[PaginationSource],
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
      nationalInsuranceNumber: Identifier,
      createdAt: Instant
  ) =
    new GyspPageTask(
      correlationId,
      pageTaskId,
      PaginationType.GyspPagination,
      benefitSchemeMembershipDetailsPaging,
      marriageDetailsPaging,
      contributionAndCreditsPaging,
      nationalInsuranceNumber,
      createdAt
    )

}

object PageTask {

  private val pageTaskReads: Reads[PageTask] = Reads { json =>
    (json \ "system").toOption match {
      case Some(value) => SearchLightPageTask.searchLightPageTaskFormat.reads(json)
      case None =>
        (json \ "paginationType").validate[PaginationType].flatMap {
          case PaginationType.BspPagination  => BspPageTask.bspPageTaskformat.reads(json)
          case PaginationType.MaPagination   => MaPageTask.maPageTaskformat.reads(json)
          case PaginationType.GyspPagination => GyspPageTask.gyspPageTaskformat.reads(json)
          case BspSearchLightPagination      => SearchLightPageTask.searchLightPageTaskFormat.reads(json)
        }
    }

  }

  private val pageTaskWrites: OWrites[PageTask] = OWrites {
    case task: MaPageTask          => MaPageTask.maPageTaskformat.writes(task)
    case task: BspPageTask         => BspPageTask.bspPageTaskformat.writes(task)
    case task: GyspPageTask        => GyspPageTask.gyspPageTaskformat.writes(task)
    case task: SearchLightPageTask => SearchLightPageTask.searchLightPageTaskFormat.writes(task)
  }

  implicit val pageTaskFormat: OFormat[PageTask] = OFormat(pageTaskReads, pageTaskWrites)

  def createPaginatingTask(
      paginationResult: PaginationResult,
      currentTime: CurrentTimeSource
  ): Option[PageTask] =
    paginationResult.getPageTaskId.map { pageTaskId =>
      val now = currentTime.instantNow()
      (paginationResult.callSystem, paginationResult.paginationType) match {
        case (Some(callSystem), paginationType) =>
          SearchLightPageTask(
            correlationId = paginationResult.correlationId,
            pageTaskId = pageTaskId,
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            paginationType = paginationType,
            nationalInsuranceNumber = paginationResult.nationalInsuranceNumber,
            createdAt = now
          )

        case (_, PaginationType.MaPagination) =>
          MaPageTask(
            correlationId = paginationResult.correlationId,
            pageTaskId = pageTaskId,
            liabilitiesPaging = PaginationSource.fromLiabilities(paginationResult.liabilitiesResult),
            nationalInsuranceNumber = paginationResult.nationalInsuranceNumber,
            createdAt = now
          )
        case (_, PaginationType.GyspPagination) =>
          GyspPageTask(
            correlationId = paginationResult.correlationId,
            pageTaskId = pageTaskId,
            benefitSchemeMembershipDetailsPaging = PaginationSource.fromBenefitSchemeMembershipDetails(
              paginationResult.benefitSchemeMembershipDetailsData
            ),
            marriageDetailsPaging = PaginationSource.fromMarriageDetails(paginationResult.marriageDetailsResult),
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            paginationResult.nationalInsuranceNumber,
            now
          )
        case (_, PaginationType.BspPagination) =>
          BspPageTask(
            correlationId = paginationResult.correlationId,
            pageTaskId = pageTaskId,
            marriageDetailsPaging = PaginationSource.fromMarriageDetails(paginationResult.marriageDetailsResult),
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            paginationResult.nationalInsuranceNumber,
            now
          )
        case (None, BspSearchLightPagination) =>
          SearchLightPageTask(
            correlationId = paginationResult.correlationId,
            pageTaskId = pageTaskId,
            paginationType = paginationResult.paginationType,
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            nationalInsuranceNumber = paginationResult.nationalInsuranceNumber,
            createdAt = now
          )
      }
    }

}
