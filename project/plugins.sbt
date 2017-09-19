resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"
resolvers += "Jenkins repo" at "http://repo.jenkins-ci.org/public/"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.8.0")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1")
addCommandAlias("bintrayPublish", "; set publishTo := (publishTo in bintray).value ; publish; session clear")
