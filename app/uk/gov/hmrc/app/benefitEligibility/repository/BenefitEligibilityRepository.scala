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

import com.google.inject.Inject
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.*
import uk.gov.hmrc.app.benefitEligibility.models.*
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

trait BenefitEligibilityRepository {
  def getItem(id: UUID): Future[Option[PageTask]]
}

@Singleton
class BenefitEligibilityRepositoryImpl @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[PageTask](
      collectionName = "page-tasks",
      mongoComponent = mongoComponent,
      domainFormat = PageTask.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("id"), // here is important
          IndexOptions()
            .name("id")
            .unique(true)
            .background(false)
            .expireAfter(5000, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    )
    with BenefitEligibilityRepository {

  def getItem(id: UUID): Future[Option[PageTask]] =
    collection.find(Filters.equal("id", id.toString)).headOption()

  def delete(id: UUID): Future[Boolean] =
    collection.deleteOne(Filters.equal("id", id.toString)).toFuture().map(_.wasAcknowledged)

}
