sbtPlugin := true

name         := "sbt-s3-resolver"
organization := "ohnosequences"
description  := "SBT plugin which provides Amazon S3 bucket resolvers"

javaVersion  := "1.8"
scalaVersion := "2.12.3"
sbtVersion   := "1.0.2"

bucketSuffix := "era7.com"

resolvers += Resolver.jcenterRepo

libraryDependencies += "ohnosequences" % "ivy-s3-resolver" % "0.12.0"

dependencyOverrides ++= Set(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
)

wartremoverErrors in (Compile, compile) --= Seq(Wart.Any, Wart.NonUnitStatements)

bintrayReleaseOnPublish := !isSnapshot.value
bintrayOrganization     := Some(organization.value)
bintrayPackageLabels    := Seq("sbt-plugin", "s3", "resolver")

publishMavenStyle := false
publishTo := (publishTo in bintray).value
