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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config

import play.api.mvc.PathBindable
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.{NICCNino, TaxYear}

import scala.util.{Failure, Success, Try}

object Binders {
  implicit val ninoBinder: PathBindable[NICCNino] = new PathBindable[NICCNino] {

    override def bind(key: String, value: String): Either[String, NICCNino] = {
      Try[NICCNino](NICCNino.apply(value)) match {
        case Success(nino) => Right(nino)
        case Failure(e) => Left("Bad NINO") // TODO Error response should be 400 with error message: Bad Nino
      }
    }

    override def unbind(key: String, value: NICCNino): String = value.value
  }

    implicit val taxYearBinder: PathBindable[TaxYear] = new PathBindable[TaxYear] {

      override def bind(key: String, value: String): Either[String, TaxYear] = {
        Try[TaxYear](TaxYear.apply(value)) match {
          case Success(taxYear) => Right(taxYear)
          case Failure(e) => Left("BAD TAX YEAR")
        }
      }

      override def unbind(key: String, value: TaxYear): String = value.value
    }

}

