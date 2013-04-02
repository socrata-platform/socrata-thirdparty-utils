import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact

mimaDefaultSettings

name := "socrata-csv"

organization := "com.socrata"

version := "1.1.1-SNAPSHOT"

resolvers += "socrata releases" at "https://repo.socrata.com/artifactory/libs-release"

previousArtifact <<= scalaBinaryVersion { sv => Some("com.socrata" % ("socrata-csv_" + sv) % "1.1.0") }

libraryDependencies += "net.sf.opencsv" % "opencsv" % "2.3"

scalaVersion := "2.10.0"

crossScalaVersions := List("2.8.1", "2.8.2", "2.9.2", "2.10.0")
