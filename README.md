# Quarkus project Develocity extension

[![Version](https://img.shields.io/maven-central/v/io.quarkus.develocity/quarkus-project-develocity-extension?logo=apache-maven&style=for-the-badge)](https://central.sonatype.com/artifact/io.quarkus.develocity/quarkus-project-develocity-extension)

## About

This Maven extension is designed to configure the build cache for the Quarkus project.

## Developing

The reference documentation for the API can be found [here](https://docs.gradle.com/enterprise/maven-extension/api/com/gradle/maven/extension/api/cache/MojoMetadataProvider.Context.html).

When caching new goals, the debug output is very useful:

```
./mvnw -Dquickly -Dorg.slf4j.simpleLogger.log.gradle.goal.cache=debug -Dorg.slf4j.simpleLogger.log.io.quarkus.develocity=debug -e clean install
```

Note: the `clean install` is important even if not strictly necessary when using `-Dquickly`.
The cache won't be populated otherwise.

## Releasing

```
mvn release:prepare release:perform -Prelease
```
