package ohnosequences.sbt

import sbt._
import Keys._
import com.amazonaws.internal.StaticCredentialsProvider

// import com.amazonaws.services.s3.model.Region;
import com.amazonaws.auth.{BasicAWSCredentials, AWSCredentialsProvider}

object SbtS3Resolver extends Plugin {

  type S3Credentials = (String, String)

  lazy val s3credentialsProvider = SettingKey[AWSCredentialsProvider]("s3credentialsProvider", "AWS credentials provider to access S3")
  lazy val s3credentialsFile = SettingKey[File]("s3credentialsFile", "Properties format file with amazon credentials to access S3")
  lazy val s3credentials = SettingKey[S3Credentials]("s3credentials", "S3 credentials accessKey and secretKey")
  lazy val s3region = SettingKey[Region]("s3region", "AWS Region for your S3 resolvers")
  lazy val s3overwrite = SettingKey[Boolean]("s3overwrite", "Controls whether publishing resolver can overwrite artifacts")
  lazy val s3resolver = SettingKey[(String, s3) => S3Resolver]("s3resolver", "Takes name and bucket url and returns an S3 resolver")


  def s3credentialsParser(file: File): S3Credentials = {
    if (!file.exists)
      sys.error("[WARN] File with S3 credentials doesn't exist: " + file + "; S3 resolvers won't work!")
    else {
      val p = new java.util.Properties
      p.load(new java.io.FileInputStream(file))
      val creds = (p.getProperty("accessKey"), p.getProperty("secretKey"))
      // println("[info] S3 credentials were loaded from " + file)
      creds
    }
  }

  type Region = com.amazonaws.services.s3.model.Region
  
  // S3 bucket url
  case class s3(url: String) {
    // adds s3:// prefix if it was not there
    override def toString: String = "s3://" + url.stripPrefix("s3://")
    
    // convenience method, to use normal bucket addresses with `at`
    // without this resolver: "foo" at s3("maven.bucket.com").toHttp
    def toHttp: String = "http://"+url.stripPrefix("s3://")+".s3.amazonaws.com"
  }

  case class S3Resolver
    (credentialsProvider: AWSCredentialsProvider, overwrite: Boolean, region: Region)
    (name: String, url: s3)
      extends ohnosequences.ivy.S3Resolver(name, credentialsProvider, overwrite, region) {

    def withPatterns(patterns: Patterns): S3Resolver = {
      if (patterns.isMavenCompatible) this.setM2compatible(true)

      def withBase(p: String): String = url.toString.stripSuffix("/") + "/" + p.stripPrefix("/")

      patterns.ivyPatterns.foreach{ p => this.addIvyPattern(withBase(p)) }
      patterns.artifactPatterns.foreach{ p => this.addArtifactPattern(withBase(p)) }

      this
    }

    def withIvyPatterns = withPatterns(Resolver.ivyStylePatterns)
    def withMavenPatterns = withPatterns(Resolver.mavenStylePatterns)
  }


  implicit def toSbtResolver(s3r: S3Resolver): Resolver = {
    if (s3r.getIvyPatterns.isEmpty || s3r.getArtifactPatterns.isEmpty) 
      s3r withPatterns Resolver.defaultPatterns

    new sbt.RawRepository(s3r)
  }

  def staticProvider(credentials: S3Credentials): AWSCredentialsProvider = {
    new StaticCredentialsProvider(new BasicAWSCredentials(credentials._1, credentials._2))
  }

  object S3Resolver {
    lazy val settings = Seq[Setting[_]](
      //s3credentialsFile := file(System.getProperty("user.home")) / ".sbt" / ".s3credentials",
      s3credentials     <<= s3credentialsFile (s3credentialsParser),//,
      s3credentialsProvider <<= s3credentials(staticProvider),
      s3region          := com.amazonaws.services.s3.model.Region.EU_Ireland,
      s3overwrite       <<= isSnapshot,
      s3resolver <<= (s3credentialsProvider, s3overwrite, s3region) (S3Resolver.apply)
    )

  }
}
