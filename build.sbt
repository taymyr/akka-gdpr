organization in ThisBuild := "org.taymyr"
version in ThisBuild := "0.0.1-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val akkaVersion = "2.6.3"
val playVersion = "2.8.1"
val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val protobuf = "com.google.protobuf" % "protobuf-java" % "3.11.1"
val jacksonDataBind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.3"
val playJson = "com.typesafe.play" %% "play-json" % playVersion

val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.0" % Test
val scalaTestWordSpec = "org.scalatest" %% "scalatest-wordspec" % "3.2.0" % Test
val jacksonParameterNames = "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % "2.10.3" % Test

lazy val `akka-gdpr` = (project in file ("gdpr"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      akka,
      protobuf,
      akkaTestKit,
      scalaTest,
      scalaTestWordSpec
    )
  )

lazy val `akka-gdpr-jackson` = (project in file("jackson"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    jacksonDataBind,
    akkaTestKit,
    scalaTest,
    scalaTestWordSpec,
    jacksonParameterNames
  ))
  .settings(javacOptions +="-parameters")
  .dependsOn(`akka-gdpr`)

lazy val `akka-gdpr-play-json` = (project in file("play-json"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    playJson,
    akkaTestKit,
    scalaTest,
    scalaTestWordSpec
  ))
  .dependsOn(`akka-gdpr`)

lazy val `akka-gdpr-root` = (project in file("."))
  .aggregate(`akka-gdpr`, `akka-gdpr-jackson`, `akka-gdpr-play-json`)

lazy val commonSettings = Seq()
