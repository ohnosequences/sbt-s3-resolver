name := "test-a"

organization := "ohnosequences"

version := "0.0.7"

scalaVersion := "2.10.2"

publishMavenStyle := false

publishTo <<= (isSnapshot, s3credentials) { 
                (snapshot,   credentials) => 
  credentials map S3Resolver(
    "s3 resolver",
    "s3://test2.frutero.org",
    Resolver.ivyStylePatterns
  ).toSbtResolver
}

resolvers <++= s3credentials { cs => Seq(
    S3Resolver("Snapshots resolver", "s3://test2.frutero.org")
  ) map {r => cs map r.toSbtResolver} flatten
}


resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases",
                    "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases",
                    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots",
                    "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
                    "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"                 
                  )







