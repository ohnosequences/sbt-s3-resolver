
import sbtrelease._
import ReleaseStateTransformations._

import SbtS3ResolverBuild._

sbtPlugin := true

name := "sbt-s3-resolver"

organization := "ohnosequences"

description := "sbt plugin which provides s3 resolvers for statika bundles"

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0")

publishMavenStyle := false

s3credentialsFile := Some("AwsCredentials.properties")

publishPrivate := false

publishTo <<= (s3credentials, version, publishPrivate)(s3publisher(era7Prefix)) 

resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases"
                  , "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases"
                  , "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"
                  , "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com"
                  , "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
                  , DefaultMavenRepository
                  , "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/repositories/everything/"
                  )

libraryDependencies += "org.springframework.aws" % "spring-aws-ivy" % "1.0.3"

// scalacOptions ++= Seq(
//                       "-feature",
//                       "-language:higherKinds",
//                       "-language:implicitConversions",
//                       "-deprecation",
//                       "-unchecked"
//                     )
