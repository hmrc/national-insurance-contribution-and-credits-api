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
//import play.api.libs.json.{Json, Reads}
//import uk.gov.hmrc.app.benefitEligibility.common.Reason
//
//case class NpsError422(code: NpsErrorCode, reason: Reason) extends NpsError
//
//object NpsError422 {
//
//  implicit val NpsError422Reads: Reads[NpsError422] =
//    Json.reads[NpsError422]
//
//}
//
//case class NpsErrorResponse422(failures: List[NpsError422])
//
//object NpsErrorResponse422 {
//
//  implicit val NpsErrorResponse422Reads: Reads[NpsErrorResponse422] =
//    Json.reads[NpsErrorResponse422]
//
//}
//
//case class NpsErrorResponseHipOrigin(origin: HipOrigin, failures: List[HipFailureResponse]) extends NpsError
//
//object NpsErrorResponseHipOrigin {
//
//  implicit val npsErrorResponseHipOriginReads: Reads[NpsErrorResponseHipOrigin] =
//    Json.reads[NpsErrorResponseHipOrigin]
//
//}
