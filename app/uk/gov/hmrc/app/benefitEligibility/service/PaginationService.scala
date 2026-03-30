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
import uk.gov.hmrc.app.benefitEligibility.model.common.CallSystem.SEARCHLIGHT
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitEligibilityError, BenefitType, DatabaseError, Identifier}
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
    class2MAReceiptsConnector: Class2MAReceiptsConnector,
    niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
    marriageDetailsConnector: MarriageDetailsConnector,
    schemeMembershipDetailsConnector: SchemeMembershipDetailsConnector,
    benefitSchemeDetailsConnector: BenefitSchemeDetailsConnector,
    pageTaskRepo: BenefitEligibilityRepository,
    currentTime: CurrentTimeSource,
    uuidGenerator: UuidGenerator
)(implicit ec: ExecutionContext) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def addTask(pageTask: PageTask): EitherT[Future, BenefitEligibilityError, UUID] = {
    def createNewPageTask(pageTask: PageTask) = {
      val newPageTask: PageTask = pageTask match {
        case m: MaPageTask =>
          MaPageTask(
            m.correlationId,
            PageTaskId(uuidGenerator.generate),
            m.liabilitiesPaging,
            m.class2MaReceipts,
            m.nationalInsuranceNumber,
            m.createdAt
          )
        case b: BspPageTask =>
          BspPageTask(
            b.correlationId,
            PageTaskId(uuidGenerator.generate),
            b.marriageDetailsPaging,
            b.contributionAndCreditsPaging,
            b.nationalInsuranceNumber,
            b.createdAt
          )
        case g: GyspPageTask =>
          GyspPageTask(
            g.correlationId,
            PageTaskId(uuidGenerator.generate),
            g.benefitSchemeMembershipDetailsPaging,
            g.marriageDetailsPaging,
            g.contributionAndCreditsPaging,
            g.nationalInsuranceNumber,
            g.createdAt
          )
        case s: SearchLightPageTask =>
          SearchLightPageTask(
            s.correlationId,
            PageTaskId(uuidGenerator.generate),
            s.paginationType,
            s.contributionAndCreditsPaging,
            s.nationalInsuranceNumber,
            s.createdAt
          )
      }
      addTask(newPageTask)
    }

    pageTaskRepo.insert(pageTask).recoverWith {
      case DatabaseError(dbError: com.mongodb.MongoWriteException)
          if dbError.getError.getCategory == com.mongodb.ErrorCategory.DUPLICATE_KEY =>
        createNewPageTask(pageTask)
      case DatabaseError(dbError: com.mongodb.DuplicateKeyException) =>
        createNewPageTask(pageTask)
      case error =>
        EitherT.leftT(error)
    }
  }

  def paginate(
      paginationCursor: PaginationCursor
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginate has been called")

    for {
      existingPageTask <- pageTaskRepo.getItem(paginationCursor)
      paginationResult <- existingPageTask match {
        case task: MaPageTask          => processMaPageTask(task)
        case task: BspPageTask         => processBspPageTask(task)
        case task: GyspPageTask        => processGyspPageTask(task)
        case task: SearchLightPageTask => processSearchlightPageTask(task)
      }
      pageTask = PageTask.createPaginatingTask(paginationResult, currentTime)
      _ <- pageTask.fold(pageTaskRepo.delete(existingPageTask.pageTaskId.value).map(_ => ()))(newPageTask =>
        pageTaskRepo.upsert(Some(existingPageTask.pageTaskId.value), newPageTask).map(_ => ())
      )
    } yield paginationResult
  }

  private[service] def processMaPageTask(
      maPageTask: MaPageTask
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for MA")
    (
      maPageTask.liabilitiesPaging.map { pageSource =>
        liabilitySummaryDetailsConnector
          .fetchData(BenefitType.from(maPageTask.paginationType), pageSource.callBackURL)
      }.sequence,
      maPageTask.class2MaReceipts.map { pageSource =>
        class2MAReceiptsConnector
          .fetchData(BenefitType.from(maPageTask.paginationType), pageSource.callBackURL)
      }.sequence
    ).parTupled
      .map { case (liabilityResult, class2MaReceiptsResult) =>
        PaginationResult(
          correlationId = maPageTask.correlationId,
          paginationType = maPageTask.paginationType,
          nationalInsuranceNumber = maPageTask.nationalInsuranceNumber,
          liabilitiesResult = liabilityResult,
          class2MaReceiptsResult = class2MaReceiptsResult,
          contributionCreditResult = ContributionCreditPagingResult(None, None),
          marriageDetailsResult = None,
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          nextCursor = None
        ).setNextCursor(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process MA task with $error")
        error
      }
  }

  private[service] def processBspPageTask(bspPageTask: BspPageTask)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for BSP")
    (
      marriageDetailsConnectorFetchData(bspPageTask.marriageDetailsPaging),
      fetchContributionsAndCreditsData(
        BenefitType.from(bspPageTask.paginationType),
        bspPageTask.nationalInsuranceNumber,
        bspPageTask.contributionAndCreditsPaging
      )
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult) =>
        PaginationResult(
          correlationId = bspPageTask.correlationId,
          paginationType = bspPageTask.paginationType,
          liabilitiesResult = Nil,
          None,
          nationalInsuranceNumber = bspPageTask.nationalInsuranceNumber,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            bspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          nextCursor = None
        ).setNextCursor(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process BSP task with $error")
        error
      }
  }

  private[service] def processSearchlightPageTask(searchLightPageTask: SearchLightPageTask)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for BSP")

    fetchContributionsAndCreditsData(
      BenefitType.from(searchLightPageTask.paginationType),
      searchLightPageTask.nationalInsuranceNumber,
      searchLightPageTask.contributionAndCreditsPaging
    )
      .map { contributionCreditResult =>
        PaginationResult(
          correlationId = searchLightPageTask.correlationId,
          paginationType = searchLightPageTask.paginationType,
          liabilitiesResult = Nil,
          None,
          nationalInsuranceNumber = searchLightPageTask.nationalInsuranceNumber,
          marriageDetailsResult = None,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            searchLightPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = Some(SEARCHLIGHT),
          nextCursor = None
        ).setNextCursor(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process ${searchLightPageTask.paginationType} searchlight task with $error")
        error
      }
  }

  private[service] def processGyspPageTask(gyspPageTask: GyspPageTask)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for GYSP")

    def fetchBenefitSchemeMembershipDetailsData(
        pageTask: GyspPageTask
    )(
        implicit headerCarrier: HeaderCarrier
    ): EitherT[Future, BenefitEligibilityError, Option[BenefitSchemeMembershipDetailsData]] =
      pageTask.benefitSchemeMembershipDetailsPaging
        .map { page =>
          schemeMembershipDetailsConnector
            .fetchData(
              benefitType = BenefitType.from(pageTask.paginationType),
              path = page.callBackURL
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
                          pageTask.nationalInsuranceNumber,
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
      fetchContributionsAndCreditsData(
        BenefitType.from(gyspPageTask.paginationType),
        gyspPageTask.nationalInsuranceNumber,
        gyspPageTask.contributionAndCreditsPaging
      ),
      fetchBenefitSchemeMembershipDetailsData(gyspPageTask)
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult, benefitSchemeMembershipDetailsData) =>
        PaginationResult(
          correlationId = gyspPageTask.correlationId,
          paginationType = gyspPageTask.paginationType,
          gyspPageTask.nationalInsuranceNumber,
          liabilitiesResult = Nil,
          None,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            gyspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = benefitSchemeMembershipDetailsData,
          callSystem = None,
          nextCursor = None
        ).setNextCursor(uuidGenerator.generate)
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
      .map(paginationSource => marriageDetailsConnector.fetchMarriageDetailsData(paginationSource.callBackURL))
      .sequence
  }

  private def fetchContributionsAndCreditsData(
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
