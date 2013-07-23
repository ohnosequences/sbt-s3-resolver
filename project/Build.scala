
import sbt._
import Keys._

// sbt-release plugin
import sbtrelease.ReleasePlugin._
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.ReleaseKeys._

import java.io.File
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}

object S3Sync {

  def sync(credentialsFile: File, path: File, bucket: String, version: String) {
    val files= recursiveListFiles(path).filterNot(_.isDirectory).filter(_.getName.contains(version))
    val s3Client = new AmazonS3Client(new PropertiesCredentials(credentialsFile))
    s3Client.setRegion(Region.getRegion(Regions.EU_WEST_1))


    if (!s3Client.doesBucketExist(bucket)) {
      s3Client.createBucket(bucket)
    }

    files.foreach { file =>
      val prefix = path.getPath + File.separator
      val key = file.getPath.replace(prefix, "")
      println("uploading " + key)
      s3Client.putObject(new PutObjectRequest(bucket , key, file)
        .withCannedAcl(CannedAccessControlList.PublicRead)
      )
    }
  }

  def recursiveListFiles(file: File, exclude: List[File] = Nil, root: Boolean = true): List[File] = root match {
    case false =>
      if (exclude.contains(file)) {
        Nil
      } else {
        val these = file.listFiles.toList
        these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, exclude, false))
      }
    case true => file +: recursiveListFiles(file, exclude, false)
  }
}


object sbtS3ResolverBuild extends Build {

  lazy val sbtS3Resolver = Project(
    id = "sbt-s3-resolver",
    base = file("."),
    settings = Defaults.defaultSettings ++ releaseSettings ++ Seq(

        releaseProcess <<= thisProjectRef apply { ref =>
          Seq[ReleaseStep](
            checkSnapshotDependencies,              // : ReleaseStep
            inquireVersions,                        // : ReleaseStep
            runTest,                                // : ReleaseStep
            setReleaseVersion,                      // : ReleaseStep
            publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
            uploadArtifacts                        // : ReleaseStep, uploads generated artifacts to s3
          )
        }
      )
  )

  // sample release step
  val uploadArtifacts = ReleaseStep(action = st => {
    // extract the build state
    val extracted = Project.extract(st)
    // get version
    // WARN: this is the version in build.sbt!!
    val releasedVersion = extracted.get(version in ThisBuild)
    val credentials = Some(new File("AwsCredentials.properties"))

    if (credentials.isEmpty) {
      st.log.info("you should specify s3credentialsFile")
    } else {
      if (releasedVersion.endsWith("-SNAPSHOT")) {
        st.log.info("a snapshot release")
        st.log.info("artifacts will be uploaded to the snapshots repo")
        S3Sync.sync(credentials.get, new File("artifacts/snapshots.era7.com"), "snapshots.era7.com", releasedVersion)
      } else {
        st.log.info("a normal release")
        st.log.info("artifacts will be uploaded to the releases repo")
        S3Sync.sync(credentials.get, new File("artifacts/releases.era7.com"), "releases.era7.com", releasedVersion)
      }
    }

    st

  })

}