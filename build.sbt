name := "servicediscovery"
version := "0.0.1"

scalaVersion  := Dependencies.scalaVersion

libraryDependencies ++= Dependencies.Compile.all
libraryDependencies ++= Dependencies.Test.all

updateOptions := updateOptions.value.withCachedResolution(true)