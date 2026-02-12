/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.app.benefitEligibility.util

import cats.data.{EitherT, Validated}
import cats.implicits.catsSyntaxTuple6Semigroupal
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, UnprocessableEntity}
import uk.gov.hmrc.app.benefitEligibility.common.Identifier
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.EligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.request.error.{Error400, Error422, ErrorCode, ErrorReason}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

object BenefitEligibilityRequestErrorHandler {

  private val logger = new RequestAwareLogger(this.getClass)

  def validate(
      body: Option[JsValue]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): EitherT[Future, Result, EligibilityCheckDataRequest] =

    body match {
      case Some(data) =>
        data.validate[EligibilityCheckDataRequest] match {
          case JsSuccess(request, _) =>
            requestFieldValidate(request).value match {
              case eligibilityCheckDataRequest: EligibilityCheckDataRequest =>
                EitherT.rightT[Future, Result](eligibilityCheckDataRequest)
              case errors: List[Error422] =>
                EitherT.leftT[Future, EligibilityCheckDataRequest](
                  UnprocessableEntity(Json.toJson(errors))
                )
            }
          case JsError(error) =>
            logger.warn(s"Request parse returned a 400: $error")
            EitherT.leftT[Future, EligibilityCheckDataRequest](
              BadRequest(
                Json.toJson(
                  Error400(ErrorCode.BadRequest, ErrorReason("type field is required"))
                )
              )
            )
        }

      case None =>
        logger.warn(s"Request was empty")
        EitherT.leftT[Future, EligibilityCheckDataRequest](
          BadRequest(Json.toJson(Error400(ErrorCode.BadRequest, ErrorReason("type field is required"))))
        )
    }

  private def requestFieldValidate(
      data: EligibilityCheckDataRequest
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): EitherT[Future, List[Error422], EligibilityCheckDataRequest] =
    (
      Validated.condNel(
        Identifier.pattern.matches(data.nationalInsuranceNumber.value),
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("invalid national insurance number format"))
      ),
      Validated.condNel(
        data.niContributionsAndCredits.startTaxYear.value >= 1975,
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("Start tax year before 1975"))
      ),
      Validated.condNel(
        data.niContributionsAndCredits.startTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("Start tax year after CY-1"))
      ),
      Validated.condNel(
        data.niContributionsAndCredits.endTaxYear.value <= LocalDate.now().getYear - 1,
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("End tax year after CY-1"))
      ),
      Validated.condNel(
        data.niContributionsAndCredits.startTaxYear.value < data.niContributionsAndCredits.endTaxYear.value,
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("Start tax year after end tax year"))
      ),
      Validated.condNel(
        data.niContributionsAndCredits.startTaxYear.value >= 1975,
        SuccessfulResult,
        Error422(ErrorCode.UnprocessableEntity, ErrorReason("Start tax year before 1975"))
      )
    ).mapN((_, _, _, _, _, _) => SuccessfulResult) match {
      case Validated.Valid(a) => EitherT.rightT[Future, List[Error422]](data)
      case Validated.Invalid(e) =>
        logger.warn(s"Request field validation returned a 422: ${e.toList}")
        EitherT.leftT[Future, EligibilityCheckDataRequest](e.toList)
    }

}
