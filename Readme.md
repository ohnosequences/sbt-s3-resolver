## sbt-s3-resolver

This is an sbt-plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Usage

### Add plugin

Either in your `~/.sbt/plugins/plugins.sbt` for global configuration or in `<your_project>/project/plugins.sbt` for per-project add folowing lines:

```scala
resolvers ++= Seq (
  Resolver.url("Era7 Ivy Releases", url("http://releases.era7.com.s3.amazonaws.com"))(
    Patterns("[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]"))
, "nexus CPD" at "http://nexus.cestpasdur.com/nexus/content/repositories/everything/"
, DefaultMavenRepository
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.2.0")
```

#### Credentials

For anything you do with private S3 buckets, you need credentials. `s3credentialsFile` is `Option[String]`, so if it's `None` no credentials and no private things. If it's something, it should be an absolute path to credentials file with access key and secret key in `.properties` format. So to set the key with path to credentials you can add following either to `~/.sbt/global.sbt` for global configuration:

```scala
s3credentialsFile in Global := Some("/funny/absolute/path/to/credentials.properties")
```

or this to `<your_project>/build.sbt`:

```scala
s3credentialsFile := Some("cool/path/in/your/project/credentials.properties")
```

### Use plugin

#### Publishing

Publishing works so far only ivy-style. You can choose either to publish public or private (if you set credentials) and depending on the project `version` key, it will be published as a release or as snapshot.

```scala
publishMavenStyle := false

publishPrivate := false

publishTo <<= (s3credentials, version, publishPrivate)(s3publisher(statikaPrefix)) 
```

for `s3publisher` you can choose either `statikaPrefix` or `era7Prefix` (or even write your own prefix function).

#### Resolving

There are two predefined resolvers for statika bundles, which you can use just as usual:

```scala
resolvers ++= Seq(
    ...
  , PublicBundleSnapshots
  , PublicBundleReleases
  )
```

And there are also resolvers for private bundles, which are again dependent on credentials, so you can set them as follows:

```scala
resolvers <++= s3credentials(PrivateBundleResolvers(statikaPrefix))
```

That's it!

Of course, this plugin should be generalized, to be useful not only for statika.
