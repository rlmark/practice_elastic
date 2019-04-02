name := "practice_elastic"

version := "0.1"

scalaVersion := "2.12.8"

val http4sVersion = "0.20.0-M7"
val elastic4sVersion = "6.5.1"
val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "org.typelevel" %% "cats-core" % "1.6.0",
  
  "org.http4s"             %% "http4s-dsl"              % http4sVersion,
  "org.http4s"             %% "http4s-blaze-server"     % http4sVersion,
  "org.http4s"             %% "http4s-blaze-client"     % http4sVersion,
  "org.http4s"             %% "http4s-circe"            % http4sVersion,

  "io.circe" %% "circe-core"          % circeVersion,
  "io.circe" %% "circe-generic"       % circeVersion,
  "io.circe" %% "circe-parser"        % circeVersion,
  
  // elasticsearch 
  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,

  // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,

  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test",
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % "test"
)

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "utf8",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-unused-import",
  "-Ypartial-unification",
  "-feature")
