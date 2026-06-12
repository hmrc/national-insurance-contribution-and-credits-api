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
    with DefaultPlayMongoRepositorySupport[PageTaskDocument]
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

  override protected val repository: BenefitEligibilityRepositoryImpl =
    app.injector.instanceOf[BenefitEligibilityRepositoryImpl]

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

        val pageTaskId1 = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val pageTaskId2 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId3 = PageTaskId(UUID.fromString("f2968e2a-37cd-4f4e-9d66-bb0351c6dd6c"))

        val paginationSource1 = PaginationSource(Class2MAReceipts, "SomeCallBackURLOne")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")
        val paginationSource3 = PaginationSource(MarriageDetails, "SomeCallBackURLThree")

        val pageTaskDocList = List(
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource2, paginationSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId2,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId3,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource3),
                  Some(paginationSource1),
                  Some(
                    ContributionAndCreditsPaging(
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

        pageTaskDocList.foreach(task => insert(task).futureValue)

        val pageTasks = Table("page_task", pageTaskDocList: _*)

        forAll(pageTasks) { pageTask =>
          repository
            .getItem(pageTask.pageTaskId)
            .value
            .futureValue shouldBe Right(pageTask)
        }
      }
      "should return a RecordNotFound error if the record being retrieved does not exist in the db" in {
        val unknownPageTaskId = PageTaskId(UUID.fromString("cc7df9a9-ce5b-4a51-8402-01108c88a9df"))

        val cursorId = CursorId.from(unknownPageTaskId)
        repository.getItem(unknownPageTaskId).value.futureValue shouldBe Left(
          RecordNotFound(cursorId)
        )
      }
    }
    ".upsert" - {
      "should insert a new BspPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val bspPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource1),
                  Some(
                    ContributionAndCreditsPaging(
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

        repository.upsert(None, bspPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should insert a new MaPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val maPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource1, paginationSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(None, maPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should insert a new GyspPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val gyspPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource1),
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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

        repository.upsert(None, gyspPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should return a failure if mongo database fails" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val gyspPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource1),
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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

        repository.upsert(None, gyspPageTask).value.futureValue shouldBe a[Left[DatabaseError, _]]
      }
      "should overwrite an existing MaPageTask" in {

        val pageTaskId1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLOne")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val maPageTask1 =
          PageTaskDocument(
            correlationId,
            pageTaskId1,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource1, paginationSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        deleteAll().futureValue
        insert(maPageTask1).futureValue

        val maPageTask2 =
          PageTaskDocument(
            correlationId,
            pageTaskId2,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource2, paginationSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(pageTaskId1.value), maPageTask2).value.futureValue shouldBe Right(pageTaskId2.value)
        findAll().futureValue shouldBe List(maPageTask2)
      }
      "should overwrite an existing BspPageTask" in {

        val pageTaskId1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLOne")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val contributionAndCreditsPaging1 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsPaging2 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )

        val bspPageTask1 =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource1),
                  Some(contributionAndCreditsPaging1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )
        deleteAll().futureValue
        insert(bspPageTask1).futureValue

        val bspPageTask2 =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId2,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource2),
                  Some(contributionAndCreditsPaging2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(pageTaskId1.value), bspPageTask2).value.futureValue shouldBe Right(pageTaskId2.value)
        findAll().futureValue shouldBe List(bspPageTask2)
      }
      "should overwrite an existing GyspPageTask" in {

        val pageTaskId1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLOne")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val contributionAndCreditsPaging1 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsPaging2 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )

        val gyspPageTask1 =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource1),
                  Some(paginationSource1),
                  Some(contributionAndCreditsPaging1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        deleteAll().futureValue
        insert(gyspPageTask1).futureValue

        val gyspPageTask2 =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId2,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource2),
                  Some(paginationSource2),
                  Some(contributionAndCreditsPaging2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.upsert(Some(pageTaskId1.value), gyspPageTask2).value.futureValue shouldBe Right(pageTaskId2.value)
        findAll().futureValue should contain theSameElementsAs List(gyspPageTask2)
      }
    }
    ".insert" - {
      "should insert a new BspPageTask" in {
        val pageTaskId1       = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val bspPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource1),
                  Some(
                    ContributionAndCreditsPaging(
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

        repository.insert(bspPageTask).value.futureValue shouldBe Right(pageTaskId1.value)
        find(Filters.equal("pageTaskId", Codecs.toBson(pageTaskId1))).futureValue shouldBe List(bspPageTask)
      }
      "should insert a new MaPageTask" in {
        val pageTaskId1       = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val maPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource1, paginationSource1),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          )

        repository.insert(maPageTask).value.futureValue shouldBe Right(pageTaskId1.value)
        find(Filters.equal("pageTaskId", Codecs.toBson(pageTaskId1))).futureValue shouldBe List(maPageTask)

      }
      "should insert a new GyspPageTask" in {
        val pageTaskId1       = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, "SomeCallBackURLTwo")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")

        val gyspPageTask =
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource1),
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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

        repository.insert(gyspPageTask).value.futureValue shouldBe Right(pageTaskId1.value)
        find(Filters.equal("pageTaskId", Codecs.toBson(pageTaskId1))).futureValue shouldBe List(gyspPageTask)

      }
    }
    ".delete" - {
      "should delete a PageTask" in {
        val pageTaskId1 = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val pageTaskId2 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId3 = PageTaskId(UUID.fromString("f2968e2a-37cd-4f4e-9d66-bb0351c6dd6c"))

        val paginationSource1 = PaginationSource(Class2MAReceipts, "SomeCallBackURLOne")
        val paginationSource2 = PaginationSource(Liabilities, "SomeCallBackURLTwo")
        val paginationSource3 = PaginationSource(MarriageDetails, "SomeCallBackURLThree")

        val pageTasksList = List(
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId1,
            Json
              .toJson(
                MaPageTask(
                  List(paginationSource2, paginationSource2),
                  nationalInsuranceNumber
                )
              )
              .as[JsObject],
            currentTimeSource.instantNow()
          ),
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId2,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId3,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource3),
                  Some(paginationSource1),
                  Some(
                    ContributionAndCreditsPaging(
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
        val newPageTasksList = List(
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId2,
            Json
              .toJson(
                BspPageTask(
                  Some(paginationSource2),
                  Some(
                    ContributionAndCreditsPaging(
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
          PageTaskDocument(
            correlationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764")),
            pageTaskId3,
            Json
              .toJson(
                GyspPageTask(
                  Some(paginationSource3),
                  Some(paginationSource1),
                  Some(
                    ContributionAndCreditsPaging(
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

        pageTasksList.foreach(task => insert(task).futureValue)

        repository.delete(pageTaskId1.value).value.futureValue shouldBe Right(1)
        findAll().futureValue should contain theSameElementsAs newPageTasksList

      }
      "should try delete a PageTask that doesn't exist and return 0" in {
        repository.delete(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde")).value.futureValue shouldBe Right(0)
      }

    }
  }

}
