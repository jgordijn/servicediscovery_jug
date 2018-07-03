import sbt._

object Dependencies {
  val scalaVersion = "2.12.2"
  val akkaVersion = "2.5.2"
  val akkaHttpVersion = "10.0.6"
  val circeVersion = "0.8.0"

  object Compile {
    val akkaActor     = "com.typesafe.akka" %% "akka-actor"             % akkaVersion
    val akkaRemote    = "com.typesafe.akka" %% "akka-remote"            % akkaVersion
    val akkaDistData  = "com.typesafe.akka" %% "akka-distributed-data"  % akkaVersion
    val akkaHttpCore  = "com.typesafe.akka" %% "akka-http-core"         % akkaHttpVersion
    val akkaHttp      = "com.typesafe.akka" %% "akka-http"              % akkaHttpVersion
//    val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json"   % akkaHttpVersion
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe"        % "1.12.0"
    val circeDeps = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)

    val all = Seq(akkaActor, akkaHttp, akkaHttp, akkaHttpCore, akkaHttpCirce, akkaDistData) ++ circeDeps
  }

  object Test {
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    val all = Seq(akkaTestkit, scalatest)
  }
  
}

