package ohnosequences.sbt

import sbt._, Keys._
import com.amazonaws.auth._, profile._

object SbtS3Resolver extends AutoPlugin {

  object autoImport {
    // Type aliases
    type Region = com.amazonaws.services.s3.model.Region
    type AWSCredentialsProvider = com.amazonaws.auth.AWSCredentialsProvider
    type S3ACL = com.amazonaws.services.s3.model.CannedAccessControlList

    case class S3Resolver(
      credentialsProvider: AWSCredentialsProvider,
      overwrite: Boolean,
      region: Region,
      acl: S3ACL,
      serverSideEncryption: Boolean
    )(val name: String, val url: s3)
      extends ohnosequences.ivy.S3Resolver(name, credentialsProvider, overwrite, region, acl,serverSideEncryption) {

      def withPatterns(patterns: Patterns): S3Resolver = {
        if (patterns.isMavenCompatible) this.setM2compatible(true)

        def withBase(p: String): String = url.toString.stripSuffix("/") + "/" + p.stripPrefix("/")

        patterns.ivyPatterns.foreach{ p => this.addIvyPattern(withBase(p)) }
        patterns.artifactPatterns.foreach{ p => this.addArtifactPattern(withBase(p)) }

        this
      }

      def withIvyPatterns: S3Resolver = withPatterns(Resolver.ivyStylePatterns)
      def withMavenPatterns: S3Resolver = withPatterns(Resolver.mavenStylePatterns)
    }

    // Just extending AWSCredentialsProvider with | method for combining them in a chain
    case class ExtCredentialsProvider(val provider: AWSCredentialsProvider) {

      def |(another: AWSCredentialsProvider): AWSCredentialsProviderChain =
        new AWSCredentialsProviderChain(provider, another)
    }

    implicit def toAmazonProvider(e: ExtCredentialsProvider): AWSCredentialsProvider = e.provider

    // Converts file to AWSCredentialsProvider (treating it as a properties file)
    implicit def fileToCredsProvider(f: File):
          PropertiesFileCredentialsProvider =
      new PropertiesFileCredentialsProvider(f.getAbsolutePath)

    // Converts anything that can be AWSCredentialsProvider to the extended thing
    implicit def toExtProvider[P](p: P)(implicit prov: P => AWSCredentialsProvider):
      ExtCredentialsProvider =
      ExtCredentialsProvider(prov(p))

    // Converting S3Resolver to the standard sbt Resolver
    implicit def toSbtResolver(s3r: S3Resolver): Resolver = {
      if (s3r.getIvyPatterns.isEmpty || s3r.getArtifactPatterns.isEmpty) {
        s3r withPatterns Resolver.defaultPatterns
      }
      new sbt.RawRepository(s3r)
    }

    // Adding setting keys
    lazy val awsProfile = SettingKey[String]("awsProfile", "AWS credentials profile")
    lazy val s3credentials = SettingKey[AWSCredentialsProvider]("s3credentials", "AWS credentials provider to access S3")
    lazy val s3region = SettingKey[Region]("s3region", "AWS Region for your S3 resolvers")
    lazy val s3overwrite = SettingKey[Boolean]("s3overwrite", "Controls whether publishing resolver can overwrite artifacts")
    lazy val s3sse = SettingKey[Boolean]("s3sse", "Controls whether publishing resolver will use server side encryption")
    lazy val s3acl = SettingKey[S3ACL]("s3acl", "Controls whether published artifacts are accessible publicly via http(s) or not")
    lazy val s3resolver = SettingKey[(String, s3) => S3Resolver]("s3resolver", "Takes name and bucket url and returns an S3 resolver")
    lazy val showS3Credentials = TaskKey[Unit]("showS3Credentials", "Just outputs credentials that are loaded by the s3credentials provider")

    // S3 bucket url
    case class s3(url: String) {
      // adds 's3://' prefix if it was not there
      override def toString: String = "s3://" + url.stripPrefix("s3://")

      // convenience method, to use normal bucket addresses with `at`
      // without this resolver: "foo" at s3("maven.bucket.com").toHttps(s3region.value)
      def toHttps(region: String): String = s"""https://s3-${region}.amazonaws.com/${url.stripPrefix("s3://")}"""
    }
  }
  import autoImport._


  // This plugin will load automatically
  override def trigger: PluginTrigger = allRequirements

  // Default settings
  override def projectSettings: Seq[Setting[_]] = Seq(
    awsProfile := "default",
    s3credentials :=
      new ProfileCredentialsProvider(awsProfile.value) |
      new EnvironmentVariableCredentialsProvider() |
      new InstanceProfileCredentialsProvider(),
    s3region      := com.amazonaws.services.s3.model.Region.EU_Ireland,
    s3overwrite   := isSnapshot.value,
    s3sse         := false,
    s3acl         := com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead,
    s3resolver    := S3Resolver(s3credentials.value, s3overwrite.value, s3region.value, s3acl.value,s3sse.value),
    showS3Credentials := {
      val log = streams.value.log
      val creds = s3credentials.value.getCredentials

      log.info("AWS credentials loaded in 's3credentials' setting key:")
      log.info("Access key: " + creds.getAWSAccessKeyId)
      log.info("Secret key: " + creds.getAWSSecretKey)
    }
  )

}
