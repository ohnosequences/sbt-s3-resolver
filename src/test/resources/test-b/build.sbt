import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

name := "test-b"

organization := "ohnosequences"

version := "0.0.1"

scalaVersion := "2.10.0"

publishMavenStyle := true

publishTo <<= (isSnapshot, s3resolver) { 
                (snapshot,   resolver) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  resolver("ohnosequences "+prefix+" S3 bucket", "s3://"+prefix+".ohnosequences.com")
}

resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases",
                    "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases",
                    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots",
                    "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
                    "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"                 
                  )

s3credentialsFile in Global := Some("/home/evdokim/AwsCredentials.properties")

resolvers <++= s3resolver { s3 => Seq(
    s3("Releases resolver", "s3://releases.ohnosequences.com")
  , s3("Snapshots resolver", "s3://snapshots.ohnosequences.com")
  ).flatten }

libraryDependencies += "ohnosequences" % "test-a_2.10" % "0.0.1"

libraryDependencies += "ohnosequences" % "ivy-s3-resolver_2.9.2" % "0.0.6"


scalacOptions ++= Seq(
                      "-feature",
                      "-language:higherKinds",
                      "-language:implicitConversions",
                      "-language:postfixOps",
                      "-deprecation",
                      "-unchecked"
                    )

// sbt-release settings

releaseSettings

releaseProcess <<= thisProjectRef apply { ref =>
  Seq[ReleaseStep](
    checkSnapshotDependencies
  , inquireVersions
  , setReleaseVersion
  , publishArtifacts
  , setNextVersion
  )
}