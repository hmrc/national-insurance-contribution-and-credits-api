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

package uk.gov.hmrc.bereavementsupportpaymentapi.models

import uk.gov.hmrc.bereavementsupportpaymentapi.utils.AdditionalHeaderNames
import uk.gov.hmrc.bereavementsupportpaymentapi.utils.RequestParams
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeParseException}

case class Request(nino: String,
                   forename: String,
                   surname: String,
                   dateOfBirth: String,
                   dateRange: String,
                   correlationId: String)

object Request {
  def fromMap(citizenInfo: Map[String, String]): Option[Request] = {
    val dobFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    for {
      nino <- citizenInfo.get(RequestParams.NINO)
      forename <- citizenInfo.get(RequestParams.FORENAME)
      surname <- citizenInfo.get(RequestParams.SURNAME)
      dobAsString <- citizenInfo.get(RequestParams.DATE_OF_BIRTH)
      dateOfBirth <- try {
        LocalDate.parse(dobAsString, dobFormatter)
        citizenInfo.get(RequestParams.DATE_OF_BIRTH)
      } catch {
        case _: DateTimeParseException => None
      }
      dateRange <- citizenInfo.get(RequestParams.DATE_RANGE)
      correlationId <- citizenInfo.get(AdditionalHeaderNames.CORRELATION_ID)
    } yield Request(nino, forename, surname, dateOfBirth, dateRange, correlationId)
  }
}

