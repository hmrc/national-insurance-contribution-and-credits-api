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
import uk.gov.hmrc.app.benefitEligibility.integration.outbound.ltbNotes.model.response.LongTermBenefitNotesError.{HipFailureItem, HipFailureResponse, HipFailureResponse400, HipFailureResponse500, HipFailureResponse503, LongTermBenefitNotesErrorItem400, LongTermBenefitNotesErrorResponse403, LongTermBenefitNotesErrorResponse404, LongTermBenefitNotesErrorResponse422, StandardErrorResponse400}
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

    val longTermBenefitNotesSuccessResponse = LongTermBenefitNotesSuccessResponse(List.empty)

    val jsonString =
      """{
        | "longTermBenefitNotes":[]
        }""".stripMargin

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

    "should match the openapi schema" in {

      val invalidResponse = StandardErrorResponse400(
        HiporiginEnum.Hip,
        LongTermBenefitNotesError.LongTermBenefitNotesError400(
          List(
            LongTermBenefitNotesErrorItem400(
              Reason("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
              NpsErrorCode400.NpsErrorCode400_2
            ),
            LongTermBenefitNotesErrorItem400(
              Reason(""),
              NpsErrorCode400.NpsErrorCode400_1
            )
          )
        )
      )

      longTermBenefitNotes400JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          //"""$.failures[0].reason: must be at most 128 characters long""",
          //"""$.failures[1].reason: must be at least 1 characters long"""
        ) //TODO: Update with errors when using new .yaml based schema

    }
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

    "should match the openapi schema" in {

      val invalidResponse = HipFailureResponse400(
        origin = HiporiginEnum.Hip,
        response = HipFailureResponse(
          failures = List(
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
      println(Json.toJson(invalidResponse))


      hipFailureResponse400JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.response.failures: must have only unique items in the array"""
        )
    }
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

    "should match the openapi schema" in {

      val invalidResponse = HipFailureResponse500(
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

      hipFailureResponse500JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.response.failures: must have only unique items in the array"""
        )

    }
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

    "should match the openapi schema" in {

      val invalidResponse = HipFailureResponse503(
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

      hipFailureResponse503JsonSchema.validateAndGetErrors(
        Json.toJson(invalidResponse)
      ) shouldBe
        List(
          """$.response.failures: must have only unique items in the array"""
        )

    }
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
