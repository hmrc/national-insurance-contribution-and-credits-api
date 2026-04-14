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

package uk.gov.hmrc.app.config

import uk.gov.hmrc.app.benefitEligibility.model.common.ApiName
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: ServicesConfig) {

  final case class OriginatorIdConfig(standardId: String, searchlightId: String)

  def baseUrl(apiName: ApiName): String = {
    val npsEndpointName: String = apiName match {
      case ApiName.Class2MAReceipts                                                 => "class2MaReceipts"
      case ApiName.Liabilities                                                      => "liabilities"
      case ApiName.NiContributionAndCredits                                         => "niContributionAndCredits"
      case ApiName.MarriageDetails                                                  => "marriageDetails"
      case ApiName.IndividualStatePension                                           => "individualStatePension"
      case ApiName.SchemeMembershipDetails | ApiName.BenefitSchemeDetails           => "schemeMembershipDetails"
      case ApiName.LongTermBenefitNotes | ApiName.LongTermBenefitCalculationDetails => "longTermBenefitCalculation"
    }
    config.baseUrl(s"hip.nps.$npsEndpointName")
  }

  private val hipServicePrefix = "microservice.services.hip"

  val benefitEligibilityInfoEndpointEnabled: Boolean = config.getBoolean("benefitEligibilityInfoEndpointEnabled")
  val hipBaseUrl: String                             = config.baseUrl("hip")

  val hipOriginatorId: String = config.getString(s"$hipServicePrefix.originatorId")

  val npsOriginatorIdPrefix = s"$hipServicePrefix.nps.originatorId"

  val hipOriginatorIdBsp =
    OriginatorIdConfig(
      standardId = config.getString(s"$npsOriginatorIdPrefix.bsp.standard"),
      searchlightId = config.getString(s"$npsOriginatorIdPrefix.bsp.searchlight")
    )

  val hipOriginatorIdGysp = OriginatorIdConfig(
    standardId = config.getString(s"$npsOriginatorIdPrefix.gysp.standard"),
    searchlightId = config.getString(s"$npsOriginatorIdPrefix.gysp.searchlight")
  )

  val hipOriginatorIdMa = OriginatorIdConfig(
    standardId = config.getString(s"$npsOriginatorIdPrefix.ma.standard"),
    searchlightId = config.getString(s"$npsOriginatorIdPrefix.ma.searchlight")
  )

  val hipOriginatorIdEsa = OriginatorIdConfig(
    standardId = config.getString(s"$npsOriginatorIdPrefix.esa.standard"),
    searchlightId = config.getString(s"$npsOriginatorIdPrefix.esa.searchlight")
  )

  val hipOriginatorIdJsa = OriginatorIdConfig(
    standardId = config.getString(s"$npsOriginatorIdPrefix.jsa.standard"),
    searchlightId = config.getString(s"$npsOriginatorIdPrefix.jsa.searchlight")
  )

  private val hipClientId: String     = config.getString(s"$hipServicePrefix.clientId")
  private val hipClientSecret: String = config.getString(s"$hipServicePrefix.clientSecret")

  val base64HipAuthToken: String =
    Base64.getEncoder.encode(s"$hipClientId:$hipClientSecret".getBytes(StandardCharsets.UTF_8)).map(_.toChar).mkString

}
