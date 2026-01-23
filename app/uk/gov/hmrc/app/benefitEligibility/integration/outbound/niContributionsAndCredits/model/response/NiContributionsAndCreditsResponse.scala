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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.response.MarriageDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{NpsApiResponse, NpsSuccessfulApiResponse}

import java.time.LocalDate
import scala.collection.immutable

sealed trait NiContributionsAndCreditsResponse extends NpsApiResponse

object NiContributionsAndCreditsError {

  case class NiContributionsAndCredits400(reason: Reason, code: NpsErrorCode400)

  object NiContributionsAndCredits400 {

    implicit val npsErrorResponse400Reads: Reads[NiContributionsAndCredits400] =
      Json.reads[NiContributionsAndCredits400]

  }

  case class NiContributionsAndCreditsResponse400(failures: List[NiContributionsAndCredits400])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse400 {

    implicit val npsFailureResponse400Reads: Reads[NiContributionsAndCreditsResponse400] =
      Json.reads[NiContributionsAndCreditsResponse400]

  }

  case class NiContributionsAndCreditsResponse403(reason: NpsErrorReason403, code: NpsErrorCode403)
      extends NiContributionsAndCreditsResponse
      with NpsApiResponse

  object NiContributionsAndCreditsResponse403 {

    implicit val npsFailureResponse403Reads: Reads[NiContributionsAndCreditsResponse403] =
      Json.reads[NiContributionsAndCreditsResponse403]

  }

  case class NiContributionsAndCredits422(reason: Reason, code: ErrorCode422)

  object NiContributionsAndCredits422 {

    implicit val NpsErrorResponse422Reads: Reads[NiContributionsAndCredits422] =
      Json.reads[NiContributionsAndCredits422]

  }

  case class NiContributionsAndCreditsResponse422(failures: List[NiContributionsAndCredits422])
      extends NiContributionsAndCreditsResponse

  object NiContributionsAndCreditsResponse422 {

    implicit val npsFailureResponse422Reads: Reads[NiContributionsAndCreditsResponse422] =
      Json.reads[NiContributionsAndCreditsResponse422]

  }

}

object NiContributionsAndCreditsSuccess {

  case class PrimaryContribution(value: BigDecimal) extends AnyVal

  object PrimaryContribution {
    implicit val primaryContributionReads: Reads[PrimaryContribution] = Json.valueReads[PrimaryContribution]
  }

  case class PrimaryPaidEarnings(value: BigDecimal) extends AnyVal

  object PrimaryPaidEarnings {

    implicit val primaryPaidEarningsReads: Reads[PrimaryPaidEarnings] =
      Json.valueReads[PrimaryPaidEarnings]

  }

  case class ReceivablePeriodStartDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodStartDate {

    implicit val receivablePeriodStartDateReads: Format[ReceivablePeriodStartDate] =
      Json.valueFormat[ReceivablePeriodStartDate]

  }

  case class ReceivablePeriodEndDate(value: LocalDate) extends AnyVal

  object ReceivablePeriodEndDate {

    implicit val receivablePeriodEndDateReads: Format[ReceivablePeriodEndDate] =
      Json.valueFormat[ReceivablePeriodEndDate]

  }

  case class LiabilityStartDate(value: LocalDate) extends AnyVal

  object LiabilityStartDate {
    implicit val liabilityStartDateReads: Format[LiabilityStartDate] = Json.valueFormat[LiabilityStartDate]
  }

  case class LiabilityEndDate(value: LocalDate) extends AnyVal

  object LiabilityEndDate {
    implicit val liabilityEndDateReads: Format[LiabilityEndDate] = Json.valueFormat[LiabilityEndDate]
  }

  case class BillScheduleNumber(value: Int) extends AnyVal

  object BillScheduleNumber {
    implicit val billScheduleNumberReads: Format[BillScheduleNumber] = Json.valueFormat[BillScheduleNumber]
  }

  final case class ContributionCategoryLetter(value: String) extends AnyVal

  object ContributionCategoryLetter {

    implicit val contributionCategoryLetterWrites: Reads[ContributionCategoryLetter] =
      Json.valueReads[ContributionCategoryLetter]

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

  case class Class2Or3EarningsFactor(value: BigDecimal) extends AnyVal

  object Class2Or3EarningsFactor {

    implicit val class2Or3EarningsFactorFormat: Format[Class2Or3EarningsFactor] =
      Json.valueFormat[Class2Or3EarningsFactor]

  }

  case class NiClass1(
      taxYear: Option[TaxYear],
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

  object NiClass1 {
    implicit val nicClass1Reads: Reads[NiClass1] = Json.reads[NiClass1]
  }

  case class NiClass2(
      taxYear: Option[TaxYear],
      noOfCreditsAndConts: Option[NumberOfCreditsAndContributions],
      contributionCreditType: Option[ContributionCreditType],
      class2Or3EarningsFactor: Option[Class2Or3EarningsFactor],
      class2NIContributionAmount: Option[Class2NIContributionAmount],
      class2Or3CreditStatus: Option[Class2Or3CreditStatus],
      creditSource: Option[CreditSource],
      latePaymentPeriod: Option[LatePaymentPeriod]
  )

  object NiClass2 {
    implicit val nicClass2Reads: Reads[NiClass2] = Json.reads[NiClass2]
  }

  case class NiContributionsAndCreditsSuccessResponse(
      niClass1: Option[List[NiClass1]],
      niClass2: Option[List[NiClass2]]
  ) extends NiContributionsAndCreditsResponse
      with NpsSuccessfulApiResponse

  object NiContributionsAndCreditsSuccessResponse {

    implicit val getNiContributionsAndCreditsReads: Reads[NiContributionsAndCreditsSuccessResponse] =
      Json.reads[NiContributionsAndCreditsSuccessResponse]

  }

}
