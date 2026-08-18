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
import uk.gov.hmrc.app.benefitEligibility.model.common.BatchType.BspSearchLightBatch
import uk.gov.hmrc.app.benefitEligibility.model.nps.{Class2MaReceiptsResult, LiabilityResult, MarriageDetailsResult}
import uk.gov.hmrc.app.benefitEligibility.service.{BenefitSchemeMembershipDetailsData, BatchResult}
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.app.benefitEligibility.util.{CurrentTimeSource, NonEmptyListFormat}

import java.util.UUID

final case class BatchSource(
    apiName: ApiName,
    callBackURL: String
)

object BatchSource {
  implicit val format: OFormat[BatchSource] = Json.format[BatchSource]

  def fromBenefitSchemeMembershipDetails(
      benefitSchemeMembershipDetailsData: Option[BenefitSchemeMembershipDetailsData]
  ): Option[BatchSource] =
    benefitSchemeMembershipDetailsData.flatMap {
      _.schemeMembershipDetailsResult.getSuccess
        .flatMap(_.callback)
        .flatMap {
          _.callbackURL.map(url => BatchSource(BenefitSchemeDetails, url.value))
        }
    }

  def fromMarriageDetails(marriageDetailsResult: Option[MarriageDetailsResult]): Option[BatchSource] =
    marriageDetailsResult.flatMap {
      _.getSuccess
        .map(_.marriageDetails)
        .flatMap(m => m._links.flatMap(_.self.href.map(_.value)))
        .map(url => BatchSource(MarriageDetails, url))
    }

  def fromLiabilities(liabilitiesResult: List[LiabilityResult]): List[BatchSource] = liabilitiesResult.flatMap {
    _.getSuccess
      .flatMap(_.callback)
      .flatMap {
        _.callbackURL.map(url => BatchSource(Liabilities, url.value))
      }
  }

  def fromClass2MAReceipts(class2MaReceiptsResult: Option[Class2MaReceiptsResult]): Option[BatchSource] =
    class2MaReceiptsResult.flatMap(
      _.getSuccess
        .flatMap(_.callBack)
        .flatMap {
          _.callbackURL.map(url => BatchSource(Liabilities, url.value))
        }
    )

}

final case class ContributionAndCreditsBatching private (
    apiName: ApiName,
    niContributionAndCreditsTaxWindows: NonEmptyList[TaxWindow],
    dateOfBirth: DateOfBirth
) {

  def tail: Option[ContributionAndCreditsBatching] = niContributionAndCreditsTaxWindows.toList.safeTailNel.map(
    remaining => this.copy(niContributionAndCreditsTaxWindows = remaining)
  )

}

object ContributionAndCreditsBatching {

  implicit val nonEmptyListTawWindowFormat: Format[NonEmptyList[TaxWindow]] =
    NonEmptyListFormat.nonEmptyListFormat[TaxWindow]

  implicit val format: OFormat[ContributionAndCreditsBatching] = Json.format[ContributionAndCreditsBatching]

  def apply(niContributionAndCreditsTaxWindows: NonEmptyList[TaxWindow], dateOfBirth: DateOfBirth) =
    new ContributionAndCreditsBatching(ApiName.NiContributionAndCredits, niContributionAndCreditsTaxWindows, dateOfBirth)

}

case class BatchId(value: UUID) extends AnyVal

object BatchId {
  implicit val batchIdFormat: Format[BatchId] = Json.valueFormat[BatchId]

  def from(cursorId: CursorId): Option[BatchId] = {
    val uuidRegex = """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".r
    val id        = cursorId.value
    if (uuidRegex.matches(id)) Some(BatchId(UUID.fromString(id))) else None
  }

}

sealed trait Batch {
  def batchType: BatchType
  def nationalInsuranceNumber: Identifier
}

final case class MaBatch private (
    batchType: BatchType,
    liabilitiesBatching: List[BatchSource],
    nationalInsuranceNumber: Identifier
) extends Batch

object MaBatch {

  implicit val maBatchformat: OFormat[MaBatch] = Json.format[MaBatch]

  def apply(
      liabilitiesBatching: List[BatchSource],
      nationalInsuranceNumber: Identifier
  ) =
    new MaBatch(
      BatchType.MaBatch,
      liabilitiesBatching,
      nationalInsuranceNumber
    )

}

final case class BspBatch private (
    batchType: BatchType,
    marriageDetailsBatching: Option[BatchSource],
    contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
    nationalInsuranceNumber: Identifier
) extends Batch

object BspBatch {

  implicit val bspBatchformat: OFormat[BspBatch] = Json.format[BspBatch]

  def apply(
      marriageDetailsBatching: Option[BatchSource],
      contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
      nationalInsuranceNumber: Identifier
  ) =
    new BspBatch(
      BatchType.BspBatch,
      marriageDetailsBatching,
      contributionAndCreditsBatching,
      nationalInsuranceNumber
    )

}

final case class SearchLightBatch private (
    system: CallSystem,
    batchType: BatchType,
    contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
    nationalInsuranceNumber: Identifier
) extends Batch

object SearchLightBatch {
  implicit val searchLightBatchFormat: OFormat[SearchLightBatch] = Json.format[SearchLightBatch]

  def apply(
      batchType: BatchType,
      contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
      nationalInsuranceNumber: Identifier
  ) =
    new SearchLightBatch(
      CallSystem.SEARCHLIGHT,
      batchType,
      contributionAndCreditsBatching,
      nationalInsuranceNumber
    )

}

final case class GyspBatch private (
    batchType: BatchType,
    benefitSchemeMembershipDetailsBatching: Option[BatchSource],
    marriageDetailsBatching: Option[BatchSource],
    contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
    nationalInsuranceNumber: Identifier
) extends Batch

object GyspBatch {

  implicit val gyspBatchformat: OFormat[GyspBatch] = Json.format[GyspBatch]

  def apply(
      benefitSchemeMembershipDetailsBatching: Option[BatchSource],
      marriageDetailsBatching: Option[BatchSource],
      contributionAndCreditsBatching: Option[ContributionAndCreditsBatching],
      nationalInsuranceNumber: Identifier
  ) =
    new GyspBatch(
      BatchType.GyspBatch,
      benefitSchemeMembershipDetailsBatching,
      marriageDetailsBatching,
      contributionAndCreditsBatching,
      nationalInsuranceNumber
    )

}

object Batch {

  private val batchReads: Reads[Batch] = Reads { json =>
    (json \ "system").toOption match {
      case Some(value) => SearchLightBatch.searchLightBatchFormat.reads(json)
      case None =>
        (json \ "batchType").validate[BatchType].flatMap {
          case BatchType.BspBatch  => BspBatch.bspBatchformat.reads(json)
          case BatchType.MaBatch   => MaBatch.maBatchformat.reads(json)
          case BatchType.GyspBatch => GyspBatch.gyspBatchformat.reads(json)
          case BspSearchLightBatch      => SearchLightBatch.searchLightBatchFormat.reads(json)
        }
    }

  }

  private val batchWrites: OWrites[Batch] = OWrites {
    case task: MaBatch          => MaBatch.maBatchformat.writes(task)
    case task: BspBatch         => BspBatch.bspBatchformat.writes(task)
    case task: GyspBatch        => GyspBatch.gyspBatchformat.writes(task)
    case task: SearchLightBatch => SearchLightBatch.searchLightBatchFormat.writes(task)
  }

  implicit val batchFormat: OFormat[Batch] = OFormat(batchReads, batchWrites)

  def createBatchDocument(
      batchResult: BatchResult,
      currentTime: CurrentTimeSource
  ): Option[BatchDocument] =
    batchResult.getBatchId.map { batchId =>
      val now = currentTime.instantNow()

      val batch = (batchResult.callSystem, batchResult.batchType) match {
        case (Some(callSystem), batchType) =>
          SearchLightBatch(
            contributionAndCreditsBatching = batchResult.contributionCreditResult.contributionAndCreditsBatching,
            batchType = batchType,
            nationalInsuranceNumber = batchResult.nationalInsuranceNumber
          )

        case (_, BatchType.MaBatch) =>
          MaBatch(
            liabilitiesBatching = BatchSource.fromLiabilities(batchResult.liabilitiesResult),
            nationalInsuranceNumber = batchResult.nationalInsuranceNumber
          )
        case (_, BatchType.GyspBatch) =>
          GyspBatch(
            benefitSchemeMembershipDetailsBatching = BatchSource.fromBenefitSchemeMembershipDetails(
              batchResult.benefitSchemeMembershipDetailsData
            ),
            marriageDetailsBatching = BatchSource.fromMarriageDetails(batchResult.marriageDetailsResult),
            contributionAndCreditsBatching = batchResult.contributionCreditResult.contributionAndCreditsBatching,
            batchResult.nationalInsuranceNumber
          )
        case (_, BatchType.BspBatch) =>
          BspBatch(
            marriageDetailsBatching = BatchSource.fromMarriageDetails(batchResult.marriageDetailsResult),
            contributionAndCreditsBatching = batchResult.contributionCreditResult.contributionAndCreditsBatching,
            batchResult.nationalInsuranceNumber
          )
        case (None, BspSearchLightBatch) =>
          SearchLightBatch(
            batchType = batchResult.batchType,
            contributionAndCreditsBatching = batchResult.contributionCreditResult.contributionAndCreditsBatching,
            nationalInsuranceNumber = batchResult.nationalInsuranceNumber
          )
      }
      BatchDocument(
        correlationId = batchResult.correlationId,
        batchId = batchId,
        data = Json.toJson(batch).as[JsObject],
        createdAt = now
      )
    }

}
