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

package uk.gov.hmrc.app.benefitEligibility.integration.inbound.request

import play.api.libs.json.*
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.BenefitType.{BSP, ESA, GYSP, JSA, MA}
import uk.gov.hmrc.app.benefitEligibility.common.LongTermBenefitType.{Ltb, WidowsBenefit}
import uk.gov.hmrc.app.benefitEligibility.common.MaternityAllowanceSortType.NinoAscending
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.LiabilitySummaryDetailsSuccess.OccurrenceNumber
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.liabilitySummaryDetails.model.enums.LiabilitySearchCategoryHyphenated

import java.time.LocalDate

object EligibilityCheckDataRequest {

  implicit val reads: Reads[EligibilityCheckDataRequest] = Reads { json =>
    (json \ "benefitType").validate[BenefitType].flatMap {
      case BenefitType.MA   => json.validate[MAEligibilityCheckDataRequest]
      case BenefitType.ESA  => json.validate[ESAEligibilityCheckDataRequest]
      case BenefitType.JSA  => json.validate[JSAEligibilityCheckDataRequest]
      case BenefitType.GYSP => json.validate[GYSPEligibilityCheckDataRequest]
      case BenefitType.BSP  => json.validate[BSPEligibilityCheckDataRequest]
    }
  }

}

sealed trait EligibilityCheckDataRequest {
  def benefitType: BenefitType
  def nationalInsuranceNumber: Identifier
}

case class Liabilities(
    searchCategory: LiabilitySearchCategoryHyphenated,              // liability api
    liabilityOccurrenceNumber: Option[LiabilitiesOccurrenceNumber], // liability api
    liabilityType: Option[LiabilitySearchCategoryHyphenated],       // liability api
    earliestLiabilityStartDate: Option[LocalDate],                  // liability api
    liabilityStart: Option[LocalDate],                              // liability api
    liabilityEnd: Option[LocalDate]                                 // liability api
)

object Liabilities {

  implicit val liabilitiesReads: Reads[Liabilities] =
    Json.reads[Liabilities]

}

case class Class2MaReceipts(
    archived: Option[Boolean],                 // ma receipts api
    receiptDate: Option[ReceiptDate],          // ma receipts api
    sortBy: Option[MaternityAllowanceSortType] // ma receipts api
)

object Class2MaReceipts {

  implicit val class2MaReceiptsReads: Reads[Class2MaReceipts] =
    Json.reads[Class2MaReceipts]

}

final case class MAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCredits, // no callback
    liabilities: Liabilities,                           // has callbackUrl
    class2MaReceipts: Class2MaReceipts                  // no callback
) extends EligibilityCheckDataRequest

object MAEligibilityCheckDataRequest {

  def apply(
      nationalInsuranceNumber: Identifier,
      contributionsAndCredits: ContributionsAndCredits,
      liabilities: Liabilities,
      class2MaReceipts: Class2MaReceipts
  ) = new MAEligibilityCheckDataRequest(
    MA,
    nationalInsuranceNumber,
    contributionsAndCredits,
    liabilities,
    class2MaReceipts
  )

  implicit val maEligibilityCheckDataRequestReads: Reads[MAEligibilityCheckDataRequest] =
    Json.reads[MAEligibilityCheckDataRequest]

}

final case class ESAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCredits
) extends EligibilityCheckDataRequest

object ESAEligibilityCheckDataRequest {

  implicit val esaEligibilityCheckDataRequestReads: Reads[ESAEligibilityCheckDataRequest] =
    Json.reads[ESAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCredits
  ) = new ESAEligibilityCheckDataRequest(ESA, nationalInsuranceNumber, niContributionsAndCredits)

}

final case class JSAEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier,
    niContributionsAndCredits: ContributionsAndCredits
) extends EligibilityCheckDataRequest

object JSAEligibilityCheckDataRequest {

  implicit val jsaEligibilityCheckDataRequestReads: Reads[JSAEligibilityCheckDataRequest] =
    Json.reads[JSAEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier,
      niContributionsAndCredits: ContributionsAndCredits
  ) = new JSAEligibilityCheckDataRequest(JSA, nationalInsuranceNumber, niContributionsAndCredits)

}

case class ContributionsAndCredits(
    dateOfBirth: DateOfBirth,   // contribution credit api
    startTaxYear: StartTaxYear, // contribution credit api
    endTaxYear: EndTaxYear      // contribution credit api
)

object ContributionsAndCredits {

  implicit val contributionsAndCreditsReads: Reads[ContributionsAndCredits] =
    Json.reads[ContributionsAndCredits]

}

case class LongTermBenefitCalculation(
    longTermBenefitType: Option[LongTermBenefitType],    // benefit calculation API + benefit calculation notes API
    pensionProcessingArea: Option[PensionProcessingArea] // benefit calculation API
)

object LongTermBenefitCalculation {

  implicit val BenefitCalculation: Reads[LongTermBenefitCalculation] =
    Json.reads[LongTermBenefitCalculation]

}

case class MarriageDetails(
    searchStartYear: Option[Int], // Marriage Details API
    latest: Option[Boolean]       // Marriage Details API
)

object MarriageDetails {

  implicit val MarriageDetails: Reads[MarriageDetails] =
    Json.reads[MarriageDetails]

}

case class BenefitSchemeDetails(
    schemeContractedOutNumber: Int // benefit-scheme-details API
)

object BenefitSchemeDetails {

  implicit val BenefitSchemeDetails: Reads[BenefitSchemeDetails] =
    Json.reads[BenefitSchemeDetails]

}

case class SchemeMembershipDetails(
    schemeMembershipSequenceNumber: Option[SequenceNumber],                           // scheme-membership-details API
    schemeMembershipTransferSequenceNumber: Option[TransferSequenceNumber],           // scheme-membership-details API
    schemeMembershipOccurrenceNumber: Option[SchemeMembershipDetailsOccurrenceNumber] // scheme-membership-details API
)

object SchemeMembershipDetails {

  implicit val SchemeMembershipDetails: Reads[SchemeMembershipDetails] =
    Json.reads[SchemeMembershipDetails]

}

final case class GYSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier, // contribution credit api
    niContributionsAndCredits: ContributionsAndCredits,
    longTermBenefitCalculation: LongTermBenefitCalculation,
    marriageDetails: MarriageDetails
) extends EligibilityCheckDataRequest

object GYSPEligibilityCheckDataRequest {

  implicit val gyspEligibilityCheckDataRequestReads: Reads[GYSPEligibilityCheckDataRequest] =
    Json.reads[GYSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier, // contribution credit api
      niContributionsAndCredits: ContributionsAndCredits,
      longTermBenefitCalculation: LongTermBenefitCalculation,
      marriageDetails: MarriageDetails
  ) = new GYSPEligibilityCheckDataRequest(
    GYSP,
    nationalInsuranceNumber,
    niContributionsAndCredits,
    longTermBenefitCalculation,
    marriageDetails
  )

}

final case class BSPEligibilityCheckDataRequest private (
    benefitType: BenefitType,
    nationalInsuranceNumber: Identifier, // contribution credit api
    niContributionsAndCredits: ContributionsAndCredits,
    marriageDetails: MarriageDetails
) extends EligibilityCheckDataRequest

object BSPEligibilityCheckDataRequest {

  implicit val bspEligibilityCheckDataRequestReads: Reads[BSPEligibilityCheckDataRequest] =
    Json.reads[BSPEligibilityCheckDataRequest]

  def apply(
      nationalInsuranceNumber: Identifier, // contribution credit api
      niContributionsAndCredits: ContributionsAndCredits,
      marriageDetails: MarriageDetails
  ) = new BSPEligibilityCheckDataRequest(BSP, nationalInsuranceNumber, niContributionsAndCredits, marriageDetails)

}

object Test {

//  def main(args: Array[String]): Unit = {
//    println(
//      Json.prettyPrint(
//        Json.toJson(
//          BSPEligibilityCheckDataRequest(
//            BSP,
//            Identifier("ABCDEFG"),
//            ContributionsAndCredits(LocalDate.parse("2001-02-08"), 2, 4),
//            MarriageDetails(Some(2), Some(2), Some(true), Some(1))
//          )
//        )
//      )
//    )
//
//    println(
//      Json.prettyPrint(
//        Json.toJson(
//          GYSPEligibilityCheckDataRequest(
//            GYSP,
//            Identifier("ABCDEFG"),
//            ContributionsAndCredits(LocalDate.parse("2001-02-08"), 2, 4),
//            BenefitCalculation(LongTermBenefitType.WidowsBenefit, 2, Some("3")),
//            BenefitCalculationNotes(WidowsBenefit, 2),
//            BenefitSchemeDetails(1),
//            MarriageDetails(Some(2), Some(2), Some(true), Some(1)),
//            SchemeMembershipDetails(Some(2), Some(3), Some(4))
//          )
//        )
//      )
//    )
//
//    println(
//      Json.prettyPrint(
//        Json.toJson(
//          ESAEligibilityCheckDataRequest(
//            ESA,
//            Identifier("ABCDEFG"),
//            ContributionsAndCredits(LocalDate.parse("2001-02-08"), 2, 4)
//          )
//        )
//      )
//    )
//
//    println(
//      Json.prettyPrint(
//        Json.toJson(
//          JSAEligibilityCheckDataRequest(
//            JSA,
//            Identifier("ABCDEFG"),
//            ContributionsAndCredits(LocalDate.parse("2001-02-08"), 2, 4)
//          )
//        )
//      )
//    )
//
//    println(
//      Json.prettyPrint(
//        Json.toJson(
//          MAEligibilityCheckDataRequest(
//            MA,
//            Identifier("ABCDEFG"),
//            ContributionsAndCredits(LocalDate.parse("2001-02-08"), 2, 4),
//            Liabilities(
//              LiabilitySearchCategoryHyphenated.Class2LiabilityUk,
//              Some(SchemeMembershipDetailsOccurrenceNumber(2)),
//              Some(LiabilitySearchCategoryHyphenated.Class2LiabilityUk),
//              Some(LocalDate.parse("2001-02-08")),
//              Some(LocalDate.parse("2001-02-08")),
//              Some(LocalDate.parse("2001-02-08"))
//            ),
//            Class2MaReceipts(Some(true), Some(ReceiptDate(LocalDate.parse("2001-02-08"))), Some(NinoAscending))
//          )
//        )
//      )
//    )
//  }

}
