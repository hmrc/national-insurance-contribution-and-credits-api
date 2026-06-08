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

package uk.gov.hmrc.app.benefitEligibility.testUtils

import cats.data.ValidatedNel
import com.networknt.schema.{Schema, SchemaRegistry, SchemaRegistryConfig, SpecificationVersion}
import tools.jackson.databind.{JsonNode, ObjectMapper}
import tools.jackson.dataformat.yaml.YAMLFactory
import com.networknt.schema.regex.JDKRegularExpressionFactory
import org.scalactic.source.Position
import org.scalatest.Assertions.fail
import org.scalatest.matchers.should.Matchers.*
import play.api.libs.json.{JsString, JsValue, Json}
import tools.jackson.databind.node.ObjectNode

import java.util.Locale
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.Using

sealed trait SchemaValidation {

  /** Validates a given `JsonNode`. Unlikely to be useful in tests, but it's the most basic thing we can do. */
  protected def validateAndGetErrors(jsonNode: JsonNode)(implicit pos: Position): List[String]

  /** Validates JSON given as a `String`. */
  final def validateJsonAndGetErrors(json: String)(implicit pos: Position): List[String] = {
    val jsonNode: JsonNode = SchemaValidation.InternalUtils.jsonToJsonNode(json)
    validateAndGetErrors(jsonNode)
  }

  /** Converts YAML `String` to JSON, then validates it. */
  final def validateYamlAndGetErrors(yaml: String)(implicit pos: Position): List[String] = {
    val jsonNode: JsonNode = SchemaValidation.InternalUtils.yamlToJsonNode(yaml)
    validateAndGetErrors(jsonNode)
  }

  /** Converts a given `JsValue` to a `String`, then validates it. */
  final def validateAndGetErrors(json: JsValue)(implicit pos: Position): List[String] = {
    val jsonString: String = Json.asciiStringify(json)
    validateJsonAndGetErrors(jsonString)
  }

  /** Reads JSON from a file, then validates it. */
  final def validateFromPathAndGetErrors(jsonPath: String)(implicit pos: Position): List[String] = {
    val jsonNode = SchemaValidation.InternalUtils.readJsonNode(jsonOrYamlPath = jsonPath)
    validateAndGetErrors(jsonNode)
  }

}

object SchemaValidation {

  object InternalUtils {

    private val mapper: ObjectMapper   = new ObjectMapper()
    def createObjectNode(): ObjectNode = mapper.createObjectNode()

    def readJsonNode(jsonOrYamlPath: String)(implicit pos: Position): JsonNode = {
      val fileContent = Using(Source.fromFile(jsonOrYamlPath))(_.mkString).get

      jsonOrYamlPath.toLowerCase(Locale.ENGLISH) match {
        case s"$_.json"             => jsonToJsonNode(fileContent)
        case s"$_.yaml" | s"$_.yml" => yamlToJsonNode(fileContent)
        case _ =>
          fail(s"Cannot read file because it doesn't have a json/yaml/yml file extension: $jsonOrYamlPath")
      }
    }

    def jsonToJsonNode(json: String): JsonNode = mapper.readTree(json)

    def yamlToJsonNode(yaml: String): JsonNode = new ObjectMapper(new YAMLFactory()).readTree(yaml)
  }

  final class SimpleJsonSchema(
      jsonSchemaFilePath: String,
      version: SpecificationVersion,
      schemaComponent: Option[String],
      metaSchemaValidation: Option[ValidatedNel[String, Unit]]
  ) extends SchemaValidation {

    /** Constructing this class will automatically verify it against the meta-schema. */
    metaSchemaValidation match {
      case None => ()
      case Some(expectedValidationResult) =>
        val validator            = MetaSchema.jsonSchemaSchema(version)
        val errors: List[String] = validator.validateFromPathAndGetErrors(jsonPath = jsonSchemaFilePath)

        withClue(s"Validate $jsonSchemaFilePath against $validator\n\n") {
          errors shouldBe expectedValidationResult.fold[List[String]](_.toList, { case () => Nil })
        }
    }

    private val jsonSchema: Schema = {

      val config = SchemaRegistryConfig
        .builder()
        .regularExpressionFactory(
          // Without depending on other libraries, it's not possible to validate ECMAScript regular expressions.
          // Not much point anyway, because the abandoned OpenAPI validator library doesn't know how to use them.
          JDKRegularExpressionFactory.getInstance()
        )
        .build()

      val schemaRegistry: SchemaRegistry =
        SchemaRegistry.withDefaultDialect(version, builder => builder.schemaRegistryConfig(config))

      schemaRegistry.getSchema(InternalUtils.readJsonNode(jsonOrYamlPath = jsonSchemaFilePath))
    }

    def getSchemaWithComponents(schemaComponent: String): Either[String, Schema] = {

      val openApiNode = InternalUtils.readJsonNode(jsonSchemaFilePath)

      val schemaNode: JsonNode = openApiNode
        .path("components")
        .path("schemas")
        .path(schemaComponent)

      if (openApiNode.isMissingNode) {
        Left(s"Schema $schemaComponent not found in OpenAPI spec")
      } else {

        val config = SchemaRegistryConfig
          .builder()
          .regularExpressionFactory(JDKRegularExpressionFactory.getInstance())
          .build()

        schemaNode match {
          case objectNode: ObjectNode =>
            val schemaWithComponents = InternalUtils.createObjectNode()
            schemaWithComponents.set("components", openApiNode.path("components"))
            schemaWithComponents.setAll(objectNode)

            val schemaRegistry: SchemaRegistry =
              SchemaRegistry.withDefaultDialect(version, builder => builder.schemaRegistryConfig(config))

            val schema = schemaRegistry.getSchema(schemaWithComponents)

            Right(schema)

          case _ =>
            Left(s"Schema '$schemaComponent' is not a valid object schema")
        }
      }

    }

    protected def validateAndGetErrors(json: JsonNode)(implicit pos: Position): List[String] =
      schemaComponent match {
        case Some(component) =>
          getSchemaWithComponents(component) match {
            case Left(value)   => List(value)
            case Right(schema) => schema.validate(json).asScala.map(_.toString).toList.sorted
          }
        case None => jsonSchema.validate(json).asScala.map(_.toString).toList.sorted
      }

    override def toString: String = s"""${getClass.getSimpleName}(${JsString(jsonSchemaFilePath)}, $version)"""
  }

  object MetaSchema {

    def jsonSchemaSchema(version: SpecificationVersion)(implicit pos: Position): SimpleJsonSchema =
      version match {
        case SpecificationVersion.DRAFT_4 =>
          new SimpleJsonSchema(
            jsonSchemaFilePath = "test/resources/schemas/general/json-meta-schema-v4.json",
            // This version is independent of parameter. It's file-specific.
            SpecificationVersion.DRAFT_4,
            schemaComponent = None,
            metaSchemaValidation = None
          )
        case SpecificationVersion.DRAFT_7 =>
          new SimpleJsonSchema(
            jsonSchemaFilePath = "test/resources/schemas/general/json-meta-schema-v7.json",
            SpecificationVersion.DRAFT_7,
            schemaComponent = None,
            metaSchemaValidation = None
          )
        case _ =>
          fail(
            s"Cannot validate JSON schema $version against a meta-schema because no such meta-schema has been added yet."
          )
      }

  }

}
