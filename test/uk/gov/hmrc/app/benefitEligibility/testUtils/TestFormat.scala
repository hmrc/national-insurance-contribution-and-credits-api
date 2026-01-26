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

import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.ErrorCode422
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.response.IndividualStatePensionInformationError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.response.LiabilitySummaryDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.*

object TestFormat {

  object CommonFormats {
    implicit val errorCode422: Writes[ErrorCode422] = Json.valueWrites[ErrorCode422]
  }

  object Class2MAReceiptsFormats {

    import CommonFormats.errorCode422

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

    import CommonFormats.errorCode422

    implicit val contributionCategoryLetterWrites: Writes[ContributionCategoryLetter] =
      Json.valueWrites[ContributionCategoryLetter]

    implicit val primaryContributionWrites: Writes[PrimaryContribution] =
      Json.valueWrites[PrimaryContribution]

    implicit val employerNameWrites: Writes[EmployerName] =
      Json.valueWrites[EmployerName]

    implicit val primaryPaidEarningsWrites: Writes[PrimaryPaidEarnings] =
      Json.valueWrites[PrimaryPaidEarnings]

    implicit val nicClass1Format: Format[NiClass1] =
      Json.format[NiClass1]

    implicit val nicClass2Format: Format[NiClass2] =
      Json.format[NiClass2]

    implicit val niContributionsAndCreditsSuccessResponseFormat: Format[NiContributionsAndCreditsSuccessResponse] =
      Json.format[NiContributionsAndCreditsSuccessResponse]

    implicit val niContributionsAndCreditsError400Writes: Writes[NiContributionsAndCredits400] =
      Json.writes[NiContributionsAndCredits400]

    implicit val niContributionsAndCreditsResponse400Writes: Writes[NiContributionsAndCreditsResponse400] =
      Json.writes[NiContributionsAndCreditsResponse400]

    implicit val niContributionsAndCreditsError403Writes: Writes[NiContributionsAndCreditsResponse403] =
      Json.writes[NiContributionsAndCreditsResponse403]

    implicit val niContributionsAndCredits422Writes: Writes[NiContributionsAndCredits422] =
      Json.writes[NiContributionsAndCredits422]

    implicit val niContributionsAndCreditsErrorResponse422Writes: Writes[NiContributionsAndCreditsResponse422] =
      Json.writes[NiContributionsAndCreditsResponse422]

  }

  object LiabilitySummaryDetailsFormats {

    import CommonFormats.errorCode422

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

    import CommonFormats.errorCode422

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

    import CommonFormats.errorCode422

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

}
