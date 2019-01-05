import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val `scala 211` = "2.11.12"
lazy val `scala 212` = "2.12.8"

/**
  * Scalac options
  */
lazy val crossBuildOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-Xfuture",
  "-Ypartial-unification",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Yno-adapted-args",
  "-Xlint:_,-type-parameter-shadow",
  "-Xsource:2.13",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfatal-warnings"
)

lazy val scala212Options = Seq(
  "-opt:l:inline",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:_,imports",
  "-opt-warnings",
  "-Xlint:constant",
  "-Ywarn-extra-implicit",
  "-opt-inline-from:**"
)

/**
  * Dependencies
  */
lazy val versionOf = new {
  val shapeless     = "2.3.3"
  val cats          = "1.5.0"
  val refined       = "0.9.3"
  val scalaCheck    = "1.14.0"
  val scalaTest     = "3.0.5"
  val kindProjector = "0.9.9"
  val silencer      = "1.3.1"
  val scalazZio     = "0.5.3"
}

lazy val sharedDependencies = Seq(
  "com.github.ghik" %% "silencer-lib" % versionOf.silencer
) map (_.withSources)

lazy val compilerPluginsDependencies = Seq(
  compilerPlugin(
    "org.spire-math" %% "kind-projector" % versionOf.kindProjector cross CrossVersion.binary
  ),
  compilerPlugin("com.github.ghik" %% "silencer-plugin" % versionOf.silencer)
)

lazy val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % versionOf.scalaCheck % Test,
  "org.scalatest"  %% "scalatest"  % versionOf.scalaTest  % Test
)

lazy val apiDependencies = Seq(
  "com.chuusai"   %% "shapeless" % versionOf.shapeless,
  "eu.timepit"    %% "refined"   % versionOf.refined,
  "org.typelevel" %% "cats-core" % versionOf.cats
) map (_.withSources)

lazy val zioDependencies = Seq(
  "org.scalaz" %% "scalaz-zio" % versionOf.scalazZio
) map (_.withSources)

/**
  * Settings
  */
lazy val crossBuildSettings = Seq(
  scalaVersion        := `scala 212`,
  crossScalaVersions  := Seq(`scala 211`, `scala 212`),
  scalacOptions       ++= crossBuildOptions,
  libraryDependencies ++= sharedDependencies ++ testDependencies ++ compilerPluginsDependencies,
  organization        := "com.github.barambani",
  parallelExecution   in Test := false,
  scalacOptions ++=
    (scalaVersion.value match {
      case `scala 212` =>
        scala212Options
      case _ => Seq()
    })
)

lazy val releaseSettings: Seq[Def.Setting[_]] = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeRelease"),
    pushChanges
  ),
  releaseCrossBuild             := true,
  publishMavenStyle             := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishArtifact               in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  licenses := Seq(
    "MIT License" ->
      url("https://raw.githubusercontent.com/barambani/docker-effect/master/LICENSE")
  ),
  homepage  := Some(url("https://github.com/barambani/http4s-extend")),
  publishTo := sonatypePublishTo.value,
  pomExtra :=
    <scm>
      <url>https://github.com/barambani/docker-effect</url>
      <connection>scm:git:git@github.com:barambani/docker-effect.git</connection>
    </scm>
    <developers>
      <developer>
        <id>barambani</id>
        <name>Filippo Mariotti</name>
        <url>https://github.com/barambani</url>
      </developer>
    </developers>
)

lazy val root = project
  .in(file("."))
  .aggregate(api, zio)
  .settings(crossBuildSettings)
  .settings(releaseSettings)
  .settings(
    name            := "docker-effect",
    publishArtifact := false,
    addCommandAlias("format", ";scalafmt;test:scalafmt;scalafmtSbt"),
    addCommandAlias("updates", ";dependencyUpdates; reload plugins; dependencyUpdates;reload return"),
    addCommandAlias(
      "checkFormat",
      ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck"
    ),
    addCommandAlias("fullCiBuild", ";checkFormat;clean;test")
  )

lazy val api = project
  .in(file("api"))
  .settings(crossBuildSettings)
  .settings(releaseSettings)
  .settings(
    name                := "docker-effect-api",
    libraryDependencies ++= apiDependencies,
    publishArtifact     := false
  )

lazy val zio = project
  .in(file("zio"))
  .dependsOn(api)
  .settings(crossBuildSettings)
  .settings(releaseSettings)
  .settings(
    name                := "docker-effect-zio",
    libraryDependencies ++= zioDependencies
  )
