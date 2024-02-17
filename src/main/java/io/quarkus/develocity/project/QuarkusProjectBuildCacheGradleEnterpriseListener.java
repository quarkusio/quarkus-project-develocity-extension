package io.quarkus.develocity.project;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import com.gradle.maven.extension.api.GradleEnterpriseApi;
import com.gradle.maven.extension.api.GradleEnterpriseListener;

import io.quarkus.develocity.project.goals.EnforcerConfiguredPlugin;
import io.quarkus.develocity.project.goals.FormatterConfiguredPlugin;
import io.quarkus.develocity.project.goals.ImpsortConfiguredPlugin;
import io.quarkus.develocity.project.goals.QuarkusConfiguredPlugin;
import io.quarkus.develocity.project.goals.SourceConfiguredPlugin;

@SuppressWarnings("deprecation")
@Component(
        role = GradleEnterpriseListener.class,
        hint = "quarkus-project-build-cache",
        description = "Configures the build cache of the Quarkus project"
)
public class QuarkusProjectBuildCacheGradleEnterpriseListener implements GradleEnterpriseListener {

    @Override
    public void configure(GradleEnterpriseApi gradleEnterpriseApi, MavenSession mavenSession) throws Exception {
        List<ConfiguredPlugin> configuredGoals = List.of(
                new EnforcerConfiguredPlugin(),
                new QuarkusConfiguredPlugin(),
                new SourceConfiguredPlugin(),
                new FormatterConfiguredPlugin(),
                new ImpsortConfiguredPlugin()
                //new QuarkusExtensionConfiguredPlugin()
        );

        for (ConfiguredPlugin configuredGoal : configuredGoals) {
            configuredGoal.configureBuildCache(gradleEnterpriseApi, mavenSession);
        }
    }
}
