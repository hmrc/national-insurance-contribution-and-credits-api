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

package uk.gov.hmrc.app.benefitEligibility.service

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.Inject
import uk.gov.hmrc.app.benefitEligibility.common.{
  AssociatedCalculationSequenceNumber,
  BenefitEligibilityError,
  BenefitType,
  Identifier,
  LongTermBenefitType
}
import uk.gov.hmrc.app.benefitEligibility.common.BenefitEligibilityError.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  ContributionsAndCredits,
  GYSPEligibilityCheckDataRequest,
  LongTermBenefitCalculation,
  MarriageDetails,
  SchemeMembershipDetails
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.connector.BenefitSchemeDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.benefitSchemeDetails.model.BenefitSchemeDetailsSuccess.SchemeContractedOutNumberDetails
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.individualStatePensionInformation.connector.IndividualStatePensionInformationConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.connector.LongTermBenefitCalculationDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitCalculationDetails.model.BenefitCalculationDetailsSuccess.LongTermBenefitCalculationDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.longTermBenefitNotes.connector.LongTermBenefitNotesConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.connector.MarriageDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.connector.NiContributionsAndCreditsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.schemeMembershipDetails.connector.SchemeMembershipDetailsConnector
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.{
  BenefitSchemeDetailsResult,
  EligibilityCheckDataResult,
  LongTermBenefitCalculationDetailsResult,
  LongTermBenefitNotesResult,
  NpsApiResult,
  SchemeMembershipDetailsResult
}
import uk.gov.hmrc.app.benefitEligibility.util.ContributionCreditTaxWindowCalculator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.NpsApiResult.ErrorReport

import scala.concurrent.{ExecutionContext, Future}

final case class RequestKey(benefitType: BenefitType, nationalInsuranceNumber: Identifier)

final case class BenefitSchemeMembershipDetailsData(
    schemeMembershipDetailsResult: SchemeMembershipDetailsResult,
    benefitSchemeDetailsResults: List[BenefitSchemeDetailsResult]
)

final case class LongTermBenefitCalculationDetailsData(
    longTermBenefitCalculationDetailsResult: LongTermBenefitCalculationDetailsResult,
    longTermBenefitNotesResults: List[LongTermBenefitNotesResult]
)

class GetYourStatePensionDataRetrievalService @Inject() (
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    benefitSchemeDetailsConnector: BenefitSchemeDetailsConnector,
    marriageDetailsConnector: MarriageDetailsConnector,
    longTermBenefitCalculationDetailsConnector: LongTermBenefitCalculationDetailsConnector,
    longTermBenefitNotesConnector: LongTermBenefitNotesConnector,
    schemeMembershipDetailsConnector: SchemeMembershipDetailsConnector,
    statePensionInformationConnector: IndividualStatePensionInformationConnector
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: GYSPEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultGYSP] = {

    implicit val requestKey: RequestKey =
      RequestKey(eligibilityCheckDataRequest.benefitType, eligibilityCheckDataRequest.nationalInsuranceNumber)

    (
      fetchNiContributionsAndCreditsData(eligibilityCheckDataRequest.niContributionsAndCredits),
      fetchMarriageDetailsData(eligibilityCheckDataRequest.marriageDetails),
      fetchBenefitSchemeMembershipDetailsData(),
      fetchLongTermBenefitCalculationDetailsData(eligibilityCheckDataRequest.longTermBenefitCalculation),
      fetchIndividualStatePensionInformation()
    ).parTupled.map {
      case (
            contributionsAndCreditResult,
            marriageDetailsResult,
            BenefitSchemeMembershipDetailsData(schemeMembershipDetailsResult, benefitSchemeDetailsResults),
            LongTermBenefitCalculationDetailsData(longTermBenefitCalculationDetailsResult, longTermBenefitNotesResults),
            individualStatePensionResult
          ) =>
        EligibilityCheckDataResultGYSP(
          contributionsAndCreditResult,
          schemeMembershipDetailsResult,
          benefitSchemeDetailsResults,
          longTermBenefitCalculationDetailsResult,
          longTermBenefitNotesResults,
          marriageDetailsResult,
          individualStatePensionResult
        )
    }

  }

  private[service] def fetchNiContributionsAndCreditsData(
      contributionsAndCredits: ContributionsAndCredits
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) = {

    val taxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
      contributionsAndCredits.startTaxYear,
      contributionsAndCredits.endTaxYear
    )

    taxWindows.map { window =>
      niContributionsAndCreditsConnector.fetchContributionsAndCredits(
        requestKey.benefitType,
        NiContributionsAndCreditsRequest(
          requestKey.nationalInsuranceNumber,
          contributionsAndCredits.dateOfBirth,
          window.startTaxYear,
          window.endTaxYear
        )
      )
    }.sequence
  }

  private[service] def fetchMarriageDetailsData(
      marriageDetails: Option[MarriageDetails]
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) =
    marriageDetailsConnector.fetchMarriageDetails(
      requestKey.benefitType,
      requestKey.nationalInsuranceNumber,
      marriageDetails.flatMap(_.searchStartYear),
      marriageDetails.flatMap(_.latest),
      None
    )

  private[service] def fetchBenefitSchemeMembershipDetailsData()(
      implicit headerCarrier: HeaderCarrier,
      requestKey: RequestKey
  ): EitherT[
    Future,
    BenefitEligibilityError,
    BenefitSchemeMembershipDetailsData
  ] = for {
    detailsResult <- schemeMembershipDetailsConnector.fetchSchemeMembershipDetails(
      benefitType = requestKey.benefitType,
      nationalInsuranceNumber = requestKey.nationalInsuranceNumber,
      sequenceNumber = None,
      transferSequenceNumber = None,
      occurrenceNumber = None
    )
    resultTuple <-
      detailsResult match {
        case NpsApiResult.FailureResult(apiName, result) =>
          EitherT.pure[Future, BenefitEligibilityError]((detailsResult, Nil))
        case NpsApiResult.SuccessResult(apiName, successResponse) =>
          successResponse.schemeMembershipDetailsSummaryList
            .map(_.flatMap(_.schemeMembershipDetails.employersContractedOutNumberDetails)) match {
            case Some(contractedOutNumberDetailsList) =>
              contractedOutNumberDetailsList
                .map { contractedOutNumberDetails =>
                  benefitSchemeDetailsConnector.fetchBenefitSchemeDetails(
                    requestKey.benefitType,
                    requestKey.nationalInsuranceNumber,
                    SchemeContractedOutNumberDetails(contractedOutNumberDetails.value)
                  )
                }
                .sequence
                .flatMap(i => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, i)))
            case None => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, Nil))
          }

      }

  } yield BenefitSchemeMembershipDetailsData(resultTuple._1, resultTuple._2)

  private[service] def fetchLongTermBenefitCalculationDetailsData(
      longTermBenefitCalculation: LongTermBenefitCalculation
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey): EitherT[
    Future,
    BenefitEligibilityError,
    LongTermBenefitCalculationDetailsData
  ] = for {
    longTermBenefitCalculationDetailsResult <- longTermBenefitCalculationDetailsConnector
      .fetchBenefitCalculationDetails(
        requestKey.benefitType,
        requestKey.nationalInsuranceNumber,
        None,
        longTermBenefitCalculation.longTermBenefitType,
        longTermBenefitCalculation.pensionProcessingArea
      )

    resultTuple <-
      longTermBenefitCalculationDetailsResult match {
        case NpsApiResult.FailureResult(_, _) =>
          EitherT.pure[Future, BenefitEligibilityError]((longTermBenefitCalculationDetailsResult, Nil))
        case NpsApiResult.SuccessResult(apiName, result) =>
          val longTermBenefitTypesAndSeqNumbers: List[(LongTermBenefitType, AssociatedCalculationSequenceNumber)] =
            result.benefitCalculationDetailsList.map(_.flatMap(_.benefitCalculationDetail.map { detail =>
              (detail.benefitType, detail.associatedCalculationSequenceNumber)
            })) match {
              case Some(value) => value
              case None        => Nil
            }

          longTermBenefitTypesAndSeqNumbers
            .map { case (longTermBenefitType, sequenceNumber) =>
              longTermBenefitNotesConnector.fetchLongTermBenefitNotes(
                requestKey.benefitType,
                requestKey.nationalInsuranceNumber,
                longTermBenefitType,
                sequenceNumber
              )
            }
            .sequence
            .flatMap(i => EitherT.pure[Future, BenefitEligibilityError]((longTermBenefitCalculationDetailsResult, i)))
      }
  } yield LongTermBenefitCalculationDetailsData(resultTuple._1, resultTuple._2)

  private[service] def fetchIndividualStatePensionInformation()(
      implicit headerCarrier: HeaderCarrier,
      requestKey: RequestKey
  ) =
    statePensionInformationConnector.fetchIndividualStatePensionInformation(
      requestKey.benefitType,
      requestKey.nationalInsuranceNumber
    )

}
