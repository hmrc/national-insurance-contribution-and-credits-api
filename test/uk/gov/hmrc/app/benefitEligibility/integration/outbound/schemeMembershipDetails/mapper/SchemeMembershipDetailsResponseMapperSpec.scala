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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.mapper

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.npsError.{
  ErrorCode422,
  NpsErrorCode400,
  NpsErrorCode403,
  NpsErrorReason403
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.{FailureResult, SuccessResult}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.enums.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsError.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.model.response.SchemeMembershipDetailsSuccess.*

import java.time.LocalDate

class SchemeMembershipDetailsResponseMapperSpec extends AnyFreeSpec with MockFactory {

  val schemeMembershipDetailsSuccessResponse = SchemeMembershipDetailsSuccessResponse(
    schemeMembershipDetailsSummaryList = Some(
      List(
        SchemeMembershipDetailsSummary(
          stakeholderPensionSchemeType = StakeholderPensionSchemeType.NonStakeholderPension,
          schemeMembershipDetails = SchemeMembershipDetails(
            nationalInsuranceNumber = Identifier("AA123456"),
            schemeMembershipSequenceNumber = SchemeMembershipSequenceNumber(123),
            schemeMembershipOccurrenceNumber = SchemeMembershipOccurrenceNumber(1),
            schemeMembershipStartDate = Some(SchemeMembershipStartDate(LocalDate.of(2022, 6, 27))),
            contractedOutEmployerIdentifier = Some(ContractedOutEmployerIdentifier(789)),
            schemeMembershipEndDate = Some(SchemeMembershipEndDate(LocalDate.of(2022, 6, 27))),
            methodOfPreservationType = Some(MethodOfPreservation.NotApplicable0),
            totalLinkedGuaranteedMinimumPensionContractedOutDeductions =
              Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductions(BigDecimal("10.56"))),
            accruedPensionContractedOutDeductionsValue =
              Some(AccruedPensionContractedOutDeductionsValue(BigDecimal("10.56"))),
            totalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988 =
              Some(TotalLinkedGuaranteedMinimumPensionContractedOutDeductionsPost1988(BigDecimal("10.56"))),
            accruedPensionContractedOutDeductionsValuePost1988 =
              Some(AccruedPensionContractedOutDeductionsValuePost1988(BigDecimal("10.56"))),
            revaluationRate = Some(RevaluationRate.None),
            guaranteedMinimumPensionReconciliationStatus =
              Some(GuaranteedMinimumPensionReconciliationStatus.NotApplicable),
            employeesReference = Some(EmployeesReference("123/456/ABC")),
            finalYearEarnings = Some(FinalYearEarnings(BigDecimal("10.56"))),
            penultimateYearEarnings = Some(PenultimateYearEarnings(BigDecimal("10.56"))),
            retrospectiveEarnings = Some(RetrospectiveEarnings(BigDecimal("10.56"))),
            furtherPaymentsConfirmation = Some(FurtherPaymentsConfirmation.FurtherPaymentAllowed),
            survivorStatus = Some(SurvivorStatus.NotApplicable),
            transferPremiumElectionDate = Some(TransferPremiumElectionDate(LocalDate.of(2022, 6, 27))),
            revaluationApplied = Some(RevaluationApplied(true)),
            stateEarningsRelatedPensionsSchemeNonRestorationValue =
              Some(StateEarningsRelatedPensionsSchemeNonRestorationValue(BigDecimal("10.56"))),
            stateEarningsRelatedPensionsSchemeValuePost1988 =
              Some(StateEarningsRelatedPensionsSchemeValuePost1988(BigDecimal("10.56"))),
            apparentUnnotifiedTerminationStatus =
              Some(ApparentUnnotifiedTerminationStatus.NoApparentUnnotifiedTermination),
            terminationMicrofilmNumber = Some(TerminationMicrofilmNumber(789)),
            debitVoucherMicrofilmNumber = Some(DebitVoucherMicrofilmNumber(40599123)),
            creationMicrofilmNumber = Some(CreationMicrofilmNumber(40599123)),
            inhibitSchemeProcessing = Some(InhibitSchemeProcessing(true)),
            extensionDate = Some(ExtensionDate(LocalDate.of(2022, 6, 27))),
            guaranteedMinimumPensionContractedOutDeductionsRevalued =
              Some(GuaranteedMinimumPensionContractedOutDeductionsRevalued(BigDecimal("10.56"))),
            clericalCalculationInvolved = Some(Clercalc.NoClericalCalculationInvolved),
            clericallyControlledTotal = Some(ClericallyControlledTotal(BigDecimal("10.56"))),
            clericallyControlledTotalPost1988 = Some(ClericallyControlledTotalPost1988(BigDecimal("10.56"))),
            certifiedAmount = Some(CertifiedAmount(BigDecimal("10.56"))),
            enforcementStatus = Some(Enfcment.NotEnforced),
            stateSchemePremiumDeemed = Some(SspDeem.SspTypeReceivablesToBeTreatAsDeemed),
            transferTakeUpDate = Some(TransferTakeUpDate(LocalDate.of(2022, 6, 27))),
            schemeMembershipTransferSequenceNumber = Some(SchemeMembershipTransferSequenceNumber(123)),
            contributionCategoryFinalYear = Some(ContCatLetter.A),
            contributionCategoryPenultimateYear = Some(ContCatLetter.A),
            contributionCategoryRetrospectiveYear = Some(ContCatLetter.A),
            protectedRightsStartDate = Some(ProtectedRightsStartDate(LocalDate.of(2022, 6, 27))),
            schemeMembershipDebitReason = Some(SchemeMembershipDebitReason.NotApplicable),
            technicalAmount = Some(TechnicalAmount(BigDecimal("10.56"))),
            minimumFundTransferAmount = Some(MinimumFundTransferAmount(BigDecimal("10.56"))),
            actualTransferValue = Some(ActualTransferValue(BigDecimal("10.56"))),
            schemeSuspensionType = Some(SchemeSuspensionType.NoSuspension),
            guaranteedMinimumPensionConversionApplied = Some(GuaranteedMinimumPensionConversionApplied(true)),
            employersContractedOutNumberDetails = Some(EmployersContractedOutNumberDetails("S3123456B")),
            schemeCreatingContractedOutNumberDetails = Some(SchemeCreatingContractedOutNumberDetails("A7123456Q")),
            schemeTerminatingContractedOutNumberDetails =
              Some(SchemeTerminatingContractedOutNumberDetails("S2123456B")),
            importingAppropriateSchemeNumberDetails = Some(ImportingAppropriateSchemeNumberDetails("S2123456B")),
            apparentUnnotifiedTerminationDestinationDetails =
              Some(ApparentUnnotifiedTerminationDestinationDetails("S2123456B"))
          )
        )
      )
    ),
    callback = Some(
      Callback(
        callbackURL = Some(
          CallbackURL(
            "some-url"
          )
        )
      )
    )
  )

  val schemeMembershipDetailsErrorResponse400 = SchemeMembershipDetailsErrorResponse400(
    List(
      SchemeMembershipDetailsError400(
        Reason("HTTP message not readable"),
        NpsErrorCode400.NpsErrorCode400_2
      ),
      SchemeMembershipDetailsError400(
        Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
        NpsErrorCode400.NpsErrorCode400_1
      )
    )
  )

  val schemeMembershipDetailsErrorResponse403 =
    SchemeMembershipDetailsErrorResponse403(NpsErrorReason403.Forbidden, NpsErrorCode403.NpsErrorCode403_2)

  val schemeMembershipDetailsErrorResponse422 = SchemeMembershipDetailsErrorResponse422(
    failures = List(
      SchemeMembershipDetailsError422(
        Reason("HTTP message not readable"),
        ErrorCode422("A589")
      )
    )
  )

  val underTest = new SchemeMembershipDetailsResponseMapper

  "SchemeMembershipDetailsResponseMapper" - {
    ".toApiResult" - {
      "should successfully return a Success result when given a SuccessResponse" in {

        underTest.toApiResult(schemeMembershipDetailsSuccessResponse) shouldBe
          SuccessResult(ApiName.SchemeMembershipDetails, schemeMembershipDetailsSuccessResponse)
      }

      "should successfully return a Failure result when given an ErrorResponse400" in {

        underTest.toApiResult(schemeMembershipDetailsErrorResponse400) shouldBe
          FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.BadRequest)
      }

      "should successfully return a Failure result when given a ErrorResponse403" in {

        underTest.toApiResult(schemeMembershipDetailsErrorResponse403) shouldBe
          FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.AccessForbidden)
      }

      "should successfully return a Failure result when given a ErrorResponse422" in {

        underTest.toApiResult(schemeMembershipDetailsErrorResponse422) shouldBe
          FailureResult(ApiName.SchemeMembershipDetails, NpsNormalizedError.UnprocessableEntity)
      }
    }
  }

}
