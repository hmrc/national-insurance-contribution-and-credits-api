import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("national-insurance-contribution-and-credits-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    routesImport += "uk.gov.hmrc.nationalinsurancecontributionandcreditsapi.config.Binders._",
    libraryDependencies += "uk.gov.hmrc" %% "domain-play-30" % "9.0.0",
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .settings(resolvers ++= Seq(
    MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2"),
    Resolver.jcenterRepo))
  .settings(CodeCoverageSettings.settings *)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
  )
  .settings(PlayKeys.playDefaultPort := 16105)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
