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

package uk.gov.hmrc.app.benefitEligibility.model.response

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits.NiContributionsAndCreditsSuccess.{
  Class1ContributionAndCredits,
  Class2or3ContributionAndCredits,
  NiContributionsAndCreditsSuccessResponse,
  TotalGraduatedPensionUnits
}

case class ContributionsAndCreditsResponse(
    totalGraduatedPensionUnits: Option[TotalGraduatedPensionUnits],
    class1ContributionAndCredits: List[Class1ContributionAndCredits],
    class2Or3ContributionAndCredits: List[Class2or3ContributionAndCredits]
)

object ContributionsAndCreditsResponse {

  implicit val format: OFormat[ContributionsAndCreditsResponse] = Json.format[ContributionsAndCreditsResponse]

  def from(
      niContributionsAndCreditsSuccessResponse: NiContributionsAndCreditsSuccessResponse
  ): ContributionsAndCreditsResponse =
    ContributionsAndCreditsResponse(
      niContributionsAndCreditsSuccessResponse.totalGraduatedPensionUnits,
      niContributionsAndCreditsSuccessResponse.class1ContributionAndCredits.getOrElse(Nil),
      niContributionsAndCreditsSuccessResponse.class2Or3ContributionAndCredits.getOrElse(Nil)
    )

}
