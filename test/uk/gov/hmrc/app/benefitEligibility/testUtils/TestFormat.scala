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

package uk.gov.hmrc.app.benefitEligibility.testUtils

import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.ErrorCode422
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsError.{
  Class2MAReceiptsError400,
  Class2MAReceiptsError422,
  Class2MAReceiptsErrorResponse400,
  Class2MAReceiptsErrorResponse403,
  Class2MAReceiptsErrorResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsError.{
  NiContributionsAndCredits400,
  NiContributionsAndCredits422,
  NiContributionsAndCreditsResponse400,
  NiContributionsAndCreditsResponse403,
  NiContributionsAndCreditsResponse422
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.niContributionsAndCredits.model.response.NiContributionsAndCreditsSuccess.{
  ContributionCategoryLetter,
  EmployerName,
  NiContributionsAndCreditsSuccessResponse,
  NicClass1,
  NicClass2,
  PrimaryContribution,
  PrimaryPaidEarnings
}

object TestFormat {

  object CommonFormats {
    implicit val errorCode422: Writes[ErrorCode422] = Json.valueWrites[ErrorCode422]
  }

  object Class2MAReceiptsFormats {

    import CommonFormats.errorCode422

    implicit val class2MAReceiptsError400Writes: Writes[Class2MAReceiptsError400] =
      Json.writes[Class2MAReceiptsError400]

    implicit val class2MAReceiptsErrorResponse400Writes: Writes[Class2MAReceiptsErrorResponse400] =
      Json.writes[Class2MAReceiptsErrorResponse400]

    implicit val class2MAReceiptsErrorResponse403Writes: Writes[Class2MAReceiptsErrorResponse403] =
      Json.writes[Class2MAReceiptsErrorResponse403]

    implicit val class2MAReceiptsError422Writes: Writes[Class2MAReceiptsError422] =
      Json.writes[Class2MAReceiptsError422]

    implicit val class2MAReceiptsErrorResponse422Writes: Writes[Class2MAReceiptsErrorResponse422] =
      Json.writes[Class2MAReceiptsErrorResponse422]

  }

  object ContributionCreditFormats {

    import CommonFormats.errorCode422

    implicit val contributionCategoryLetterWrites: Writes[ContributionCategoryLetter] =
      Json.valueWrites[ContributionCategoryLetter]

    implicit val primaryContributionWrites: Writes[PrimaryContribution] =
      Json.valueWrites[PrimaryContribution]

    implicit val employerNameWrites: Writes[EmployerName] =
      Json.valueWrites[EmployerName]

    implicit val primaryPaidEarningsWrites: Writes[PrimaryPaidEarnings] =
      Json.valueWrites[PrimaryPaidEarnings]

    implicit val nicClass1Format: Format[NicClass1] =
      Json.format[NicClass1]

    implicit val nicClass2Format: Format[NicClass2] =
      Json.format[NicClass2]

    implicit val niContributionsAndCreditsSuccessResponseFormat: Format[NiContributionsAndCreditsSuccessResponse] =
      Json.format[NiContributionsAndCreditsSuccessResponse]

    implicit val niContributionsAndCreditsError400Writes: Writes[NiContributionsAndCredits400] =
      Json.writes[NiContributionsAndCredits400]

    implicit val niContributionsAndCreditsResponse400Writes: Writes[NiContributionsAndCreditsResponse400] =
      Json.writes[NiContributionsAndCreditsResponse400]

    implicit val niContributionsAndCreditsError403Writes: Writes[NiContributionsAndCreditsResponse403] =
      Json.writes[NiContributionsAndCreditsResponse403]

    implicit val niContributionsAndCredits422Writes: Writes[NiContributionsAndCredits422] =
      Json.writes[NiContributionsAndCredits422]

    implicit val niContributionsAndCreditsErrorResponse422Writes: Writes[NiContributionsAndCreditsResponse422] =
      Json.writes[NiContributionsAndCreditsResponse422]

  }

}
