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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.services

import play.api.http.Status.{BAD_REQUEST, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError, Ok, UnprocessableEntity}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.AppConfig
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models._
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.NICCNino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NICCService @Inject()(connector: HipConnector, config: AppConfig)(implicit ec: ExecutionContext) {

  def statusMapping(nationalInsuranceNumber: NICCNino, startTaxYear: String, endTaxYear: String, dateOfBirth: String)(implicit hc: HeaderCarrier): Future[Result] = {
    val newRequest = new NICCRequest(nationalInsuranceNumber, startTaxYear, endTaxYear, dateOfBirth)

    connector.fetchData(newRequest).map { response => {

      response.status match {
        case OK => response.json.validate[NPSResponse] match {
          case JsSuccess(data, _) =>
            val niccContributions: Option[Seq[NICCContribution]] = if (data.niClass1.nonEmpty) Option(data.niClass1.get.map(niClass1Obj => new NICCContribution(niClass1Obj))) else None
            val niccCredits: Option[Seq[NICCCredit]] = if (data.niClass2.nonEmpty) Option(data.niClass2.get.map(niClass2Obj => new NICCCredit(niClass2Obj))) else None

            Ok(Json.toJson(new NICCResponse(niccContributions, niccCredits))).withHeaders(config.correlationIdHeader)

          case JsError(_) => InternalServerError.withHeaders(config.correlationIdHeader)
        }
        case BAD_REQUEST =>
          if (response.json.toString().contains("HIP")) response.json.validate[HIPErrorResponse] match {
            case JsSuccess(data, _) =>
              val mappedFailures: Seq[Failure] = data.response.failures.map(hipFailure => new Failure(hipFailure))
              BadRequest(Json.toJson(new Response(mappedFailures))).withHeaders(config.correlationIdHeader)

            case JsError(_) => InternalServerError.withHeaders(config.correlationIdHeader)
          }

          else if (response.json.toString().contains("HoD")) response.json.validate[ErrorResponse] match {
            case JsSuccess(data, _) => BadRequest(Json.toJson(new Response(data.response.failures))).withHeaders(config.correlationIdHeader)
            case JsError(_) => InternalServerError.withHeaders(config.correlationIdHeader)
          }

          else InternalServerError.withHeaders(config.correlationIdHeader)
        case UNPROCESSABLE_ENTITY => response.json.validate[Failures] match {
          case JsSuccess(data, _) => UnprocessableEntity(Json.toJson(data)).withHeaders(config.correlationIdHeader)
          case JsError(_) => InternalServerError.withHeaders(config.correlationIdHeader)
        }
        case _ => InternalServerError.withHeaders(config.correlationIdHeader)
      }
    }
    }
  }

}