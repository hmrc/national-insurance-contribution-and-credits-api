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

import play.api.Configuration
import javax.inject.{Inject, Singleton}

trait ApiDefinitionConfig {

  def status(): String

  def accessType(): String

  def endpointsEnabled(): Boolean

}

@Singleton
class ApiDefinitionConfigImpl @Inject()(configuration: Configuration) extends ApiDefinitionConfig {

  private val PRIVATE = "PRIVATE"

  override lazy val status: String = configuration.get[String]("api.status")
  override lazy val accessType: String = configuration.getOptional[String]("api.access.type").getOrElse(PRIVATE)
  override lazy val endpointsEnabled: Boolean = configuration.getOptional[Boolean]("api.endpointsEnabled").getOrElse(false)
}

