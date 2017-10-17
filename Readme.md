# Sbt S3 resolver

[![](https://travis-ci.org/ohnosequences/sbt-s3-resolver.svg?branch=master)](https://travis-ci.org/ohnosequences/sbt-s3-resolver)
[![](https://img.shields.io/codacy/96ad3cc701a54c548deb4ef0d5564655.svg)](https://www.codacy.com/app/ohnosequences/sbt-s3-resolver)
[![](http://img.shields.io/bintray/v/ohnosequences/sbt-plugins/sbt-s3-resolver.svg)](https://bintray.com/ohnosequences/sbt-plugins/sbt-s3-resolver/_latestVersion)
[![](http://img.shields.io/github/release/ohnosequences/sbt-s3-resolver/all.svg)](https://github.com/ohnosequences/sbt-s3-resolver/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/gitter/room/ohnosequences/sbt-s3-resolver.svg?colorB=dd1054)](https://gitter.im/ohnosequences/sbt-s3-resolver)

This is an sbt plugin which helps resolving dependencies from and publish to Amazon S3 buckets (private or public).

It can publish artifacts in maven or ivy style, but it can resolve only ivy artifacts:

| _Ivy artifacts_ | publish | resolve | • | _Maven artifacts_ | publish |             resolve              |
|:---------------:|:-------:|:-------:|:-:|:-----------------:|:-------:|:--------------------------------:|
|   **public**    |    ✓    |    ✓    |   |    **public**     |    ✓    | [__*__](#public-maven-artifacts) |
|   **private**   |    ✓    |    ✓    |   |    **private**    |    ✓    |                ✗                 |


## Usage

### Plugin sbt dependency

In `project/plugins.sbt`:

```scala
resolvers += Resolver.jcenterRepo
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "<version>")
```

(see the latest release version on the badge above)

#### Notes

* Since `v0.17.0` this plugin is compiled and published **only for sbt-1.+**. If you need it for sbt-0.13, use [`v0.16.0`](https://github.com/ohnosequences/sbt-s3-resolver/tree/v0.16.0).
* If you are using Java 9 and encounter problems, check information in [#58](https://github.com/ohnosequences/sbt-s3-resolver/issues/58).

### Settings

* `awsProfile`: AWS configuration profile
* `s3credentials`: AWS credentials provider to access S3
* `s3region`: AWS Region for your S3 resolvers
* `s3acl`: Controls whether published artifacts are accessible publicly via http(s) or not
* `s3storageClass`: Controls storage class for the published S3 objects
* `s3overwrite`: Controls whether publishing resolver can overwrite artifacts
* `s3sse`: Controls whether publishing resolver will use server side encryption

| Key              | Type                              | Default                                |
|:-----------------|:----------------------------------|:---------------------------------------|
| `awsProfile`     | `Option[String]`                  | `None`                                 |
| `s3credentials`  | [`AWSCredentialsProvider`]        | [`DefaultAWSCredentialsProviderChain`] |
| `s3region`       | [`Region`]                        | [`DefaultAwsRegionProviderChain`]      |
| `s3acl`          | `Option[CannedAccessControlList]` | `Some(PublicRead)`                     |
| `s3storageClass` | [`StorageClass`]                  | `Standard`                             |
| `s3overwrite`    | `Boolean`                         | `isSnapshot.value`                     |
| `s3sse`          | `Boolean`                         | `false`                                |

These defaults are added to your project automatically. So if you're fine with them, you don't need to do anything special, just set the resolver and publish. Otherwise you can tune the settings by overriding them in your `build.sbt`.

You can use `s3resolver` setting key that takes a _name_ and an _S3 bucket url_ and returns `S3Resolver` which is implicitly converted to `sbt.Resolver`.


#### AWS configuration profiles

If you have different [configuration profiles](http://docs.aws.amazon.com/cli/latest/userguide/cli-multiple-profiles.html), you can choose the one you need by setting

```scala
awsProfile := Some("my-profile")
```

If you didn't touch `s3region` and `s3credentials` settings, they will both use this profile region and credentials.

By default `awsProfile` is set to `None` which means that both region and credentials will be set from the default provider chains. See below for details.

#### Credentials

`s3credentials` key has the [`AWSCredentialsProvider`] type from AWS Java SDK. Different kinds of providers look for credentials in different places, plus they can be [chained](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProviderChain.html). [`DefaultAWSCredentialsProviderChain`] looks in

1. **Environment Variables**:
    + `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
    + or `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`
1. **Java System Properties**: `aws.accessKeyId` and `aws.secretKey`
1. **Credential profiles file**: `~/.aws/credentials` shared by all AWS SDKs and the AWS CLI
1. **ECS container credentials** loaded from the Amazon ECS if the environment variable `AWS_CONTAINER_CREDENTIALS_RELATIVE_URI` is set
1. **Instance profile credentials** delivered through the Amazon EC2 metadata service

You can find other types of credentials providers in the [AWS Java SDK docs][`AWSCredentialsProvider`].

If you changed the `awsProfile` setting, default credentials provider becomes that of the corresponding profile. To check which credentials are used by the plugin, use `showS3Credentials` task.


#### Region

You can set the `s3region` setting in a number of ways:
* using the [`Region`] type directly
* using [`s3.model.Region`] enum
* using one of the [`AwsRegionProvider`]s (or a chain of providers)

By default it is set to the [`DefaultAwsRegionProviderChain`] which is similar to the default credentials provider chain and includes

1. **Environment Variable**: `AWS_REGION`
1. **Java System Property**: `aws.region`
1. **Profiles configuration file**: `~/.aws/config`
1. **EC2 instance metadata service**

If you changed the `awsProfile` setting, default region provider becomes that of the corresponding profile.

### Publishing

A common practice is to use different (snapshots and releases) repositories depending on the version. For example, here is such publishing resolver with ivy-style patterns:

```scala
publishMavenStyle := false

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value(s"My ${prefix} S3 bucket", s3(s"${prefix}.cool.bucket.com")) withIvyPatterns)
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

[`AWSCredentialsProvider`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html
[`Region`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/Region.html
[`s3.model.Region`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/Region.html
[`AwsRegionProvider`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/AwsRegionProvider.html
[`CannedAccessControlList`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/CannedAccessControlList.html
[`StorageClass`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/model/StorageClass.html
[`DefaultAwsRegionProviderChain`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/regions/DefaultAwsRegionProviderChain.html
[`DefaultAWSCredentialsProviderChain`]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html
