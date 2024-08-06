package io.quarkus.develocity.project;

import org.apache.maven.project.MavenProject;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.scan.BuildScanApi;

import io.quarkus.develocity.project.util.MavenMojoExecutionConfig;
import io.quarkus.develocity.project.util.MavenProperties;

@FunctionalInterface
public interface GoalMetadataProvider {

    void configure(Context context);

    class Context {
        private final BuildScanApi buildScanApi;
        private final MojoMetadataProvider.Context metadataContext;
        private final MavenProperties properties;
        private final MavenMojoExecutionConfig configuration;

        public Context(BuildScanApi buildScanApi, MojoMetadataProvider.Context metadataContext) {
            this.buildScanApi = buildScanApi;
            this.metadataContext = metadataContext;
            this.properties = new MavenProperties(metadataContext.getSession(), metadataContext.getMojoExecution());
            this.configuration = new MavenMojoExecutionConfig(metadataContext.getMojoExecution());
        }

        public BuildScanApi buildScan() {
            return buildScanApi;
        }

        public MojoMetadataProvider.Context metadata() {
            return metadataContext;
        }

        public MavenProject project() {
            return metadataContext.getProject();
        }

        public MavenProperties properties() {
            return properties;
        }

        public MavenMojoExecutionConfig configuration() {
            return configuration;
        }

        public void buildScanDeduplicatedValue(String key, String value) {
            buildScanApi.executeOnce(key + value, ignored -> buildScanApi.value(key, value));
        }
    }
}
