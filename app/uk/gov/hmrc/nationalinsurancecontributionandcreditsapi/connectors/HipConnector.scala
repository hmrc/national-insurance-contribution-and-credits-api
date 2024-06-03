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

import play.api.http.HeaderNames
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.AppConfig
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.{HIPOutcome, Request}
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.AdditionalHeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.util.concurrent.Future
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class HipConnector @Inject()(http: HttpClient,
                             config: AppConfig) {
  private[connectors] def hipHeaders(correlationId: String): Seq[(String, String)] =
    Seq(
      HeaderNames.AUTHORIZATION -> s"Bearer ${config.hipToken}",
      AdditionalHeaderNames.ENVIRONMENT -> config.hipEnvironment,
      AdditionalHeaderNames.CORRELATION_ID -> correlationId,
      HeaderNames.CONTENT_TYPE -> "application/json",
      AdditionalHeaderNames.ORIGINATING_SYSTEM -> "DWP" //todo: change to dynamic retrieval
    )

  def getCitizenInfo(request: Request)/*(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HIPOutcome]*/ : String = {
    s"""From the connector, going to base url:${config.hipBaseUrl}
       |$request
       |""".stripMargin


    /*val url = s"${config.hipBaseUrl}/nps-json-service/nps/v1/api/national-insurance/${request.nationalInsuranceNumber}/contributions-and-credits/from/${request.startTaxYear}/to/${request.endTaxYear}"
    //erroring implicitly parameter below is missing response checker implementation
    http.PUT(url, request, hipHeaders(request.correlationId))(implicitly, implicitly, hc, ec)*/

  }

}
