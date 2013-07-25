## sbt-s3-resolver

This is an sbt-plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Usage

### Add plugin

Either in your `~/.sbt/plugins/plugins.sbt` for global configuration or in `<your_project>/project/plugins.sbt` for per-project configuration, add some the resolver plugin:

```scala
resolvers += "Era7 Releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.4.0")
```

#### Set credentials

For anything you do with S3 buckets, you need credentials. `s3credentialsFile` is `Option[String]` and by default it's `None`. So to set the key with path to credentials you can add following either to `~/.sbt/global.sbt` for global configuration:

```scala
s3credentialsFile in Global := Some("/funny/absolute/path/to/credentials.properties")
```

or this to `<your_project>/build.sbt`:

```scala
s3credentialsFile in Global := Some("cool/path/in/your/project/to/credentials.properties")
```

This file should contain the access key and secret key of your AWS account (or that of an IAM user), in the following format:

```
accessKey = 322wasa923...
secretKey = 2342xasd8fDfaa9C...
```

As soon as you set `s3credentialsFile`, the `s3credentials` key contains the parsed credentials from that file.

### Use resolver

You can construct s3 resolver using `s3resolver` function:

```scala
def s3resolver(
      name: String
    , url: String
    , pattern: String = Resolver.mavenStyleBasePattern
    )(credentials: S3Credentials): Resolver
```

Default is maven-style pattern, but you can change it.

#### Publishing

Normal practice is to use different (snapshots and releases) repositories depending on the version:

```scala
publishMavenStyle := false

publishTo <<= (isSnapshot, s3credentials) { 
                (snapshot,   credentials) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  // if credentials are None, publishTo is also None
  credentials map s3resolver("My "+prefix+" S3 bucket", "s3://"+prefix+".cool.bucket.com")
}
```

You can also switch repository for public and private artifacts — you just set the url of your bucket depending on something.

#### Resolving

You can add a sequence of s3 resolvers, and `flatten` it in the end, as results are `Option`s:

```scala
resolvers <++= s3credentials { cs => Seq(
    cs map s3("Releases resolver", "s3://releases.bucket.com")
  , cs map s3("Snapshots resolver", "s3://snapshots.bucket.com")
  ).flatten }
```

**That's it!**


### Changing this plugin

If you made some changes and want to publish this plugin, you should set credentials. It uses itself for publishing, so if you have no access to it's current version artifact, you can publish it locally and then use itself for publishing to a repository.
