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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.request

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.app.benefitEligibility.common.{Identifier, ReceiptDate}
import uk.gov.hmrc.app.benefitEligibility.integration.inbound.MAEligibilityCheckDataRequest
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.class2MAReceipts.model.reqeust.{
  Class2MAReceiptsRequestHelper,
  MaternityAllowanceSortType
}

import java.time.LocalDate

class Class2MAReceiptsRequestHelperSpec extends AnyFreeSpec with Matchers {

  private val underTest      = new Class2MAReceiptsRequestHelper()
  private val baseHost       = "https://api.example.com"
  private val testIdentifier = Identifier("AB123456C")

  "Class2MAReceiptsRequestHelper" - {
    ".buildRequestPath" - {
      "when no optional parameters are provided" - {
        "should build basic path with identifier only" in {
          val request = createBaseRequest()

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts"
        }
      }

      "when single filter is provided" - {
        "should build path with archived filter only" in {
          val request = createBaseRequest().copy(archived = Some(true))

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?latest=true"
        }

        "should build path with receiptDate filter only" in {

          val receiptDate = LocalDate.of(2023, 6, 15)
          val request     = createBaseRequest().copy(receiptDate = Some(ReceiptDate(receiptDate)))

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?receiptDate=$receiptDate"
        }

        "should build path with sortBy filter only" in {
          val request = createBaseRequest().copy(sortBy = Some(MaternityAllowanceSortType.NinoDescending))

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?type=NinoDescending"
        }
      }

      "when multiple filters are provided" - {
        "should build path with archived and receiptDate filters" in {
          val receiptDate = LocalDate.of(2023, 6, 15)
          val request = createBaseRequest().copy(
            archived = Some(false),
            receiptDate = Some(ReceiptDate(receiptDate))
          )

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?latest=false&receiptDate=$receiptDate"
        }

        "should build path with archived and sortBy filters" in {
          val request = createBaseRequest().copy(
            archived = Some(true),
            sortBy = Some(MaternityAllowanceSortType.NinoDescending)
          )

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?latest=true&type=NinoDescending"
        }

        "should build path with receiptDate and sortBy filters" in {
          val receiptDate = LocalDate.of(2023, 12, 31)
          val request = createBaseRequest().copy(
            receiptDate = Some(ReceiptDate(receiptDate)),
            sortBy = Some(MaternityAllowanceSortType.NinoDescending)
          )

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?receiptDate=$receiptDate&type=NinoDescending"
        }

        "should build path with all filters" in {
          val receiptDate = LocalDate.of(2023, 3, 20)
          val request = createBaseRequest().copy(
            archived = Some(false),
            receiptDate = Some(ReceiptDate(receiptDate)),
            sortBy = Some(MaternityAllowanceSortType.NinoDescending)
          )

          val result = underTest.buildRequestPath(baseHost, request)

          result shouldBe s"$baseHost/ni/class-2/$testIdentifier/maternity-allowance/receipts?latest=false&receiptDate=$receiptDate&type=NinoDescending"
        }
      }

      "when different host formats are used" - {
        "should handle various host URL formats correctly" in {
          val hosts = List(
            "http://localhost:8080",
            "https://prod.api.gov.uk",
            "http://api.internal"
          )
          val request = createBaseRequest()

          hosts.foreach { host =>
            val result = underTest.buildRequestPath(host, request)

            result should startWith(host)
            result should include(s"/ni/class-2/$testIdentifier/maternity-allowance/receipts")
          }
        }
      }

      "when boolean archived values are used" - {
        "should handle archived=true correctly" in {
          val request = createBaseRequest().copy(archived = Some(true))

          val result = underTest.buildRequestPath(baseHost, request)

          result should include("latest=true")
        }

        "should handle archived=false correctly" in {
          val request = createBaseRequest().copy(archived = Some(false))

          val result = underTest.buildRequestPath(baseHost, request)

          result should include("latest=false")
        }
      }
    }
  }

  private def createBaseRequest(): MAEligibilityCheckDataRequest =
    MAEligibilityCheckDataRequest(
      nationalInsuranceNumber = "AB123456C",
      dateOfBirth = LocalDate.of(1990, 1, 1),
      startTaxYear = 2020,
      endTaxYear = 2023,
      identifier = testIdentifier,
      liabilitySearchCategoryHyphenated = false,
      liabilityOccurrenceNumber = None,
      liabilityType = None,
      earliestLiabilityStartDate = None,
      liabilityStart = None,
      liabilityEnd = None,
      archived = None,
      receiptDate = None,
      sortBy = None
    )

}
