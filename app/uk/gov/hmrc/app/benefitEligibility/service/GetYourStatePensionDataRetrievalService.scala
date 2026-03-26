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
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.EligibilityCheckDataResultGYSP
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.ErrorReport
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.SchemeContractedOutNumberDetails
import uk.gov.hmrc.app.benefitEligibility.model.nps.longTermBenefitCalculationDetails.BenefitCalculationDetailsSuccess.LongTermBenefitCalculationDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.connectors.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{MarriageDetails, SchemeMembershipDetails}
import uk.gov.hmrc.app.benefitEligibility.model.common.BenefitEligibilityError.benefitEligibilityErrorSemiGroup
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.{
  BenefitSchemeDetailsResult,
  LongTermBenefitCalculationDetailsResult,
  LongTermBenefitNotesResult,
  NpsApiResult,
  SchemeMembershipDetailsResult
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.request.EligibilityCheckDataRequestParams.{
  ContributionsAndCreditsRequestParams,
  LongTermBenefitCalculationRequestParams
}
import uk.gov.hmrc.app.benefitEligibility.model.request.GYSPEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.repository.{
  ContributionAndCreditsPaging,
  GyspPageTask,
  PageTaskId,
  PaginationCursor,
  PaginationSource
}
import uk.gov.hmrc.app.benefitEligibility.util.ContributionCreditTaxWindowCalculator
import uk.gov.hmrc.app.benefitEligibility.util.implicits.ListImplicits.ListSyntax
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalDateTime}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource

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
    statePensionInformationConnector: IndividualStatePensionInformationConnector,
    paginationService: PaginationService,
    uuidGenerator: UuidGenerator,
    currentTimeSource: CurrentTimeSource
)(implicit ec: ExecutionContext) {

  def fetchEligibilityData(
      eligibilityCheckDataRequest: GYSPEligibilityCheckDataRequest
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultGYSP] = {

    implicit val requestKey: RequestKey =
      RequestKey(eligibilityCheckDataRequest.benefitType, eligibilityCheckDataRequest.nationalInsuranceNumber)

    (
      fetchNiContributionsAndCreditsData(eligibilityCheckDataRequest.niContributionsAndCredits),
      fetchMarriageDetailsData(),
      fetchBenefitSchemeMembershipDetailsData(),
      fetchLongTermBenefitCalculationDetailsData(eligibilityCheckDataRequest.longTermBenefitCalculation),
      fetchIndividualStatePensionInformation()
    ).parTupled
      .flatMap {
        case (
              contributionsAndCreditResult,
              marriageDetailsResult,
              benefitSchemeMembershipDetailsData,
              longTermBenefitCalculationDetailsData,
              individualStatePensionResult
            ) =>
          val result = EligibilityCheckDataResultGYSP(
            contributionsAndCreditResult,
            benefitSchemeMembershipDetailsData,
            longTermBenefitCalculationDetailsData,
            marriageDetailsResult,
            individualStatePensionResult,
            None
          )
          val taxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
            eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
            eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
          )

          val shouldPage =
            if (result.allResults.exists(_.isFailure)) false
            else {
              marriageDetailsResult.getSuccess.get.marriageDetails._links.isDefined ||
              benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.get.callback.isDefined || taxWindows.length > 1
            }

          if (shouldPage) {
            val marriageDetailsPaginate = marriageDetailsResult.getSuccess.flatMap(
              _.marriageDetails._links
                .flatMap(_.self.href)
                .map(url => PaginationSource(MarriageDetails, Some(url.value)))
            )

            val benefitSchemeDetailsPaginate =
              benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.flatMap(
                _.callback.flatMap(_.callbackURL).map(url => PaginationSource(SchemeMembershipDetails, Some(url.value)))
              )

            val niContributionsCreditsPaginate = taxWindows.safeTailNel.map { remainingWindows =>
              ContributionAndCreditsPaging(
                remainingWindows,
                eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth
              )
            }

            paginationService
              .addTask(
                GyspPageTask(
                  PageTaskId(uuidGenerator.generate),
                  benefitSchemeMembershipDetailsPaging = benefitSchemeDetailsPaginate,
                  marriageDetailsPaging = marriageDetailsPaginate,
                  contributionAndCreditsPaging = niContributionsCreditsPaginate,
                  eligibilityCheckDataRequest.nationalInsuranceNumber,
                  currentTimeSource.instantNow()
                )
              )
              .map(id => result.copy(nextCursor = Some(PaginationCursor(PaginationType.GYSP, PageTaskId(id)))))

          } else EitherT.rightT[Future, BenefitEligibilityError](result)
      }

  }
    .leftMap { error =>
      // TODO add logging
      DataRetrievalServiceError()
    }

  // TODO - generate aggregation/paginationId
  private[service] def fetchNiContributionsAndCreditsData(
      contributionsAndCredits: ContributionsAndCreditsRequestParams
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) = {

    val window = ContributionCreditTaxWindowCalculator
      .createTaxWindows(
        contributionsAndCredits.startTaxYear,
        contributionsAndCredits.endTaxYear
      )
      .head

    // taxWindows.map { window =>
    niContributionsAndCreditsConnector.fetchContributionsAndCredits(
      requestKey.benefitType,
      NiContributionsAndCreditsRequest(
        requestKey.nationalInsuranceNumber,
        contributionsAndCredits.dateOfBirth,
        window.startTaxYear,
        window.endTaxYear
      )
    )
    //  }.sequence
  }

  private[service] def fetchMarriageDetailsData(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) =
    marriageDetailsConnector.fetchMarriageDetails(
      requestKey.nationalInsuranceNumber
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
      nationalInsuranceNumber = requestKey.nationalInsuranceNumber
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
      longTermBenefitCalculation: Option[LongTermBenefitCalculationRequestParams]
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey): EitherT[
    Future,
    BenefitEligibilityError,
    LongTermBenefitCalculationDetailsData
  ] = for {
    longTermBenefitCalculationDetailsResult <- longTermBenefitCalculationDetailsConnector
      .fetchBenefitCalculationDetails(
        requestKey.benefitType,
        requestKey.nationalInsuranceNumber,
        longTermBenefitCalculation.flatMap(_.longTermBenefitType),
        longTermBenefitCalculation.flatMap(_.pensionProcessingArea)
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
