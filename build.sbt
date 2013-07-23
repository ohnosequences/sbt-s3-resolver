
import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._

sbtPlugin := true

name := "sbt-s3-resolver"

organization := "ohnosequences"

description := "sbt plugin which provides s3 resolvers for statika bundles"

scalaVersion := "2.9.2"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("local-snapshots", file("artifacts/snapshots.era7.com")))
  else
    Some(Resolver.file("local-releases", file("artifacts/releases.era7.com")))
}

resolvers ++= Seq ( Resolver.typesafeRepo("releases")
                  , Resolver.sonatypeRepo("releases")
                  , Resolver.sonatypeRepo("snapshots")
                  , DefaultMavenRepository
                  , "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com"
                  , "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
                  )

libraryDependencies += "ohnosequences" % "ivy-s3-resolver_2.9.2" % "0.0.6"

