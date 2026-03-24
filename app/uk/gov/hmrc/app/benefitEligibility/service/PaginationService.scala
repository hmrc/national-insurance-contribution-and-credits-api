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

package uk.gov.hmrc.app.benefitEligibility.service

import cats.data.EitherT
import cats.implicits.*
import uk.gov.hmrc.app.benefitEligibility.connectors.*
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitEligibilityError, BenefitType, Identifier}
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.ErrorReport
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.SchemeContractedOutNumberDetails
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.util.{CurrentTimeSource, RequestAwareLogger}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaginationService @Inject() (
    liabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector,
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    marriageDetailsConnector: MarriageDetailsConnector,
    schemeMembershipDetailsConnector: SchemeMembershipDetailsConnector,
    benefitSchemeDetailsConnector: BenefitSchemeDetailsConnector,
    pageTaskRepo: BenefitEligibilityRepository,
    currentTime: CurrentTimeSource,
    uuidGenerator: UuidGenerator
)(implicit ec: ExecutionContext) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def addTask(pageTask: PageTask): EitherT[Future, BenefitEligibilityError, UUID] =
    pageTaskRepo.upsert(None, pageTask)

  def paginate(
      id: PageTaskId,
      nationalInsuranceNumber: Identifier
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginate has been called")

    for {
      existingPageTask <- pageTaskRepo.getItem(id.value)
      paginationResult <- existingPageTask match {
        case task: MaPageTask   => processMaPageTask(task)
        case task: BspPageTask  => processBspPageTask(task, nationalInsuranceNumber)
        case task: GyspPageTask => processGyspPageTask(task, nationalInsuranceNumber)
      }
      paginationResultWithCursor = paginationResult.setNextCursor(uuidGenerator.generate)
      _ <- PageTask
        .createPaginatingTask(paginationResultWithCursor, currentTime)
        .map(newPageTask => pageTaskRepo.upsert(Some(existingPageTask.pageTaskId.value), newPageTask))
        .sequence
    } yield paginationResultWithCursor
  }

  private[service] def processMaPageTask(
      maPageTask: MaPageTask
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for MA")
    maPageTask.liabilitiesPaging
      .flatMap(_.callBackURL)
      .map { callBackURL =>
        liabilitySummaryDetailsConnector
          .fetchData(BenefitType.from(maPageTask.paginationType), callBackURL)
      }
      .sequence
      .map { result =>
        PaginationResult(
          paginationType = maPageTask.paginationType,
          liabilitiesResult = result,
          contributionCreditResult = ContributionCreditPagingResult(None, None),
          marriageDetailsResult = None,
          benefitSchemeMembershipDetailsData = None,
          None
        )
      }
      .leftMap { error =>
        logger.error(s"Failed to process MA task with $error")
        error
      }
  }

  private[service] def processBspPageTask(bspPageTask: BspPageTask, nationalInsuranceNumber: Identifier)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for BSP")
    (
      marriageDetailsConnectorFetchData(bspPageTask.marriageDetailsPaging),
      creditsAndContributionsFetchData(
        BenefitType.from(bspPageTask.paginationType),
        nationalInsuranceNumber,
        bspPageTask.contributionAndCreditsPaging
      )
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult) =>
        PaginationResult(
          paginationType = bspPageTask.paginationType,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            bspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          liabilitiesResult = Nil,
          benefitSchemeMembershipDetailsData = None,
          None
        )
      }
      .leftMap { error =>
        logger.error(s"Failed to process BSP task with $error")
        error
      }
  }

  private[service] def processGyspPageTask(gyspPageTask: GyspPageTask, nationalInsuranceNumber: Identifier)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for GYSP")

    def fetchBenefitSchemeMembershipDetailsData(
        pageTask: GyspPageTask,
        nationalInsuranceNumber: Identifier
    )(
        implicit headerCarrier: HeaderCarrier
    ): EitherT[Future, BenefitEligibilityError, Option[BenefitSchemeMembershipDetailsData]] =
      pageTask.benefitSchemeMembershipDetailsPaging
        .map { page =>
          schemeMembershipDetailsConnector
            .fetchSchemeMembershipDetails(
              benefitType = BenefitType.from(pageTask.paginationType),
              nationalInsuranceNumber = nationalInsuranceNumber
            )
            .flatMap {
              case detailsResult @ NpsApiResult.FailureResult(apiName, result) =>
                EitherT.pure[Future, BenefitEligibilityError]((detailsResult, Nil))
              case detailsResult @ NpsApiResult.SuccessResult(apiName, successResponse) =>
                successResponse.schemeMembershipDetailsSummaryList
                  .map(_.flatMap(_.schemeMembershipDetails.employersContractedOutNumberDetails)) match {
                  case Some(contractedOutNumberDetailsList) =>
                    contractedOutNumberDetailsList
                      .map { contractedOutNumberDetails =>
                        benefitSchemeDetailsConnector.fetchBenefitSchemeDetails(
                          BenefitType.from(pageTask.paginationType),
                          nationalInsuranceNumber,
                          SchemeContractedOutNumberDetails(contractedOutNumberDetails.value)
                        )
                      }
                      .sequence
                      .flatMap(i => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, i)))
                  case None => EitherT.pure[Future, BenefitEligibilityError]((detailsResult, Nil))
                }

            }
        }
        .sequence
        .map(maybeTupledResult =>
          maybeTupledResult.map(tuple => BenefitSchemeMembershipDetailsData(tuple._1, tuple._2))
        )

    (
      marriageDetailsConnectorFetchData(gyspPageTask.marriageDetailsPaging),
      creditsAndContributionsFetchData(
        BenefitType.from(gyspPageTask.paginationType),
        nationalInsuranceNumber,
        gyspPageTask.contributionAndCreditsPaging
      ),
      fetchBenefitSchemeMembershipDetailsData(gyspPageTask, nationalInsuranceNumber)
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult, benefitSchemeMembershipDetailsData) =>
        PaginationResult(
          paginationType = gyspPageTask.paginationType,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            gyspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = benefitSchemeMembershipDetailsData,
          liabilitiesResult = Nil,
          None
        )
      }
      .leftMap { error =>
        logger.error(s"Failed to process GYSP task with $error")
        error
      }
  }

  private def marriageDetailsConnectorFetchData(marriageDetailsPaging: Option[PaginationSource])(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Option[MarriageDetailsResult]] = {
    logger.info("Marriage Details Connector called")
    marriageDetailsPaging
      .flatMap(_.callBackURL)
      .map(callBackURL => marriageDetailsConnector.fetchMarriageDetailsData(callBackURL))
      .sequence
  }

  private def creditsAndContributionsFetchData(
      benefitType: BenefitType,
      nationInsuranceNumber: Identifier,
      contributionAndCreditsPaging: Option[ContributionAndCreditsPaging]
  )(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Option[ContributionCreditResult]] = {
    logger.info("Contributions and Credits Connector called")
    contributionAndCreditsPaging.map { paging =>
      val taxWindow = paging.niContributionAndCreditsTaxWindows.head
      niContributionsAndCreditsConnector
        .fetchContributionsAndCredits(
          benefitType,
          NiContributionsAndCreditsRequest(
            nationInsuranceNumber,
            paging.dateOfBirth,
            taxWindow.startTaxYear,
            taxWindow.endTaxYear
          )
        )
    }.sequence
  }

}
