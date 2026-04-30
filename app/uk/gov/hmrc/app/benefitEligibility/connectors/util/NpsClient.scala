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

package uk.gov.hmrc.app.benefitEligibility.connectors.util

import cats.data.EitherT
import cats.implicits.catsSyntaxApplicativeError
import izumi.reflect.Tag
import play.api.http.HeaderNames.{AUTHORIZATION, CONTENT_TYPE}
import play.api.http.MimeTypes.JSON
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.app.benefitEligibility.model.common.{BenefitType, CallSystem, NpsClientError}
import uk.gov.hmrc.app.config.AppConfig
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames.{
  CORRELATION_ID,
  ORIGINATING_SYSTEM
}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NpsClient @Inject() (httpClientV2: HttpClientV2, config: AppConfig)(implicit ec: ExecutionContext) {

  private def getOriginatorId(benefitType: BenefitType, callSystem: Option[CallSystem] = None): String =
    callSystem match {
      case Some(_) =>
        benefitType match {
          case BenefitType.MA   => config.hipOriginatorIdMa.searchlightId
          case BenefitType.ESA  => config.hipOriginatorIdEsa.searchlightId
          case BenefitType.JSA  => config.hipOriginatorIdJsa.searchlightId
          case BenefitType.GYSP => config.hipOriginatorIdGysp.searchlightId
          case BenefitType.BSP  => config.hipOriginatorIdBsp.searchlightId
        }
      case None =>
        benefitType match {
          case BenefitType.MA   => config.hipOriginatorIdMa.standardId
          case BenefitType.ESA  => config.hipOriginatorIdEsa.standardId
          case BenefitType.JSA  => config.hipOriginatorIdJsa.standardId
          case BenefitType.GYSP => config.hipOriginatorIdGysp.standardId
          case BenefitType.BSP  => config.hipOriginatorIdBsp.standardId
        }
    }

  private val commonHeaders: List[(String, String)] = List(
    AUTHORIZATION -> s"Basic ${config.base64HipAuthToken}",
    CONTENT_TYPE  -> JSON
  )

  def post[A: Tag](benefitType: BenefitType, path: String, body: A, callSystem: Option[CallSystem])(
      implicit hc: HeaderCarrier,
      writes: Writes[A]
  ): EitherT[Future, NpsClientError, HttpResponse] = {
    val requestHeaders =
      (ORIGINATING_SYSTEM, getOriginatorId(benefitType, callSystem)) +: (hc.headers(
        Seq("CorrelationId")
      ) ++ commonHeaders)
    EitherT(
      httpClientV2
        .post(url"$path")
        .setHeader(requestHeaders *)
        .withBody(Json.toJson(body))
        .execute[HttpResponse]
        .attempt
    ).leftMap(NpsClientError(_))
  }

  def get(benefitType: BenefitType, path: String)(
      implicit hc: HeaderCarrier
  ): EitherT[Future, NpsClientError, HttpResponse] = {
    val requestHeaders =
      (ORIGINATING_SYSTEM, getOriginatorId(benefitType)) +: (hc.headers(Seq("CorrelationId")) ++ commonHeaders)
    httpClientV2
      .get(url"$path")
      .setHeader(requestHeaders *)
      .execute[HttpResponse]
      .attemptT
      .leftMap(NpsClientError(_))
  }

}
