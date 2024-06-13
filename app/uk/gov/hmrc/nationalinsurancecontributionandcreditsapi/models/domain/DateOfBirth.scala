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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain.{SimpleName, SimpleObjectReads, SimpleObjectWrites, TaxIdentifier}
import java.time.{LocalDate, Period}
import java.time.format.{DateTimeFormatter, DateTimeParseException}

case class DateOfBirth(dateOfBirth: String) extends TaxIdentifier with SimpleName {
  if (!DateOfBirth.isValid(dateOfBirth)) throw new IllegalArgumentException

  override def value: String = dateOfBirth

  override val name: String = "dateOfBirth"
}

object DateOfBirth {

  implicit val dateOfBirthWrite: Writes[DateOfBirth] = new SimpleObjectWrites[DateOfBirth](_.value)
  implicit val dateOfBirthRead: Reads[DateOfBirth] = new SimpleObjectReads[DateOfBirth]("dateOfBirth", DateOfBirth.apply)

  def isValid(dateOfBirth: String): Boolean = {
    val dobFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    try {
      dobFormatter parse dateOfBirth
      true
    } catch {
      case _: DateTimeParseException => false
    }
    //    val parsedDob = try {
    //      dobFormatter parse dateOfBirth
    //    } catch {
    //      case e: DateTimeParseException => None
    //    }

    //todo: potentially removing this business logic, remove it if not needed, Jacob to confirm with Tech Leads

//    parsedDob.exists { birthDate =>
//      val today = LocalDate.now()
//      val age = Period.between(birthDate, today).getYears
//      age >= 18
//    }
  }

}

