## Sbt S3 resolver

This is an sbt plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Features

This plugin can publish artifacts in maven or ivy style, but it can resolve only ivy artifacts:

| _Ivy artifacts_ | publish | resolve |     | _Maven artifacts_ | publish | resolve |
| :-------------: | :-----: | :-----: | --- | :---------------: | :-----: | :-----: |
|    **public**   |    ✓    |    ✓    |     |     **public**    |    ✓    |  __*__  |
|   **private**   |    ✓    |    ✓    |     |    **private**    |    ✓    |    ✗    |

__*__ If your maven artifacts are public, you can resolve them using usual sbt resolvers just transforming your `s3://my.bucket.com` to

```scala
"My S3 bucket" at "http://my.bucket.com.s3.amazonaws.com"
```

i.e. without using this plugin. Or if you're using it anyway, you can write:

```scala
"My S3 bucket" at s3("my.bucket.com").toHttp
```


## Usage

### Plugin sbt dependency

In `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.10.1")
```


### Setting keys

|         Key         |             Type             |             Default             |
| ------------------: | :--------------------------: | :------------------------------ |
|     `s3credentials` |   `AWSCredentialsProvider`   | parsed from `s3credentialsFile` |
|          `s3region` |           `Region`           | `EU_Ireland`                    |
|       `s3overwrite` |          `Boolean`           | same as `isSnapshot` key        |
|        `s3resolver` | `(String, s3) => S3Resolver` | is set using all above          |

Where

```scala
type Region = com.amazonaws.services.s3.model.Region
type AWSCredentialsProvider = com.amazonaws.auth.AWSCredentialsProvider
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
  Some(s3resolver.value("My "+prefix+" S3 bucket", s3(prefix+".cool.bucket.com") withIvyPatterns)
}
```

You can also switch repository for public and private artifacts — you just set the url of your bucket depending on something. Here `s3` constructor takes the name of your S3 bucket (don't worry about `s3://` prefix).


### Resolving

You can add a sequence of S3 resolvers just like this:

```scala
resolvers ++= Seq(
  s3resolver.value("Releases resolver", "s3://releases.bucket.com"),
  s3resolver.value("Snapshots resolver", "s3://snapshots.bucket.com")
)
```


### Credentials

#### Types of credentials providers

`s3credentials` key has the [`AWSCredentialsProvider`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html) type from AWS Java SDK, so it can be

* `file("/some/absolute/path/to/credentials.properties")` which will be implicitly converted to the [`PropertiesFileCredentialsProvider`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/PropertiesFileCredentialsProvider.html)
* [`new EnvironmentVariableCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html) which looks for credentials in the environment variables
* [`new InstanceProfileCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/InstanceProfileCredentialsProvider.html) which loads credentials from the Amazon EC2 Instance Metadata Service
* [`new SystemPropertiesCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/SystemPropertiesCredentialsProvider.html) which looks for credentials at the `aws.accessKeyId` and `aws.secretKey` Java system properties
* Some other types of credentials providers which you can find in the [AWS Java SDK docs](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html)

Note that for using them you will need to add `import com.amazonaws.auth._` to the beginning of your `build.sbt`.

#### Combining credentials providers

You can combine several credentials providers with the `|` ("or") operator, which will construct the [`AWSCredentialsProviderChain`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProviderChain.html). For example the **default credentials** chain in this plugin is

```scala
s3credentials := {
  file(System.getProperty("user.home")) / ".sbt" / ".s3credentials" |
  new EnvironmentVariableCredentialsProvider() |
  new SystemPropertiesCredentialsProvider()
}
```

It means that the plugin looks for credentials in the following places (in this particular order):

1. Property file `~/.sbt/.s3credentials` of the following format:  

   ```properties
   accessKey = 322wasa923...
   secretKey = 2342xasd8fDfaa9C...
   ```
2. Environment Variables: `AWS_ACCESS_KEY_ID` and `AWS_SECRET_KEY`
3. Java System Properties: `aws.accessKeyId` and `aws.secretKey`

You can check which credentials are loaded with the `showS3Credentials` task:

```bash
sbt showS3Credentials
```

### Patterns

You can set patterns using `.withPatterns(...)` method of `S3Resolver`. **Default are maven-style patterns** (just as in sbt), but you can change it with the convenience method `.withIvyPatterns`.
