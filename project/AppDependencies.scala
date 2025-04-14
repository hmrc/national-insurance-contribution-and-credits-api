import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.11.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "domain-play-30"            % "10.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalamock" %% "scalamock"              % "7.3.0"          % Test,
    "org.scalatest" %% "scalatest"              % "3.2.19"         % Test
  )

  val it = Seq.empty
}
