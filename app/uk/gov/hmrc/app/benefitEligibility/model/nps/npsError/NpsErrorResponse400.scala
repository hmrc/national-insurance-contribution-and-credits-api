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

package uk.gov.hmrc.app.benefitEligibility.model.nps.npsError

import play.api.libs.json.{JsError, JsSuccess, Reads}

trait NpsErrorResponse400 extends NpsError

object NpsErrorResponse400 {

  implicit val npsErrorResponse400Reads: Reads[NpsErrorResponse400] =
    Reads[NpsErrorResponse400] { resp =>
      NpsStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
        case JsSuccess(value, path) => JsSuccess(value, path)
        case JsError(errors) =>
          NpsErrorResponseHipOrigin.npsErrorResponseHipOriginReads.reads(resp) match {
            case JsSuccess(value, path) => JsSuccess(value, path)
            case JsError(errors)        => JsError(errors)
          }
      }
    }

}
