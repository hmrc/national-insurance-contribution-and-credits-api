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

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeParseException}

case class Request(nino: String,
                   forename: String,
                   surname: String,
                   datOfBirth: LocalDate,
                   dateRange: String,
                   correlationId: String)

object Request {
  def fromMap(citizenInfo: Map[String, String]): Option[Request] = {
    val dobFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")

    for {
      nino <- citizenInfo.get("nino")
      forename <- citizenInfo.get("forename")
      surname <- citizenInfo.get("surname")
      dobAsString <- citizenInfo.get("dateOfBirth")
      dateOfBirth <- try {
        Some(LocalDate.parse(dobAsString, dobFormatter))
      } catch {
        case _: DateTimeParseException => None
      }
      dateRange <- citizenInfo.get("dateRange")
      correlationId <- citizenInfo.get("correlationId")
    } yield Request(nino, forename, surname, dateOfBirth, dateRange, correlationId)
  }
}

