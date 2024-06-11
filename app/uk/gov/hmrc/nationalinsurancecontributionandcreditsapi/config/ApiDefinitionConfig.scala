package uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config

import play.api.Configuration
import javax.inject.{Inject, Singleton}

trait ApiDefinitionConfig {

  def status(): String

  def accessType(): String

  def endpointsEnabled(): Boolean

}

@Singleton
class ApiDefinitionConfigImpl @Inject()(configuration: Configuration) extends ApiDefinitionConfig {

  private val PRIVATE = "PRIVATE"

  override lazy val status: String = configuration.get[String]("api.status")
  override lazy val accessType: String = configuration.getOptional[String]("api.access.type").getOrElse(PRIVATE)
  override lazy val endpointsEnabled: Boolean = configuration.getOptional[Boolean]("api.endpointsEnabled").getOrElse(false)
}

