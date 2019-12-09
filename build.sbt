organization := "com.socrata"

mimaPreviousArtifacts := Set("com.socrata" % ("socrata-thirdparty-utils_" + scalaBinaryVersion.value) % "2.0.0")

def astyanaxExcludes(x: ModuleID) = x exclude ("commons-logging", "commons-logging") exclude ("org.mortbay.jetty", "servlet-api") exclude ("javax.servlet", "servlet-api")
val astyanaxVersion =  "1.56.48"
val astyanaxCassandra = astyanaxExcludes("com.netflix.astyanax" % "astyanax-cassandra" % astyanaxVersion % "provided")
val astyanaxThrift = astyanaxExcludes("com.netflix.astyanax" % "astyanax-thrift" % astyanaxVersion % "provided")

def scalatraVersion(scalaVersion: String) =
  scalaVersion match {
    case "2.10.4" => "2.3.0"
    case _ => "2.5.0"
  }

def metrics(scalaVersion: String) =
  scalaVersion match {
    case "2.10.4" =>
      "nl.grons" %% "metrics-scala" % "3.5.10" % "provided"
    case _ =>
      "nl.grons" %% "metrics4-scala" % "4.1.1" % "provided"
  }

def commonDeps(scalaVersion: String) = Seq(
  astyanaxCassandra,
  astyanaxThrift,
  "org.slf4j"          % "slf4j-api"           % "1.7.5",
  "net.sf.opencsv"     % "opencsv"             % "2.3" % "provided",
  "com.typesafe"       % "config"              % "1.0.0" % "provided",
  "com.ning"           % "async-http-client"   % "1.7.13" % "provided",
  "org.apache.curator" % "curator-x-discovery" % "2.4.2" % "provided",
  "org.scalacheck"    %% "scalacheck"          % "1.13.4" % "test",
  "com.rojoma"        %% "simple-arm-v2"       % "[2.1.0,3.0.0)" % "provided",
  "com.rojoma"        %% "rojoma-json-v3-grisu"  % "1.0.0" % "provided",
  "org.scalatra"      %% "scalatra"            % scalatraVersion(scalaVersion) % "provided",
  "com.vividsolutions" % "jts"                 % "1.13" % "provided",
  metrics(scalaVersion),
  "io.dropwizard.metrics" % "metrics-jetty9"   % "4.1.1" % "provided",
  "io.dropwizard.metrics" % "metrics-graphite" % "4.1.1" % "provided",
  "io.dropwizard.metrics" % "metrics-jmx" % "4.1.1" % "provided",
  "com.mchange"        % "c3p0"                % "0.9.5-pre9" % "provided",
  "joda-time" % "joda-time" % "2.0" % "provided"
)

val testDeps = Seq(
  "org.slf4j"          % "slf4j-simple"        % "1.7.5" % "test",
  "org.apache.curator" % "curator-test"        % "2.4.2" % "provided",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

publish / skip := true

val commonSettings = Seq(
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.10.4", "2.11.7", scalaVersion.value),
  organization := "com.socrata",
  scalacOptions ++= Seq("-deprecation")
)

val core = project.settings(
             name := "socrata-thirdparty-utils",
             libraryDependencies ++= commonDeps(scalaVersion.value) ++ testDeps
           ).settings(commonSettings:_*)

val test = project.settings(
             name := "socrata-thirdparty-test-utils",
             libraryDependencies ++= commonDeps(scalaVersion.value) ++ testDeps
           ).settings(commonSettings:_*)
           .dependsOn(core)

val perf = project.settings(
             name := "socrata-thirdparty-utils-perf",
             libraryDependencies ++= commonDeps(scalaVersion.value)
           ).settings(commonSettings:_*)
           .dependsOn(core)

commonSettings

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oFD")
