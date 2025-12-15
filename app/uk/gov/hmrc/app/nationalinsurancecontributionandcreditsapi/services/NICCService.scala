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

package uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.services

import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, InternalServerError, NotFound, Ok, UnprocessableEntity}
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.connectors.HipConnector
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.domain.NICCNino
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.errors.*
import uk.gov.hmrc.app.nationalinsurancecontributionandcreditsapi.models.*
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NICCService @Inject() (connector: HipConnector)(implicit ec: ExecutionContext) {

  def statusMapping(
      nationalInsuranceNumber: NICCNino,
      startTaxYear: String,
      endTaxYear: String,
      dateOfBirth: String,
      correlationId: String
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val newRequest = new NICCRequest(nationalInsuranceNumber, startTaxYear, endTaxYear, dateOfBirth)

    connector.fetchData(newRequest, correlationId).map { response =>
      val correlationIdHeader = "correlationId" -> correlationId
      response.status match {

        case OK =>
          response.json.validate[NPSResponse] match {
            case JsSuccess(data, _) =>
              val niccContributions: Option[Seq[NICCContribution]] =
                data.niClass1.map(niClass1Seq => niClass1Seq.map(niClass1Obj => new NICCContribution(niClass1Obj)))
              val niccCredits: Option[Seq[NICCCredit]] =
                data.niClass2.map(niClass2Seq => niClass2Seq.map(niClass2Obj => new NICCCredit(niClass2Obj)))

              Ok(Json.toJson(new NICCResponse(niccContributions, niccCredits))).withHeaders(correlationIdHeader)

            case JsError(_) => InternalServerError.withHeaders(correlationIdHeader)
          }

        case BAD_REQUEST =>
          if (response.json.toString().contains("HIP")) response.json.validate[HIPErrorResponse] match {
            case JsSuccess(data, _) =>
              val mappedFailures: Seq[Failure] = data.response.failures.map(hipFailure => new Failure(hipFailure))
              BadRequest(Json.toJson(new Response(mappedFailures))).withHeaders(correlationIdHeader)
            case JsError(_) => InternalServerError.withHeaders(correlationIdHeader)
          }
          else if (response.json.toString().contains("HoD")) response.json.validate[ErrorResponse] match {
            case JsSuccess(data, _) =>
              BadRequest(Json.toJson(new Response(data.response.failures))).withHeaders(correlationIdHeader)
            case JsError(_) => InternalServerError.withHeaders(correlationIdHeader)
          }
          else InternalServerError.withHeaders(correlationIdHeader)

        case NOT_FOUND =>
          response.json.validate[Failure] match {
            case JsSuccess(data, _) => NotFound(Json.toJson(new Failures(Seq(data)))).withHeaders(correlationIdHeader)
            case JsError(_)         => InternalServerError.withHeaders(correlationIdHeader)
          }

        case UNPROCESSABLE_ENTITY =>
          response.json.validate[Failures] match {
            case JsSuccess(data, _) => UnprocessableEntity(Json.toJson(data)()).withHeaders(correlationIdHeader)
            case JsError(_)         => InternalServerError.withHeaders(correlationIdHeader)
          }

        case _ => InternalServerError.withHeaders(correlationIdHeader)
      }
    }
  }

}
