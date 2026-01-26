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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response

import play.api.libs.json.{Format, JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.enums.{
  ContributionCreditType,
  CreditSourceType
}

import scala.collection.immutable

sealed trait IndividualStatePensionInformationResponse extends NpsApiResponse

object IndividualStatePensionInformationError {

  // 400 start
  sealed trait IndividualStatePensionInformationErrorResponse400 extends IndividualStatePensionInformationResponse

  object IndividualStatePensionInformationErrorResponse400 {

    implicit val individualStatePensionInformationErrorResponse400Reads
        : Reads[IndividualStatePensionInformationErrorResponse400] =
      Reads[IndividualStatePensionInformationErrorResponse400] { resp =>
        IndividualStatePensionInformationHipFailureResponse400.hipFailureResponse400Reads.reads(resp) match {
          case JsSuccess(value, path) => JsSuccess(value, path)
          case JsError(errors) =>
            IndividualStatePensionInformationStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
              case JsSuccess(value, path) => JsSuccess(value, path)
              case JsError(errors)        => JsError(errors)
            }
        }
      }

  }

  case class ErrorResourceObj400(
      reason: Reason,
      code: NpsErrorCode400
  )

  object ErrorResourceObj400 {
    implicit val errorResourceObj400Reads: Reads[ErrorResourceObj400] = Json.reads[ErrorResourceObj400]
  }

  case class IndividualStatePensionInformationHipFailureResponse400(origin: HipOrigin, response: HipFailureResponse)
      extends IndividualStatePensionInformationErrorResponse400

  object IndividualStatePensionInformationHipFailureResponse400 {

    implicit val hipFailureResponse400Reads: Reads[IndividualStatePensionInformationHipFailureResponse400] =
      Json.reads[IndividualStatePensionInformationHipFailureResponse400]

  }

  case class ErrorResponse400(failures: List[ErrorResourceObj400])

  object ErrorResponse400 {
    implicit val errorResponse400Reads: Reads[ErrorResponse400] = Json.reads[ErrorResponse400]
  }

  case class IndividualStatePensionInformationStandardErrorResponse400(origin: HipOrigin, response: ErrorResponse400)
      extends IndividualStatePensionInformationErrorResponse400

  object IndividualStatePensionInformationStandardErrorResponse400 {

    implicit val standardErrorResponse400Reads: Reads[IndividualStatePensionInformationStandardErrorResponse400] =
      Json.reads[IndividualStatePensionInformationStandardErrorResponse400]

  }
  // 400 end

  // 403 start
  case class IndividualStatePensionInformationErrorResponse403(
      reason: NpsErrorReason403,
      code: NpsErrorCode403
  ) extends IndividualStatePensionInformationResponse

  object IndividualStatePensionInformationErrorResponse403 {

    implicit val individualStatePensionInformationErrorResponse403Reads
        : Format[IndividualStatePensionInformationErrorResponse403] =
      Json.format[IndividualStatePensionInformationErrorResponse403]

  }

  // 503 start
  case class IndividualStatePensionInformationErrorResponse503(
      origin: HipOrigin,
      response: HipFailureResponse
  ) extends IndividualStatePensionInformationResponse

  object IndividualStatePensionInformationErrorResponse503 {

    implicit val individualStatePensionInformationErrorResponse503Reads
        : Reads[IndividualStatePensionInformationErrorResponse503] =
      Json.reads[IndividualStatePensionInformationErrorResponse503]

  }
  // 503 end

}

object IndividualStatePensionInformationSuccess {

  case class NumberOfQualifyingYears(value: Int) extends AnyVal

  object NumberOfQualifyingYears {
    implicit val reads: Format[NumberOfQualifyingYears] = Json.valueFormat[NumberOfQualifyingYears]
  }

  case class NonQualifyingYears(value: Int) extends AnyVal

  object NonQualifyingYears {
    implicit val reads: Format[NonQualifyingYears] = Json.valueFormat[NonQualifyingYears]
  }

  case class YearsToFinalRelevantYear(value: Int) extends AnyVal

  object YearsToFinalRelevantYear {
    implicit val reads: Format[YearsToFinalRelevantYear] = Json.valueFormat[YearsToFinalRelevantYear]
  }

  case class NonQualifyingYearsPayable(value: Int) extends AnyVal

  object NonQualifyingYearsPayable {
    implicit val reads: Format[NonQualifyingYearsPayable] = Json.valueFormat[NonQualifyingYearsPayable]
  }

  case class Pre1975CCCount(value: Int) extends AnyVal

  object Pre1975CCCount {
    implicit val reads: Format[Pre1975CCCount] = Json.valueFormat[Pre1975CCCount]
  }

  case class DateOfEntry(value: String) extends AnyVal

  object DateOfEntry {
    implicit val reads: Format[DateOfEntry] = Json.valueFormat[DateOfEntry]
  }

  case class QualifyingTaxYear(value: Boolean) extends AnyVal

  object QualifyingTaxYear {
    implicit val reads: Format[QualifyingTaxYear] = Json.valueFormat[QualifyingTaxYear]
  }

  case class PayableAccepted(value: Boolean) extends AnyVal

  object PayableAccepted {
    implicit val reads: Format[PayableAccepted] = Json.valueFormat[PayableAccepted]
  }

  case class AmountNeeded(value: BigDecimal) extends AnyVal

  object AmountNeeded {
    implicit val reads: Format[AmountNeeded] = Json.valueFormat[AmountNeeded]
  }

  case class ClassThreePayable(value: BigDecimal) extends AnyVal

  object ClassThreePayable {
    implicit val reads: Format[ClassThreePayable] = Json.valueFormat[ClassThreePayable]
  }

  case class ClassThreePayableBy(value: String) extends AnyVal

  object ClassThreePayableBy {
    implicit val reads: Format[ClassThreePayableBy] = Json.valueFormat[ClassThreePayableBy]
  }

  case class ClassThreePayableByPenalty(value: String) extends AnyVal

  object ClassThreePayableByPenalty {
    implicit val reads: Format[ClassThreePayableByPenalty] = Json.valueFormat[ClassThreePayableByPenalty]
  }

  case class ClassTwoPayable(value: BigDecimal) extends AnyVal

  object ClassTwoPayable {
    implicit val reads: Format[ClassTwoPayable] = Json.valueFormat[ClassTwoPayable]
  }

  case class ClassTwoPayableBy(value: String) extends AnyVal

  object ClassTwoPayableBy {
    implicit val reads: Format[ClassTwoPayableBy] = Json.valueFormat[ClassTwoPayableBy]
  }

  case class ClassTwoPayableByPenalty(value: String) extends AnyVal

  object ClassTwoPayableByPenalty {
    implicit val reads: Format[ClassTwoPayableByPenalty] = Json.valueFormat[ClassTwoPayableByPenalty]
  }

  case class ClassTwoOutstandingWeeks(value: Int) extends AnyVal

  object ClassTwoOutstandingWeeks {
    implicit val reads: Format[ClassTwoOutstandingWeeks] = Json.valueFormat[ClassTwoOutstandingWeeks]
  }

  case class TotalPrimaryContributions(value: BigDecimal) extends AnyVal

  object TotalPrimaryContributions {
    implicit val reads: Format[TotalPrimaryContributions] = Json.valueFormat[TotalPrimaryContributions]
  }

  case class NiEarnings(value: BigDecimal) extends AnyVal

  object NiEarnings {
    implicit val reads: Format[NiEarnings] = Json.valueFormat[NiEarnings]
  }

  case class CoClassOnePaid(value: BigDecimal) extends AnyVal

  object CoClassOnePaid {
    implicit val reads: Format[CoClassOnePaid] = Json.valueFormat[CoClassOnePaid]
  }

  case class TotalPrimaryEarnings(value: BigDecimal) extends AnyVal

  object TotalPrimaryEarnings {
    implicit val reads: Format[TotalPrimaryEarnings] = Json.valueFormat[TotalPrimaryEarnings]
  }

  case class NiEarningsSelfEmployed(value: Int) extends AnyVal

  object NiEarningsSelfEmployed {
    implicit val reads: Format[NiEarningsSelfEmployed] = Json.valueFormat[NiEarningsSelfEmployed]
  }

  case class NiEarningsVoluntary(value: Int) extends AnyVal

  object NiEarningsVoluntary {
    implicit val reads: Format[NiEarningsVoluntary] = Json.valueFormat[NiEarningsVoluntary]
  }

  case class UnderInvestigationFlag(value: Boolean) extends AnyVal

  object UnderInvestigationFlag {
    implicit val reads: Format[UnderInvestigationFlag] = Json.valueFormat[UnderInvestigationFlag]
  }

  case class PrimaryPaidEarnings(value: BigDecimal) extends AnyVal

  object PrimaryPaidEarnings {
    implicit val reads: Format[PrimaryPaidEarnings] = Json.valueFormat[PrimaryPaidEarnings]
  }

  case class ContributionCreditCount(value: Int) extends AnyVal

  object ContributionCreditCount {
    implicit val reads: Format[ContributionCreditCount] = Json.valueFormat[ContributionCreditCount]
  }

  case class OtherCredits(
      contributionCreditType: Option[ContributionCreditType],
      creditSourceType: Option[CreditSourceType],
      contributionCreditCount: Option[ContributionCreditCount]
  )

  object OtherCredits {
    implicit val reads: Format[OtherCredits] = Json.format[OtherCredits]
  }

  case class ContributionsByTaxYear(
      taxYear: Option[TaxYear],
      qualifyingTaxYear: Option[QualifyingTaxYear],
      payableAccepted: Option[PayableAccepted],
      amountNeeded: Option[AmountNeeded],
      classThreePayable: Option[ClassThreePayable],
      classThreePayableBy: Option[ClassThreePayableBy],
      classThreePayableByPenalty: Option[ClassThreePayableByPenalty],
      classTwoPayable: Option[ClassTwoPayable],
      classTwoPayableBy: Option[ClassTwoPayableBy],
      classTwoPayableByPenalty: Option[ClassTwoPayableByPenalty],
      classTwoOutstandingWeeks: Option[ClassTwoOutstandingWeeks],
      totalPrimaryContributions: Option[TotalPrimaryContributions],
      niEarnings: Option[NiEarnings],
      coClassOnePaid: Option[CoClassOnePaid],
      totalPrimaryEarnings: Option[TotalPrimaryEarnings],
      niEarningsSelfEmployed: Option[NiEarningsSelfEmployed],
      niEarningsVoluntary: Option[NiEarningsVoluntary],
      underInvestigationFlag: Option[UnderInvestigationFlag],
      primaryPaidEarnings: Option[PrimaryPaidEarnings],
      otherCredits: Option[List[OtherCredits]]
  )

  object ContributionsByTaxYear {
    implicit val reads: Format[ContributionsByTaxYear] = Json.format[ContributionsByTaxYear]
  }

  case class IndividualStatePensionInformationSuccessResponse(
      identifier: Identifier,
      numberOfQualifyingYears: Option[NumberOfQualifyingYears],
      nonQualifyingYears: Option[NonQualifyingYears],
      yearsToFinalRelevantYear: Option[YearsToFinalRelevantYear],
      nonQualifyingYearsPayable: Option[NonQualifyingYearsPayable],
      pre1975CCCount: Option[Pre1975CCCount],
      dateOfEntry: Option[DateOfEntry],
      contributionsByTaxYear: Option[List[ContributionsByTaxYear]]
  ) extends IndividualStatePensionInformationResponse
      with NpsSuccessfulApiResponse

  object IndividualStatePensionInformationSuccessResponse {

    implicit val reads: Format[IndividualStatePensionInformationSuccessResponse] =
      Json.format[IndividualStatePensionInformationSuccessResponse]

  }

}
