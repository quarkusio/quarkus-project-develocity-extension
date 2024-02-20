package io.quarkus.develocity.project;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;
import com.gradle.maven.extension.api.cache.BuildCacheApi;

import io.quarkus.develocity.project.goals.EnforcerConfiguredPlugin;
import io.quarkus.develocity.project.goals.FormatterConfiguredPlugin;
import io.quarkus.develocity.project.goals.ImpsortConfiguredPlugin;
import io.quarkus.develocity.project.goals.KotlinConfiguredPlugin;
import io.quarkus.develocity.project.goals.QuarkusConfiguredPlugin;
import io.quarkus.develocity.project.goals.SourceConfiguredPlugin;

@SuppressWarnings("deprecation")
@Component(
        role = GradleEnterpriseListener.class,
        hint = "quarkus-project-build-cache",
        description = "Configures the build cache of the Quarkus project"
)
public class QuarkusProjectBuildCacheGradleEnterpriseListener implements GradleEnterpriseListener {

    private static final String QUICKLY = "-Dquickly";
    private static final String DASH = "-";

    @Override
    public void configure(GradleEnterpriseApi gradleEnterpriseApi, MavenSession mavenSession) throws Exception {
        workaroundQuickly(gradleEnterpriseApi.getBuildCache());

        List<ConfiguredPlugin> configuredGoals = List.of(
                new EnforcerConfiguredPlugin(),
                new QuarkusConfiguredPlugin(),
                new SourceConfiguredPlugin(),
                new FormatterConfiguredPlugin(),
                new ImpsortConfiguredPlugin(),
                new KotlinConfiguredPlugin()
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
