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

package uk.gov.hmrc.bereavementsupportpaymentapi.utils

import scala.util.matching.Regex

class Validator {
  def ninoValidator(nino: String): Option[String] = {
    val validationPattern: Regex = """^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\s]?$""".r
    Some(nino).filter(validationPattern.matches)
  }

  //DOB Validator
  /*def dobValidator(dob: String): Option[LocalDate] = {
    val dobFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
    val parsedDob = try {
      Some(LocalDate.parse(dob, dobFormatter))
    } catch {
      case e: DateTimeParseException => None
    }

    parsedDob.filter { birthDate =>
      val today = LocalDate.now()
      val age = Period.between(birthDate, today).getYears
      age >= 18
    }
  }*/
}
