
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

sbtPlugin := true

name := "sbt-s3-resolver"

organization := "ohnosequences"

description := "SBT plugin which provides Amazon S3 bucket resolvers"

scalaVersion := "2.9.2"

publishMavenStyle := true

//// For publishing set s3credentialsFile (see readme)
publishTo <<= (isSnapshot, s3credentials) { 
                (snapshot,   credentials) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  credentials map s3resolver("Era7 "+prefix+" S3 bucket", "s3://"+prefix+".era7.com")
}

resolvers ++= Seq ( 
    "Era7 Releases"  at "http://releases.era7.com.s3.amazonaws.com"
  , "Era7 Snapshots" at "http://snapshots.era7.com.s3.amazonaws.com"
  )

libraryDependencies += "ohnosequences" %% "ivy-s3-resolver" % "0.0.6"

// sbt-release
releaseSettings
