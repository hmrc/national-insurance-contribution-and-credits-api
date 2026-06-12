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

@ImplementedBy(classOf[BenefitEligibilityRepositoryImpl])
trait BenefitEligibilityRepository {

  def getItem(pageTaskId: PageTaskId)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, PageTaskDocument]

  def upsert(id: Option[UUID], update: PageTaskDocument)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID]

  def insert(pageTask: PageTaskDocument)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID]
  def delete(id: UUID)(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, Long]
}

@Singleton
class BenefitEligibilityRepositoryImpl @Inject() (
    mongoComponent: MongoComponent,
    encrypterDecrypter: Encrypter & Decrypter,
    config: AppConfig
)(
    implicit ec: ExecutionContext
) extends PlayMongoRepository[PageTaskDocument](
      collectionName = "page-tasks",
      mongoComponent = mongoComponent,
      domainFormat = PageTaskDocument.pageTaskDocumentFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("pageTaskId"), IndexOptions().unique(true)),
        IndexModel(
          Indexes.ascending("createdAt"),
          IndexOptions()
            .expireAfter(config.pageTaskTtlSeconds.toLong, TimeUnit.SECONDS)
            .unique(false)
        )
      ),
      replaceIndexes = true
    )
    with BenefitEligibilityRepository {

  private val logger = new RequestAwareLogger(this.getClass)

  def getItem(
      pageTaskId: PageTaskId
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, PageTaskDocument] = {
    logger.info("getItem called - Retrieving page task from Database ")
    collection
      .find(Filters.equal("pageTaskId", Codecs.toBson(pageTaskId)))
      .headOption()
      .attemptT
      .leftMap(error => DatabaseError(error))
      .flatMap {
        case None => EitherT.leftT(RecordNotFound(CursorId.from(pageTaskId)))
        case Some(pageTaskDoc) =>
          val maybePageTaskDoc =
            if (config.encryptData) {
              pageTaskDoc.decrypt(encrypterDecrypter)

            } else {
              Some(pageTaskDoc)
            }

          EitherT.fromOption[Future](maybePageTaskDoc, RecordNotFound(CursorId(pageTaskId.value.toString)))
      }
  }

  def upsert(existingPageTaskId: Option[UUID], pageTaskDocument: PageTaskDocument)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("Upsert called - Updating page task in database")

    val documentData = if (config.encryptData) {
      pageTaskDocument.encrypt(encrypterDecrypter).data
    } else {
      pageTaskDocument.data
    }

    val updatedDocumentBson = Updates.combine(
      Updates.set("correlationId", Codecs.toBson(pageTaskDocument.correlationId)),
      Updates.set("pageTaskId", Codecs.toBson(pageTaskDocument.pageTaskId)),
      Updates.set("data", Codecs.toBson(documentData)),
      Updates.set("createdAt", Codecs.toBson(pageTaskDocument.createdAt))
    )

    collection
      .findOneAndUpdate(
        Filters.equal("pageTaskId", Codecs.toBson(existingPageTaskId.map(_.toString))),
        updatedDocumentBson,
        FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .attemptT
      .map(_ => pageTaskDocument.pageTaskId.value)
      .leftMap(error => DatabaseError(error))
  }

  def insert(
      pageTaskDocument: PageTaskDocument
  )(implicit hc: HeaderCarrier): EitherT[Future, BenefitEligibilityError, UUID] = {
    logger.info("insert called - inserting page task in database")

    val document = if (config.encryptData) {
      pageTaskDocument.encrypt(encrypterDecrypter)
    } else {
      pageTaskDocument
    }

    collection
      .insertOne(
        document
      )
      .toFuture()
      .attemptT
      .map(_ => document.pageTaskId.value)
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
