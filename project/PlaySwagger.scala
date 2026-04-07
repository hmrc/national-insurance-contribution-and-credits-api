import com.iheart.sbtPlaySwagger.SwaggerPlugin.autoImport.*
import sbt.Def

object PlaySwagger {

  lazy val settings: Seq[Def.Setting[_]] = Seq(
    swaggerDomainNameSpaces := Seq(
      "uk.gov.hmrc.app.benefitEligibility.model.request",
      "uk.gov.hmrc.app.benefitEligibility.model.response",
      "uk.gov.hmrc.app.benefitEligibility.model.common",
      "uk.gov.hmrc.app.benefitEligibility.model.nps.niContributionsAndCredits"
    ),
    swaggerRoutesFile := "app.routes",
    swaggerV3         := true,
    swaggerPrettyJson := true
  )

}
