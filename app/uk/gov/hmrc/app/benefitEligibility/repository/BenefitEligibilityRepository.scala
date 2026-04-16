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

package uk.gov.hmrc.app.benefitEligibility.repository

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeError
import com.google.inject.{ImplementedBy, Inject}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.*
import uk.gov.hmrc.app.benefitEligibility.model.common.{
  BenefitEligibilityError,
  CursorId,
  DatabaseError,
  RecordNotFound
}
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BenefitEligibilityRepositoryImpl])
trait BenefitEligibilityRepository {

  def getItem(paginationCursor: PaginationCursor)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PageTask]

  def upsert(id: Option[UUID], update: PageTask)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID]

  def insert(pageTask: PageTask)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID]
  def delete(id: UUID)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Long]
}

@Singleton
class BenefitEligibilityRepositoryImpl @Inject() (mongoComponent: MongoComponent)(
    implicit ec: ExecutionContext
) extends PlayMongoRepository[PageTask](
      collectionName = "page-tasks",
      mongoComponent = mongoComponent,
      domainFormat = PageTask.pageTaskFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("pageTaskId"), IndexOptions().unique(true)),
        IndexModel(
          Indexes.ascending("createdAt"),
          IndexOptions()
            .expireAfter(5000, TimeUnit.SECONDS)
            .unique(false)
        )
      ),
      replaceIndexes = true
    )
    with BenefitEligibilityRepository {

  private val logger = new RequestAwareLogger(this.getClass)

  def getItem(
      paginationCursor: PaginationCursor
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PageTask] = {
    logger.info("getItem called - Retrieving page task from Database ")
    collection
      .find(Filters.equal("pageTaskId", Codecs.toBson(paginationCursor.pageTaskId)))
      .headOption()
      .attemptT
      .leftMap(error => DatabaseError(error))
      .flatMap {
        case None           => EitherT.leftT(RecordNotFound(CursorId.from(paginationCursor)))
        case Some(pageTask) => EitherT.rightT(pageTask)
      }
  }

  def upsert(existingPageTaskId: Option[UUID], pageTask: PageTask)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("Upsert called - Updating page task in database")
    val updates = pageTask match {
      case MaPageTask(
            correlationId,
            id,
            paginationType,
            liabilitiesPaging,
            class2MaReceipts,
            nationalInsuranceNumber,
            createdAt
          ) =>
        Updates.combine(
          Updates.set("correlationId", Codecs.toBson(correlationId)),
          Updates.set("pageTaskId", Codecs.toBson(id)),
          Updates.set("nationalInsuranceNumber", Codecs.toBson(nationalInsuranceNumber)),
          Updates.set("liabilitiesPaging", Codecs.toBson(liabilitiesPaging)),
          Updates.set("class2MaReceipts", Codecs.toBson(class2MaReceipts)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
      case BspPageTask(
            correlationId,
            id,
            paginationType,
            marriageDetailsPaging,
            contributionAndCreditsPaging,
            nationalInsuranceNumber,
            createdAt
          ) =>
        Updates.combine(
          Updates.set("correlationId", Codecs.toBson(correlationId)),
          Updates.set("pageTaskId", Codecs.toBson(id)),
          Updates.set("nationalInsuranceNumber", Codecs.toBson(nationalInsuranceNumber)),
          Updates.set("marriageDetailsPaging", Codecs.toBson(marriageDetailsPaging)),
          Updates.set("contributionAndCreditsPaging", Codecs.toBson(contributionAndCreditsPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
      case GyspPageTask(
            correlationId,
            id,
            paginationType,
            benefitSchemeMembershipDetailsPaging,
            marriageDetailsPaging,
            contributionAndCreditsPaging,
            nationalInsuranceNumber,
            createdAt
          ) =>
        Updates.combine(
          Updates.set("correlationId", Codecs.toBson(correlationId)),
          Updates.set("pageTaskId", Codecs.toBson(id)),
          Updates.set("nationalInsuranceNumber", Codecs.toBson(nationalInsuranceNumber)),
          Updates.set("benefitSchemeMembershipDetailsPaging", Codecs.toBson(benefitSchemeMembershipDetailsPaging)),
          Updates.set("marriageDetailsPaging", Codecs.toBson(marriageDetailsPaging)),
          Updates.set("contributionAndCreditsPaging", Codecs.toBson(contributionAndCreditsPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )

      case SearchLightPageTask(
            callSystem,
            correlationId,
            id,
            paginationType,
            contributionAndCreditsPaging,
            nationalInsuranceNumber,
            createdAt
          ) =>
        Updates.combine(
          Updates.set("callSystem", Codecs.toBson(callSystem)),
          Updates.set("correlationId", Codecs.toBson(correlationId)),
          Updates.set("pageTaskId", Codecs.toBson(id)),
          Updates.set("nationalInsuranceNumber", Codecs.toBson(nationalInsuranceNumber)),
          Updates.set("contributionAndCreditsPaging", Codecs.toBson(contributionAndCreditsPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
    }
    collection
      .findOneAndUpdate(
        Filters.equal("pageTaskId", Codecs.toBson(existingPageTaskId.map(_.toString))),
        updates,
        FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .attemptT
      .map(_ => pageTask.pageTaskId.value)
      .leftMap(error => DatabaseError(error))
  }

  def insert(pageTask: PageTask)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("insert called - inserting page task in database")

    collection
      .insertOne(
        pageTask
      )
      .toFuture()
      .attemptT
      .map(_ => pageTask.pageTaskId.value)
      .leftMap(error => DatabaseError(error))
  }

  def delete(id: UUID)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Long] = {
    logger.info("delete called - deleting page task from database")

    collection
      .deleteOne(Filters.equal("pageTaskId", id.toString))
      .toFuture()
      .attemptT
      .map(_.getDeletedCount())
      .leftMap(error => DatabaseError(error))
  }

}
