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

package uk.gov.hmrc.app.benefitEligibility.model.request

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.liabilitySummaryDetails.enums.LiabilitySearchCategoryHyphenated

import java.time.LocalDate

object EligibilityCheckDataRequestParams {

  final case class ContributionsAndCreditsRequestParams(
      dateOfBirth: DateOfBirth,
      startTaxYear: StartTaxYear,
      endTaxYear: EndTaxYear
  )

  object ContributionsAndCreditsRequestParams {

    implicit val contributionsAndCreditsReads: Reads[ContributionsAndCreditsRequestParams] =
      Json.reads[ContributionsAndCreditsRequestParams]

  }

  final case class LiabilitiesRequestParams(
      searchCategories: List[LiabilitySearchCategoryHyphenated],
      earliestLiabilityStartDate: Option[LocalDate],
      liabilityStart: Option[LocalDate],
      liabilityEnd: Option[LocalDate]
  )

  object LiabilitiesRequestParams {

    implicit val liabilitiesReads: Reads[LiabilitiesRequestParams] =
      Json.reads[LiabilitiesRequestParams]

  }

  final case class LongTermBenefitCalculationRequestParams(
      longTermBenefitType: Option[LongTermBenefitType],
      pensionProcessingArea: Option[PensionProcessingArea]
  )

  object LongTermBenefitCalculationRequestParams {

    implicit val BenefitCalculation: Reads[LongTermBenefitCalculationRequestParams] =
      Json.reads[LongTermBenefitCalculationRequestParams]

  }

}
