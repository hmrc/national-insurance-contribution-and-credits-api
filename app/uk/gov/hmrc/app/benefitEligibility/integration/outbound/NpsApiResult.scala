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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.app.benefitEligibility.common.ApiName.{Class2MAReceipts, ContributionCredit, Liabilities}
import uk.gov.hmrc.app.benefitEligibility.common.{ApiName, TextualErrorStatusCode}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.response.Class2MAReceiptsSuccess.Class2MAReceiptsSuccessResponse

import scala.collection.immutable

sealed abstract class NpsApiResponseStatus(override val entryName: String) extends EnumEntry

object NpsApiResponseStatus extends Enum[NpsApiResponseStatus] with PlayJsonEnum[NpsApiResponseStatus] {
  val values: immutable.IndexedSeq[NpsApiResponseStatus] = findValues
  case object Success extends NpsApiResponseStatus("SUCCESS")
  case object Failure extends NpsApiResponseStatus("FAILURE")
}

case class NpsError(code: TextualErrorStatusCode, message: String, downstreamStatus: Int)

object NpsError {
  implicit val writes: Writes[NpsError] = Json.writes[NpsError]
}

sealed trait ApiResult {
  val apiName: ApiName
  val status: NpsApiResponseStatus
  val error: Option[NpsError]
}

sealed trait NpsApiResult extends ApiResult {
  val apiName: ApiName
  val status: NpsApiResponseStatus
  val error: Option[NpsError]
  val successResponse: Option[NpsSuccessfulApiResponse]
}

object NpsApiResult {

  case class Class2MaReceiptsResult(
      status: NpsApiResponseStatus,
      successResponse: Option[Class2MAReceiptsSuccessResponse],
      error: Option[NpsError]
  ) extends NpsApiResult {
    val apiName: ApiName = Class2MAReceipts
  }

  case class ContributionCreditResult(
      status: NpsApiResponseStatus,
      successResponse: Option[Class2MAReceiptsSuccessResponse], // TODO ContributionCreditResponse will go here
      error: Option[NpsError]
  ) extends NpsApiResult {
    val apiName: ApiName = ContributionCredit
  }

  case class LiabilityResult(
      status: NpsApiResponseStatus,
      successResponse: Option[Class2MAReceiptsSuccessResponse], // TODO LiabilityResult will go here
      error: Option[NpsError]
  ) extends NpsApiResult {
    val apiName: ApiName = Liabilities
  }

}
