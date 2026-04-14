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
import uk.gov.hmrc.app.benefitEligibility.model.nps.EligibilityCheckDataResult.{
  EligibilityCheckDataResultBSP,
  EligibilityCheckDataResultGYSP
}
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
  )(
      implicit hc: HeaderCarrier,
      correlationId: CorrelationId
  ): EitherT[Future, BenefitEligibilityError, EligibilityCheckDataResultGYSP] = {

    implicit val requestKey: RequestKey =
      RequestKey(eligibilityCheckDataRequest.benefitType, eligibilityCheckDataRequest.nationalInsuranceNumber)
    val maybeTaxWindows = ContributionCreditTaxWindowCalculator.createTaxWindows(
      eligibilityCheckDataRequest.niContributionsAndCredits.startTaxYear,
      eligibilityCheckDataRequest.niContributionsAndCredits.endTaxYear
    )

    maybeTaxWindows match {
      case Left(error) => EitherT.leftT[Future, EligibilityCheckDataResultGYSP](error)
      case Right(taxWindows) =>
        (
          fetchNiContributionsAndCreditsData(
            eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth,
            taxWindows.head.startTaxYear,
            taxWindows.head.endTaxYear
          ),
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
                    .map(url => PaginationSource(MarriageDetails, url.value))
                )

                val benefitSchemeDetailsPaginate =
                  benefitSchemeMembershipDetailsData.schemeMembershipDetailsResult.getSuccess.flatMap(
                    _.callback.flatMap(_.callbackURL).map(url => PaginationSource(SchemeMembershipDetails, url.value))
                  )

                val niContributionsCreditsPaginate = taxWindows.toList.safeTailNel.map { remainingWindows =>
                  ContributionAndCreditsPaging(
                    remainingWindows,
                    eligibilityCheckDataRequest.niContributionsAndCredits.dateOfBirth
                  )
                }

                paginationService
                  .addTask(
                    GyspPageTask(
                      correlationId,
                      PageTaskId(uuidGenerator.generate),
                      benefitSchemeMembershipDetailsPaging = benefitSchemeDetailsPaginate,
                      marriageDetailsPaging = marriageDetailsPaginate,
                      contributionAndCreditsPaging = niContributionsCreditsPaginate,
                      eligibilityCheckDataRequest.nationalInsuranceNumber,
                      currentTimeSource.instantNow()
                    )
                  )
                  .map(id =>
                    result.copy(nextCursor = Some(PaginationCursor(PaginationType.GyspPagination, PageTaskId(id))))
                  )

              } else EitherT.rightT[Future, BenefitEligibilityError](result)

          }

    }
  }.leftMap {
    case error @ DataRetrievalServiceError(_) => error
    case error                                => DataRetrievalServiceError(List(error))
  }

  private[service] def fetchNiContributionsAndCreditsData(
      dateOfBirth: DateOfBirth,
      startTaxYear: StartTaxYear,
      endTaxYear: EndTaxYear
  )(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) =

    niContributionsAndCreditsConnector.fetchContributionsAndCredits(
      requestKey.benefitType,
      NiContributionsAndCreditsRequest(
        requestKey.nationalInsuranceNumber,
        dateOfBirth,
        startTaxYear,
        endTaxYear
      )
    )

  private[service] def fetchMarriageDetailsData(implicit headerCarrier: HeaderCarrier, requestKey: RequestKey) =
    marriageDetailsConnector.fetchMarriageDetails(
      requestKey.benefitType,
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
            .map(_.flatMap { summary =>
              List(
                summary.schemeMembershipDetails.schemeTerminatingContractedOutNumberDetails.map(_.value),
                summary.schemeMembershipDetails.schemeCreatingContractedOutNumberDetails.map(_.value)
              ).flatten.distinct
            }) match {
            case Some(contractedOutNumberDetailsList) =>
              contractedOutNumberDetailsList
                .map { contractedOutNumberDetails =>
                  benefitSchemeDetailsConnector.fetchBenefitSchemeDetails(
                    requestKey.benefitType,
                    requestKey.nationalInsuranceNumber,
                    SchemeContractedOutNumberDetails(contractedOutNumberDetails)
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
