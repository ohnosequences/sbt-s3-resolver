## Sbt S3 resolver

This is an sbt plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Usage

### Plugin sbt dependency

In `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.9.0")
```


### Setting keys

| Key                 | Type                         | Default                         |
| ---:                | :----:                       | :------                         |
| `s3credentialsFile` | `File`                       | `~/.sbt/.s3credentials`         |
| `s3credentials`     | `S3Credentials`              | parsed from `s3credentialsFile` |
| `s3region`          | `Region`                     | `EU_Ireland`                    |
| `s3overwrite`       | `Boolean`                    | same as `isSnapshot` key        |
| `s3resolver`        | `(String, s3) => S3Resolver` | is set using all above          |

Note that `S3Credentials` type is just `(String, String)` and by `Region` type we mean `com.amazonaws.services.s3.model.Region`.
Also `S3Resolver` is implicitly converted to `sbt.Resolver`.

To add these defaults to your project add to `build.sbt`

```scala
S3Resolver.settings

// then you can adjust these settings if you need
```


### Credentials

By default, credentials (the access key and secret key of your AWS account (or that of an IAM user)) are expected to be in the `~/.sbt/.s3credentials` file in the following format:

```
accessKey = 322wasa923...
secretKey = 2342xasd8fDfaa9C...
```

If you want to store your credentials somewhere else, you can set it in your `build.sbt` (after `S3Resolver.settings`):

```scala
s3credentialsFile := Some("/funny/absolute/path/to/credentials.properties")
```

> don't forget to **add your credentials file to `.gitignore`**, so that you won't publish this file anywhere

As soon as you set `s3credentialsFile`, the `s3credentials` key contains the parsed credentials from that file.


### Patterns

You can just use `s3resolver` setting key, which takes the name and S3 bucket url and returns `S3Resolver` type. You can also set patterns using `.withPatterns(...)` method. Default are maven-style patterns (just as in sbt), but you can change it with the convenience method `.withIvyPatterns`.


### Publishing

Normal practice is to use different (snapshots and releases) repositories depending on the version. For example, here is such publishing resolver with ivy-style patterns:

```scala
publishMavenStyle := false

publishTo <<= (isSnapshot, s3resolver) { 
                (snapshot,   resolver) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  Some(resolver("My "+prefix+" S3 bucket", s3(prefix+".cool.bucket.com"))
}
```

You can also switch repository for public and private artifacts — you just set the url of your bucket depending on something. Here `s3` constructor takes the name of your S3 bucket (don't worry about `s3://` prefix).


### Resolving

You can add a sequence of s3 resolvers, and `flatten` it in the end, as results are `Option`s:

```scala
resolvers <++= s3resolver { resolver => Seq(
    resolver("Releases resolver", "s3://releases.bucket.com"),
    resolver("Snapshots resolver", "s3://snapshots.bucket.com")
  )
}
```

Note, that in sbt `0.13` you can use `s3resolver.value` notation. For example:

```scala
resolvers ++= Seq(
  s3resolver.value("Releases resolver", "s3://releases.bucket.com") withIvyPatterns,
  s3resolver.value("Snapshots resolver", "s3://snapshots.bucket.com") withMavenPatterns
)
```


#### Note about maven artifacts

This plugin can publish maven or ivy artifacts, but it can resolve only ivy-style artifacts. If your maven artifacts are public, you can resolve them using usual sbt resolvers just transforming your `s3://my.bucket.com` to

```scala
"My S3 bucket" at "http://my.bucket.com.s3.amazonaws.com"
```

i.e. without using this plugin. Or if you're using it anyway, you can write:

```scala
"My S3 bucket" at s3("my.bucket.com").toHttp
```
