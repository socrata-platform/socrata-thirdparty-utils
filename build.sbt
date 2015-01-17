import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.socrata.cloudbeessbt.SocrataCloudbeesSbt.socrataSettings

previousArtifact <<= scalaBinaryVersion { sv => Some("com.socrata" % ("socrata-thirdparty-utils_" + sv) % "2.0.0") }

val commonDeps = Seq(
  "org.slf4j"          % "slf4j-api"           % "1.7.5",
  "net.sf.opencsv"     % "opencsv"             % "2.3" % "optional",
  "com.typesafe"       % "config"              % "1.0.0" % "optional",
  "com.ning"           % "async-http-client"   % "1.7.13" % "optional",
  "org.apache.curator" % "curator-x-discovery" % "2.4.2" % "optional",
  "com.socrata"       %% "socrata-http-jetty"  % "2.3.4" % "optional",
  "com.socrata"       %% "socrata-http-client" % "2.3.4" % "optional",
  "org.scalatest"     %% "scalatest"           % "1.9.1" % "test",
  "com.rojoma"        %% "simple-arm"          % "[1.2.0,2.0.0)",
  "com.rojoma"        %% "simple-arm-v2"       % "2.0.0" % "optional",
  "com.rojoma"        %% "rojoma-json"         % "2.4.3" % "optional",
  "com.rojoma"        %% "rojoma-json-v3"      % "3.2.1" % "optional",
  "com.vividsolutions" % "jts"                 % "1.13" % "optional",
  "nl.grons"          %% "metrics-scala"       % "3.3.0" % "optional",
  "io.dropwizard.metrics" % "metrics-jetty9"   % "3.1.0" % "optional",
  // Use older metrics-graphite to fix issue with reconnecting to graphite
  // See https://github.com/dropwizard/metrics/issues/694
  "com.codahale.metrics" % "metrics-graphite" % "3.0.2" exclude(
                             "com.codahale.metrics", "metrics-core"),
  "com.mchange"        % "c3p0"                % "0.9.5-pre9" % "optional"
)

val testDeps = Seq(
  "org.slf4j"          % "slf4j-simple"        % "1.7.5" % "test",
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
