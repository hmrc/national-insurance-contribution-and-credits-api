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
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.AppConfig
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{HIPOutcome, NICCRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames.{CORRELATION_ID, ENVIRONMENT, ORIGINATING_SYSTEM}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HipConnector @Inject()(http: HttpClient,
                             config: AppConfig) {
  private[connectors] def hipHeaders(correlationId: String): Seq[(String, String)] =
    Seq(
      AUTHORIZATION -> s"Bearer ${config.hipToken}",
      ENVIRONMENT -> config.hipEnvironment,
      CORRELATION_ID -> correlationId,
      CONTENT_TYPE -> JSON,
      ORIGINATING_SYSTEM -> "DWP" //todo: change to dynamic retrieval
    )

  def fetchData(request: NICCRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HIPOutcome] = {
    val correlationId: String = UUID.randomUUID().toString
    val url = s"${config.hipBaseUrl}/nps-json-service/nps/v1/api/national-insurance/${request.nationalInsuranceNumber}/contributions-and-credits/from/${request.startTaxYear}/to/${request.endTaxYear}"
    import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.httpParsers.ApiHttpParser.apiHttpReads
    http.PUT(url, request, hipHeaders(correlationId))(implicitly, apiHttpReads, implicitly, implicitly)
    //todo: change this to a POST
  }

}
