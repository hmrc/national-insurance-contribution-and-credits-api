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

package uk.gov.hmrc.app.benefitEligibility.service

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import uk.gov.hmrc.app.benefitEligibility.model.common.{CorrelationId, OriginatorId}
import uk.gov.hmrc.app.benefitEligibility.util.CurrentTimeSource
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

trait BaseSpec extends AnyFreeSpec with MockFactory {

  export uk.gov.hmrc.http.HeaderCarrier
  export uk.gov.hmrc.app.benefitEligibility.model.common.OriginatorId

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val correlationId: CorrelationId = CorrelationId(UUID.fromString("434369a5-e0b9-4fb0-97db-c5e2753eb764"))

  implicit val originatorId: OriginatorId = OriginatorId("test-originatorId")
  
  def createCurrentTimeSource(instant: Instant): CurrentTimeSource = new CurrentTimeSource {
    override def instantNow(): Instant = instant
  }
  
  val testInstant: Instant = Instant.parse("2007-12-03T10:15:30.00Z")

}