sbtPlugin := true

name := "sbt-s3-resolver"
organization := "ohnosequences"
description := "SBT plugin which provides Amazon S3 bucket resolvers"

javaVersion := "1.7"
scalaVersion := "2.10.6"

bucketSuffix := "era7.com"

libraryDependencies += "ohnosequences" % "ivy-s3-resolver" % "0.10.0"

wartremoverErrors in (Compile, compile) --= Seq(Wart.Any, Wart.NonUnitStatements)
