sbtPlugin := true

name := "sbt-s3-resolver"
organization := "ohnosequences"
description := "SBT plugin which provides Amazon S3 bucket resolvers"

javaVersion := "1.7"
scalaVersion := "2.12.3"
sbtVersion := "1.0.1"

bucketSuffix := "era7.com"

resolvers += Resolver.bintrayRepo("ohnosequences", "maven")

libraryDependencies += "ohnosequences" % "ivy-s3-resolver" % "0.11.0"

dependencyOverrides ++= Set(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
)

wartremoverErrors in (Compile, compile) --= Seq(Wart.Any, Wart.NonUnitStatements)
