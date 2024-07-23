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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: ServicesConfig) {

  private val hipServicePrefix = "microservice.services.hip"

  val hipBaseUrl: String = config.baseUrl("hip")
  val hipOriginatorId: String = config.getString(s"$hipServicePrefix.originatorId")
  val correlationId: String = UUID.randomUUID().toString
  private val hipClientId: String = config.getString(s"$hipServicePrefix.clientId")
  private val hipClientSecret: String = config.getString(s"$hipServicePrefix.clientSecret")

  val base64HipAuthToken: String =
    Base64.getEncoder.encode(s"$hipClientId:$hipClientSecret".getBytes(StandardCharsets.UTF_8)).map(_.toChar).mkString

  val correlationIdHeader: (String, String) = "correlationId" -> correlationId
}
