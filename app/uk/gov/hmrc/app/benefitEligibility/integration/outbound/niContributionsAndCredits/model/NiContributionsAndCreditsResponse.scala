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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  ErrorCode422,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.*
import NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.enums.{
  Class1ContributionStatus,
  Class2Or3CreditStatus,
  ContributionCategory,
  ContributionCreditType,
  CreditSource,
  LatePaymentPeriod
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait NiContributionsAndCreditsResponse extends NpsApiResponse

object NiContributionsAndCreditsError {

  case class NiContributionsAndCredits400(reason: NpsErrorReason, code: NpsErrorCode400)

  object NiContributionsAndCredits400 {

    implicit val npsErrorResponse400Format: Format[NiContributionsAndCredits400] =
      Json.format[NiContributionsAndCredits400]

  }

  case class NiContributionsAndCreditsResponse400(failures: List[NiContributionsAndCredits400])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse400 {

    implicit val npsFailureResponse400Format: Format[NiContributionsAndCreditsResponse400] =
      Json.format[NiContributionsAndCreditsResponse400]

  }

  case class NiContributionsAndCreditsResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends NiContributionsAndCreditsResponse
      with NpsApiResponse

  object NiContributionsAndCreditsResponse403 {

    implicit val npsFailureResponse403Reads: Format[NiContributionsAndCreditsResponse403] =
      Json.format[NiContributionsAndCreditsResponse403]

  }

  case class NiContributionsAndCredits422(reason: NpsErrorReason, code: ErrorCode422)

  object NiContributionsAndCredits422 {

    implicit val NpsErrorResponse422Reads: Format[NiContributionsAndCredits422] =
      Json.format[NiContributionsAndCredits422]

  }

  case class NiContributionsAndCreditsResponse422(failures: List[NiContributionsAndCredits422])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse422 {

    implicit val npsFailureResponse422Format: Format[NiContributionsAndCreditsResponse422] =
      Json.format[NiContributionsAndCreditsResponse422]

  }

}

object NiContributionsAndCreditsSuccess {

  case class PrimaryContribution(value: BigDecimal) extends AnyVal

  object PrimaryContribution {
    implicit val primaryContributionFormat: Format[PrimaryContribution] = Json.valueFormat[PrimaryContribution]
  }

  case class PrimaryPaidEarnings(value: BigDecimal) extends AnyVal

  object PrimaryPaidEarnings {

    implicit val primaryPaidEarningsFormat: Format[PrimaryPaidEarnings] =
      Json.valueFormat[PrimaryPaidEarnings]

  }

  case class ReceivablePeriodStartDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodStartDate {

    implicit val receivablePeriodStartDateFormat: Format[ReceivablePeriodStartDate] =
      Json.valueFormat[ReceivablePeriodStartDate]

  }

  case class ReceivablePeriodEndDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodEndDate {

    implicit val receivablePeriodEndDateFormat: Format[ReceivablePeriodEndDate] =
      Json.valueFormat[ReceivablePeriodEndDate]

  }

  case class LiabilityStartDate(value: LocalDate) extends AnyVal

  object LiabilityStartDate {
    implicit val liabilityStartDateFormat: Format[LiabilityStartDate] = Json.valueFormat[LiabilityStartDate]
  }

  case class LiabilityEndDate(value: LocalDate) extends AnyVal

  object LiabilityEndDate {
    implicit val liabilityEndDateFormat: Format[LiabilityEndDate] = Json.valueFormat[LiabilityEndDate]
  }

  case class BillScheduleNumber(value: Int) extends AnyVal

  object BillScheduleNumber {
    implicit val billScheduleNumberFormat: Format[BillScheduleNumber] = Json.valueFormat[BillScheduleNumber]
  }

  final case class ContributionCategoryLetter(value: String) extends AnyVal

  object ContributionCategoryLetter {

    implicit val contributionCategoryLetterWrites: Format[ContributionCategoryLetter] =
      Json.valueFormat[ContributionCategoryLetter]

  }

  final case class EmployerName(value: String) extends AnyVal

  object EmployerName {

    implicit val employerNameFormat: Format[EmployerName] =
      Json.valueFormat[EmployerName]

  }

  case class NumberOfCreditsAndContributions(value: Int) extends AnyVal

  object NumberOfCreditsAndContributions {

    implicit val numberOfCreditsAndContsFormat: Format[NumberOfCreditsAndContributions] =
      Json.valueFormat[NumberOfCreditsAndContributions]

  }

  case class Class2NIContributionAmount(value: BigDecimal) extends AnyVal

  object Class2NIContributionAmount {

    implicit val class2NIContributionAmountFormat: Format[Class2NIContributionAmount] =
      Json.valueFormat[Class2NIContributionAmount]

  }

  case class TotalGraduatedPensionUnits(value: BigDecimal) extends AnyVal

  object TotalGraduatedPensionUnits {

    implicit val totalGraduatedPensionUnitsFormat: Format[TotalGraduatedPensionUnits] =
      Json.valueFormat[TotalGraduatedPensionUnits]

  }

  case class Class2Or3EarningsFactor(value: BigDecimal) extends AnyVal

  object Class2Or3EarningsFactor {

    implicit val class2Or3EarningsFactorFormat: Format[Class2Or3EarningsFactor] =
      Json.valueFormat[Class2Or3EarningsFactor]

  }

  case class Class1ContributionAndCredits(
      taxYear: Option[TaxYear],
      numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions],
      contributionCategoryLetter: Option[ContributionCategoryLetter],
      contributionCategory: Option[ContributionCategory],
      contributionCreditType: Option[ContributionCreditType],
      primaryContribution: Option[PrimaryContribution],
      class1ContributionStatus: Option[Class1ContributionStatus],
      primaryPaidEarnings: Option[PrimaryPaidEarnings],
      creditSource: Option[CreditSource],
      employerName: Option[EmployerName],
      latePaymentPeriod: Option[LatePaymentPeriod]
  )

  object Class1ContributionAndCredits {
    implicit val nicClass1Format: Format[Class1ContributionAndCredits] = Json.format[Class1ContributionAndCredits]
  }

  case class Class2ContributionAndCredits(
      taxYear: Option[TaxYear],
      numberOfContributionsAndCredits: Option[NumberOfCreditsAndContributions],
      contributionCreditType: Option[ContributionCreditType],
      class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
      class2NIContributionAmount: Option[Class2NIContributionAmount],
      class2Or3CreditStatus: Option[Class2Or3CreditStatus],
      creditSource: Option[CreditSource],
      latePaymentPeriod: Option[LatePaymentPeriod]
  )

  object Class2ContributionAndCredits {
    implicit val nicClass2Format: Format[Class2ContributionAndCredits] = Json.format[Class2ContributionAndCredits]
  }

  case class NiContributionsAndCreditsSuccessResponse(
      totalGraduatedPensionUnits: Option[TotalGraduatedPensionUnits],
      class1ContributionAndCredits: Option[List[Class1ContributionAndCredits]],
      class2ContributionAndCredits: Option[List[Class2ContributionAndCredits]]
  ) extends NiContributionsAndCreditsResponse
      with NpsSuccessfulApiResponse

  object NiContributionsAndCreditsSuccessResponse {

    implicit val getNiContributionsAndCreditsFormat: Format[NiContributionsAndCreditsSuccessResponse] =
      Json.format[NiContributionsAndCreditsSuccessResponse]

  }

}
