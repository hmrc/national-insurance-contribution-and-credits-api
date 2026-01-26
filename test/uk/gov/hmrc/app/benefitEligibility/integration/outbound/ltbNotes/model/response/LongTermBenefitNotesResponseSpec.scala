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

package uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response

import cats.data.Validated
import cats.data.Validated.Valid
import com.networknt.schema.SpecVersion
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.app.benefitEligibility.common.*
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorCode403.NpsErrorCode403_2
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorCode404.ErrorCode404
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorReason403.Forbidden
import uk.gov.hmrc.app.benefitEligibility.common.NpsErrorReason404.NotFound
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesError.{
  HipFailureItem,
  HipFailureResponse,
  HipFailureResponse400,
  HipFailureResponse500,
  HipFailureResponse503,
  LongTermBenefitNotesErrorItem400,
  LongTermBenefitNotesErrorResponse403,
  LongTermBenefitNotesErrorResponse404,
  LongTermBenefitNotesErrorResponse422,
  StandardErrorResponse400
}
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesSuccess.*
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.enums.*
import uk.gov.hmrc.app.benefitEligibility.testUtils.SchemaValidation.SimpleJsonSchema
import uk.gov.hmrc.app.benefitEligibility.testUtils.TestFormat.LongTermBenefitNotesFormats.*

class LongTermBenefitNotesResponseSpec extends AnyFreeSpec with Matchers {

  val longTermBenefitNotesOpenApiSpec =
    "test/resources/schemas/api/longTermBenefitNotes/longTermBenefitNotes.yaml"

  def longTermBenefitNotesOpenApi: SimpleJsonSchema =
    SimpleJsonSchema(
      longTermBenefitNotesOpenApiSpec,
      SpecVersion.VersionFlag.V7,
      Some("LongTermBenefitNotesResponse"),
      metaSchemaValidation = Some(Valid(()))
    )

  "LongTermBenefitNotesResponse" - {

    val jsonFormat = implicitly[Format[LongTermBenefitNotesSuccessResponse]]

    val longTermBenefitNotesSuccessResponse = LongTermBenefitNotesSuccessResponse(
      List(
        Note("Invalid Note Type Encountered."),
        Note(
          "Married Woman's/Widow's Reduced Rate Authority recorded on this account between 07/04/2020 and 07/04/2025."
        ),
        Note("Married Woman's/Widow's Reduced Rate Authority recorded on this account from 07/04/2025"),
        Note("Widow's Benefit Award UNKNOWN  recorded on this account between 07/04/2020 and 07/04/2025."),
        Note("Widow's Benefit Award UNKNOWN  recorded on this account from 07/04/2025."),
        Note("Retirement Position of UNKNOWN recorded on this account between 07/04/2020 and 07/04/2025."),
        Note("Retirement Position of UNKNOWN recorded on this account from 07/04/2025."),
        Note("Retirement Position of UNKNOWN recorded on this account between NOT KNOWN."),
        Note(
          "Un-employability Supplement information recorded on this account, of type UNKNOWN between 07/04/2024 and 07/04/2025."
        ),
        Note("Un-employability Supplement information recorded on this account, of type UNKNOWN from 07/04/2025."),
        Note(
          "Widow's Benefit Award UNKNOWN recorded on this account between 07/04/2021 and 07/04/2023 with an overlapping period of abroad recorded."
        ),
        Note(
          "Widow's Benefit Award UNKNOWN recorded on this account from 07/04/2025 with an overlapping period of abroad recorded."
        ),
        Note("An Overlapping period of UNKNOWN is recorded on this account between 07/04/2022 and 07/04/2024."),
        Note("An Overlapping period of UNKNOWN is recorded on this account from 07/04/2025."),
        Note("An Overpayment of UNKNOWN is recorded on this account between 07/04/2024 and 07/04/2025."),
        Note("An Overpayment of UNKNOWN is recorded on this account from 07/04/2025."),
        Note("An award of JOB SEEKER'S ALLOWANCE is recorded on this account between 15/3/06 and UNKNOWN."),
        Note("An award of JOB SEEKER'S ALLOWANCE is recorded on this account from 07/04/2025."),
        Note("An award of SICKNESS BENEFIT/IVB is recorded on this account between NOT KNOWN."),
        Note("RPFA UNKNOWN LO UNKNOWN ."),
        Note(
          "This account is recorded as having been UNKNOWN on JOB SEEKER'S ALLOWANCE.  The details of the old account are:"
        ),
        Note("NINO. AA000001, Date of Birth 11/11/1960, Name Jack."),
        Note(
          "A Widow's Cross reference is recorded on this account, to NINO AA000001, with name detail of Jack. UNKNOWN. Smith."
        ),
        Note("The following marriage / Civil Partnership details are recorded on this account:"),
        Note(
          "Start Date: 07/04/2020,  Verification : UNKNOWN,  End Date: 07/04/2025,  Verification: UNKNOWN,  Status: CIVIL PARTNER,  Spouse's / Civil Partner's NINo: AB123456, Spouse's / Civil Partner's Name: Lucy UNKNOWN Smith  UNKNOWN."
        ),
        Note("The following previous names are recorded on this account:"),
        Note("Jack UNKNOWN. UNKNOWN.  UNKNOWN."),
        Note("A marriage / Civil Partnership verification is recorded in the account under reference number UNKNOWN."),
        Note("A GRB Total of 43 earned by the Widow's Deceased husband has been included in this assessment."),
        Note(
          "The following overseas insurance details are recorded on the account: Country France, Insurance Number UNKNOWN."
        ),
        Note(
          "The following overseas residential details are recorded on the account: Country Italy from 07/04/2020 to 07/04/2025."
        ),
        Note(
          "The following overseas residential details are recorded on the account: Country Australia from 07/04/2025."
        ),
        Note(
          "There are 10 years of HRP are recorded on this account in a working life period of 35 years., UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN UNKNOWN UNKNOWN"
        ),
        Note("A GRB Total of 50 has been accumulated on this account."),
        Note("The following Deferments are recorded on this account:"),
        Note("Deferment Type UNKNOWN, between 07/04/2020 and 07/04/2025."),
        Note("Deferment Type UNKNOWN, from 07/04/2025."),
        Note("A previous Retirement Position of UNKNOWN was recorded on this account."),
        Note(
          "The following GMP/COD details have been calculated on this account: Pre-1988 GMP: 0.00, 1988 onwards GMP: 0.00,  Pre-1988 COD: 31.53, 1988 onwards COD: 5.84., "
        ),
        Note(
          "Substitution Calculation Method 1: Commencement Year: 1975,  Number of Requisite Years: 2,  Number of HRP Years: 10,  Number of Qualifying Years: 25, Basic Pension: 0.00, ,  Substitution Calculation Method 2: Commencement Year:1997,  Number of Requisite Years: 2,  Number of HRP Years: 5,  Number of Qualifying Years: 15,  Basic Pension: 0.00"
        ),
        Note(
          "The Benefit Entitlement include the following Widower's Inheritance Details: Spouse's NINo.: AB123456,  Spouse's Surname: Smith,  Spouse's Forenames: Lucy UNKNOWN,  Spouse's Date of Birth: 25/11/1971,  Spouse's GRB Units: 43,  GRB Unit Value: 0.0748"
        ),
        Note(
          "Tax Year: 2024,  BP Increments Weekly Rate: 0.00,  AP Increments Weekly Rate: 0.00, GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00"
        ),
        Note(
          "Tax Year: 2024,  BP Increments Weekly Rate: 0.00,  AP Increments Notional Amount (Pre 02): 0.00,  AP Increments Inheritable Amount (Pre 02): 0.00,  AP Increments Notional Amount (Post 02): 0.00,  AP Increments Inheritable Amount (Post 02): 0.00,  GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00"
        ),
        Note("A UNKNOWN Quarterly Billing arrangement is recorded on this account."),
        Note(
          "A UNKNOWN Cyclical Billing arrangement is recorded on this account from 07/04/2025.  The next bill issued under this arrangement will be dated 07/04/2026."
        ),
        Note(
          "The following Excess Contributions are recorded on the account:  <b>See Tax Year Contributions/Credits Screen for more detail"
        ),
        Note("Tax Year: 2024"),
        Note(
          "The following Erroneous Contributions are recorded on the account: <b>See Tax Year Contributions/Credits Screen for more detail"
        ),
        Note("Tax Year: 2024"),
        Note(
          "The following Late Paid Contributions are recorded on the account: <b>See Tax Year Contributions/Credits Screen for more detail"
        ),
        Note("Tax Year: 2024"),
        Note(
          "The following contributions are recorded on the account with a posting query: <b>See Tax Year Contributions/Credits Screen for more detail"
        ),
        Note("Tax Year: 2024"),
        Note("A date of death of 07/04/2025 is recorded on this account."),
        Note("The current residential address is recorded as starting on 07/04/2025."),
        Note(
          "An Uprating has been applied to this Benefit Entitlement on 07/04/2025 with values of:CAT A Weekly Rate:0.00,  Corresponding CAT BL Weekly Rate: UNKNOWN,  AP Total Weekly Rate (Pre 97): 2.75,  AP Total Weekly Rate (Post 97): 0.00, Pre 88 GMP: 0.00 , Post 88 GMP Value: 0.00, Pre 88 COD Value: 31.53, POst 88 COD Value: 5.84, GRB Weekly Rate: 3.22, WB BP age related cash value: UNKNOWN, WB AP age related cash value Pre 97: UNKNOWN,  WB AP age related cash value Post 97: UNKNOWN"
        ),
        Note(
          "An Uprating has been applied to this Benefit Entitlement on 07/04/2025 with values of:CAT A Weekly Rate:0.00,  Corresponding CAT BL Weekly Rate: UNKNOWN,  AP Total Weekly Rate (Pre 97): 2.75,  AP Total Weekly Rate (Post 97): 0.00,  AP Total Weekly Rate (Post 02): UNKNOWN, Pre 88 GMP: 0.00, Post 88 GMP Value: 0.00, Pre 88 COD Value: 31.61, POst 88 COD Value: 4.82, GRB Weekly Rate: 4.32, WB BP age related cash value: UNKNOWN, WB AP age related cash value Pre 97: UNKNOWN,  WB AP age related cash value Post 97: UNKNOWN"
        ),
        Note("An Inheritance calculation could not be performed as there is more than one marriage on the account."),
        Note("A Substitution calculation could not be performed as the relevant marriage could not be identified."),
        Note("A Method 2 Substitution calculation has not been performed because the Substitution Fraction is 1/1."),
        Note(
          "A Method 1 Substitution calculation could not be performed as the Substitution Period involves Pre 75 years."
        ),
        Note("A period of UNKNOWN from 07/04/2020 to 07/04/2022 is recorded with a reference number of UNKNOWN."),
        Note(
          "Contracted out Employment: Scheme Membership Sequence Number: 1, Scheme Membership Start Date: '1978-04-06', Scheme Membership End Date: '08/12/1983', Method of Preservation: 'CHANGE OF RPA FROM COSR TO PP', Econ: 'E3004227R', Terminating Scon/Ascn: 'A7990001X', Revaluation Rate: 'FIXED', Termination Microfilm Number: '47510120', POST 88 GMP: 'UNKNOW', POST 88 COD: 0.00, UNKNOWN"
        ),
        Note(
          "Contracted out Employment: Scheme Membership Sequence Number: 1, Scheme Membership Start Date: '1978-04-06', Scheme Membership End Date: '08/12/1983', Method of Preservation: 'CHANGE OF RPA FROM COSR TO PP', Econ: 'E3004227R', Terminating Scon/Ascn: 'A7990001X', Revaluation Rate: 'FIXED', Termination Microfilm Number: '47510120', POST 88 GMP: 'UNKNOW', POST 88 COD: 0.00, PRE 88 GMP: 'UNKNOWN', PRE 88 COD: 26.53, Date GMP Extinguished: 'UNKNOWN', 'UNKNOWN'"
        ),
        Note(
          "A Method 2 Substitution calculation could not be performed as the Substitution Period involves Pre 75 years."
        ),
        Note("Bereavement Benefit Award (UNKNOWN) recorded on this account from 07/04/2020 to 07/04/2025."),
        Note("Bereavement Benefit Award (UNKNOWN) recorded on this account from 07/04/2025"),
        Note(
          "Bereavement Benefit Award recorded on this account between 07/04/2023 and 07/04/2024 with an overlapping period of abroad recorded."
        ),
        Note(
          "Bereavement Benefit Award recorded on this account from 07/04/2024 with an overlapping period of abroad recorded."
        ),
        Note(
          "A Widower's /Civil Partner's cross-reference is recorded on this account, to NINO AA000001, with name Jack. UNKNOWN. Smith."
        ),
        Note("PP/COMP scheme involvement.  Please seek FRY and FRY+1 earnings."),
        Note("Period(s) of CHB S2P are held on this account."),
        Note("CHB S2P Start Date 07/04/2024, CHB S2P End Date 07/04/2025."),
        Note("Periods(s) of ICA S2P are held on this account."),
        Note("ICA S2P Start Date 07/04/2024, ICA S2P End Date 07/04/2025."),
        Note("Period(s) of AA/CAA/DLA S2P are held on this account."),
        Note("AA/CAA/DLA S2P Start Date 07/04/2024, AA/ CAA/DLA S2P End Date 07/04/2025"),
        Note("Period(s) of IB (Long Term and Youth)/SDA/ESA S2P are held on this account"),
        Note(
          "IB (Long Term and Youth)/SDA/ESA S2P Start Date 07/04/2024,  IB (Long Term and Youth)/SDA/ESA S2P End Date 07/04/2025."
        ),
        Note("The LMA test has been satisfied"),
        Note(
          "The LMA test has not been satisfied, the number of S2P qualifying years held is UNKNOWN. (If the LMA test is subsequently satisfied following notification of foreign earnings by Overseas Division a further calculation will be performed.)"
        ),
        Note(
          "The Benefit Entitlement include the following Widower's Inheritance Details: Spouse's NINo.: AB123456,  Spouse's Surname: Smith,  Spouse's Forenames: Lucy UNKNOWN,  Spouse's Date of Birth: 25/11/1971,  Spouse's GRB Units: 43,  GRB Unit Value: 0.0748"
        ),
        Note(
          "Tax Year: 2024,  BP Increments Weekly Rate: 0.08,  AP Increments Notional Amount: UNKNOWN,  AP Increments Inheritable Amount: UNKNOWN,  GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00"
        ),
        Note(
          "The following contributions recorded on the account have not yet been through Compliance and Yield checking: <b>See Tax Year Contributions/Credits Screen for more detail"
        ),
        Note("Tax Year: 2024"),
        Note("Calculation routed to Pre-1975 Section"),
        Note("A period of UNKNOWN is recorded on the account between 07/04/2024 and 07/04/2025."),
        Note("NSP Upratings:"),
        Note("Uprating date: 07/04/2025 , NSP Entitlement: 252.05, Protected Payment: 21.80, Category BL: 105.70")
      )
    )

    val jsonString =
      """{
        | "longTermBenefitNotes":[
        | "Invalid Note Type Encountered.",
        |    "Married Woman's/Widow's Reduced Rate Authority recorded on this account between 07/04/2020 and 07/04/2025.",
        |    "Married Woman's/Widow's Reduced Rate Authority recorded on this account from 07/04/2025",
        |    "Widow's Benefit Award UNKNOWN  recorded on this account between 07/04/2020 and 07/04/2025.",
        |    "Widow's Benefit Award UNKNOWN  recorded on this account from 07/04/2025.",
        |    "Retirement Position of UNKNOWN recorded on this account between 07/04/2020 and 07/04/2025.",
        |    "Retirement Position of UNKNOWN recorded on this account from 07/04/2025.",
        |    "Retirement Position of UNKNOWN recorded on this account between NOT KNOWN.",
        |    "Un-employability Supplement information recorded on this account, of type UNKNOWN between 07/04/2024 and 07/04/2025.",
        |    "Un-employability Supplement information recorded on this account, of type UNKNOWN from 07/04/2025.",
        |    "Widow's Benefit Award UNKNOWN recorded on this account between 07/04/2021 and 07/04/2023 with an overlapping period of abroad recorded.",
        |    "Widow's Benefit Award UNKNOWN recorded on this account from 07/04/2025 with an overlapping period of abroad recorded.",
        |    "An Overlapping period of UNKNOWN is recorded on this account between 07/04/2022 and 07/04/2024.",
        |    "An Overlapping period of UNKNOWN is recorded on this account from 07/04/2025.",
        |    "An Overpayment of UNKNOWN is recorded on this account between 07/04/2024 and 07/04/2025.",
        |    "An Overpayment of UNKNOWN is recorded on this account from 07/04/2025.",
        |    "An award of JOB SEEKER'S ALLOWANCE is recorded on this account between 15/3/06 and UNKNOWN.",
        |    "An award of JOB SEEKER'S ALLOWANCE is recorded on this account from 07/04/2025.",
        |    "An award of SICKNESS BENEFIT/IVB is recorded on this account between NOT KNOWN.",
        |    "RPFA UNKNOWN LO UNKNOWN .",
        |    "This account is recorded as having been UNKNOWN on JOB SEEKER'S ALLOWANCE.  The details of the old account are:",
        |    "NINO. AA000001, Date of Birth 11/11/1960, Name Jack.",
        |    "A Widow's Cross reference is recorded on this account, to NINO AA000001, with name detail of Jack. UNKNOWN. Smith.",
        |    "The following marriage / Civil Partnership details are recorded on this account:",
        |    "Start Date: 07/04/2020,  Verification : UNKNOWN,  End Date: 07/04/2025,  Verification: UNKNOWN,  Status: CIVIL PARTNER,  Spouse's / Civil Partner's NINo: AB123456, Spouse's / Civil Partner's Name: Lucy UNKNOWN Smith  UNKNOWN.",
        |    "The following previous names are recorded on this account:",
        |    "Jack UNKNOWN. UNKNOWN.  UNKNOWN.",
        |    "A marriage / Civil Partnership verification is recorded in the account under reference number UNKNOWN.",
        |    "A GRB Total of 43 earned by the Widow's Deceased husband has been included in this assessment.",
        |    "The following overseas insurance details are recorded on the account: Country France, Insurance Number UNKNOWN.",
        |    "The following overseas residential details are recorded on the account: Country Italy from 07/04/2020 to 07/04/2025.",
        |    "The following overseas residential details are recorded on the account: Country Australia from 07/04/2025.",
        |    "There are 10 years of HRP are recorded on this account in a working life period of 35 years., UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN, UNKNOWN UNKNOWN UNKNOWN UNKNOWN UNKNOWN UNKNOWN",
        |    "A GRB Total of 50 has been accumulated on this account.",
        |    "The following Deferments are recorded on this account:",
        |    "Deferment Type UNKNOWN, between 07/04/2020 and 07/04/2025.",
        |    "Deferment Type UNKNOWN, from 07/04/2025.",
        |    "A previous Retirement Position of UNKNOWN was recorded on this account.",
        |    "The following GMP/COD details have been calculated on this account: Pre-1988 GMP: 0.00, 1988 onwards GMP: 0.00,  Pre-1988 COD: 31.53, 1988 onwards COD: 5.84., ",
        |    "Substitution Calculation Method 1: Commencement Year: 1975,  Number of Requisite Years: 2,  Number of HRP Years: 10,  Number of Qualifying Years: 25, Basic Pension: 0.00, ,  Substitution Calculation Method 2: Commencement Year:1997,  Number of Requisite Years: 2,  Number of HRP Years: 5,  Number of Qualifying Years: 15,  Basic Pension: 0.00",
        |    "The Benefit Entitlement include the following Widower's Inheritance Details: Spouse's NINo.: AB123456,  Spouse's Surname: Smith,  Spouse's Forenames: Lucy UNKNOWN,  Spouse's Date of Birth: 25/11/1971,  Spouse's GRB Units: 43,  GRB Unit Value: 0.0748",
        |    "Tax Year: 2024,  BP Increments Weekly Rate: 0.00,  AP Increments Weekly Rate: 0.00, GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00",
        |    "Tax Year: 2024,  BP Increments Weekly Rate: 0.00,  AP Increments Notional Amount (Pre 02): 0.00,  AP Increments Inheritable Amount (Pre 02): 0.00,  AP Increments Notional Amount (Post 02): 0.00,  AP Increments Inheritable Amount (Post 02): 0.00,  GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00",
        |    "A UNKNOWN Quarterly Billing arrangement is recorded on this account.",
        |    "A UNKNOWN Cyclical Billing arrangement is recorded on this account from 07/04/2025.  The next bill issued under this arrangement will be dated 07/04/2026.",
        |    "The following Excess Contributions are recorded on the account:  <b>See Tax Year Contributions/Credits Screen for more detail",
        |    "Tax Year: 2024",
        |    "The following Erroneous Contributions are recorded on the account: <b>See Tax Year Contributions/Credits Screen for more detail",
        |    "Tax Year: 2024",
        |    "The following Late Paid Contributions are recorded on the account: <b>See Tax Year Contributions/Credits Screen for more detail",
        |    "Tax Year: 2024",
        |    "The following contributions are recorded on the account with a posting query: <b>See Tax Year Contributions/Credits Screen for more detail",
        |    "Tax Year: 2024",
        |    "A date of death of 07/04/2025 is recorded on this account.",
        |    "The current residential address is recorded as starting on 07/04/2025.",
        |    "An Uprating has been applied to this Benefit Entitlement on 07/04/2025 with values of:CAT A Weekly Rate:0.00,  Corresponding CAT BL Weekly Rate: UNKNOWN,  AP Total Weekly Rate (Pre 97): 2.75,  AP Total Weekly Rate (Post 97): 0.00, Pre 88 GMP: 0.00 , Post 88 GMP Value: 0.00, Pre 88 COD Value: 31.53, POst 88 COD Value: 5.84, GRB Weekly Rate: 3.22, WB BP age related cash value: UNKNOWN, WB AP age related cash value Pre 97: UNKNOWN,  WB AP age related cash value Post 97: UNKNOWN",
        |    "An Uprating has been applied to this Benefit Entitlement on 07/04/2025 with values of:CAT A Weekly Rate:0.00,  Corresponding CAT BL Weekly Rate: UNKNOWN,  AP Total Weekly Rate (Pre 97): 2.75,  AP Total Weekly Rate (Post 97): 0.00,  AP Total Weekly Rate (Post 02): UNKNOWN, Pre 88 GMP: 0.00, Post 88 GMP Value: 0.00, Pre 88 COD Value: 31.61, POst 88 COD Value: 4.82, GRB Weekly Rate: 4.32, WB BP age related cash value: UNKNOWN, WB AP age related cash value Pre 97: UNKNOWN,  WB AP age related cash value Post 97: UNKNOWN",
        |    "An Inheritance calculation could not be performed as there is more than one marriage on the account.",
        |    "A Substitution calculation could not be performed as the relevant marriage could not be identified.",
        |    "A Method 2 Substitution calculation has not been performed because the Substitution Fraction is 1/1.",
        |    "A Method 1 Substitution calculation could not be performed as the Substitution Period involves Pre 75 years.",
        |    "A period of UNKNOWN from 07/04/2020 to 07/04/2022 is recorded with a reference number of UNKNOWN.",
        |    "Contracted out Employment: Scheme Membership Sequence Number: 1, Scheme Membership Start Date: '1978-04-06', Scheme Membership End Date: '08/12/1983', Method of Preservation: 'CHANGE OF RPA FROM COSR TO PP', Econ: 'E3004227R', Terminating Scon/Ascn: 'A7990001X', Revaluation Rate: 'FIXED', Termination Microfilm Number: '47510120', POST 88 GMP: 'UNKNOW', POST 88 COD: 0.00, UNKNOWN",
        |    "Contracted out Employment: Scheme Membership Sequence Number: 1, Scheme Membership Start Date: '1978-04-06', Scheme Membership End Date: '08/12/1983', Method of Preservation: 'CHANGE OF RPA FROM COSR TO PP', Econ: 'E3004227R', Terminating Scon/Ascn: 'A7990001X', Revaluation Rate: 'FIXED', Termination Microfilm Number: '47510120', POST 88 GMP: 'UNKNOW', POST 88 COD: 0.00, PRE 88 GMP: 'UNKNOWN', PRE 88 COD: 26.53, Date GMP Extinguished: 'UNKNOWN', 'UNKNOWN'",
        |    "A Method 2 Substitution calculation could not be performed as the Substitution Period involves Pre 75 years.",
        |    "Bereavement Benefit Award (UNKNOWN) recorded on this account from 07/04/2020 to 07/04/2025.",
        |    "Bereavement Benefit Award (UNKNOWN) recorded on this account from 07/04/2025",
        |    "Bereavement Benefit Award recorded on this account between 07/04/2023 and 07/04/2024 with an overlapping period of abroad recorded.",
        |    "Bereavement Benefit Award recorded on this account from 07/04/2024 with an overlapping period of abroad recorded.",
        |    "A Widower's /Civil Partner's cross-reference is recorded on this account, to NINO AA000001, with name Jack. UNKNOWN. Smith.",
        |    "PP/COMP scheme involvement.  Please seek FRY and FRY+1 earnings.",
        |    "Period(s) of CHB S2P are held on this account.",
        |    "CHB S2P Start Date 07/04/2024, CHB S2P End Date 07/04/2025.",
        |    "Periods(s) of ICA S2P are held on this account.",
        |    "ICA S2P Start Date 07/04/2024, ICA S2P End Date 07/04/2025.",
        |    "Period(s) of AA/CAA/DLA S2P are held on this account.",
        |    "AA/CAA/DLA S2P Start Date 07/04/2024, AA/ CAA/DLA S2P End Date 07/04/2025",
        |    "Period(s) of IB (Long Term and Youth)/SDA/ESA S2P are held on this account",
        |    "IB (Long Term and Youth)/SDA/ESA S2P Start Date 07/04/2024,  IB (Long Term and Youth)/SDA/ESA S2P End Date 07/04/2025.",
        |    "The LMA test has been satisfied",
        |    "The LMA test has not been satisfied, the number of S2P qualifying years held is UNKNOWN. (If the LMA test is subsequently satisfied following notification of foreign earnings by Overseas Division a further calculation will be performed.)",
        |    "The Benefit Entitlement include the following Widower's Inheritance Details: Spouse's NINo.: AB123456,  Spouse's Surname: Smith,  Spouse's Forenames: Lucy UNKNOWN,  Spouse's Date of Birth: 25/11/1971,  Spouse's GRB Units: 43,  GRB Unit Value: 0.0748",
        |    "Tax Year: 2024,  BP Increments Weekly Rate: 0.08,  AP Increments Notional Amount: UNKNOWN,  AP Increments Inheritable Amount: UNKNOWN,  GRB Weekly Rate: 3.22,  GRB Increments Weekly Rate: 0.00",
        |    "The following contributions recorded on the account have not yet been through Compliance and Yield checking: <b>See Tax Year Contributions/Credits Screen for more detail",
        |    "Tax Year: 2024",
        |    "Calculation routed to Pre-1975 Section",
        |    "A period of UNKNOWN is recorded on the account between 07/04/2024 and 07/04/2025.",
        |    "NSP Upratings:",
        |    "Uprating date: 07/04/2025 , NSP Entitlement: 252.05, Protected Payment: 21.80, Category BL: 105.70"
        | ]
        }""".stripMargin

    "should match the openapi schema for a full response" in {
      longTermBenefitNotesOpenApi.validateAndGetErrors(
        Json.toJson(longTermBenefitNotesSuccessResponse)
      ) shouldBe Nil
    }

    "deserialises and serialises successfully" in {
      Json.toJson(longTermBenefitNotesSuccessResponse) shouldBe Json.parse(jsonString)
    }

    "deserialises to the model class" in {
      val _: LongTermBenefitNotesSuccessResponse = jsonFormat.reads(Json.parse(jsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue                                                          = Json.parse(jsonString)
      val longTermBenefitNotesSuccessResponse: LongTermBenefitNotesSuccessResponse = jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(longTermBenefitNotesSuccessResponse)

      writtenJson shouldBe jValue
    }

  }

  "StandardErrorResponse400" - {

    val jsonFormat = implicitly[Format[StandardErrorResponse400]]

    def longTermBenefitNotes400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        longTermBenefitNotesOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_400"),
        metaSchemaValidation = Some(Valid(()))
      )

    val standardErrorResponse400 = StandardErrorResponse400(
      HiporiginEnum.Hip,
      LongTermBenefitNotesError.LongTermBenefitNotesError400(
        List(
          LongTermBenefitNotesErrorItem400(
            Reason("HTTP message not readable"),
            NpsErrorCode400.NpsErrorCode400_2
          ),
          LongTermBenefitNotesErrorItem400(
            Reason("Constraint violation: Invalid/Missing input parameter: <parameter>"),
            NpsErrorCode400.NpsErrorCode400_1
          )
        )
      )
    )

    val standardErrorResponse400JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "reason": "HTTP message not readable",
        |       "code": "400.2"
        |     },
        |     {
        |       "reason": "Constraint violation: Invalid/Missing input parameter: <parameter>",
        |       "code": "400.1"
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(standardErrorResponse400) shouldBe Json.parse(
        standardErrorResponse400JsonString
      )
    }

    "deserialises to the model class" in {
      val _: StandardErrorResponse400 =
        jsonFormat.reads(Json.parse(standardErrorResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(standardErrorResponse400JsonString)
      val standardErrorResponse400Json: StandardErrorResponse400 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(standardErrorResponse400Json)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse400" - {

    val jsonFormat = implicitly[Format[HipFailureResponse400]]

    def hipFailureResponse400JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        longTermBenefitNotesOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse400 = HipFailureResponse400(
      HiporiginEnum.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            "",
            Reason("")
          ),
          HipFailureItem(
            "",
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse400JsonString =
      """{
        | "origin": "HIP",
        | "response": {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse400) shouldBe Json.parse(
        hipFailureResponse400JsonString
      )
    }

    "deserialises to the model class" in {
      val _: HipFailureResponse400 =
        jsonFormat.reads(Json.parse(hipFailureResponse400JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse400JsonString)
      val hipFailureResponse400Json: HipFailureResponse400 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse400Json)

      writtenJson shouldBe jValue
    }
  }

  "LongTermBenefitNotesErrorResponse403" - {

    val jsonFormat = implicitly[Format[LongTermBenefitNotesErrorResponse403]]

    val longTermBenefitNotesErrorResponse403_2 =
      LongTermBenefitNotesErrorResponse403(Forbidden, NpsErrorCode403_2)

    val longTermBenefitNotesErrorResponse403_2JsonString =
      """{
        |  "reason": "Forbidden",
        |  "code": "403.2"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(longTermBenefitNotesErrorResponse403_2) shouldBe Json.parse(
        longTermBenefitNotesErrorResponse403_2JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LongTermBenefitNotesErrorResponse403 =
        jsonFormat.reads(Json.parse(longTermBenefitNotesErrorResponse403_2JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue = Json.parse(longTermBenefitNotesErrorResponse403_2JsonString)
      val longTermBenefitNotesErrorResponse403_2: LongTermBenefitNotesErrorResponse403 =
        jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue = jsonFormat.writes(longTermBenefitNotesErrorResponse403_2)

      writtenJson2 shouldBe jValue2
    }
  }

  "LongTermBenefitNotesErrorResponse404" - {

    val jsonFormat = implicitly[Format[LongTermBenefitNotesErrorResponse404]]

    val longTermBenefitNotesErrorResponse404 =
      LongTermBenefitNotesErrorResponse404(ErrorCode404, NotFound)

    val longTermBenefitNotesErrorResponse404JsonString =
      """{
        |  "code": "404",
        |  "reason": "Not Found"
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(longTermBenefitNotesErrorResponse404) shouldBe Json.parse(
        longTermBenefitNotesErrorResponse404JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LongTermBenefitNotesErrorResponse404 =
        jsonFormat.reads(Json.parse(longTermBenefitNotesErrorResponse404JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {

      val jValue2: JsValue = Json.parse(longTermBenefitNotesErrorResponse404JsonString)
      val longTermBenefitNotesErrorResponse404Json: LongTermBenefitNotesErrorResponse404 =
        jsonFormat.reads(jValue2).get
      val writtenJson2: JsValue = jsonFormat.writes(longTermBenefitNotesErrorResponse404Json)

      writtenJson2 shouldBe jValue2
    }
  }

  "LongTermBenefitNotesErrorResponse422" - {

    def longTermBenefitNotes422JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        longTermBenefitNotesOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("errorResponse_422"),
        metaSchemaValidation = Some(Valid(()))
      )

    val jsonFormat = implicitly[Format[LongTermBenefitNotesErrorResponse422]]

    val longTermBenefitNotesErrorResponse422 = LongTermBenefitNotesErrorResponse422(
      failures = Some(
        List(
          LongTermBenefitNotesError.LongTermBenefitNotesError422(
            Reason("HTTP message not readable"),
            ErrorCode422("A589")
          )
        )
      )
    )

    val longTermBenefitNotesErrorResponse422JsonString =
      """{
        |  "failures": [
        |    {
        |      "reason": "HTTP message not readable",
        |      "code": "A589"
        |    }
        |  ]
        |}""".stripMargin

    "should match the openapi schema" in {

      val invalidResponse = LongTermBenefitNotesErrorResponse422(
        Some(
          List(
            LongTermBenefitNotesError.LongTermBenefitNotesError422(
              Reason(
                "some reason with way too many letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters letters"
              ),
              ErrorCode422("")
            )
          )
        )
      )

      longTermBenefitNotes422JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.failures[0].code: must be at least 1 characters long""",
          """$.failures[0].reason: must be at most 128 characters long"""
        )

    }
    "deserialises and serialises successfully" in {
      Json.toJson(longTermBenefitNotesErrorResponse422) shouldBe Json.parse(
        longTermBenefitNotesErrorResponse422JsonString
      )
    }

    "deserialises to the model class" in {
      val _: LongTermBenefitNotesErrorResponse422 =
        jsonFormat.reads(Json.parse(longTermBenefitNotesErrorResponse422JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(longTermBenefitNotesErrorResponse422JsonString)
      val longTermBenefitNotesErrorResponse422: LongTermBenefitNotesErrorResponse422 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(longTermBenefitNotesErrorResponse422)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse500" - {

    val jsonFormat = implicitly[Format[HipFailureResponse500]]

    def hipFailureResponse500JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        longTermBenefitNotesOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse500 = HipFailureResponse500(
      HiporiginEnum.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            "",
            Reason("")
          ),
          HipFailureItem(
            "",
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse500JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse500) shouldBe Json.parse(
        hipFailureResponse500JsonString
      )
    }

    "deserialises to the model class" in {
      val _: HipFailureResponse500 =
        jsonFormat.reads(Json.parse(hipFailureResponse500JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse500JsonString)
      val hipFailureResponse500Json: HipFailureResponse500 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse500Json)

      writtenJson shouldBe jValue
    }
  }

  "HipFailureResponse503" - {

    val jsonFormat = implicitly[Format[HipFailureResponse503]]

    def hipFailureResponse503JsonSchema: SimpleJsonSchema =
      SimpleJsonSchema(
        longTermBenefitNotesOpenApiSpec,
        SpecVersion.VersionFlag.V7,
        Some("HIP-originResponse"),
        metaSchemaValidation = Some(Valid(()))
      )

    val hipFailureResponse503 = HipFailureResponse503(
      HiporiginEnum.Hip,
      HipFailureResponse(
        List(
          HipFailureItem(
            "",
            Reason("")
          ),
          HipFailureItem(
            "",
            Reason("")
          )
        )
      )
    )

    val hipFailureResponse503JsonString =
      """{
        | "origin": "HIP",
        | "response":
        | {
        |   "failures": [
        |     {
        |       "type": "",
        |       "reason": ""
        |     },
        |     {
        |       "type": "",
        |       "reason": ""
        |     }
        |   ]
        | }
        |}""".stripMargin

    "deserialises and serialises successfully" in {
      Json.toJson(hipFailureResponse503) shouldBe Json.parse(
        hipFailureResponse503JsonString
      )
    }

    "deserialises to the model class" in {
      val _: HipFailureResponse503 =
        jsonFormat.reads(Json.parse(hipFailureResponse503JsonString)).get
    }

    "deserialises and reserialises to the same thing (no JSON fields are ignored)" in {
      val jValue: JsValue = Json.parse(hipFailureResponse503JsonString)
      val hipFailureResponse503Json: HipFailureResponse503 =
        jsonFormat.reads(jValue).get
      val writtenJson: JsValue = jsonFormat.writes(hipFailureResponse503Json)

      writtenJson shouldBe jValue
    }
  }

}
