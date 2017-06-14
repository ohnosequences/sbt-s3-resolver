* Updates for compatibility with EU Frankfurt AWS Region (See #49 & #47)
* Updated ivy-resolver dependency (with important updates to AWS Java SDK)
* Fixed how `toHttps` method works for different regions with different endpoint prefixes
* Changed Region type to `com.amazonaws.regions.Region`
* Changed default region setting to `DefaultAwsRegionProviderChain`; added implicit conversions
