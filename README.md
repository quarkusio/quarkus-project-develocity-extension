# Maven extension configuring Develocity for the Quarkus project

[![Version](https://img.shields.io/maven-central/v/io.quarkus.develocity/quarkus-project-develocity-extension?logo=apache-maven&style=for-the-badge)](https://central.sonatype.com/artifact/io.quarkus.develocity/quarkus-project-develocity-extension)

## About

This Maven extension is designed to configure Develocity for the Quarkus project.

## Developing

The reference documentation for the API can be found [here](https://docs.gradle.com/enterprise/maven-extension/api/).

When working on caching new goals, you can obtain a debug output with the following command:

```
./mvnw -Dquickly -Dorg.slf4j.simpleLogger.log.develocity.goal.cache=debug -Dorg.slf4j.simpleLogger.log.io.quarkus.develocity=debug -e clean install
```

This command should be run on a single module on the Quarkus project for easier debugging.

Note: the `clean install` goals are important even if not strictly necessary when using `-Dquickly`.
The cache won't be populated otherwise.

You can also get some information about the generation of the cache key with `-Dorg.slf4j.simpleLogger.log.develocity.goal.fingerprint=trace`.

## Release

To release a new version, follow these steps:

https://github.com/smallrye/smallrye/wiki/Release-Process#releasing

The staging repository is automatically closed. The sync with Maven Central should take ~30 minutes.
