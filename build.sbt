sbtPlugin := true

name         := "sbt-s3-resolver"
organization := "ohnosequences"
description  := "SBT plugin which provides Amazon S3 bucket resolvers"
bucketSuffix := "era7.com"

javaVersion  := "1.8"
scalaVersion := "2.12.3"
sbtVersion   := "1.0.2"

resolvers += Resolver.jcenterRepo
libraryDependencies += "ohnosequences" % "ivy-s3-resolver" % "0.13.0"

bintrayReleaseOnPublish := !isSnapshot.value
bintrayOrganization     := Some(organization.value)
bintrayPackageLabels    := Seq("sbt-plugin", "s3", "resolver")

publishMavenStyle := false
publishTo := (publishTo in bintray).value
