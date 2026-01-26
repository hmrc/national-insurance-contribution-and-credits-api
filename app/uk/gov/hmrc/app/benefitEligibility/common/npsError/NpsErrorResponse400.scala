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

//package uk.gov.hmrc.app.benefitEligibility.common.npsError
//
//import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
//import uk.gov.hmrc.app.benefitEligibility.common.Reason
//
//sealed trait NpsErrorResponse400 extends NpsError
//
//case class ErrorResourceObj400(
//    reason: Reason,
//    code: NpsErrorCode400
//)
//
//object ErrorResourceObj400 {
//  implicit val errorResourceObj400Reads: Reads[ErrorResourceObj400] = Json.reads[ErrorResourceObj400]
//}
//
//case class ErrorResponse400(failures: List[ErrorResourceObj400])
//
//object ErrorResponse400 {
//  implicit val errorResponse400Reads: Reads[ErrorResponse400] = Json.reads[ErrorResponse400]
//}
//
//case class NpsStandardErrorResponse400(origin: HipOrigin, response: ErrorResponse400) extends NpsErrorResponse400
//
//object NpsStandardErrorResponse400 {
//
//  implicit val standardErrorResponse400Reads: Reads[NpsStandardErrorResponse400] =
//    Json.reads[NpsStandardErrorResponse400]
//
//}
//
//case class NpsErrorResponseHipOrigin400(origin: HipOrigin, failures: List[HipFailureResponse])
//    extends NpsErrorResponse400
//
//object NpsErrorResponseHipOrigin400 {
//
//  implicit val npsErrorResponseHipOrigin400Reads: Reads[NpsErrorResponseHipOrigin400] =
//    Json.reads[NpsErrorResponseHipOrigin400]
//
//}
//
//object NpsErrorResponse400 {
//
//  implicit val npsErrorResponse400Reads: Reads[NpsErrorResponse400] =
//    Reads[NpsErrorResponse400] { resp =>
//      NpsStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
//        case JsSuccess(value, path) => JsSuccess(value, path)
//        case JsError(errors) =>
//          NpsStandardErrorResponse400.standardErrorResponse400Reads.reads(resp) match {
//            case JsSuccess(value, path) => JsSuccess(value, path)
//            case JsError(errors)        => JsError(errors)
//          }
//      }
//    }
//
//}
