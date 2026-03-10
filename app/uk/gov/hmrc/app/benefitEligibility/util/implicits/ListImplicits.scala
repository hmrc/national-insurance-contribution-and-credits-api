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

package uk.gov.hmrc.app.benefitEligibility.util.implicits

import cats.data.NonEmptyList

import scala.util.{Failure, Success, Try}

object ListImplicits {

  implicit class ListSyntax[A](list: List[A]) {

    def safeTailNel: Option[NonEmptyList[A]] =
      Try(list.tail) match {
        case Failure(exception) => None
        case Success(tail)      => NonEmptyList.fromList(tail)
      }

  }

}
