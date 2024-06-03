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

package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class ValidatorSpec extends AnyWordSpec with Matchers {
  val validator = new Validator()

  "Valid nino request" should {
    "return true" in {
      val expectedNino: String = "PA888629B"
      val actualResponse = validator.ninoValidator(expectedNino)

      actualResponse shouldBe Some(expectedNino)
    }
  }

  "Empty nino request" should {
    "return false" in {
      val expectedNino: String = ""
      val actualResponse = validator.ninoValidator(expectedNino)

      actualResponse shouldBe None
    }
  }

  "Short nino request" should {
    "return false" in {
      val expectedNino: String = "PA8829B"
      val actualResponse = validator.ninoValidator(expectedNino)

      actualResponse shouldBe None
    }
  }

  "A valid name" should {
    "return true" in {
      val expectedName = "John"
      val actualResponse = validator.textValidator(expectedName)

      actualResponse shouldBe Some(expectedName)
    }
  }

  "A name longer than 10 characters" should {
    "return false" in {
      val expectedName = "JohnAppletonDoe"
      val actualResponse = validator.textValidator(expectedName)

      actualResponse shouldBe None
    }
  }

  //todo: double check this is handled by regex from spec
  /*"A empty name" should {
    "return false" in {
      val expectedName = ""
      val actualResponse = validator.textValidator(expectedName)

      actualResponse shouldBe None
    }
  }*/

  "A valid date of birth" should {
    "return true" in {
      val strDob = "22052000"
      val expectedDob: Option[LocalDate] = Some(LocalDate.of(2000, 5, 22))
      val actualResponse = validator.dobValidator(strDob)

      actualResponse shouldBe expectedDob
    }
  }

  "An invalid date of birth" should {
    "return false" in {
      val strDob = "22052020"
      val actualResponse = validator.dobValidator(strDob)

      actualResponse shouldBe None
    }
  }

}
