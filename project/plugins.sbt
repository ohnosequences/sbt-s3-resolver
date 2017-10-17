resolvers ++= Seq(
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  "repo.jenkins-ci.org" at "https://repo.jenkins-ci.org/public",
  Resolver.jcenterRepo
)
addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.9.0")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
