
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

sbtPlugin := true

name := "sbt-s3-resolver"

organization := "ohnosequences"

description := "sbt plugin which provides s3 resolvers for statika bundles"

scalaVersion := "2.9.2"

// crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0")

publishMavenStyle := false

publishTo <<= (isSnapshot, s3resolver) { 
                (snapshot,   resolver) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  resolver("Era7 "+prefix+" S3 bucket", "s3://"+prefix+".era7.com")
}

resolvers ++= Seq ( Resolver.typesafeRepo("releases")
                  , Resolver.sonatypeRepo("releases")
                  , Resolver.sonatypeRepo("snapshots")
                  , DefaultMavenRepository
                  , "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/repositories/everything/"
                  )

libraryDependencies += "org.springframework.aws" % "spring-aws-ivy" % "1.0.3"

// sbt-release settings

releaseSettings

releaseProcess <<= thisProjectRef apply { ref =>
  Seq[ReleaseStep](
    checkSnapshotDependencies
  , inquireVersions
  , runTest
  , setReleaseVersion
  , commitReleaseVersion
  , tagRelease
  , publishArtifacts
  , setNextVersion
  , commitNextVersion
  , pushChanges
  )
}
