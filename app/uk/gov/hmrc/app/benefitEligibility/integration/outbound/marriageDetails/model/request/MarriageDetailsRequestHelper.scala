/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.marriageDetails.model.request

import cats.implicits.catsSyntaxSemigroup
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.{
  BSPEligibilityCheckDataRequest,
  GYSPEligibilityCheckDataRequest
}

class MarriageDetailsRequestHelper {

  def buildRequestPath(host: String, req: GYSPEligibilityCheckDataRequest): String = {
    def searchStartYear: Option[String] = req.searchStartYear.map(sY => s"searchStartYear=$sY&")

    def searchEndYear: Option[String] = req.searchEndYear.map(eY => s"searchEndYear=$eY&")

    def latest: Option[String] = req.latest.map(l => s"latest=$l&")

    def sequence: Option[String] = req.sequence.map(s => s"sequence=$s")

    val options = searchStartYear.combine(searchEndYear).combine(latest).combine(sequence.map(result => s"?$result"))
    s"$host/ni/individual/${req.identifier}/marriage-cp$options/"
  }

  def buildRequestPath(host: String, req: BSPEligibilityCheckDataRequest): String = {
    def searchStartYear: Option[String] = req.searchStartYear.map(sY => s"searchStartYear=$sY&")

    def searchEndYear: Option[String] = req.searchEndYear.map(eY => s"searchEndYear=$eY&")

    def latest: Option[String] = req.latest.map(l => s"latest=$l&")

    def sequence: Option[String] = req.sequence.map(s => s"sequence=$s")

    val options = searchStartYear.combine(searchEndYear).combine(latest).combine(sequence.map(result => s"?$result"))
    s"$host/ni/individual/${req.identifier}/marriage-cp$options/"
  }

}
