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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.response.{
  FilteredClass2MaReceipts,
  FilteredLiabilitySummaryDetailItem,
  FilteredLiabilitySummaryDetails,
  FilteredSchemeMembershipDetails,
  FilteredSchemeMembershipDetailsItem
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.{
  AwardAmount,
  CasepaperReferenceNumber,
  EndDate,
  HomeResponsibilitiesProtectionBenefitReference,
  HomeResponsibilitiesProtectionRate,
  HomeResponsibilityProtectionCalculationYear,
  LiabilityDetailsList,
  LiabilitySummaryDetailsSuccessResponse,
  OccurrenceNumber,
  OfficeDetails,
  OfficeLocationDecode,
  OfficeLocationValue,
  ResourceGroupIdentifier,
  StartDate
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.{
  EnumAtcredfg,
  EnumHrpIndicator,
  EnumLcheadtp,
  EnumLcruletp,
  EnumLiabtp,
  EnumLtpedttp,
  EnumLtpsdttp,
  EnumOffidtp
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*

import java.time.LocalDate

class FilteredLiabilitySummaryDetailsSpec extends AnyFreeSpec with Matchers {

  val liabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
    Some(
      List(
        LiabilityDetailsList(
          identifier = Identifier("RN000001A"),
          `type` = EnumLiabtp.Abroad,
          occurrenceNumber = OccurrenceNumber(1),
          startDateStatus = Some(EnumLtpsdttp.StartDateHeld),
          endDateStatus = Some(EnumLtpedttp.EndDateHeld),
          startDate = StartDate(LocalDate.parse("2026-01-01")),
          endDate = Some(EndDate(LocalDate.parse("2027-01-01"))),
          country = Some(Country.GreatBritain),
          trainingCreditApprovalStatus = Some(EnumAtcredfg.NoCreditForApprovedTraining),
          casepaperReferenceNumber = Some(CasepaperReferenceNumber("SCH/123/4")),
          homeResponsibilitiesProtectionBenefitReference =
            Some(HomeResponsibilitiesProtectionBenefitReference("12345678AB")),
          homeResponsibilitiesProtectionRate = Some(HomeResponsibilitiesProtectionRate(10.56)),
          lostCardNotificationReason = Some(EnumLcheadtp.NotApplicable),
          lostCardRulingReason = Some(EnumLcruletp.NotApplicable),
          homeResponsibilityProtectionCalculationYear = Some(HomeResponsibilityProtectionCalculationYear(2022)),
          awardAmount = Some(AwardAmount(10.56)),
          resourceGroupIdentifier = Some(ResourceGroupIdentifier(789)),
          homeResponsibilitiesProtectionIndicator = Some(EnumHrpIndicator.None),
          officeDetails = Some(
            OfficeDetails(
              officeLocationDecode = Some(OfficeLocationDecode(1)),
              officeLocationValue = Some(OfficeLocationValue("HQ STATIONARY STORE")),
              officeIdentifier = Some(EnumOffidtp.None)
            )
          )
        )
      )
    ),
    Some(Callback(Some(CallbackUrl("/some/url"))))
  )

  val minimalLiabilitySummaryDetailsSuccessResponse = LiabilitySummaryDetailsSuccessResponse(
    liabilityDetailsList = None,
    callback = None
  )

  "FilteredLiabilitySummaryDetails" - {
    ".from" - {
      "should construct a filtered object from a liabilitySummaryDetailsSuccessResponse (maximal response)" in {

        val result = FilteredLiabilitySummaryDetails.from(
          liabilitySummaryDetailsSuccessResponse
        )

        val expected = FilteredLiabilitySummaryDetails(
          List(
            FilteredLiabilitySummaryDetailItem(
              StartDate(LocalDate.parse("2026-01-01")),
              Some(EndDate(LocalDate.parse("2027-01-01")))
            )
          )
        )

        result shouldBe expected
      }
      "should construct a filtered object from a liabilitySummaryDetailsSuccessResponse (minimal response)" in {

        val result = FilteredLiabilitySummaryDetails.from(
          minimalLiabilitySummaryDetailsSuccessResponse
        )

        val expected = FilteredLiabilitySummaryDetails(List())

        result shouldBe expected
      }
    }

  }

}
