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
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitEligibilityError, DatabaseError, RecordNotFound}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BenefitEligibilityRepositoryImpl])
trait BenefitEligibilityRepository {
  def getItem(id: UUID): EitherT[Future, BenefitEligibilityError, PageTask]
  def upsert(id: Option[UUID], update: PageTask): EitherT[Future, BenefitEligibilityError, UUID]
}

@Singleton
class BenefitEligibilityRepositoryImpl @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PageTask](
      collectionName = "page-tasks",
      mongoComponent = mongoComponent,
      domainFormat = PageTask.pageTaskFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("id"), IndexOptions().unique(true)),
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

  def getItem(id: UUID): EitherT[Future, BenefitEligibilityError, PageTask] =
    collection
      .find(Filters.equal("id", id.toString))
      .headOption()
      .attemptT
      .flatMap {
        case None           => EitherT.leftT(RecordNotFound(id))
        case Some(pageTask) => EitherT.rightT(pageTask)
      }
      .leftMap(error => DatabaseError(error))

  def upsert(id: Option[UUID], pageTask: PageTask): EitherT[Future, BenefitEligibilityError, UUID] = {
    val updates = pageTask match {
      case MaPageTask(id, paginationType, liabilitiesPaging, createdAt) =>
        Updates.combine(
          Updates.set("id", id.toString),
          Updates.set("liabilitiesPaging", Codecs.toBson(liabilitiesPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
      case BspPageTask(id, paginationType, marriageDetailsPaging, contributionAndCreditsPaging, createdAt) =>
        Updates.combine(
          Updates.set("id", id.toString),
          Updates.set("marriageDetailsPaging", Codecs.toBson(marriageDetailsPaging)),
          Updates.set("contributionAndCreditsPaging", Codecs.toBson(contributionAndCreditsPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
      case GyspPageTask(
            id,
            paginationType,
            benefitSchemeMembershipDetailsPaging,
            marriageDetailsPaging,
            contributionAndCreditsPaging,
            createdAt
          ) =>
        Updates.combine(
          Updates.set("id", id.toString),
          Updates.set("benefitSchemeMembershipDetailsPaging", Codecs.toBson(benefitSchemeMembershipDetailsPaging)),
          Updates.set("marriageDetailsPaging", Codecs.toBson(marriageDetailsPaging)),
          Updates.set("contributionAndCreditsPaging", Codecs.toBson(contributionAndCreditsPaging)),
          Updates.set("paginationType", Codecs.toBson(paginationType)),
          Updates.set("createdAt", Codecs.toBson(createdAt))
        )
    }
    collection
      .findOneAndUpdate(
        Filters.equal("id", Codecs.toBson(id.map(_.toString))),
        updates,
        FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .attemptT
      .map(_ => pageTask.id)
      .leftMap(error => DatabaseError(error))
  }

}
