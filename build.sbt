import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

mimaDefaultSettings

name := "socrata-thirdparty-utils"

organization := "com.socrata"

version := "1.0.0-SNAPSHOT"

// previousArtifact <<= scalaBinaryVersion { sv => Some("com.socrata" % ("socrata-thirdparty-utils_" + sv) % "1.0.0") }

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "net.sf.opencsv" % "opencsv" % "2.3" % "optional",
  "com.typesafe" % "config" % "1.0.0" % "optional",
  "com.ning" % "async-http-client" % "1.7.13" % "optional",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.5" % "test"
)

scalaVersion := "2.10.0"

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oFD")
