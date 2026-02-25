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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.{BeforeAndAfterAll, OptionValues}
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{Class2MAReceipts, Liabilities, MarriageDetails}
import uk.gov.hmrc.app.benefitEligibility.models.{PageTask, Pages}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class BenefitEligibilityDataRepositoryItSpec
    extends AnyFreeSpecLike
    with DefaultPlayMongoRepositorySupport[PageTask]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with BeforeAndAfterAll {

  private val app = GuiceApplicationBuilder()
    .overrides(
      inject.bind[MongoComponent].toInstance(mongoComponent)
    )
    .build()

  override protected val repository: BenefitEligibilityRepositoryImpl =
    app.injector.instanceOf[BenefitEligibilityRepositoryImpl]

  override protected def checkTtlIndex = false

  "BenefitEligibilityRepository" - {
    ".getItem" - {
      "should successfully return an item by id" in {

        val pageListForIdOne   = List(Pages(Class2MAReceipts, "SomeCallBackURLOne"))
        val pageListForIdTwo   = List(Pages(Liabilities, "SomeCallBackURLTwo"))
        val pageListForIdThree = List(Pages(MarriageDetails, "SomeCallBackURLThree"))

        val pageTasksList = List(
          PageTask(
            "IdOne",
            pageListForIdOne
          ),
          PageTask(
            "IdTwo",
            pageListForIdTwo
          ),
          PageTask(
            "IdThree",
            pageListForIdThree
          )
        )

        pageTasksList.foreach(insert)

        val pageTasks = Table("page_task", pageTasksList: _*)

        forAll(pageTasks)(pageTaskList => repository.getItem(pageTaskList.id).futureValue mustBe Some(pageTaskList))
      }
    }

    ".delete" - {
      "should successfully delete an item by ID" in {

        val pageListForIdOne   = List(Pages(Class2MAReceipts, "SomeCallBackURLOne"))
        val pageListForIdTwo   = List(Pages(Liabilities, "SomeCallBackURLTwo"))
        val pageListForIdThree = List(Pages(MarriageDetails, "SomeCallBackURLThree"))

        val pageTasksList = List(
          PageTask(
            "IdOne",
            pageListForIdOne
          ),
          PageTask(
            "IdTwo",
            pageListForIdTwo
          ),
          PageTask(
            "IdThree",
            pageListForIdThree
          )
        )

        pageTasksList.foreach(insert)

        val pageTasks = Table("page_task", pageTasksList: _*)

        forAll(pageTasks)(pageTaskList => repository.delete("IdTwo").futureValue mustBe true)

      }
    }
  }

}
