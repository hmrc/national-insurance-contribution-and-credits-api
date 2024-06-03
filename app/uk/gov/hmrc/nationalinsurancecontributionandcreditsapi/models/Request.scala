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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models
import com.google.inject.Inject
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.{AdditionalHeaderNames, RequestParams, Validator}
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeParseException}


case class Request(nino: String,
                   forename: String,
                   surname: String,
                   dateOfBirth: String,
                   dateRange: String,
                   correlationId: String)
@Inject
object Request {
  def buildRequestFromMap(citizenInfo: Map[String, String ]): Option[Request] = {
    val dobFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val queryParams: Map[String, String] = validateParams(citizenInfo)

    for {
      nino <- queryParams.get(RequestParams.NINO)
      forename <- queryParams.get(RequestParams.FORENAME)
      surname <- queryParams.get(RequestParams.SURNAME)
      dobAsString <- queryParams.get(RequestParams.DATE_OF_BIRTH)
      dateOfBirth <- try {
        LocalDate.parse(dobAsString, dobFormatter)
        queryParams.get(RequestParams.DATE_OF_BIRTH)
      } catch {
        case _: DateTimeParseException => None
      }
      dateRange <- queryParams.get(RequestParams.DATE_RANGE)
      correlationId <- queryParams.get(AdditionalHeaderNames.CORRELATION_ID)
    } yield Request(nino, forename, surname, dobAsString, dateRange, correlationId)
  }

  private def validateParams(flattenedQueryParams: Map[String, String]): Map[String, String] = {
    val validator = new Validator()

    val validatedFlatQueryParams: Map[String, String] = flattenedQueryParams.flatMap{
      case (RequestParams.NINO, value) =>
        validator.ninoValidator(value).map( value => RequestParams.NINO -> value)
      case (RequestParams.FORENAME, value) =>
        validator.textValidator(value).map ( value => RequestParams.FORENAME -> value)
      case (RequestParams.SURNAME, value) =>
        validator.textValidator(value).map ( value => RequestParams.SURNAME -> value)
      case (RequestParams.DATE_OF_BIRTH, value) =>
        validator.dobValidator(value).map ( value => RequestParams.DATE_OF_BIRTH -> value.toString )
      case (RequestParams.DATE_RANGE, value) =>
        validator.textValidator(value).map ( value => RequestParams.DATE_RANGE -> value )
      case (AdditionalHeaderNames.CORRELATION_ID, value) =>  Option((AdditionalHeaderNames.CORRELATION_ID, value): (String, String))
      case _ => None //todo: throw exception with status code for any other parameter sent not expected
    }

    validatedFlatQueryParams.map {
      case (key: String, value: String) => {
        key -> value
      }
    }.toMap

  }
}

