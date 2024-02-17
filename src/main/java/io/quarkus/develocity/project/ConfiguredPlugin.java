package io.quarkus.develocity.project;

import org.apache.maven.execution.MavenSession;

import com.gradle.maven.extension.api.GradleEnterpriseApi;

public interface ConfiguredPlugin {

    void configureBuildCache(GradleEnterpriseApi gradleEnterpriseApi, MavenSession mavenSession);

}
