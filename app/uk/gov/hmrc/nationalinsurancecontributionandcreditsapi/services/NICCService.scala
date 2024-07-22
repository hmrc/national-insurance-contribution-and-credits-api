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
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models._
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.domain.NICCNino
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models.errors._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NICCService @Inject()(connector: HipConnector)(implicit ec: ExecutionContext) {

  def statusMapping(nationalInsuranceNumber: NICCNino, startTaxYear: String, endTaxYear: String, dateOfBirth: String)(implicit hc: HeaderCarrier): Future[Result] = {
    val newRequest = new NICCRequest(nationalInsuranceNumber, startTaxYear, endTaxYear, dateOfBirth)
    val correlationId: String = UUID.randomUUID().toString
    val responseHeader = "correlationId" -> correlationId
    connector.fetchData(newRequest, correlationId).map { response => {
      response.status match {
        case OK => response.json.validate[NPSResponse] match {
          case JsSuccess(data, _) =>
            val seq1: Seq[NICCContribution] = data.niClass1.map(niClass1Obj => new NICCContribution(niClass1Obj))
            val seq2: Seq[NICCCredit] = data.niClass2.map(niClass2Obj => new NICCCredit(niClass2Obj))

            Ok(Json.toJson(new NICCResponse(seq1, seq2))).withHeaders(responseHeader)
          case JsError(_) => InternalServerError.withHeaders(responseHeader)
        }
        case BAD_REQUEST =>
          if (response.json.toString().contains("HIP")) response.json.validate[HIPErrorResponse] match {
            case JsSuccess(data, _) =>
              val mappedFailures: Seq[Failure] = data.response.failures.map(hipFailure => new Failure(hipFailure))
              BadRequest(Json.toJson(new Response(mappedFailures))).withHeaders(responseHeader)

            case JsError(_) => InternalServerError.withHeaders(responseHeader)
          }

          else if (response.json.toString().contains("HoD")) response.json.validate[ErrorResponse] match {
            case JsSuccess(data, _) => BadRequest(Json.toJson(new Response(data.response.failures))).withHeaders(responseHeader)
            case JsError(_) => InternalServerError.withHeaders(responseHeader)
          }

          else InternalServerError.withHeaders(responseHeader)
        case UNPROCESSABLE_ENTITY => response.json.validate[Failures] match {
          case JsSuccess(data, _) => UnprocessableEntity(Json.toJson(data)).withHeaders(responseHeader)
          case JsError(_) => InternalServerError.withHeaders(responseHeader)
        }
        case _ => InternalServerError.withHeaders(responseHeader)
      }
    }
    }
  }

}