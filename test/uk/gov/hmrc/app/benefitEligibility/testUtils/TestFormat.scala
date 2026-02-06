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

package uk.gov.hmrc.app.benefitEligibility.testUtils

import play.api.libs.json.{Format, Json, OWrites, Reads, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  ErrorCode422,
  HipFailureResponse,
  NpsErrorResponseHipOrigin,
  NpsMultiErrorResponse,
  NpsSingleErrorResponse,
  NpsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.{
  BenefitSchemeAddressDetails,
  BenefitSchemeDetails,
  BenefitSchemeDetailsSuccessResponse,
  SchemeAddressDetails
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsError.{
  BenefitCalculationDetailsError400,
  BenefitCalculationDetailsError422,
  BenefitCalculationDetailsErrorItem400,
  BenefitCalculationDetailsErrorResponse403,
  BenefitCalculationDetailsErrorResponse404,
  BenefitCalculationDetailsErrorResponse422,
  BenefitCalculationDetailsHipFailureResponse400,
  BenefitCalculationDetailsHipFailureResponse500,
  BenefitCalculationDetailsHipFailureResponse503,
  BenefitCalculationDetailsStandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.LongTermBenefitCalculationDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationError
import IndividualStatePensionInformationError.*
import uk.gov.hmrc.app.benefitEligibility.common.Callback
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.*

object TestFormat {

  implicit val npsSingleErrorResponseWrites: Writes[NpsSingleErrorResponse] = Json.writes[NpsSingleErrorResponse]
  implicit val npsMultiErrorResponseWrites: OWrites[NpsMultiErrorResponse]  = Json.writes[NpsMultiErrorResponse]

  implicit val npsStandardErrorResponse400Writes: Writes[NpsStandardErrorResponse400] =
    Json.writes[NpsStandardErrorResponse400]

  implicit val hipFailureResponseWrites: Writes[HipFailureResponse] = Json.writes[HipFailureResponse]

  implicit val npsErrorResponseHipOriginResponseWrites: Writes[NpsErrorResponseHipOrigin] =
    Json.writes[NpsErrorResponseHipOrigin]

  object CommonFormats {
    implicit val errorCode422: Writes[ErrorCode422] = Json.valueWrites[ErrorCode422]
  }

  object Class2MAReceiptsFormats {

    implicit val class2MAReceiptsError400Writes: Writes[Class2MAReceiptsError400] =
      Json.writes[Class2MAReceiptsError400]

    implicit val class2MAReceiptsErrorResponse400Writes: Writes[Class2MAReceiptsErrorResponse400] =
      Json.writes[Class2MAReceiptsErrorResponse400]

    implicit val class2MAReceiptsErrorResponse403Writes: Writes[Class2MAReceiptsErrorResponse403] =
      Json.writes[Class2MAReceiptsErrorResponse403]

    implicit val class2MAReceiptsError422Writes: Writes[Class2MAReceiptsError422] =
      Json.writes[Class2MAReceiptsError422]

    implicit val class2MAReceiptsErrorResponse422Writes: Writes[Class2MAReceiptsErrorResponse422] =
      Json.writes[Class2MAReceiptsErrorResponse422]

  }

  object ContributionCreditFormats {

    implicit val contributionCategoryLetterWrites: Writes[ContributionCategoryLetter] =
      Json.valueWrites[ContributionCategoryLetter]

    implicit val primaryContributionWrites: Writes[PrimaryContribution] =
      Json.valueWrites[PrimaryContribution]

    implicit val employerNameWrites: Writes[EmployerName] =
      Json.valueWrites[EmployerName]

    implicit val primaryPaidEarningsWrites: Writes[PrimaryPaidEarnings] =
      Json.valueWrites[PrimaryPaidEarnings]

    implicit val nicClass1Format: Format[Class1ContributionAndCredits] =
      Json.format[Class1ContributionAndCredits]

    implicit val nicClass2Format: Format[Class2ContributionAndCredits] =
      Json.format[Class2ContributionAndCredits]

    implicit val niContributionsAndCreditsSuccessResponseFormat: Format[NiContributionsAndCreditsSuccessResponse] =
      Json.format[NiContributionsAndCreditsSuccessResponse]

  }

  object LiabilitySummaryDetailsFormats {

    implicit val liabilitySummaryDetailsError400Writes: Writes[LiabilitySummaryDetailsError400] =
      Json.writes[LiabilitySummaryDetailsError400]

    implicit val liabilitySummaryDetailsErrorResponse400Writes: Writes[LiabilitySummaryDetailsErrorResponse400] =
      Json.writes[LiabilitySummaryDetailsErrorResponse400]

    implicit val liabilitySummaryDetailsErrorResponse403Writes: Writes[LiabilitySummaryDetailsErrorResponse403] =
      Json.writes[LiabilitySummaryDetailsErrorResponse403]

    implicit val liabilitySummaryDetailsError422Writes: Writes[LiabilitySummaryDetailsError422] =
      Json.writes[LiabilitySummaryDetailsError422]

    implicit val liabilitySummaryDetailsErrorResponse422Writes: Writes[LiabilitySummaryDetailsErrorResponse422] =
      Json.writes[LiabilitySummaryDetailsErrorResponse422]

  }

  object SchemeMembership {

    implicit val schemeMembershipDetailsError400Writes: Writes[SchemeMembershipDetailsError400] =
      Json.writes[SchemeMembershipDetailsError400]

    implicit val schemeMembershipDetailsErrorResponse400Writes: Writes[SchemeMembershipDetailsErrorResponse400] =
      Json.writes[SchemeMembershipDetailsErrorResponse400]

    implicit val schemeMembershipDetailsErrorResponse403Writes: Writes[SchemeMembershipDetailsErrorResponse403] =
      Json.writes[SchemeMembershipDetailsErrorResponse403]

    implicit val schemeMembershipDetailsError422Writes: Writes[SchemeMembershipDetailsError422] =
      Json.writes[SchemeMembershipDetailsError422]

    implicit val schemeMembershipDetailsErrorResponse422Writes: Writes[SchemeMembershipDetailsErrorResponse422] =
      Json.writes[SchemeMembershipDetailsErrorResponse422]

    implicit val schemeMembershipDetailsWrites: Writes[SchemeMembershipDetails] = Json.writes[SchemeMembershipDetails]

    implicit val schemeMembershipDetailsSummaryWrites: Writes[SchemeMembershipDetailsSummary] =
      Json.writes[SchemeMembershipDetailsSummary]

    implicit val callbackWrites: Writes[Callback] = Json.writes[Callback]

    implicit val schemeMembershipDetailsSuccessResponseWrites: Writes[SchemeMembershipDetailsSuccessResponse] =
      Json.writes[SchemeMembershipDetailsSuccessResponse]

  }

  object LongTermBenefitNotesFormats {

    implicit val longTermBenefitNotesHipFailureResponse400Writes: Writes[LongTermBenefitNotesHipFailureResponse400] =
      Json.writes[LongTermBenefitNotesHipFailureResponse400]

    implicit val longTermBenefitNotesHipFailureResponse500Writes: Writes[LongTermBenefitNotesHipFailureResponse500] =
      Json.writes[LongTermBenefitNotesHipFailureResponse500]

    implicit val longTermBenefitNotesHipFailureResponse503Writes: Writes[LongTermBenefitNotesHipFailureResponse503] =
      Json.writes[LongTermBenefitNotesHipFailureResponse503]

    implicit val longTermBenefitNotesErrorItem400Write: Writes[LongTermBenefitNotesErrorItem400] =
      Json.writes[LongTermBenefitNotesErrorItem400]

    implicit val longTermBenefitNotesError400Writes: Writes[LongTermBenefitNotesError400] =
      Json.writes[LongTermBenefitNotesError400]

    implicit val longTermBenefitNotesStandardErrorResponse400Writes
        : Writes[LongTermBenefitNotesStandardErrorResponse400] =
      Json.writes[LongTermBenefitNotesStandardErrorResponse400]

    implicit val longTermBenefitNotesErrorResponse403Writes: Writes[LongTermBenefitNotesErrorResponse403] =
      Json.writes[LongTermBenefitNotesErrorResponse403]

    implicit val longTermBenefitNotesErrorResponse404Writes: Writes[LongTermBenefitNotesErrorResponse404] =
      Json.writes[LongTermBenefitNotesErrorResponse404]

    implicit val longTermBenefitNotesError422Writes: Writes[LongTermBenefitNotesError422] =
      Json.writes[LongTermBenefitNotesError422]

    implicit val longTermBenefitNotesErrorResponse422Writes: Writes[LongTermBenefitNotesErrorResponse422] =
      Json.writes[LongTermBenefitNotesErrorResponse422]

    implicit val noteWrites: Writes[Note] = Json.valueWrites[Note]

    implicit val longTermBenefitNotesSuccessResponseWrites: Writes[LongTermBenefitNotesSuccessResponse] =
      Json.writes[LongTermBenefitNotesSuccessResponse]

  }

  object IndividualStatePensionInformation {

    implicit val individualStatePensionInformationHipFailureResponse400Writes
        : Writes[IndividualStatePensionInformationHipFailureResponse400] =
      Json.writes[IndividualStatePensionInformationHipFailureResponse400]

    implicit val errorResourceObj400Writes: Writes[IndividualStatePensionInformationError.ErrorResourceObj400] =
      Json.writes[IndividualStatePensionInformationError.ErrorResourceObj400]

    implicit val errorResponse400Writes: Writes[IndividualStatePensionInformationError.ErrorResponse400] =
      Json.writes[IndividualStatePensionInformationError.ErrorResponse400]

    implicit val individualStatePensionInformationStandardErrorResponse400Writes
        : Writes[IndividualStatePensionInformationStandardErrorResponse400] =
      Json.writes[IndividualStatePensionInformationStandardErrorResponse400]

    implicit val individualStatePensionInformationErrorResponse503Writes
        : Writes[IndividualStatePensionInformationErrorResponse503] =
      Json.writes[IndividualStatePensionInformationErrorResponse503]

  }

  object BenefitSchemeDetails {

    implicit val benefitSchemeAddressDetailsWrites: Writes[BenefitSchemeAddressDetails] =
      Json.writes[BenefitSchemeAddressDetails]

    implicit val schemeAddressDetailsWrites: Writes[SchemeAddressDetails] =
      Json.writes[SchemeAddressDetails]

    implicit val benefitSchemeDetailsWrites: Writes[BenefitSchemeDetails] =
      Json.writes[BenefitSchemeDetails]

    implicit val benefitSchemeDetailsSuccessResponseWrites: Writes[BenefitSchemeDetailsSuccessResponse] =
      Json.writes[BenefitSchemeDetailsSuccessResponse]

  }

  object BenefitCalculationDetailsFormats {

    implicit val benefitCalculationDetailsHipFailureResponse400Writes
        : Writes[BenefitCalculationDetailsHipFailureResponse400] =
      Json.writes[BenefitCalculationDetailsHipFailureResponse400]

    implicit val benefitCalculationDetailsHipFailureResponse500Writes
        : Writes[BenefitCalculationDetailsHipFailureResponse500] =
      Json.writes[BenefitCalculationDetailsHipFailureResponse500]

    implicit val benefitCalculationDetailsHipFailureResponse503Writes
        : Writes[BenefitCalculationDetailsHipFailureResponse503] =
      Json.writes[BenefitCalculationDetailsHipFailureResponse503]

    implicit val benefitCalculationDetailsErrorItem400Write: Writes[BenefitCalculationDetailsErrorItem400] =
      Json.writes[BenefitCalculationDetailsErrorItem400]

    implicit val benefitCalculationDetailsError400Writes: Writes[BenefitCalculationDetailsError400] =
      Json.writes[BenefitCalculationDetailsError400]

    implicit val benefitCalculationDetailsStandardErrorResponse400Writes
        : Writes[BenefitCalculationDetailsStandardErrorResponse400] =
      Json.writes[BenefitCalculationDetailsStandardErrorResponse400]

    implicit val benefitCalculationDetailsErrorResponse403Writes: Writes[BenefitCalculationDetailsErrorResponse403] =
      Json.writes[BenefitCalculationDetailsErrorResponse403]

    implicit val benefitCalculationDetailsErrorResponse404Writes: Writes[BenefitCalculationDetailsErrorResponse404] =
      Json.writes[BenefitCalculationDetailsErrorResponse404]

    implicit val benefitCalculationDetailsError422Writes: Writes[BenefitCalculationDetailsError422] =
      Json.writes[BenefitCalculationDetailsError422]

    implicit val benefitCalculationDetailsErrorResponse422Writes: Writes[BenefitCalculationDetailsErrorResponse422] =
      Json.writes[BenefitCalculationDetailsErrorResponse422]

  }

}
