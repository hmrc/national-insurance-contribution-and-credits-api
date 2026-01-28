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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common._
import uk.gov.hmrc.app.benefitEligibility.common.npsError.NpsErrorCode400.{NpsErrorCode400_1, NpsErrorCode400_2}
import uk.gov.hmrc.app.benefitEligibility.common.npsError._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{ErrorReport, FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess._
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums.SchemeNature.UnitTrusts
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.enums._

class BenefitSchemeDetailsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val benefitSchemeDetailsSuccessResponse = BenefitSchemeDetailsSuccessResponse(
    benefitSchemeDetails = BenefitSchemeDetails(
      magneticTapeNumber = Some(MagneticTapeNumber(54321)),
      schemeName = Some(BenefitSchemeName("EXAMPLE PENSION SCHEME")),
      schemeStartDate = Some(SchemeStartDate("1985-04-06")),
      schemeCessationDate = Some(SchemeCessationDate("2024-12-31")),
      contractedOutDeductionExtinguishedDate = Some(ContractedOutDeductionExtinguishedDate("2024-12-31")),
      paymentSuspensionDate = Some(PaymentSuspensionDate("2024-10-01")),
      recoveriesSuspendedDate = Some(RecoveriesSuspendedDate("2024-10-01")),
      paymentRestartDate = Some(PaymentRestartDate("2024-10-01")),
      recoveriesRestartedDate = Some(RecoveriesRestartedDate("2024-10-01")),
      schemeNature = Some(UnitTrusts),
      benefitSchemeInstitution = Some(BenefitSchemeInstitutionType.UnitTrust),
      accruedGMPLiabilityServiceDate = Some(AccruedGMPLiabilityServiceDate("1990-04-06")),
      rerouteToSchemeCessation = Some(RerouteToSchemeCessation.ReRouteToCessation),
      statementInhibitor = Some(StatementInhibitor.Set),
      certificateCancellationDate = Some(CertificateCancellationDate("2024-12-31")),
      suspendedDate = Some(SuspendedDate("2024-10-01")),
      isleOfManInterest = Some(IsleOfManInterest(false)),
      schemeWindingUp = Some(SchemeWindingUp(true)),
      revaluationRateSequenceNumber = Some(RevaluationRateSequenceNumber(12)),
      benefitSchemeStatus = Some(BenefitSchemeStatus.BlockOnProvider),
      dateFormallyCertified = Some(DateFormallyCertified("1985-04-06")),
      privatePensionSchemeSanctionDate = Some(PrivatePensionSchemeSanctionDate("1985-04-06")),
      currentOptimisticLock = CurrentOptimisticLock(4),
      schemeConversionDate = Some(SchemeConversionDate("2024-12-31")),
      schemeInhibitionStatus = SchemeInhibitionStatus.ConvertedStakeholderPension,
      reconciliationDate = Some(ReconciliationDate("2025-03-31")),
      schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C")
    ),
    schemeAddressDetailsList = List(
      SchemeAddressDetails(
        schemeAddressType = Some(SchemeAddressType.GeneralCorrespondence),
        schemeAddressSequenceNumber = SchemeAddressSequenceNumber(5),
        schemeAddressStartDate = Some(SchemeAddressStartDate("2010-01-01")),
        schemeAddressEndDate = Some(SchemeAddressEndDate("2024-12-31")),
        country = Some(Country.Scotland),
        areaDiallingCode = Some(AreaDiallingCode.Code0131), // Note: This would need to be added to the enum
        schemeTelephoneNumber = Some(SchemeTelephoneNumber("0131 000 0000")),
        schemeContractedOutNumberDetails = SchemeContractedOutNumberDetails("S2345678C"),
        benefitSchemeAddressDetails = Some(
          BenefitSchemeAddressDetails(
            schemeAddressLine1 = Some(SchemeAddressLine1("1 Sample Road")),
            schemeAddressLine2 = Some(SchemeAddressLine2("Unit 2")),
            schemeAddressLocality = Some(SchemeAddressLocality("Old Quarter")),
            schemeAddressPostalTown = Some(SchemeAddressPostalTown("Exampleburgh")),
            schemePostcode = Some(SchemePostcode("EX2 2EX"))
          )
        )
      )
    )
  )

  val hipFailureResponse = NpsErrorResponseHipOrigin(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse503 = NpsErrorResponse503(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val npsErrorResponse500 = NpsErrorResponse500(
    origin = HipOrigin.Hip,
    response = HipFailureResponse(failures =
      List(
        HipFailureItem(`type` = FailureType("blah_1"), reason = Reason("reason_1")),
        HipFailureItem(`type` = FailureType("blah_2"), reason = Reason("reason_2")),
        HipFailureItem(`type` = FailureType("blah_3"), reason = Reason("reason_3"))
      )
    )
  )

  val standardErrorResponse400 = NpsStandardErrorResponse400(
    origin = HipOrigin.Hip,
    response = ErrorResponse400(
      failures = List(
        ErrorResourceObj400(reason = Reason("reason_1"), code = NpsErrorCode400_1),
        ErrorResourceObj400(reason = Reason("reason_2"), code = NpsErrorCode400_2)
      )
    )
  )

  val npsErrorResponse403 =
    NpsErrorResponse403(NpsErrorCode403.NpsErrorCode403_2, NpsErrorReason403.Forbidden)

  val npsErrorResponse422 =
    NpsErrorResponse422(
      List(
        NpsError422(NpsErrorCode("some code 1"), Reason("some reason 2")),
        NpsError422(NpsErrorCode("some code 2"), Reason("some reason 2"))
      )
    )

  val underTest = new BenefitSchemeDetailsResponseMapper

  "BenefitSchemeDetailsResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {
        underTest.toApiResult(benefitSchemeDetailsSuccessResponse) shouldBe
          SuccessResult(ApiName.BenefitSchemeDetails, benefitSchemeDetailsSuccessResponse)
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (HipFailureResponse)" in {
        underTest.toApiResult(hipFailureResponse) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.BadRequest, Some(hipFailureResponse))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 400 (StandardErrorResponse400)" in {

        underTest.toApiResult(standardErrorResponse400) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.BadRequest, Some(standardErrorResponse400))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 403" in {
        underTest.toApiResult(npsErrorResponse403) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.AccessForbidden, Some(npsErrorResponse403))
          )
      }

      "should successfully return a DownstreamErrorReport result when handling a 422" in {
        underTest.toApiResult(npsErrorResponse422) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.UnprocessableEntity, Some(npsErrorResponse422))
          )
      }

      "should successfully return a DownstreamErrorReport result when when handling a 500" in {
        underTest.toApiResult(npsErrorResponse500) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.InternalServerError, Some(npsErrorResponse500))
          )
      }

      "should successfully return a DownstreamErrorReport result when when handling a 503" in {
        underTest.toApiResult(npsErrorResponse503) shouldBe
          FailureResult(
            ApiName.BenefitSchemeDetails,
            ErrorReport(NpsNormalizedError.ServiceUnavailable, Some(npsErrorResponse503))
          )
      }

    }
  }

}
