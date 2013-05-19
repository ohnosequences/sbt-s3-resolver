import sbt._
import Keys._

// sbt-release plugin
import sbtrelease.ReleasePlugin._
import sbtrelease._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.ReleaseKeys._

object sbt-s3-resolverBuild extends Build {

  lazy val sbt-s3-resolver = Project(
    id = "sbt-s3-resolver",
    base = file("."),
    settings = Defaults.defaultSettings ++ releaseSettings ++ Seq(

        releaseProcess <<= thisProjectRef apply { ref =>
          Seq[ReleaseStep](
            checkSnapshotDependencies,              // : ReleaseStep
            inquireVersions,                        // : ReleaseStep
            runTest,                                // : ReleaseStep
            setReleaseVersion,                      // : ReleaseStep
            commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
            tagRelease,                             // : ReleaseStep
            publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
            uploadArtifacts,                        // : ReleaseStep, uploads generated artifacts to s3
            setNextVersion,                         // : ReleaseStep
            commitNextVersion,                      // : ReleaseStep
            pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
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

    val s3cmdOutput: String = if (releasedVersion.endsWith("-SNAPSHOT")) {

      st.log.info("a snapshot release")
      st.log.info("artifacts will be uploaded to the snapshots repo")

      Seq (
            "s3cmd", "sync", "-r", "--no-delete-removed", "--disable-multipart",
            "artifacts/snapshots.era7.com/",
            "s3://snapshots.era7.com/"
          ).!!

    } else {

      st.log.info("a normal release")
      st.log.info("artifacts will be uploaded to the releases repo")
      
      Seq (
          "s3cmd", "sync", "-r", "--no-delete-removed", "--disable-multipart",
          "artifacts/releases.era7.com/",
          "s3://releases.era7.com/"
        ).!!
    }

    st.log.info("output from s3cmd: ")
    st.log.info(s3cmdOutput)

    st
  })

}