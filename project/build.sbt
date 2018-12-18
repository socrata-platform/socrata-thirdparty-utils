resolvers ++= Seq(
  Resolver.url("socrata releases", url("https://repo.socrata.com/artifactory/ivy-libs-release"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.socrata" % "socrata-sbt-plugins" % "1.6.8")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.1.12")
