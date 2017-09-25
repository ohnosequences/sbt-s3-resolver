package ohnosequences.sbt

import sbt._
import Keys._
import com.amazonaws.auth._, profile._
import com.amazonaws.regions.{ Region, Regions, RegionUtils, AwsRegionProvider }
import com.amazonaws.services.s3.AmazonS3

object SbtS3Resolver extends AutoPlugin {

  object autoImport {
    // Type aliases
    type Region                 = com.amazonaws.regions.Region
    type RegionEnum             = com.amazonaws.services.s3.model.Region
    type AWSCredentialsProvider = com.amazonaws.auth.AWSCredentialsProvider
    type S3ACL                  = com.amazonaws.services.s3.model.CannedAccessControlList
    type StorageClass           = com.amazonaws.services.s3.model.StorageClass

    case class S3Resolver(
      credentialsProvider: AWSCredentialsProvider,
      overwrite: Boolean,
      region: Region,
      acl: Option[S3ACL],
      serverSideEncryption: Boolean,
      storageClass: StorageClass
    )(val name: String, val url: s3)
      extends ohnosequences.ivy.S3Resolver(name, credentialsProvider, overwrite, region, acl.orNull, serverSideEncryption, storageClass) {

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
      new sbt.RawRepository(s3r, s3r.name)
    }

    // Trying to parse region name
    private def regionFromString(regionStr: String): Region =
      Option(RegionUtils.getRegion(regionStr)).getOrElse {
        sys.error(s"Couldn't convert string [${regionStr}] to a valid Region value")
      }

    // Converting different types to the same Region type:
    implicit def     fromEnumToAWSRegion(region: RegionEnum): Region = region.toAWSRegion
    implicit def  fromRegionsToAWSRegion(region: Regions):    Region = Region.getRegion(region)
    implicit def fromProviderToAWSRegion(provider: AwsRegionProvider): Region = regionFromString(provider.getRegion())

    // Adding setting keys
    lazy val awsProfile     = settingKey[String]("AWS credentials profile")
    lazy val s3credentials  = settingKey[AWSCredentialsProvider]("AWS credentials provider to access S3")
    lazy val s3region       = settingKey[Region]("AWS Region for your S3 resolvers")
    lazy val s3overwrite    = settingKey[Boolean]("Controls whether publishing resolver can overwrite artifacts")
    lazy val s3sse          = settingKey[Boolean]("Controls whether publishing resolver will use server side encryption")
    @deprecated("Use s3optAcl instead. This currently forwards the value to s3optAcl, but is overwritten by it.")
    lazy val s3acl          = settingKey[S3ACL]("Controls whether published artifacts are accessible publicly via http(s) or not")
    lazy val s3optAcl       = settingKey[Option[S3ACL]]("Controls published artifacts visibility via http(s) or inherits bucket setting")
    lazy val s3storageClass = settingKey[StorageClass]("Controls storage class for the published S3 objects")
    lazy val s3resolver     = settingKey[(String, s3) => S3Resolver]("Takes name and bucket url and returns an S3 resolver")

    lazy val showS3Credentials = taskKey[Unit]("Just outputs credentials that are loaded by the s3credentials provider")

    // S3 bucket url
    case class s3(bucketName: String) {
      // adds 's3://' prefix if it was not there
      override def toString: String = "s3://" + bucketName.stripPrefix("s3://")

      // convenience method, to use normal bucket addresses with `at` without this resolver: `"foo" at s3("maven.bucket.com").toHttps(s3region.value)`
      def toHttps(region: Region): String =
        if (!region.hasHttpsEndpoint(AmazonS3.ENDPOINT_PREFIX)) {
          sys.error(s"Region [${region}] doesn't have a HTTPS S3 endpoint")
        } else {
          /* Endpoint prefix varies for different regions. For example
            - EU_Frankfurt: `s3.eu-central-1.amazonaws.com`
            - EU_Ireland: `s3-eu-west-1.amazonaws.com`
            - CN_Beijing: `s3.cn-north-1.amazonaws.com.cn`
          */
          val endpointHost = region.getServiceEndpoint(AmazonS3.ENDPOINT_PREFIX)
          val path = "/" + bucketName.stripPrefix("s3://")

          new java.net.URL("https", endpointHost, path).toString
        }

      // Same but tying to parse region from a string
      def toHttps(regionStr: String):  String = toHttps(regionFromString(regionStr))
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
      InstanceProfileCredentialsProvider.getInstance(),
    s3region       := new com.amazonaws.regions.DefaultAwsRegionProviderChain(),
    s3overwrite    := isSnapshot.value,
    s3sse          := false,
    s3acl          := com.amazonaws.services.s3.model.CannedAccessControlList.PublicRead,
    s3optAcl       := Option(s3acl.value),
    s3storageClass := com.amazonaws.services.s3.model.StorageClass.Standard,
    s3resolver     := S3Resolver(
      s3credentials.value,
      s3overwrite.value,
      s3region.value,
      s3optAcl.value,
      s3sse.value,
      s3storageClass.value
    ),
    showS3Credentials := {
      val log = streams.value.log
      val creds = s3credentials.value.getCredentials

      log.info("AWS credentials loaded in 's3credentials' setting key:")
      log.info("Access key: " + creds.getAWSAccessKeyId)
      log.info("Secret key: " + creds.getAWSSecretKey)
    }
  )

}
