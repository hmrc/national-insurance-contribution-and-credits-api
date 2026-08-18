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

import cats.data.NonEmptyList
import org.mongodb.scala.model.Filters
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers.{contain, must, mustBe}
import org.scalatest.matchers.should.Matchers.{a, should, shouldBe}
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{Class2MAReceipts, Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import java.util.UUID

class BenefitEligibilityDataRepositoryItSpec
    extends AnyFreeSpecLike
    with DefaultPlayMongoRepositorySupport[BatchDocument]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll {

  private val app = GuiceApplicationBuilder()
    .overrides(
      inject.bind[MongoComponent].toInstance(mongoComponent)
    )
    .build()

  override protected val repository: BatchRepositoryImpl =
    app.injector.instanceOf[BatchRepositoryImpl]

  override protected def checkTtlIndex = false

  val testInstant: Instant = Instant.parse("2007-12-03T10:15:30.00Z")

  val nationalInsuranceNumber = Identifier("AB123456C")

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))
  implicit val headerCarrier: HeaderCarrier = new HeaderCarrier()

  val currentTimeSource: CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = testInstant
  }

  "BenefitEligibilityRepository" - {
    ".getItem" - {
      "should successfully return an item by id" in {

        val batchId1 = BatchId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val batchId2 = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchId3 = BatchId(UUID.fromString("f2968e2a-37cd-4f4e-9d66-bb0351c6dd6c"))

        val batchSource1 = BatchCallback(Class2MAReceipts, "SomeCallBackURLOne")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")
        val batchSource3 = BatchCallback(MarriageDetails, "SomeCallBackURLThree")

        val batchDocumentList = List(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                MaBatch(
                  List(batchSource2, batchSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId2,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId3,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource3),
                  Some(batchSource1),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )

        batchDocumentList.foreach(task => insert(task).futureValue)

        val batches = Table("page_task", batchDocumentList: _*)

        forAll(batches) { batch =>
          repository
            .getItem(batch.batchId)
            .value
            .futureValue shouldBe Right(batch)
        }
      }
      "should return a RecordNotFound error if the record being retrieved does not exist in the db" in {
        val unknownBatchId = BatchId(UUID.fromString("cc7df9a9-ce5b-4a51-8402-01108c88a9df"))

        val cursorId = CursorId.from(unknownBatchId)
        repository.getItem(unknownBatchId).value.futureValue shouldBe Left(
          RecordNotFound(cursorId)
        )
      }
    }
    ".upsert" - {
      "should insert a new BspBatch" in {
        val batchId        = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val bspBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource1),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(None, bspBatch).value.futureValue shouldBe Right(batchId.value)
      }
      "should insert a new MaBatch" in {
        val batchId        = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val maBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId,
            Json
              .toJson(
                MaBatch(
                  List(batchSource1, batchSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(None, maBatch).value.futureValue shouldBe Right(batchId.value)
      }
      "should insert a new GyspBatch" in {
        val batchId        = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val gyspBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource1),
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(None, gyspBatch).value.futureValue shouldBe Right(batchId.value)
      }
      "should return a failure if mongo database fails" in {
        val batchId        = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val gyspBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource1),
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        dropDatabase()

        repository.upsert(None, gyspBatch).value.futureValue shouldBe a[Left[DatabaseError, _]]
      }
      "should overwrite an existing MaBatch" in {

        val batchId1 = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchId2 = BatchId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLOne")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val maBatch1 =
          BatchDocument(
            correlationId,
            batchId1,
            Json
              .toJson(
                MaBatch(
                  List(batchSource1, batchSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        deleteAll().futureValue
        insert(maBatch1).futureValue

        val maBatch2 =
          BatchDocument(
            correlationId,
            batchId2,
            Json
              .toJson(
                MaBatch(
                  List(batchSource2, batchSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(batchId1.value), maBatch2).value.futureValue shouldBe Right(batchId2.value)
        findAll().futureValue shouldBe List(maBatch2)
      }
      "should overwrite an existing BspBatch" in {

        val batchId1 = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchId2 = BatchId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLOne")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val contributionAndCreditsBatching1 = ContributionAndCreditsBatching(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsBatching2 = ContributionAndCreditsBatching(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )

        val bspBatch1 =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource1),
                  Some(contributionAndCreditsBatching1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        deleteAll().futureValue
        insert(bspBatch1).futureValue

        val bspBatch2 =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId2,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource2),
                  Some(contributionAndCreditsBatching2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(batchId1.value), bspBatch2).value.futureValue shouldBe Right(batchId2.value)
        findAll().futureValue shouldBe List(bspBatch2)
      }
      "should overwrite an existing GyspBatch" in {

        val batchId1 = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchId2 = BatchId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLOne")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val contributionAndCreditsBatching1 = ContributionAndCreditsBatching(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsBatching2 = ContributionAndCreditsBatching(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )

        val gyspBatch1 =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource1),
                  Some(batchSource1),
                  Some(contributionAndCreditsBatching1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        deleteAll().futureValue
        insert(gyspBatch1).futureValue

        val gyspBatch2 =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId2,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource2),
                  Some(batchSource2),
                  Some(contributionAndCreditsBatching2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(batchId1.value), gyspBatch2).value.futureValue shouldBe Right(batchId2.value)
        findAll().futureValue should contain theSameElementsAs List(gyspBatch2)
      }
    }
    ".insert" - {
      "should insert a new BspBatch" in {
        val batchId1       = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val bspBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource1),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.insert(bspBatch).value.futureValue shouldBe Right(batchId1.value)
        find(Filters.equal("batchId", Codecs.toBson(batchId1))).futureValue shouldBe List(bspBatch)
      }
      "should insert a new MaBatch" in {
        val batchId1       = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val maBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                MaBatch(
                  List(batchSource1, batchSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.insert(maBatch).value.futureValue shouldBe Right(batchId1.value)
        find(Filters.equal("batchId", Codecs.toBson(batchId1))).futureValue shouldBe List(maBatch)

      }
      "should insert a new GyspBatch" in {
        val batchId1       = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchSource1 = BatchCallback(Liabilities, "SomeCallBackURLTwo")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")

        val gyspBatch =
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource1),
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.insert(gyspBatch).value.futureValue shouldBe Right(batchId1.value)
        find(Filters.equal("batchId", Codecs.toBson(batchId1))).futureValue shouldBe List(gyspBatch)

      }
    }
    ".delete" - {
      "should delete a Batch" in {
        val batchId1 = BatchId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val batchId2 = BatchId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val batchId3 = BatchId(UUID.fromString("f2968e2a-37cd-4f4e-9d66-bb0351c6dd6c"))

        val batchSource1 = BatchCallback(Class2MAReceipts, "SomeCallBackURLOne")
        val batchSource2 = BatchCallback(Liabilities, "SomeCallBackURLTwo")
        val batchSource3 = BatchCallback(MarriageDetails, "SomeCallBackURLThree")

        val batchesList = List(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId1,
            Json
              .toJson(
                MaBatch(
                  List(batchSource2, batchSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId2,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId3,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource3),
                  Some(batchSource1),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )
        val newBatchsList = List(
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId2,
            Json
              .toJson(
                BspBatch(
                  Some(batchSource2),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          BatchDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            batchId3,
            Json
              .toJson(
                GyspBatch(
                  Some(batchSource3),
                  Some(batchSource1),
                  Some(
                    ContributionAndCreditsBatching(
                      NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                      DateOfBirth(LocalDate.parse("2025-10-10"))
                    )
                  ),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        )

        batchesList.foreach(task => insert(task).futureValue)

        repository.delete(batchId1.value).value.futureValue shouldBe Right(1)
        findAll().futureValue should contain theSameElementsAs newBatchsList

      }
      "should try delete a Batch that doesn't exist and return 0" in {
        repository.delete(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")).value.futureValue shouldBe Right(0)
      }

    }
  }

}
