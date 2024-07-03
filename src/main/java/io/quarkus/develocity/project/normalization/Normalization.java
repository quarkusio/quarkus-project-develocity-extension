package io.quarkus.develocity.project.normalization;

import java.nio.file.Path;

import com.gradle.develocity.agent.maven.api.cache.BuildCacheApi;

import io.quarkus.develocity.project.util.Matchers;

public final class Normalization {

    private Normalization() {
    }

    public static void configureNormalization(BuildCacheApi buildCacheApi) {
        // System properties
        buildCacheApi.registerNormalizationProvider(
                context -> context.configureSystemPropertiesNormalization(s -> {
                    s.addIgnoredKeys("maven.repo.local", "maven.settings");

                    if (Matchers.directory(context, Path.of("docs"))) {
                        s.addIgnoredKeys("vale.dir", "git.dir");
                    }

                    if (Matchers.directory(context, Path.of("independent-projects", "arc", "tcks", "cdi-tck-runner"))) {
                        s.addIgnoredKeys("org.jboss.cdi.tck.libraryDirectory");
                    }

                    if (Matchers.directory(context, Path.of("integration-tests"))) {
                        s.addIgnoredKeys("native.image.path", "quarkus.kubernetes-service-binding.root");
                    }

                    if (Matchers.module(context, "quarkus-integration-test-rest-client")) {
                        s.addIgnoredKeys("javax.net.ssl.trustStore", "rest-client.trustStore");
                    }

                    if (Matchers.module(context, "quarkus-integration-test-test-extension")) {
                        s.addIgnoredKeys("classpathEntriesRecordingFile");
                    }
                }));

        // Application.properties
        buildCacheApi.registerNormalizationProvider(
                context -> context.configureRuntimeClasspathNormalization(c -> {
                    // we are sharing files between JDKs so we need to ignore content that is version-dependent
                    // the most important element here is Build-Jdk-Spec, the rest is here to allow reusing caches across branches
                    // or when the maven-jar-plugin is updated
                    // see https://docs.gradle.com/develocity/maven-extension/current/#normalizing_contents_of_meta_inf
                    c.configureMetaInf(metaInf -> metaInf.setIgnoredAttributes("Build-Jdk-Spec", "Created-By",
                            "Specification-Version", "Implementation-Version"));

                    c.addIgnoredFiles("META-INF/ide-deps/**");

                    if (Matchers.module(context, "quarkus-integration-test-rest-client-reactive")) {
                        c.addPropertiesNormalization("application.properties", "quarkus.rest-client.self-signed.trust-store",
                                "quarkus.rest-client.wrong-host.trust-store",
                                "quarkus.rest-client.wrong-host-rejected.trust-store");
                    }

                    if (Matchers.module(context, "quarkus-integration-test-oidc-client-reactive")) {
                        c.addPropertiesNormalization("application.properties", "quarkus.keycloak.devservices.realm-path");
                    }

                    if (Matchers.module(context, "quarkus-integration-test-test-extension")) {
                        c.addPropertiesNormalization("application.properties", "quarkus.bt.classpath-recording.record-file",
                                "%test.quarkus.bt.classpath-recording.record-file");
                    }

                    if (Matchers.module(context, "quarkus-integration-test-gradle-plugin")) {
                        c.addIgnoredFiles(".quarkus/config.yml");
                    }
                }));
    }
}
