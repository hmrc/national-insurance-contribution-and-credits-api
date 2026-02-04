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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class RequestBuilderSpec extends AnyFreeSpec {

  val requestPath: String = "host/api"

  val optionBoolean: Option[Boolean]                 = Some(true)
  val optionInt: Option[Int]                         = Some(1)
  val optionEnum: Option[MaternityAllowanceSortType] = Some(MaternityAllowanceSortType.NinoAscending)
  val optionEmpty: Option[String]                    = None

  "RequestBuilder" - {
    ".buildPath" - {
      "should successfully build a path using options provided" in {

        val optionsList: List[RequestOption] = List(
          RequestOption("optionBoolean", optionBoolean.map(s => s.toString)),
          RequestOption("optionInt", optionInt.map(s => s.toString)),
          RequestOption("optionEnum", optionEnum.map(s => s.toString))
        )

        val expectedPath: String = "host/api?optionBoolean=true&optionInt=1&optionEnum=NinoAscending"

        RequestBuilder.buildPath(requestPath, optionsList) shouldBe expectedPath

      }

      "should return path as is when no options are provided" in {

        val optionsList: List[RequestOption] = List(
          RequestOption("optionBoolean", optionEmpty),
          RequestOption("optionInt", optionEmpty),
          RequestOption("optionEnum", optionEmpty)
        )

        RequestBuilder.buildPath(requestPath, optionsList) shouldBe requestPath
      }

      "should successfully build a path when partial options are provided" in {

        val optionsList: List[RequestOption] = List(
          RequestOption("optionBoolean", optionBoolean.map(s => s.toString)),
          RequestOption("optionInt", optionInt.map(s => s.toString)),
          RequestOption("optionEnum", optionEmpty)
        )

        val expectedPath: String = "host/api?optionBoolean=true&optionInt=1"

        RequestBuilder.buildPath(requestPath, optionsList) shouldBe expectedPath
      }
    }
  }

}
