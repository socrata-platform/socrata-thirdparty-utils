import com.typesafe.tools.mima.plugin.MimaKeys.previousArtifact
import com.socrata.cloudbeessbt.SocrataCloudbeesSbt.socrataSettings

previousArtifact <<= scalaBinaryVersion { sv => Some("com.socrata" % ("socrata-thirdparty-utils_" + sv) % "2.0.0") }

def astyanaxExcludes(x: ModuleID) = x exclude ("commons-logging", "commons-logging") exclude ("org.mortbay.jetty", "servlet-api") exclude ("javax.servlet", "servlet-api")
val astyanaxVersion =  "1.56.48"
val astyanaxCassandra = astyanaxExcludes("com.netflix.astyanax" % "astyanax-cassandra" % astyanaxVersion % "provided")
val astyanaxThrift = astyanaxExcludes("com.netflix.astyanax" % "astyanax-thrift" % astyanaxVersion % "provided")

val commonDeps = Seq(
  astyanaxCassandra,
  astyanaxThrift,
  "org.slf4j"          % "slf4j-api"           % "1.7.5",
  "net.sf.opencsv"     % "opencsv"             % "2.3" % "provided",
  "com.typesafe"       % "config"              % "1.0.0" % "provided",
  "com.ning"           % "async-http-client"   % "1.7.13" % "provided",
  "org.apache.curator" % "curator-x-discovery" % "2.4.2" % "provided",
  "com.nativelibs4java" %% "scalaxy-loops"     % "0.3.3" % "provided",
  "org.scalacheck"    %% "scalacheck"          % "1.12.2" % "test",
  "org.scalatest"     %% "scalatest"           % "2.2.1" % "test",
  "com.rojoma"        %% "simple-arm"          % "[1.2.0,2.0.0)",
  "com.rojoma"        %% "simple-arm-v2"       % "[2.1.0,3.0.0)" % "provided",
  "com.rojoma"        %% "rojoma-json-v3-grisu"  % "1.0.0" % "provided",
  "org.scalatra"      %% "scalatra"            % "2.3.0" % "provided",
  "com.vividsolutions" % "jts"                 % "1.13" % "provided",
  "nl.grons"          %% "metrics-scala"       % "3.3.0" % "provided",
  "io.dropwizard.metrics" % "metrics-jetty9"   % "3.1.0" % "provided",
  // Use older metrics-graphite to fix issue with reconnecting to graphite
  // See https://github.com/dropwizard/metrics/issues/694
  "com.codahale.metrics" % "metrics-graphite" % "3.0.2" exclude(
    "com.codahale.metrics", "metrics-core"),
  "com.mchange"        % "c3p0"                % "0.9.5-pre9" % "provided"
)

val testDeps = Seq(
  "org.slf4j"          % "slf4j-simple"        % "1.7.5" % "test",
  "org.apache.curator" % "curator-test"        % "2.4.2" % "provided"
)

val mySettings = socrataSettings() ++
  Seq(scalaVersion := "2.11.7",
      crossScalaVersions := Seq("2.10.4", scalaVersion.value))

mySettings

val core = project.settings(
  name := "socrata-thirdparty-utils",
  libraryDependencies ++= commonDeps
).settings(mySettings:_*)

val perf = project.settings(
  name := "socrata-thirdparty-utils-perf",
  libraryDependencies ++= commonDeps
).settings(mySettings:_*)
  .settings(jmhSettings:_*)
  .dependsOn(core)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oFD")
