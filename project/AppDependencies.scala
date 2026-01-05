import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.1.0"

  val compile = Seq(
    "uk.gov.hmrc"   %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"   %% "domain-play-30"            % "11.0.0",
    "com.beachape"  %% "enumeratum-play-json"      % "1.9.0",
    "org.typelevel" %% "cats-core"                 % "2.13.0",
    "io.scalaland"  %% "chimney"                   % "1.8.2"
  )

  val test = Seq(
    "uk.gov.hmrc"   %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalamock" %% "scalamock"              % "7.3.0"          % Test,
    "org.scalatest" %% "scalatest"              % "3.2.19"         % Test
  )

  val it = Seq.empty
}
