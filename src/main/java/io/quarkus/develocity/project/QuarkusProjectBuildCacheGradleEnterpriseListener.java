package io.quarkus.develocity.project;

import java.nio.file.Path;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import com.gradle.maven.extension.api.cache.BuildCacheApi;

import io.quarkus.develocity.project.goals.CompilerConfiguredPlugin;
import io.quarkus.develocity.project.goals.EnforcerConfiguredPlugin;
import io.quarkus.develocity.project.goals.FailsafeConfiguredPlugin;
import io.quarkus.develocity.project.goals.FormatterConfiguredPlugin;
import io.quarkus.develocity.project.goals.ImpsortConfiguredPlugin;
import io.quarkus.develocity.project.goals.KotlinConfiguredPlugin;
import io.quarkus.develocity.project.goals.QuarkusConfiguredPlugin;
import io.quarkus.develocity.project.goals.SourceConfiguredPlugin;
import io.quarkus.develocity.project.goals.SpotlessConfiguredPlugin;
import io.quarkus.develocity.project.goals.SurefireConfiguredPlugin;
import io.quarkus.develocity.project.util.Matchers;

@SuppressWarnings("deprecation")
@Component(role = GradleEnterpriseListener.class, hint = "quarkus-project-build-cache", description = "Configures the build cache of the Quarkus project")
public class QuarkusProjectBuildCacheGradleEnterpriseListener implements GradleEnterpriseListener {

    private static final String QUICKLY = "-Dquickly";
    private static final String DASH = "-";

    @Override
    public void configure(GradleEnterpriseApi gradleEnterpriseApi, MavenSession mavenSession) throws Exception {
        workaroundQuickly(gradleEnterpriseApi.getBuildCache());

        // System properties
        gradleEnterpriseApi.getBuildCache().registerNormalizationProvider(
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
        gradleEnterpriseApi.getBuildCache().registerNormalizationProvider(
                context -> context.configureRuntimeClasspathNormalization(c -> {
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

        List<ConfiguredPlugin> configuredGoals = List.of(
                new CompilerConfiguredPlugin(),
                new SurefireConfiguredPlugin(),
                new FailsafeConfiguredPlugin(),
                new EnforcerConfiguredPlugin(),
                new SourceConfiguredPlugin(),
                new QuarkusConfiguredPlugin(),
                new FormatterConfiguredPlugin(),
                new ImpsortConfiguredPlugin(),
                new KotlinConfiguredPlugin(),
                new SpotlessConfiguredPlugin()
        //new QuarkusExtensionConfiguredPlugin()
        );

        for (ConfiguredPlugin configuredGoal : configuredGoals) {
            configuredGoal.configureBuildCache(gradleEnterpriseApi, mavenSession);
        }
    }

    private static void workaroundQuickly(BuildCacheApi buildCacheApi) {
        String mavenCommandLine = System.getenv("MAVEN_CMD_LINE_ARGS");
        if (mavenCommandLine == null || mavenCommandLine.isBlank()) {
            return;
        }

        mavenCommandLine = mavenCommandLine.trim();

        String[] segments = mavenCommandLine.split(" ");

        boolean hasQuickly = false;
        boolean hasGoals = false;

        for (String segment : segments) {
            segment = segment.trim();
            if (segment.isEmpty()) {
                continue;
            }
            if (QUICKLY.equals(segment)) {
                hasQuickly = true;
                continue;
            }
            // any option
            if (segment.startsWith(DASH)) {
                continue;
            }
            // when using -T 6/-T 6C as it works
            if (Character.isDigit(segment.charAt(0))) {
                continue;
            }
            // when using -T C6 which seems to work for some versions of Maven (but not mine)
            if (segment.length() > 2 && segment.charAt(0) == 'C' && Character.isDigit(segment.charAt(1))) {
                continue;
            }

            hasGoals = true;
        }

        if (hasQuickly && !hasGoals) {
            buildCacheApi.setRequireClean(false);
        }
    }
}
