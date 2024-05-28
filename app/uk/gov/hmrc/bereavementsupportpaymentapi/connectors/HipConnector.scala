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

package uk.gov.hmrc.bereavementsupportpaymentapi.connectors

import play.api.http.HeaderNames
import uk.gov.hmrc.bereavementsupportpaymentapi.config.AppConfig
import uk.gov.hmrc.bereavementsupportpaymentapi.models.Request
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.AdditionalHeaderNames

import javax.inject.Inject

class HipConnector @Inject()(config: AppConfig) {
  private[connectors] def hipHeaders(correlationId: String): Seq[(String, String)] =
    Seq(
      HeaderNames.AUTHORIZATION -> s"Bearer ${config.hipToken}",
      AdditionalHeaderNames.ENVIRONMENT -> config.hipEnvironment,
      AdditionalHeaderNames.CORRELATION_ID -> correlationId,
      HeaderNames.CONTENT_TYPE -> "application/json"
    )

  def getCitizenInfo(request: Request)/*(implicit hc: HeaderCarrier, ec ExecutionContext)*/: String = {
    //val url = s"${config.hipBaseUrl}"
    s"""From the connector, going to base url:${config.hipBaseUrl}
       |$request
       |""".stripMargin

  }

}
