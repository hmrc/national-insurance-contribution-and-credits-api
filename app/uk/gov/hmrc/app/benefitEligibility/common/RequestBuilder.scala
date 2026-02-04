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

package uk.gov.hmrc.app.benefitEligibility.common

import play.api.libs.json.{Format, Json}

import scala.collection.mutable.ListBuffer

object RequestBuilder {

  def buildPath(basePath: String, options: List[RequestOption]): String = {
    val newPath: String                    = basePath.concat("?")
    val optionsStrings: ListBuffer[String] = ListBuffer.empty
    options
      .filter(ro => ro.value.isDefined)
      .foreach(option => optionsStrings.addOne(s"${option.name}=${option.value.get}&"))
    newPath.concat(optionsStrings.mkString).dropRight(1)
  }

}

case class RequestOption(name: String, value: Option[String])

object RequestOption {
  implicit val requestOptionFormat: Format[RequestOption] = Json.format[RequestOption]
}
