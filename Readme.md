## sbt-s3-resolver

This is an sbt-plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Usage

### Add plugin

Either in your `~/.sbt/plugins/plugins.sbt` for global configuration or in `<your_project>/project/plugins.sbt` for per-project configuration, add some strange resolvers:

```scala
resolvers ++= Seq (
  Resolver.url("Era7 Ivy Releases", url("http://releases.era7.com.s3.amazonaws.com"))(
    Patterns("[organisation]/[module]/[revision]/[type]s/[artifact](-[classifier]).[ext]"))
, DefaultMavenRepository
, "doveltech" at "http://www.doveltech.com/maven/"
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.3.0")
```

#### Credentials

For anything you do with private S3 buckets, you need credentials. `s3credentialsFile` is `Option[String]`, so if it's `None` no credentials and no private things. If it's something, it should be an absolute path to credentials file with access key and secret key in `.properties` format. So to set the key with path to credentials you can add following either to `~/.sbt/global.sbt` for global configuration:

```scala
s3credentialsFile in Global := Some("/funny/absolute/path/to/credentials.properties")
```

or this to `<your_project>/build.sbt`:

```scala
s3credentialsFile in Global := Some("cool/path/in/your/project/credentials.properties")
```

This file should contain the access key and secret key of your AWS account (or that of an IAM user), in the following format:

```
accessKey = 322wasa923...
secretKey = 2342xasd8fDfaa9C...
```

### Use plugin

> Everything is ivy-style.

You can construct s3 resolver using `s3resolver` setting key, it depends on `s3credentials` (which is set, if you set `s3credentialsFile`) and `s3pattern` artifact pattern (which is ivy by default, but you can change it). The key has type 

```scala
SettingKey[ (String, String) => Option[Resolver] ]
```

So that it returns function of two parameters: _name_ and _url prefix_ of repository.

#### Publishing

Normal practice is to use different (snapshots and releases) repositories depending on version:

```scala
publishMavenStyle := false

publishTo <<= (isSnapshot, s3resolver) { 
                (snapshot,   resolver) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  resolver("My ivy "+prefix+" S3 bucket", "s3://"+prefix+".cool.bucket.com")
}
```

You can also switch repository for public and private artifacts — you just set the url of your bucket depending on something.

#### Resolving

You can add a sequence of s3 resolvers, and `flatten` it in the end, as results are `Option`s:

```scala
resolvers <++= s3resolver { s3 => Seq(
    s3("Releases resolver", "s3://releases.bucket.com")
  , s3("Snapshots resolver", "s3://snapshots.bucket.com")
  ).flatten }
```

**That's it!**
