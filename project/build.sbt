resolvers ++= Seq(
  "socrata releases" at "https://repository-socrata-oss.forge.cloudbees.com/release"
)

addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.5.3")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.1.12")
