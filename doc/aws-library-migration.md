# AWS library migration
Currently sbt-s3-resolver uses springframework ivy resolver as back-end. This library works but looks deprecated and it is good idea to rewrite it. It is not so hard: actually only few classes of ivy common code should be extended 
(`RepositoryResolver`, `Resource` and `AbstractRepository`).

