resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.4.0-RC3")

dependencyOverrides += "ohnosequences" %% "sbt-s3-resolver" % "0.11.0-SNAPSHOT"
