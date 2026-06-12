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
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  BenefitType,
  CorrelationId,
  DatabaseError,
  FeatureDisabled,
  Identifier
}
import uk.gov.hmrc.app.benefitEligibility.model.nps.*
import uk.gov.hmrc.app.benefitEligibility.model.nps.NpsApiResult.ErrorReport
import uk.gov.hmrc.app.benefitEligibility.model.nps.benefitSchemeDetails.BenefitSchemeDetailsSuccess.SchemeContractedOutNumberDetails
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsRequest
import uk.gov.hmrc.app.benefitEligibility.model.nps.schemeMembershipDetails.SchemeMembershipDetailsSuccess.SchemeMembershipDetailsSuccessResponse
import uk.gov.hmrc.app.benefitEligibility.repository.*
import uk.gov.hmrc.app.benefitEligibility.util.{CurrentTimeSource, RequestAwareLogger}
import uk.gov.hmrc.app.config.AppConfig
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
    uuidGenerator: UuidGenerator,
    appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def addTask(
      pageTaskDocument: PageTaskDocument
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID] = {
    def createNewPageTask(pageTaskDocument: PageTaskDocument) = {
      logger.info("Creating new page task")
      val newPageTaskDocument = pageTaskDocument.copy(
        pageTaskId = PageTaskId(uuidGenerator.generate)
      )
      addTask(newPageTaskDocument)
    }

    pageTaskRepo.insert(pageTaskDocument).recoverWith {
      case DatabaseError(dbError: com.mongodb.MongoWriteException)
          if dbError.getError.getCategory == com.mongodb.ErrorCategory.DUPLICATE_KEY =>
        logger.warn("MongoWriteException: Duplicate key error")
        createNewPageTask(pageTaskDocument)
      case DatabaseError(dbError: com.mongodb.DuplicateKeyException) =>
        logger.warn("DuplicateKeyException: Duplicate key error")
        createNewPageTask(pageTaskDocument)
      case error =>
        EitherT.leftT(error)
    }
  }

  def paginate(
      pageTaskId: PageTaskId
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] =
    for {
      existingPageTaskDocument <- pageTaskRepo.getItem(pageTaskId)
      paginationResult <- existingPageTaskDocument.data.as[PageTask] match {
        case task: MaPageTask if appConfig.maEnabled =>
          logger.info("processing MaPageTask")
          processMaPageTask(existingPageTaskDocument.correlationId, task)
        case task: BspPageTask if appConfig.bspEnabled =>
          logger.info("processing BspPageTask")
          processBspPageTask(existingPageTaskDocument.correlationId, task)
        case task: GyspPageTask if appConfig.gyspEnabled =>
          logger.info("processing GyspPageTask")
          processGyspPageTask(existingPageTaskDocument.correlationId, task)
        case task: SearchLightPageTask if appConfig.searchlightEnabled =>
          logger.info("processing SearchLightPageTask")
          processSearchlightPageTask(existingPageTaskDocument.correlationId, task)
        case task: SearchLightPageTask if !appConfig.searchlightEnabled =>
          EitherT.left[PaginationResult](
            Future.successful(FeatureDisabled("feature disabled: SEARCHLIGHT"))
          )
        case task: PageTask =>
          EitherT.left[PaginationResult](
            Future.successful(FeatureDisabled(s"feature disabled: ${task.paginationType.entryName}"))
          )
      }
      pageTaskDoc = PageTask.createPageTaskDocument(paginationResult, currentTime)

      _ <- pageTaskDoc
        .fold(pageTaskRepo.delete(existingPageTaskDocument.pageTaskId.value).map(_ => ()))(newPageTaskDoc =>
          pageTaskRepo.upsert(Some(existingPageTaskDocument.pageTaskId.value), newPageTaskDoc)
        )
        .map(_ => ())
    } yield paginationResult

  private[service] def processMaPageTask(
      correlationId: CorrelationId,
      maPageTask: MaPageTask
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for MA")
    maPageTask.liabilitiesPaging
      .map { pageSource =>
        liabilitySummaryDetailsConnector
          .fetchData(BenefitType.from(maPageTask.paginationType), pageSource.callBackURL)
      }
      .sequence
      .map { liabilityResult =>
        PaginationResult(
          correlationId = correlationId,
          paginationType = maPageTask.paginationType,
          nationalInsuranceNumber = maPageTask.nationalInsuranceNumber,
          liabilitiesResult = liabilityResult,
          contributionCreditResult = ContributionCreditPagingResult(None, None),
          marriageDetailsResult = None,
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          pageTaskId = None
        ).setPageTaskId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process MA task", error)
        error
      }
  }

  private[service] def processBspPageTask(correlationId: CorrelationId, bspPageTask: BspPageTask)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PaginationResult] = {
    logger.info("Paginating for BSP")
    (
      marriageDetailsConnectorFetchData(
        BenefitType.from(bspPageTask.paginationType),
        bspPageTask.marriageDetailsPaging
      ),
      fetchContributionsAndCreditsData(
        BenefitType.from(bspPageTask.paginationType),
        bspPageTask.nationalInsuranceNumber,
        bspPageTask.contributionAndCreditsPaging
      )
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult) =>
        PaginationResult(
          correlationId = correlationId,
          paginationType = bspPageTask.paginationType,
          liabilitiesResult = Nil,
          nationalInsuranceNumber = bspPageTask.nationalInsuranceNumber,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            bspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          pageTaskId = None
        ).setPageTaskId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process BSP task", error)
        error
      }
  }

  private[service] def processSearchlightPageTask(
      correlationId: CorrelationId,
      searchLightPageTask: SearchLightPageTask
  )(
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
          correlationId = correlationId,
          paginationType = searchLightPageTask.paginationType,
          liabilitiesResult = Nil,
          nationalInsuranceNumber = searchLightPageTask.nationalInsuranceNumber,
          marriageDetailsResult = None,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            searchLightPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = Some(SEARCHLIGHT),
          pageTaskId = None
        ).setPageTaskId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process ${searchLightPageTask.paginationType} searchlight task", error)
        error
      }
  }

  private[service] def processGyspPageTask(correlationId: CorrelationId, gyspPageTask: GyspPageTask)(
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
      marriageDetailsConnectorFetchData(
        BenefitType.from(gyspPageTask.paginationType),
        gyspPageTask.marriageDetailsPaging
      ),
      fetchContributionsAndCreditsData(
        BenefitType.from(gyspPageTask.paginationType),
        gyspPageTask.nationalInsuranceNumber,
        gyspPageTask.contributionAndCreditsPaging
      ),
      fetchBenefitSchemeMembershipDetailsData(gyspPageTask)
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult, benefitSchemeMembershipDetailsData) =>
        PaginationResult(
          correlationId = correlationId,
          paginationType = gyspPageTask.paginationType,
          gyspPageTask.nationalInsuranceNumber,
          liabilitiesResult = Nil,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = ContributionCreditPagingResult(
            contributionCreditResult,
            gyspPageTask.contributionAndCreditsPaging.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = benefitSchemeMembershipDetailsData,
          callSystem = None,
          pageTaskId = None
        ).setPageTaskId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process GYSP task", error)
        error
      }
  }

  private def marriageDetailsConnectorFetchData(
      benefitType: BenefitType,
      marriageDetailsPaging: Option[PaginationSource]
  )(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Option[MarriageDetailsResult]] = {
    logger.info("Marriage Details Connector called")
    marriageDetailsPaging
      .map(paginationSource =>
        marriageDetailsConnector.fetchMarriageDetailsData(benefitType, paginationSource.callBackURL)
      )
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
