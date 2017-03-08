import sbt._

object Dependencies {
  val scalaVersion = "2.12.1"
  val akkaVersion = "2.5-M2"
  val akkaHttpVersion = "10.0.4"

  object Compile {
    val akkaActor     = "com.typesafe.akka" %% "akka-actor"             % akkaVersion
    val akkaRemote    = "com.typesafe.akka" %% "akka-remote"            % akkaVersion
    val akkaDistData  = "com.typesafe.akka" %% "akka-distributed-data"  % akkaVersion
    val akkaHttpCore  = "com.typesafe.akka" %% "akka-http-core"         % akkaHttpVersion
    val akkaHttp      = "com.typesafe.akka" %% "akka-http"              % akkaHttpVersion
    val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json"   % akkaHttpVersion

    val all = Seq(akkaActor, akkaHttp, akkaHttp, akkaHttpCore, akkaSprayJson, akkaDistData)
  }

  object Test {
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    val all = Seq(akkaTestkit, scalatest)
  }
  
}

