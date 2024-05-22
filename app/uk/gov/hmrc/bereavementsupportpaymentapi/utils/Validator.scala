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

import org.apache.pekko.http.scaladsl.model.DateTime
import org.apache.pekko.http.scaladsl.model.headers.Date
import java.time.format.DateTimeFormatter

import scala.util.matching.Regex

class Validator {

  def ninoValidator(nino: String): Option[String] = {
    val validationPattern: Regex = """^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\s]?$""".r
    if (validationPattern.matches(nino)) Some(nino) else None
  }

  def nameValidator(forename: String): Option[String] = {
    if(forename.length > 10) Some(forename) else None
  }

  /*def dobValidator(dob: String): Option[Date] = {
    val convertedDob = DateTimeFormatter.ofPattern("ddMMyyyy")
    if(convertedDob)



    val newDob = convertedDob.plus(568025136000L)
    if(newDob < DateTime.now) Some(newDob) else None


  }*/


}