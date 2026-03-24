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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers.{contain, must, mustBe}
import org.scalatest.matchers.should.Matchers.{a, shouldBe}
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.app.benefitEligibility.model.common.*
import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName.{Class2MAReceipts, Liabilities, MarriageDetails}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Instant, LocalDate}
import java.util.UUID

class BenefitEligibilityDataRepositoryItSpec
    extends AnyFreeSpecLike
    with DefaultPlayMongoRepositorySupport[PageTask]
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

  "BenefitEligibilityRepository" - {
    ".getItem" - {
      "should successfully return an item by id" in {

        val pageTaskId1 = PageTaskId(UUID.fromString("54c99a34-86d9-4154-b617-5f60c7064bde"))
        val pageTaskId2 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId3 = PageTaskId(UUID.fromString("f2968e2a-37cd-4f4e-9d66-bb0351c6dd6c"))

        val paginationSource1 = PaginationSource(Class2MAReceipts, Some("SomeCallBackURLOne"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))
        val paginationSource3 = PaginationSource(MarriageDetails, Some("SomeCallBackURLThree"))

        val pageTasksList = List(
          MaPageTask(
            pageTaskId1,
            List(paginationSource2, paginationSource2),
            testInstant
          ),
          BspPageTask(
            pageTaskId2,
            Some(paginationSource2),
            Some(
              ContributionAndCreditsPaging(
                NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                DateOfBirth(LocalDate.parse("2025-10-10"))
              )
            ),
            testInstant
          ),
          GyspPageTask(
            pageTaskId3,
            Some(paginationSource3),
            Some(paginationSource1),
            Some(
              ContributionAndCreditsPaging(
                NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
                DateOfBirth(LocalDate.parse("2025-10-10"))
              )
            ),
            testInstant
          )
        )

        pageTasksList.foreach(insert)

        val pageTasks = Table("page_task", pageTasksList: _*)

        forAll(pageTasks) { pageTask =>
          repository.getItem(pageTask.pageTaskId.value).value.futureValue shouldBe Right(pageTask)
        }
      }

      "should return database error if there is an unexpected db failure" in {
        val pageTaskId = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))

        repository.getItem(pageTaskId.value).value.futureValue shouldBe Left(
          DatabaseError(RecordNotFound(pageTaskId.value))
        )
      }
    }
    ".upsert" - {
      "should insert a new BspPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val bspPageTask = BspPageTask(
          pageTaskId,
          Some(paginationSource1),
          Some(
            ContributionAndCreditsPaging(
              NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
              DateOfBirth(LocalDate.parse("2025-10-10"))
            )
          ),
          testInstant
        )

        repository.upsert(None, bspPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should insert a new MaPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val maPageTask = MaPageTask(
          pageTaskId,
          List(paginationSource1, paginationSource1),
          testInstant
        )

        repository.upsert(None, maPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should insert a new GyspPageTask" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val gyspPageTask = GyspPageTask(
          pageTaskId,
          Some(paginationSource1),
          Some(paginationSource2),
          Some(
            ContributionAndCreditsPaging(
              NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
              DateOfBirth(LocalDate.parse("2025-10-10"))
            )
          ),
          testInstant
        )

        repository.upsert(None, gyspPageTask).value.futureValue shouldBe Right(pageTaskId.value)
      }
      "should return a failure if mongo database fails" in {
        val pageTaskId        = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val gyspPageTask = GyspPageTask(
          pageTaskId,
          Some(paginationSource1),
          Some(paginationSource2),
          Some(
            ContributionAndCreditsPaging(
              NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
              DateOfBirth(LocalDate.parse("2025-10-10"))
            )
          ),
          testInstant
        )
        dropDatabase()

        repository.upsert(None, gyspPageTask).value.futureValue shouldBe a[Left[DatabaseError, _]]
      }
      "should overwrite an existing MaPageTask" in {

        val pageTaskId1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTaskId2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLOne"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val maPageTask1 = MaPageTask(
          pageTaskId1,
          List(paginationSource1, paginationSource1),
          testInstant
        )

        deleteAll().futureValue
        insert(maPageTask1).futureValue

        val maPageTask2 = MaPageTask(
          pageTaskId2,
          List(paginationSource2, paginationSource2),
          testInstant
        )

        repository.upsert(Some(pageTaskId1.value), maPageTask2).value.futureValue shouldBe Right(pageTaskId2.value)
        findAll().futureValue shouldBe List(maPageTask2)
      }
      "should overwrite an existing BspPageTask" in {

        val pageTask1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTask2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLOne"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val contributionAndCreditsPaging1 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsPaging2 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )

        val bspPageTask1 = BspPageTask(
          pageTask1,
          Some(paginationSource1),
          Some(contributionAndCreditsPaging1),
          testInstant
        )

        deleteAll().futureValue
        insert(bspPageTask1).futureValue

        val bspPageTask2 = BspPageTask(
          pageTask2,
          Some(paginationSource2),
          Some(contributionAndCreditsPaging2),
          testInstant
        )

        repository.upsert(Some(pageTask1.value), bspPageTask2).value.futureValue shouldBe Right(pageTask2.value)
        findAll().futureValue shouldBe List(bspPageTask2)
      }
      "should overwrite an existing GyspPageTask" in {

        val pageTask1 = PageTaskId(UUID.fromString("fa356ed8-27f2-4c62-8204-386366713356"))
        val pageTask2 = PageTaskId(UUID.fromString("501396d3-fbd7-4d04-8757-93a0c14575ce"))

        val paginationSource1 = PaginationSource(Liabilities, Some("SomeCallBackURLOne"))
        val paginationSource2 = PaginationSource(Liabilities, Some("SomeCallBackURLTwo"))

        val contributionAndCreditsPaging1 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2015), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val contributionAndCreditsPaging2 = ContributionAndCreditsPaging(
          NonEmptyList.one(TaxWindow(StartTaxYear(2021), EndTaxYear(2020))),
          DateOfBirth(LocalDate.parse("2025-10-10"))
        )
        val gyspPageTask1 = GyspPageTask(
          pageTask1,
          Some(paginationSource1),
          Some(paginationSource1),
          Some(contributionAndCreditsPaging1),
          testInstant
        )

        deleteAll().futureValue
        insert(gyspPageTask1).futureValue

        val gyspPageTask2 = GyspPageTask(
          pageTask2,
          Some(paginationSource2),
          Some(paginationSource2),
          Some(contributionAndCreditsPaging2),
          testInstant
        )

        repository.upsert(Some(pageTask1.value), gyspPageTask2).value.futureValue shouldBe Right(pageTask2.value)
        findAll().futureValue shouldBe List(gyspPageTask2)
      }
    }

  }

}
