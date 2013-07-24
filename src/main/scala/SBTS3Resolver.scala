import sbt._
import Keys._

object SbtS3Resolver extends Plugin {

  type S3Credentials = (String, String)

  lazy val s3credentialsFile = 
    SettingKey[Option[String]]("s3-credentials-file", 
      "properties format file with amazon credentials to access S3")
 
  lazy val s3credentials = 
    SettingKey[Option[S3Credentials]]("s3-credentials", 
      "S3 credentials accessKey and secretKey")

  // parsing credentials from the file
  private def s3credentialsParser(file: Option[String]): Option[S3Credentials] = {

    file map { f: String =>
      val path = new java.io.File(f)
      val p = new java.util.Properties
      p.load(new java.io.FileInputStream(path))
      ( p.getProperty("accessKey")
      , p.getProperty("secretKey") )
    }

  }

  // setting up resolver depending on credentials and pattern
  def s3resolver(
      name: String
    , url: String
    , pattern: String = Resolver.mavenStyleBasePattern
    )(credentials: S3Credentials
    ): Resolver = {

      val s3r = new ohnosequences.ivy.S3Resolver()

      s3r.setName(name)
      
      val fullPattern = url +"/"+ pattern
      s3r.addArtifactPattern(fullPattern)
      s3r.addIvyPattern(fullPattern)

      credentials match {
        case (user, pass) =>
          s3r.setAccessKey(user)
          s3r.setSecretKey(pass)
          new sbt.RawRepository(s3r)
      }

    }

  // default values
  override def settings = Seq(
    s3credentialsFile in Global := None
  , s3credentials     in Global <<= s3credentialsFile (s3credentialsParser)
  )
} 
