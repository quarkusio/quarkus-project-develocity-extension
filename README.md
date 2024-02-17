# Maven extension configuring the Develocity build cache configuration for the Quarkus project

[![Version](https://img.shields.io/maven-central/v/io.quarkus.develocity/quarkus-project-develocity-extension?logo=apache-maven&style=for-the-badge)](https://central.sonatype.com/artifact/io.quarkus.develocity/quarkus-project-develocity-extension)

## About

This Maven extension is designed to configure the Develocity build cache for the Quarkus project.

## Developing

The reference documentation for the API can be found [here](https://docs.gradle.com/enterprise/maven-extension/api/com/gradle/maven/extension/api/cache/MojoMetadataProvider.Context.html).

When working on caching new goals, you can obtain a debug output with the following command:

```
./mvnw -Dquickly -Dorg.slf4j.simpleLogger.log.gradle.goal.cache=debug -Dorg.slf4j.simpleLogger.log.io.quarkus.develocity=debug -e clean install
```

This command should be run on a single module on the Quarkus project for easier debugging.

Note: the `clean install` goals are important even if not strictly necessary when using `-Dquickly`.
The cache won't be populated otherwise.

## Releasing

```
./mvnw release:prepare release:perform -Prelease
```
