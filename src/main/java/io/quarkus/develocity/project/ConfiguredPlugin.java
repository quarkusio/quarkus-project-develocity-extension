package io.quarkus.develocity.project;

import org.apache.maven.execution.MavenSession;

import com.gradle.develocity.agent.maven.api.DevelocityApi;

public interface ConfiguredPlugin {

    void configureBuildCache(DevelocityApi develocityApi, MavenSession mavenSession);

}
