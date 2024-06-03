package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.utils.{AdditionalHeaderNames, RequestParams}

class RequestSpec extends AnyWordSpec with Matchers {
  val nino = "PA662387B"
  val forename = "John"
  val surname = "Doe"
  val dateOfBirth = "2000-05-22"
  val dateRange = "someDate"
  val correlationId = "6c9aedff-4fac-44fd-bdf6-e786dfce07c6"

  "A valid Map of citizen info" should {

    val newPersonParams = Map[String, String](
      (RequestParams.NINO, nino),
      (RequestParams.FORENAME, forename),
      (RequestParams.SURNAME, surname),
      (RequestParams.DATE_OF_BIRTH, dateOfBirth),
      (RequestParams.DATE_RANGE, dateRange),
      (AdditionalHeaderNames.CORRELATION_ID, "6c9aedff-4fac-44fd-bdf6-e786dfce07c6")
    )

    "return a valid request object" in {

      val expectedRequest = new Request(nino, forename, surname, dateOfBirth, dateRange, correlationId)
      val actualRequest = Request.addQueryParamsFromMap(newPersonParams)

      actualRequest shouldBe Some(expectedRequest)

    }
  }
  //todo: add testing for erroneous data...not added yet as behaviour for incorrect data input hasn't been confirmed yet

}
