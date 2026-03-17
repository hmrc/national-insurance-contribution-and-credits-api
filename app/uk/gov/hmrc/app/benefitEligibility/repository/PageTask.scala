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
import io.scalaland.chimney.dsl.into
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{BenefitSchemeDetails, Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.model.common.{ApiName, DateOfBirth, PaginationType, TaxWindow}
import uk.gov.hmrc.app.benefitEligibility.model.nps.{LiabilityResult, MarriageDetailsResult}
import uk.gov.hmrc.app.benefitEligibility.service.{
  BenefitSchemeMembershipDetailsData,
  ContributionCreditPagingResult,
  PaginationResult,
  UuidGenerator
}
import uk.gov.hmrc.app.benefitEligibility.util.NonEmptyListFormat
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource

import java.time.{Instant, LocalDateTime}
import java.util.UUID

case class PaginationCursor(value: UUID) extends AnyVal

object PaginationCursor {
  implicit val format: Format[PaginationCursor] = Json.valueFormat[PaginationCursor]
}

final case class PaginationSource(
    apiName: ApiName,
    callBackURL: Option[String]
)

object PaginationSource {
  implicit val format: OFormat[PaginationSource] = Json.format[PaginationSource]

  def fromBenefitSchemeMembershipDetails(
      benefitSchemeMembershipDetailsData: Option[BenefitSchemeMembershipDetailsData]
  ): Option[PaginationSource] =
    benefitSchemeMembershipDetailsData.flatMap {
      _.schemeMembershipDetailsResult.getSuccess
        .flatMap(_.callback)
        .map(c => PaginationSource(BenefitSchemeDetails, c.callbackURL.map(_.value)))
    }

  def fromMarriageDetails(marriageDetailsResult: Option[MarriageDetailsResult]): Option[PaginationSource] =
    marriageDetailsResult.flatMap {
      _.getSuccess
        .map(_.marriageDetails)
        .flatMap(m => m._links.flatMap(_.self.href.map(_.value)))
        .map(url => PaginationSource(MarriageDetails, Some(url)))
    }

  def fromLiabilities(liabilitiesResult: List[LiabilityResult]): List[PaginationSource] = liabilitiesResult.flatMap {
    _.getSuccess.flatMap(_.callback).map(c => PaginationSource(Liabilities, c.callbackURL.map(_.value)))
  }

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

sealed trait PageTask {
  def id: UUID
  def paginationType: PaginationType
  def createdAt: Instant
}

final case class MaPageTask private (
    id: UUID,
    paginationType: PaginationType,
    liabilitiesPaging: List[PaginationSource],
    createdAt: Instant
) extends PageTask

object MaPageTask {

  implicit val maPageTaskformat: OFormat[MaPageTask] = Json.format[MaPageTask]

  def apply(
      paginationCursor: PaginationCursor,
      liabilitiesPaging: List[PaginationSource],
      createdAt: Instant
  ) =
    new MaPageTask(
      paginationCursor.value,
      PaginationType.MA,
      liabilitiesPaging,
      createdAt
    )

}

final case class BspPageTask private (
    id: UUID,
    paginationType: PaginationType,
    marriageDetailsPaging: Option[PaginationSource],
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
    createdAt: Instant
) extends PageTask

object BspPageTask {

  implicit val bspPageTaskformat: OFormat[BspPageTask] = Json.format[BspPageTask]

  def apply(
      paginationCursor: PaginationCursor,
      marriageDetailsPaging: Option[PaginationSource],
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
      createdAt: Instant
  ) =
    new BspPageTask(
      paginationCursor.value,
      PaginationType.BSP,
      marriageDetailsPaging,
      contributionAndCreditsPaging,
      createdAt
    )

}

final case class GyspPageTask private (
    id: UUID,
    paginationType: PaginationType,
    benefitSchemeMembershipDetailsPaging: Option[PaginationSource],
    marriageDetailsPaging: Option[PaginationSource],
    contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
    createdAt: Instant
) extends PageTask

object GyspPageTask {

  implicit val gyspPageTaskformat: OFormat[GyspPageTask] = Json.format[GyspPageTask]

  def apply(
      paginationCursor: PaginationCursor,
      benefitSchemeMembershipDetailsPaging: Option[PaginationSource],
      marriageDetailsPaging: Option[PaginationSource],
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging],
      createdAt: Instant
  ) =
    new GyspPageTask(
      paginationCursor.value,
      PaginationType.GYSP,
      benefitSchemeMembershipDetailsPaging,
      marriageDetailsPaging,
      contributionAndCreditsPaging,
      createdAt
    )

}

object PageTask {

  private val pageTaskReads: Reads[PageTask] = Reads { json =>
    (json \ "paginationType").validate[PaginationType].flatMap {
      case PaginationType.BSP  => BspPageTask.bspPageTaskformat.reads(json)
      case PaginationType.MA   => MaPageTask.maPageTaskformat.reads(json)
      case PaginationType.GYSP => GyspPageTask.gyspPageTaskformat.reads(json)
    }
  }

  private val pageTaskWrites: OWrites[PageTask] = OWrites {
    case task: MaPageTask   => MaPageTask.maPageTaskformat.writes(task)
    case task: BspPageTask  => BspPageTask.bspPageTaskformat.writes(task)
    case task: GyspPageTask => GyspPageTask.gyspPageTaskformat.writes(task)
  }

  implicit val pageTaskFormat: OFormat[PageTask] = OFormat(pageTaskReads, pageTaskWrites)

  def createPaginatingTask(
      paginationResult: PaginationResult,
      currentTime: CurrentTimeSource
  ): Option[PageTask] =
    paginationResult.getNextCursor.map { cursor =>
      val now = currentTime.instantNow()
      paginationResult.paginationType match {
        case PaginationType.MA =>
          MaPageTask(
            paginationCursor = cursor,
            liabilitiesPaging = PaginationSource.fromLiabilities(paginationResult.liabilitiesResult),
            now
          )
        case PaginationType.GYSP =>
          GyspPageTask(
            paginationCursor = cursor,
            benefitSchemeMembershipDetailsPaging = PaginationSource.fromBenefitSchemeMembershipDetails(
              paginationResult.benefitSchemeMembershipDetailsData
            ),
            marriageDetailsPaging = PaginationSource.fromMarriageDetails(paginationResult.marriageDetailsResult),
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            now
          )
        case PaginationType.BSP =>
          BspPageTask(
            paginationCursor = cursor,
            marriageDetailsPaging = PaginationSource.fromMarriageDetails(paginationResult.marriageDetailsResult),
            contributionAndCreditsPaging = paginationResult.contributionCreditResult.contributionAndCreditsPaging,
            now
          )
      }
    }

}
