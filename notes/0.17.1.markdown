This is a bugfix release, it updates to [ivy-s3-resolver v0.12.0](https://github.com/ohnosequences/ivy-s3-resolver/releases/tag/v0.12.0) which solves the problem with redundant delimiters the ivy-style patterns introduced in sbt 1.0 (see [sbt/sbt#3573](https://github.com/sbt/sbt/issues/3573)).

The problem was reported and handled in the underlying library by Michael Ahlers @michaelahlers:
* [ohnosequences/sbt-s3-resolver#52](https://github.com/ohnosequences/sbt-s3-resolver/issues/52)
* [ohnosequences/ivy-s3-resolver#19](https://github.com/ohnosequences/ivy-s3-resolver/pull/19)
