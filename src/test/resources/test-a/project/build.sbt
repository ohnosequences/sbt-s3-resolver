resolvers ++= Seq (
  "ohnosequences Releases"  at "http://releases.ohnosequences.com.s3.amazonaws.com",
  "ohnosequences Snapshots" at "http://snapshots.ohnosequences.com.s3.amazonaws.com"
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.4.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")


