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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{ErrorCode422, NpsErrorCode400}
import uk.gov.hmrc.app.benefitEligibility.common.{Identifier, NpsErrorReason}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.{
  ErrorCode403,
  ErrorReason403,
  MarriageEndDateStatus,
  MarriageStartDateStatus,
  MarriageStatus
}
import MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait MarriageDetailsResponse extends NpsApiResponse

object MarriageDetailsError {

  // region Error400

  case class MarriageDetailsError400(reason: NpsErrorReason, code: NpsErrorCode400) extends NpsApiResponse

  object MarriageDetailsError400 {
    implicit val npsErrorResponse400Reads: Reads[MarriageDetailsError400] = Json.reads[MarriageDetailsError400]
  }

  case class MarriageDetailsErrorResponse400(failures: List[MarriageDetailsError400]) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse400 {

    implicit val npsFailureResponse400Reads: Reads[MarriageDetailsErrorResponse400] =
      Json.reads[MarriageDetailsErrorResponse400]

  }
  // endregion Error400

  // region Error403

  case class MarriageDetailsErrorResponse403(reason: ErrorReason403, code: ErrorCode403) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse403 {

    implicit val npsFailureResponse403Reads: Reads[MarriageDetailsErrorResponse403] =
      Json.reads[MarriageDetailsErrorResponse403]

  }
  // endregion Error403

  // region Error422

  case class MarriageDetailsError422(reason: NpsErrorReason, code: ErrorCode422)

  object MarriageDetailsError422 {
    implicit val NpsErrorResponse422Reads: Reads[MarriageDetailsError422] = Json.reads[MarriageDetailsError422]
  }

  case class MarriageDetailsErrorResponse422(failures: List[MarriageDetailsError422]) extends MarriageDetailsResponse

  object MarriageDetailsErrorResponse422 {

    implicit val npsFailureResponse422Reads: Reads[MarriageDetailsErrorResponse422] =
      Json.reads[MarriageDetailsErrorResponse422]

  }
  // endregion Error422

}

object MarriageDetailsSuccess {

  // region Active Marriage

  case class ActiveMarriage(value: Boolean) extends AnyVal

  object ActiveMarriage {
    implicit val activeMarriageReads: Format[ActiveMarriage] = Json.valueFormat[ActiveMarriage]
  }

  // endregion Active Marriage

  // region Marriage Details List

  final case class SequenceNumber private (value: Int) extends AnyVal

  object SequenceNumber {
    implicit val sequenceNumberFormat: Format[SequenceNumber] = Json.valueFormat[SequenceNumber]
  }

  case class MarriageStartDate(value: LocalDate) extends AnyVal

  object MarriageStartDate {
    implicit val startDateReads: Format[MarriageStartDate] = Json.valueFormat[MarriageStartDate]
  }

  case class MarriageEndDate(value: LocalDate) extends AnyVal

  object MarriageEndDate {
    implicit val endDateReads: Format[MarriageEndDate] = Json.valueFormat[MarriageEndDate]
  }

  final case class SpouseForename private (value: String) extends AnyVal

  object SpouseForename {
    implicit val spouseForenameFormat: Format[SpouseForename] = Json.valueFormat[SpouseForename]
  }

  final case class SpouseSurname private (value: String) extends AnyVal

  object SpouseSurname {
    implicit val spouseSurnameFormat: Format[SpouseSurname] = Json.valueFormat[SpouseSurname]
  }

  case class SeparationDate(value: LocalDate) extends AnyVal

  object SeparationDate {
    implicit val separationDateReads: Format[SeparationDate] = Json.valueFormat[SeparationDate]
  }

  case class ReconciliationDate(value: LocalDate) extends AnyVal

  object ReconciliationDate {
    implicit val reconciliationDateReads: Format[ReconciliationDate] = Json.valueFormat[ReconciliationDate]
  }

  case class MarriageDetailsList(
      sequenceNumber: SequenceNumber, // TODO make required filed
      status: MarriageStatus,         // TODO make required filed
      startDate: Option[MarriageStartDate],
      startDateStatus: Option[MarriageStartDateStatus],
      endDate: Option[MarriageEndDate],
      endDateStatus: Option[MarriageEndDateStatus],
      spouseIdentifier: Option[Identifier],
      spouseForename: Option[SpouseForename],
      spouseSurname: Option[SpouseSurname],
      separationDate: Option[SeparationDate],
      reconciliationDate: Option[ReconciliationDate]
  )

  object MarriageDetailsList {
    implicit val marriageDetailsListFormats: Format[MarriageDetailsList] = Json.format[MarriageDetailsList]
  }

  // endregion MarriageDetailsList

  // region Links

  case class Href(value: String) extends AnyVal

  object Href {
    implicit val hrefReads: Format[Href] = Json.valueFormat[Href]
  }

  sealed abstract class Methods(override val entryName: String) extends EnumEntry

  object Methods extends Enum[Methods] with PlayJsonEnum[Methods] {
    val values: immutable.IndexedSeq[Methods] = findValues

    case object get extends Methods("get")
  }

  case class SelfLink(
      href: Option[Href],
      methods: Option[Methods]
  )

  object SelfLink {
    implicit val selfReads: Reads[SelfLink]   = Json.reads[SelfLink]
    implicit val selfWrites: Writes[SelfLink] = Json.writes[SelfLink]
  }

  case class Links(self: SelfLink)

  object Links {
    implicit val linksReads: Reads[Links]   = Json.reads[Links]
    implicit val linksWrites: Writes[Links] = Json.writes[Links]
  }

  // endregion Links

  // region Marriage Details Success Response

  case class MarriageDetailsDetails(
      activeMarriage: ActiveMarriage,
      marriageDetailsList: Option[MarriageDetailsList],
      _links: Option[Links]
  )

  object MarriageDetailsDetails {

    implicit val marriageDetailsFormat: Format[MarriageDetailsDetails] =
      Json.format[MarriageDetailsDetails]

  }

  case class MarriageDetailsSuccessResponse(
      marriageDetails: MarriageDetailsDetails
  ) extends MarriageDetailsResponse
      with NpsSuccessfulApiResponse

  object MarriageDetailsSuccessResponse {

    implicit val marriageDetailsSuccessResponseFormat: Format[MarriageDetailsSuccessResponse] =
      Json.format[MarriageDetailsSuccessResponse]

  }

  // endregion Marriage Details Success Response

}
