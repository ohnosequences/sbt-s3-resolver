* #57: Changed `awsProfile` type to `Option[String]` and changed `s3region` and `s3credentials` defaults to depend on it (by @tsuyoshizawa):

    By default `awsProfile` is set to `None` which means that both region and credentials will be set from the default provider chains.
    And if you change it, `s3region` and `s3credentials` will both use given profile region and credentials.

    This makes these two settings more consistent, but also introduces a potentially **breaking change**: if you used some unusual setting for `s3region` or `s3credentials`, check their values after this update:

    ```
    > show s3region
    > showS3Credentials
    ```

* This release also deprecates conversions related to credential providers: `|` for chaining and implicit conversion from `File`. Use AWS Java SDK explicitly instead.
