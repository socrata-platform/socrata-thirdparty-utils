resolvers ++= Seq(
  Resolver.url("socrata releases", url("https://repo.socrata.com/artifactory/ivy-libs-release"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.6.1")
