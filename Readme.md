## Sbt S3 resolver

This is an sbt plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Features

This plugin can publish artifacts in maven or ivy style, but it can resolve only ivy artifacts:

| _Ivy artifacts_ | publish | resolve |     | _Maven artifacts_ | publish | resolve |
| :-------------: | :-----: | :-----: | --- | :---------------: | :-----: | :-----: |
|    **public**   |    ✓    |    ✓    |     |     **public**    |    ✓    |  [__*__](#public-maven-artifacts)  |
|   **private**   |    ✓    |    ✓    |     |    **private**    |    ✓    |    ✗    |


## Usage

### Plugin sbt dependency

In `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "<version>")
```

(see the latest version in the [releases list](https://github.com/ohnosequences/sbt-s3-resolver/releases))

> Note that since `v0.11.0` this plugin is compiled and published _only for scala-2.10/sbt-0.13_ or higher. If you want it for sbt-0.12, use version `v0.10.1`.

### Setting keys

|       Key       |             Type             |          Default          |
| --------------: | :--------------------------: | :------------------------ |
|    `awsProfile` |           `String`           | `"default"`               |
| `s3credentials` |   `AWSCredentialsProvider`   | see [below](#credentials) |
|      `s3region` |           `Region`           | `EU_Ireland`              |
|   `s3overwrite` |          `Boolean`           | same as `isSnapshot` key  |
|         `s3acl` |           `S3ACL`            | `PublicRead`              |
|    `s3resolver` | `(String, s3) => S3Resolver` | is set using all above    |

Where

```scala
type Region = com.amazonaws.services.s3.model.Region
type AWSCredentialsProvider = com.amazonaws.auth.AWSCredentialsProvider
type S3ACL = com.amazonaws.services.s3.model.CannedAccessControlList
```

To add these defaults to your project add to `build.sbt`

```scala
S3Resolver.defaults

// then you can adjust these settings if you need
```

You can just use `s3resolver` setting key that takes a _name_ and an _S3 bucket url_ and returns `S3Resolver` which is implicitly converted to `sbt.Resolver`.


### Publishing

Normal practice is to use different (snapshots and releases) repositories depending on the version. For example, here is such publishing resolver with ivy-style patterns:

```scala
publishMavenStyle := false

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value("My "+prefix+" S3 bucket", s3(prefix+".cool.bucket.com")) withIvyPatterns)
}
```

You can also switch repository for public and private artifacts — you just set the url of your bucket depending on something. Here `s3` constructor takes the name of your S3 bucket (don't worry about `s3://` prefix).


### Resolving

You can add a sequence of S3 resolvers just like this:

```scala
resolvers ++= Seq[Resolver](
  s3resolver.value("Releases resolver", s3("releases.bucket.com")),
  s3resolver.value("Snapshots resolver", s3("snapshots.bucket.com"))
)
```

Note, that you have to write `Seq[Resolver]` explicitly, so that `S3Resolver`s will be converted to `sbt.Resolver` before appending.

#### Public Maven artifacts

If your maven artifacts are public, you can resolve them using usual sbt resolvers just transforming your `s3://my.bucket.com` to

```scala
"My S3 bucket" at "https://s3-<region>.amazonaws.com/my.bucket.com"
```

i.e. without using this plugin. Or if you're using it anyway, you can write:

```scala
"My S3 bucket" at s3("my.bucket.com").toHttps(s3region.value)
```


### Credentials

`s3credentials` key has the [`AWSCredentialsProvider`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html) type from AWS Java SDK. Different kinds of providers look for credentials in different places, plus they can be [chained](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProviderChain.html) by the `|` ("or") operator (added in this plugin for convenience). 

The **default credentials** chain in this plugin is

```scala
awsProfile := "default"

s3credentials :=
  new ProfileCredentialsProvider(awsProfile.value) |
  new InstanceProfileCredentialsProvider()
```

* [`new ProfileCredentialsProvider(...)`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/ProfileCredentialsProvider.html) which loads credentials for an AWS profile config file
* [`new InstanceProfileCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/InstanceProfileCredentialsProvider.html) which loads credentials from the Amazon EC2 Instance Metadata Service

You can find other types of credentials providers in the [AWS Java SDK docs](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html)

If you have [different profiles](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html#credentials-file-format) in your `~/.aws/credentials` file, you can choose the one you need by setting

```scala
awsProfile := "bob"
```

Or if you need a completely different credentials providers chain, you can change it, for example, like this:

```scala
s3credentials :=
  file(System.getProperty("user.home")) / ".sbt" / ".s3credentials" |
  new EnvironmentVariableCredentialsProvider() |
  new SystemPropertiesCredentialsProvider()
```

You can check which credentials are loaded with the `showS3Credentials` task:

```bash
sbt showS3Credentials
```


### Patterns

You can set patterns using `.withPatterns(...)` method of `S3Resolver`. **Default are maven-style patterns** (just as in sbt), but you can change it with the convenience method `.withIvyPatterns`.


## Contacts

This project is maintained by [@laughedelic](https://github.com/laughedelic). Join the chat-room if you want to ask or discuss something  
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/ohnosequences/sbt-s3-resolver?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
