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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors

import play.api.http.HeaderNames.{AUTHORIZATION, CONTENT_TYPE}
import play.api.http.MimeTypes.JSON
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.AppConfig
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.NICCRequest
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames.{CORRELATION_ID, ENVIRONMENT, ORIGINATING_SYSTEM}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipConnector @Inject()(httpClientV2: HttpClientV2,
                             config: AppConfig)(implicit ec: ExecutionContext) {

  def fetchData(request: NICCRequest)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val correlationId: String = UUID.randomUUID().toString
    val urlToRead = s"${config.hipBaseUrl}/nps-json-service/nps/v1/api/national-insurance/${request.nationalInsuranceNumber}/contributions-and-credits/from/${request.startTaxYear}/to/${request.endTaxYear}"

    httpClientV2.post(url"$urlToRead")
      .setHeader(
        AUTHORIZATION -> s"Bearer ${config.hipToken}",
        ENVIRONMENT -> config.hipEnvironment,
        CORRELATION_ID -> correlationId,
        CONTENT_TYPE -> JSON,
        ORIGINATING_SYSTEM -> "DWP" //todo: change to dynamic retrieval from request headers...is this/should this be added to our API spec for DWP?
      )
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
  }
}
