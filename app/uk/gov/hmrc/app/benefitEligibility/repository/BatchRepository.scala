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
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.libs.json.JsObject
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.util.RequestAwareLogger
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BatchRepositoryImpl])
trait BatchRepository {

  def getItem(batchId: BatchId)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, BatchDocument]

  def upsert(id: Option[UUID], update: BatchDocument)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID]

  def insert(batchDocument: BatchDocument)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID]
  def delete(id: UUID)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Long]
}

@Singleton
class BatchRepositoryImpl @Inject()(
    mongoComponent: MongoComponent,
    encrypterDecrypter: Encrypter & Decrypter,
    config: AppConfig
)(
    implicit ec: ExecutionContext
) extends PlayMongoRepository[BatchDocument](
      collectionName = "batches",
      mongoComponent = mongoComponent,
      domainFormat = BatchDocument.batchDocumentFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("batchId"), IndexOptions().unique(true)),
        IndexModel(
          Indexes.ascending("createdAt"),
          IndexOptions()
            .expireAfter(config.batchTTLSeconds.toLong, TimeUnit.SECONDS)
            .unique(false)
        )
      ),
      replaceIndexes = true
    )
    with BatchRepository {

  private val logger = new RequestAwareLogger(this.getClass)

  def getItem(
      batchId: BatchId
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, BatchDocument] = {
    logger.info("getItem called - Retrieving batch from Database ")
    collection
      .find(Filters.equal("batchId", Codecs.toBson(batchId)))
      .headOption()
      .attemptT
      .leftMap(error => DatabaseError(error))
      .flatMap {
        case None => EitherT.leftT(RecordNotFound(CursorId.from(batchId)))
        case Some(batchDocument) =>
          val maybeBatchDoc =
            if (config.encryptData) {
              batchDocument.decrypt(encrypterDecrypter)

            } else {
              Some(batchDocument)
            }

          EitherT.fromOption[Future](maybeBatchDoc, RecordNotFound(CursorId(batchId.value.toString)))
      }
  }

  def upsert(existingBatchId: Option[UUID], batchDocument: BatchDocument)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("Upsert called - Updating batch in database")

    val documentData = if (config.encryptData) {
      batchDocument.encrypt(encrypterDecrypter).data
    } else {
      batchDocument.data
    }

    val updatedDocumentBson = Updates.combine(
      Updates.set("correlationId", Codecs.toBson(batchDocument.correlationId)),
      Updates.set("batchId", Codecs.toBson(batchDocument.batchId)),
      Updates.set("data", Codecs.toBson(documentData)),
      Updates.set("createdAt", Codecs.toBson(batchDocument.createdAt))
    )

    collection
      .findOneAndUpdate(
        Filters.equal("batchId", Codecs.toBson(existingBatchId.map(_.toString))),
        updatedDocumentBson,
        FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .attemptT
      .map(_ => batchDocument.batchId.value)
      .leftMap(error => DatabaseError(error))
  }

  def insert(
      batchDocument: BatchDocument
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("insert called - inserting batch in database")

    val document = if (config.encryptData) {
      batchDocument.encrypt(encrypterDecrypter)
    } else {
      batchDocument
    }

    collection
      .insertOne(
        document
      )
      .toFuture()
      .attemptT
      .map(_ => document.batchId.value)
      .leftMap(error => DatabaseError(error))
  }

  def delete(id: UUID)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Long] = {
    logger.info("delete called - deleting batch from database")

    collection
      .deleteOne(Filters.equal("batchId", id.toString))
      .toFuture()
      .attemptT
      .map(_.getDeletedCount())
      .leftMap(error => DatabaseError(error))
  }

}
