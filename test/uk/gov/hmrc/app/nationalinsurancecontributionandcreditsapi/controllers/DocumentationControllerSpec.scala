/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.controllers

import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, route, writeableOf_AnyContentAsEmpty}

class DocumentationControllerSpec
    extends AnyFreeSpec
    with GuiceOneAppPerSuite
    with OptionValues
    with ScalaFutures
    with should.Matchers
    with BeforeAndAfterEach {

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled" -> false
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset()
  }

  "return 200 when requesting definition" in {

    val url     = "/api/definition"
    val request = FakeRequest("GET", url)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)

  }

  "return 200 when requesting specification" in {

    val url     = s"/api/conf/1.0/application.yaml"
    val request = FakeRequest("GET", url)

    val result = route(app, request).value.futureValue
    result.header.status should be(OK)

  }

}
