package uk.gov.hmrc.bereavementsupportpaymentapi.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate


class RequestSpec extends AnyWordSpec with Matchers {
  val nino = "PA662387B"
  val forename = "John"
  val surname = "Doe"
  val dateOfBirth: LocalDate = LocalDate.of(2000, 5, 22)
  val dateRange = "someDateRange"
  val correlationId = "6c9aedff-4fac-44fd-bdf6-e786dfce07c6"

  "A valid Map of citizen info" should {

    val newPersonParams = Map[String, String](
      ("nino", nino),
      ("forename", forename),
      ("surname", surname),
      ("dateOfBirth", "22052000"),
      ("dateRange", dateRange),
      ("correlationId", "6c9aedff-4fac-44fd-bdf6-e786dfce07c6")
    )

    "return a valid request object" in {

      val expectedRequest = Request(nino, forename, surname, dateOfBirth, dateRange, correlationId)
      val actualRequest = Request.fromMap(newPersonParams)

      actualRequest shouldBe Some(expectedRequest)

    }
  }

}
