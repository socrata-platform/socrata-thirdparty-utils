import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.socrata.cloudbeessbt.SocrataCloudbeesSbt.socrataSettings

previousArtifact <<= scalaBinaryVersion { sv => Some("com.socrata" % ("socrata-thirdparty-utils_" + sv) % "2.0.0") }

val commonDeps = Seq(
  "org.slf4j"          % "slf4j-api"           % "1.7.5",
  "net.sf.opencsv"     % "opencsv"             % "2.3" % "optional",
  "com.typesafe"       % "config"              % "1.0.0" % "optional",
  "com.ning"           % "async-http-client"   % "1.7.13" % "optional",
  "org.apache.curator" % "curator-x-discovery" % "2.4.2" % "optional",
  "com.socrata"       %% "socrata-http-client" % "2.0.0" % "optional",
  "org.scalatest"     %% "scalatest"           % "1.9.1" % "test",
  "org.slf4j"          % "slf4j-simple"        % "1.7.5" % "test",
  "com.rojoma"        %% "simple-arm"          % "[1.2.0,2.0.0)",
  "com.rojoma"        %% "rojoma-json"         % "2.4.3" % "optional",
  "com.vividsolutions" % "jts"                 % "1.13" % "optional",
  "nl.grons"          %% "metrics-scala"       % "3.3.0" % "optional",
  "io.dropwizard.metrics" % "metrics-jetty9"   % "3.1.0" % "optional",
  "io.dropwizard.metrics" % "metrics-graphite"   % "3.1.0" % "optional"
)

val testDeps = Seq(
  "org.apache.curator" % "curator-test"        % "2.4.2" % "optional"
)

val mySettings = socrataSettings() ++ Seq(scalaVersion := "2.10.0")

mySettings

val core = project.settings(
             name := "socrata-thirdparty-utils",
             libraryDependencies ++= commonDeps
           ).settings(mySettings:_*)

val test = project.settings(
             name := "socrata-thirdparty-test-utils",
             libraryDependencies ++= commonDeps ++ testDeps
           ).settings(mySettings:_*)
           .dependsOn(core)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oFD")
