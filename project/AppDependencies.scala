import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.0.0"


  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"             %% "domain-play-30"             % "9.0.0",
    "uk.gov.hmrc"             %% "play-auditing-play-30"      % "9.0.0",
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "org.scalamock"           %% "scalamock"                  % "6.0.0"                     % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.18"                    % Test,

  )

  val it = Seq.empty
}
