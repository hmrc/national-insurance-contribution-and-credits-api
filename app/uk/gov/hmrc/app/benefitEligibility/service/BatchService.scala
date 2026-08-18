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

class BatchService @Inject() (
                               liabilitySummaryDetailsConnector: LiabilitySummaryDetailsConnector,
                               niContributionsAndCreditsConnector: NiContributionsAndCreditsConnector,
                               marriageDetailsConnector: MarriageDetailsConnector,
                               schemeMembershipDetailsConnector: SchemeMembershipDetailsConnector,
                               benefitSchemeDetailsConnector: BenefitSchemeDetailsConnector,
                               batchRepository: BatchRepository,
                               currentTime: CurrentTimeSource,
                               uuidGenerator: UuidGenerator,
                               appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private val logger: RequestAwareLogger = new RequestAwareLogger(this.getClass)

  def addTask(
      batchDocument: BatchDocument
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID] = {
    def createNewBatch(batchDocument: BatchDocument) = {
      logger.info("Creating new batch")
      val newBatchDocument = batchDocument.copy(
        batchId = BatchId(uuidGenerator.generate)
      )
      addTask(newBatchDocument)
    }

    batchRepository.insert(batchDocument).recoverWith {
      case DatabaseError(dbError: com.mongodb.MongoWriteException)
          if dbError.getError.getCategory == com.mongodb.ErrorCategory.DUPLICATE_KEY =>
        logger.warn("MongoWriteException: Duplicate key error")
        createNewBatch(batchDocument)
      case DatabaseError(dbError: com.mongodb.DuplicateKeyException) =>
        logger.warn("DuplicateKeyException: Duplicate key error")
        createNewBatch(batchDocument)
      case error =>
        EitherT.leftT(error)
    }
  }

  def processBatch(
      batchId: BatchId
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, BatchResult] =
    for {
      existingBatchDocument <- batchRepository.get(batchId)
      batchResult <- existingBatchDocument.data.as[Batch] match {
        case task: MaBatch if appConfig.maEnabled =>
          logger.info("processing MaBatch")
          processMaBatch(existingBatchDocument.correlationId, task)
        case task: BspBatch if appConfig.bspEnabled =>
          logger.info("processing BspBatch")
          processBspBatch(existingBatchDocument.correlationId, task)
        case task: GyspBatch if appConfig.gyspEnabled =>
          logger.info("processing GyspBatch")
          processGyspBatch(existingBatchDocument.correlationId, task)
        case task: SearchLightBatch if appConfig.searchlightEnabled =>
          logger.info("processing SearchLightBatch")
          processSearchlightBatch(existingBatchDocument.correlationId, task)
        case task: SearchLightBatch if !appConfig.searchlightEnabled =>
          EitherT.left[BatchResult](
            Future.successful(FeatureDisabled("feature disabled: SEARCHLIGHT"))
          )
        case task: Batch =>
          EitherT.left[BatchResult](
            Future.successful(FeatureDisabled(s"feature disabled: ${task.batchType.entryName}"))
          )
      }
      batchDocument = Batch.createBatchDocument(batchResult, currentTime)

      _ <- batchDocument
        .fold(batchRepository.delete(existingBatchDocument.batchId.value).map(_ => ()))(newBatchDoc =>
          batchRepository.upsert(Some(existingBatchDocument.batchId.value), newBatchDoc)
        )
        .map(_ => ())
    } yield batchResult

  private[service] def processMaBatch(
      correlationId: CorrelationId,
      maBatch: MaBatch
  )(implicit headerCarrier: HeaderCarrier): EitherT[Future, BenefitEligibilityError, BatchResult] = {
    logger.info("Batching for MA")
    maBatch.liabilityDetails
      .map { batchSource =>
        liabilitySummaryDetailsConnector
          .fetchData(BenefitType.from(maBatch.batchType), batchSource.callBackURL)
      }
      .sequence
      .map { liabilityResult =>
        BatchResult(
          correlationId = correlationId,
          batchType = maBatch.batchType,
          nationalInsuranceNumber = maBatch.nationalInsuranceNumber,
          liabilitiesResult = liabilityResult,
          contributionCreditResult = BatchWithTaxWindowsResult(None, None),
          marriageDetailsResult = None,
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = None
        ).setBatchId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process MA task", error)
        error
      }
  }

  private[service] def processBspBatch(correlationId: CorrelationId, bspBatch: BspBatch)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, BatchResult] = {
    logger.info("Batching for BSP")
    (
      marriageDetailsConnectorFetchData(
        BenefitType.from(bspBatch.batchType),
        bspBatch.marriageDetails
      ),
      fetchContributionsAndCreditsData(
        BenefitType.from(bspBatch.batchType),
        bspBatch.nationalInsuranceNumber,
        bspBatch.contributionsAndCredits
      )
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult) =>
        BatchResult(
          correlationId = correlationId,
          batchType = bspBatch.batchType,
          liabilitiesResult = Nil,
          nationalInsuranceNumber = bspBatch.nationalInsuranceNumber,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = BatchWithTaxWindowsResult(
            contributionCreditResult,
            bspBatch.contributionsAndCredits.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = None,
          batchId = None
        ).setBatchId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process BSP task", error)
        error
      }
  }

  private[service] def processSearchlightBatch(
      correlationId: CorrelationId,
      searchLightBatch: SearchLightBatch
  )(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, BatchResult] = {
    logger.info("Batching for BSP")

    fetchContributionsAndCreditsData(
      BenefitType.from(searchLightBatch.batchType),
      searchLightBatch.nationalInsuranceNumber,
      searchLightBatch.contributionsAndCredits
    )
      .map { contributionCreditResult =>
        BatchResult(
          correlationId = correlationId,
          batchType = searchLightBatch.batchType,
          liabilitiesResult = Nil,
          nationalInsuranceNumber = searchLightBatch.nationalInsuranceNumber,
          marriageDetailsResult = None,
          contributionCreditResult = BatchWithTaxWindowsResult(
            contributionCreditResult,
            searchLightBatch.contributionsAndCredits.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = None,
          callSystem = Some(SEARCHLIGHT),
          batchId = None
        ).setBatchId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process ${searchLightBatch.batchType} searchlight task", error)
        error
      }
  }

  private[service] def processGyspBatch(correlationId: CorrelationId, gyspBatch: GyspBatch)(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, BatchResult] = {
    logger.info("Batching for GYSP")

    def fetchBenefitSchemeMembershipDetailsData(
        batch: GyspBatch
    )(
        implicit headerCarrier: HeaderCarrier
    ): EitherT[Future, BenefitEligibilityError, Option[BenefitSchemeMembershipDetailsData]] =
      batch.benefitSchemeMembershipDetails
        .map { batchSource =>
          schemeMembershipDetailsConnector
            .fetchData(
              benefitType = BenefitType.from(batch.batchType),
              path = batchSource.callBackURL
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
                          BenefitType.from(batch.batchType),
                          batch.nationalInsuranceNumber,
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
        BenefitType.from(gyspBatch.batchType),
        gyspBatch.marriageDetails
      ),
      fetchContributionsAndCreditsData(
        BenefitType.from(gyspBatch.batchType),
        gyspBatch.nationalInsuranceNumber,
        gyspBatch.contributionsAndCredits
      ),
      fetchBenefitSchemeMembershipDetailsData(gyspBatch)
    ).parTupled
      .map { case (marriageDetailsResult, contributionCreditResult, benefitSchemeMembershipDetailsData) =>
        BatchResult(
          correlationId = correlationId,
          batchType = gyspBatch.batchType,
          gyspBatch.nationalInsuranceNumber,
          liabilitiesResult = Nil,
          marriageDetailsResult = marriageDetailsResult,
          contributionCreditResult = BatchWithTaxWindowsResult(
            contributionCreditResult,
            gyspBatch.contributionsAndCredits.flatMap(_.tail)
          ),
          benefitSchemeMembershipDetailsData = benefitSchemeMembershipDetailsData,
          callSystem = None,
          batchId = None
        ).setBatchId(uuidGenerator.generate)
      }
      .leftMap { error =>
        logger.error(s"Failed to process GYSP task", error)
        error
      }
  }

  private def marriageDetailsConnectorFetchData(
      benefitType: BenefitType,
      marriageDetailsBatchWithCallback: Option[BatchWithCallback]
  )(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Option[MarriageDetailsResult]] = {
    logger.info("Marriage Details Connector called")
    marriageDetailsBatchWithCallback
      .map(batchSource =>
        marriageDetailsConnector.fetchMarriageDetailsData(benefitType, batchSource.callBackURL)
      )
      .sequence
  }

  private def fetchContributionsAndCreditsData(
      benefitType: BenefitType,
      nationInsuranceNumber: Identifier,
      batchWithTaxWindows: Option[BatchWithTaxWindows]
  )(
      implicit headerCarrier: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, Option[ContributionCreditResult]] = {
    logger.info("Contributions and Credits Connector called")
    batchWithTaxWindows.map { batching =>
      val taxWindow = batching.taxWindows.head
      niContributionsAndCreditsConnector
        .fetchContributionsAndCredits(
          benefitType,
          NiContributionsAndCreditsRequest(
            nationInsuranceNumber,
            batching.dateOfBirth,
            taxWindow.startTaxYear,
            taxWindow.endTaxYear
          )
        )
    }.sequence
  }

}
