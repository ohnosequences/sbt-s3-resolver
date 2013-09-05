resolvers ++= Seq(
  "Era7 Releases"  at "http://releases.era7.com.s3.amazonaws.com"
, Resolver.url("Era7 releases", url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.5.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7.1")
