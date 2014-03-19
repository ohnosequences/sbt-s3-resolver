name := "test-a"

organization := "ohnosequences"

version := "0.0.8"

scalaVersion := "2.10.2"

publishMavenStyle := false

publishTo <<= (isSnapshot, s3credentialsProvider, s3region) {
                (snapshot,   s3prov, s3reg) =>
  Some(toSbtResolver(S3Resolver(
    s3prov,
    overwrite = snapshot,
    s3reg)(
   name = "s3 resolver",
   url = s3("s3://test2.frutero.org")
  )))
}

s3region := com.amazonaws.services.s3.model.Region.EU_Ireland

s3credentials     <<= s3credentialsFile (s3credentialsParser)

s3credentialsProvider <<= s3credentials(staticProvider)

//resolvers <++= s3credentials { cs => Seq(
//    S3Resolver("Snapshots resolver", "s3://test2.frutero.org")
//  ) map {r => cs map r.toSbtResolver} flatten
//}


resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases",
                    "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases",
                    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots",
                    "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
                    "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"                 
                  )







