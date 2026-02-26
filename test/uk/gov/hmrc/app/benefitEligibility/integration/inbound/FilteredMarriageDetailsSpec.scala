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
  FilteredMarriageDetails,
  FilteredMarriageDetailsItem,
  FilteredSchemeMembershipDetails,
  FilteredSchemeMembershipDetailsItem
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.Class2MAReceiptsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.MarriageDetailsSuccess.{
  MarriageDetailsSuccessResponse,
  MarriageEndDate,
  MarriageStartDate,
  ReconciliationDate,
  SeparationDate,
  SpouseForename,
  SpouseSurname
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageEndDateStatus.Verified
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStartDateStatus
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.enums.MarriageStatus.CivilPartner
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.SchemeMembershipDetailsSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*

import java.time.LocalDate

class FilteredMarriageDetailsSpec extends AnyFreeSpec with Matchers {

  val marriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
    MarriageDetailsSuccess.MarriageDetails(
      MarriageDetailsSuccess.ActiveMarriage(true),
      Some(
        List(
          MarriageDetailsSuccess
            .MarriageDetailsListElement(
              sequenceNumber = MarriageDetailsSuccess.SequenceNumber(2),
              status = CivilPartner,
              startDate = Some(MarriageStartDate(LocalDate.parse("1999-01-01"))),
              startDateStatus = Some(MarriageStartDateStatus.Verified),
              endDate = Some(MarriageEndDate(LocalDate.parse("2001-01-01"))),
              endDateStatus = Some(Verified),
              spouseIdentifier = Some(Identifier("AB123456C")),
              spouseForename = Some(SpouseForename("Skywalker")),
              spouseSurname = Some(SpouseSurname("Luke")),
              separationDate = Some(SeparationDate(LocalDate.parse("2002-01-01"))),
              reconciliationDate = Some(ReconciliationDate(LocalDate.parse("2003-01-01")))
            )
        )
      ),
      Some(
        MarriageDetailsSuccess.Links(
          MarriageDetailsSuccess.SelfLink(
            Some(MarriageDetailsSuccess.Href("")),
            Some(MarriageDetailsSuccess.Methods.get)
          )
        )
      )
    )
  )

  val minimalMarriageDetailsSuccessResponse = MarriageDetailsSuccessResponse(
    MarriageDetailsSuccess.MarriageDetails(
      MarriageDetailsSuccess.ActiveMarriage(true),
      marriageDetailsList = None,
      _links = None
    )
  )

  "FilteredMarriageDetails" - {
    ".from" - {
      "should construct a filtered object from a marriageDetailsSuccessResponse (maximal response)" in {

        val result = FilteredMarriageDetails.from(
          marriageDetailsSuccessResponse
        )

        val expected =
          FilteredMarriageDetails(
            List(
              FilteredMarriageDetailsItem(
                status = CivilPartner,
                startDate = Some(MarriageStartDate(LocalDate.parse("1999-01-01"))),
                startDateStatus = Some(MarriageStartDateStatus.Verified),
                endDate = Some(MarriageEndDate(LocalDate.parse("2001-01-01"))),
                endDateStatus = Some(Verified),
                spouseIdentifier = Some(Identifier("AB123456C")),
                spouseForename = Some(SpouseForename("Skywalker")),
                spouseSurname = Some(SpouseSurname("Luke"))
              )
            )
          )

        result shouldBe expected
      }
      "should construct a filtered object from a marriageDetailsSuccessResponse (minimal response)" in {

        val result = FilteredMarriageDetails.from(
          minimalMarriageDetailsSuccessResponse
        )

        val expected = FilteredMarriageDetails(List())

        result shouldBe expected
      }
    }

  }

}
