addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

// using itself for publishing
resolvers += "Era7 Releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.4.0")
