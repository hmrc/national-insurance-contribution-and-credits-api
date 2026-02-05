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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import uk.gov.hmrc.app.benefitEligibility.common.BenefitType
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataSuccessResult.EligibilityCheckDataSuccessResultMa
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.BenefitSchemeDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.model.IndividualStatePensionInformationSuccess.IndividualStatePensionInformationSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.model.LongTermBenefitNotesSuccess.LongTermBenefitNotesSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.MarriageDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse

sealed trait EligibilityCheckDataSuccessResult

object EligibilityCheckDataSuccessResult {

  case class EligibilityCheckDataSuccessResultMa(
      class2MAReceiptsSuccessResponse: Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse,
      liabilitySummaryDetailsSuccessResponse: LiabilitySummaryDetailsSuccess.LiabilitySummaryDetailsSuccessResponse,
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
  ) extends EligibilityCheckDataSuccessResult

  case class EligibilityCheckDataSuccessResultEsa(
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
  ) extends EligibilityCheckDataSuccessResult

  case class EligibilityCheckDataSuccessResultJsa(
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
  ) extends EligibilityCheckDataSuccessResult

  case class EligibilityCheckDataSuccessResultGysp(
      contributionCredit: List[NiContributionsAndCreditsSuccessResponse],
      schemeMembershipDetails: SchemeMembershipDetailsSuccessResponse,
      benefitSchemeDetails: List[BenefitSchemeDetailsSuccessResponse],
      marriageDetails: MarriageDetailsSuccessResponse,
      longTermBenefitNotes: LongTermBenefitNotesSuccessResponse,
      statePensionData: IndividualStatePensionInformationSuccessResponse
  ) extends EligibilityCheckDataSuccessResult

  case class EligibilityCheckDataSuccessResultBsp(
      marriageDetailsSuccessResponse: MarriageDetailsSuccessResponse,
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccess.NiContributionsAndCreditsSuccessResponse
  ) extends EligibilityCheckDataSuccessResult

}
