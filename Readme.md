# Sbt S3 resolver

[![Build Status](https://travis-ci.org/ohnosequences/sbt-s3-resolver.svg?branch=master)](https://travis-ci.org/ohnosequences/sbt-s3-resolver)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/882bbf0e1ae64768b8932ea2bffa609b)](https://www.codacy.com/app/era7/sbt-s3-resolver)
[![Latest release](https://img.shields.io/github/release/ohnosequences/sbt-s3-resolver.svg)](https://github.com/ohnosequences/sbt-s3-resolver/releases/latest)
[![License](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![Gitter](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/sbt-s3-resolver)

This is an sbt plugin, which helps to resolve dependencies from and publish to Amazon S3 buckets (private or public).

## Features

This plugin can publish artifacts in maven or ivy style, but it can resolve only ivy artifacts:

| _Ivy artifacts_ | publish | resolve | • | _Maven artifacts_ | publish |             resolve              |
|:---------------:|:-------:|:-------:|:-:|:-----------------:|:-------:|:--------------------------------:|
|   **public**    |    ✓    |    ✓    |   |    **public**     |    ✓    | [__*__](#public-maven-artifacts) |
|   **private**   |    ✓    |    ✓    |   |    **private**    |    ✓    |                ✗                 |


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

|             Key |             Type             | Default                   |
|----------------:|:----------------------------:|:--------------------------|
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

These defaults are added to your project automatically. So you can just tune the settings keys in `build.sbt`.

You can use `s3resolver` setting key that takes a _name_ and an _S3 bucket url_ and returns `S3Resolver` which is implicitly converted to `sbt.Resolver`.


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
* [`new EnvironmentVariableCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html) which loads credentials from the environment variables
* [`new InstanceProfileCredentialsProvider()`](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/InstanceProfileCredentialsProvider.html) which loads credentials from the Amazon EC2 Instance Metadata Service

You can find other types of credentials providers in the [AWS Java SDK docs](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html)

If you have [different profiles](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html#credentials-file-format) in your `~/.aws/credentials` file, you can choose the one you need by setting

```scala
awsProfile := "bob"
```

Or if you would like to use profile credentials and have your env vars override if they exist.  This is handy if you have both a local dev environment as well as a CI environment where you need to use env vars.

```scala
s3credentials :=
  new ProfileCredentialsProvider(awsProfile.value) |
  new EnvironmentVariableCredentialsProvider()
```    

You can check which credentials are loaded with the `showS3Credentials` task:

```bash
sbt showS3Credentials
```


### Patterns

You can set patterns using `.withPatterns(...)` method of `S3Resolver`. **Default are maven-style patterns** (just as in sbt), but you can change it with the convenience method `.withIvyPatterns`.


### S3 IAM policy

If you want to publish and resolve artifacts in an S3 bucket you should have at least these permissions on your AWS-user/role:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket"
            ],
            "Resource": "arn:aws:s3:::mybucket"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
                "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::mybucket/*"
        }
    ]
}
```

In theory `s3:CreateBucket` may be also needed in the first statement in case if you publish to a non-existing bucket.
