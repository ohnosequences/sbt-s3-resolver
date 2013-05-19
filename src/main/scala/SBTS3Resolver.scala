import sbt._
import Keys._

object SBTS3Resolver extends Plugin {

  lazy val publishPrivate = SettingKey[Boolean]("publish-private", 
    "if true, publish to private S3 bucket, else to public")

  lazy val s3credentialsFile = SettingKey[Option[String]]("credentials-file", 
    "properties format file with amazon credentials to access S3")
 
  lazy val s3credentials = SettingKey[Option[(String, String)]]("s3-credentials", 
    "S3 credentials accessKey and secretKey")

  def s3resolver(
      creds: (String, String)
    , isSnapshot:  Boolean = true
    , isPrivate:   Boolean = true
    , isPublisher: Boolean = false
    ) = {
    val privacy   = if(isPrivate) "private." else ""
    val rtype     = if(isSnapshot) "snapshots" else "releases"
    val publisher = if(isPublisher) " publisher" else " resolver"

    val s3r = new org.springframework.aws.ivy.S3Resolver()
    s3r.setName("S3 bucket " + privacy + rtype + publisher)

    s3r.setAccessKey(creds._1)
    s3r.setSecretKey(creds._2)

    val pattern = "s3://"+privacy+rtype+
                  ".statika.ohnosequences.com/[organisation]/[module]/[revision]/[type]s/[artifact].[ext]"    
    s3r.addArtifactPattern(pattern)
    s3r.addIvyPattern(pattern)

    new sbt.RawRepository(s3r)
  }

  def PrivateBundleSnapshots(creds: (String, String)) = s3resolver(creds, isSnapshot = true)
  def PrivateBundleReleases(creds: (String, String))  = s3resolver(creds, isSnapshot = false)

  val PublicBundleSnapshots = Resolver.url("Bundle Snapshots", 
    url("http://snapshots.statika.ohnosequences.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)
  val PublicBundleReleases = Resolver.url("Bundle Releases" , 
    url("http://releases.statika.ohnosequences.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

  // Publishing
  def s3publisher( creds: Option[(String, String)]
                 , ver: String
                 , priv: Boolean) = creds map {
    s3resolver( _
      , isSnapshot = ver.trim.endsWith("SNAPSHOT")
      , isPrivate = priv
      , isPublisher = true
      )
  }

  // default values
  override def settings = Seq(
    s3credentialsFile := None,

    // parsing credentials from the file
    s3credentials <<= s3credentialsFile { file => 
      file map { f: String =>
        val path = new java.io.File(f)
        val p = new java.util.Properties
        p.load(new java.io.FileInputStream(path))
        (p.getProperty("accessKey"),
         p.getProperty("secretKey"))
      }
    }
  )
} 
