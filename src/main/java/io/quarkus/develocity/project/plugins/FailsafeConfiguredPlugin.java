package io.quarkus.develocity.project.plugins;

import java.nio.file.Path;
import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;
import io.quarkus.develocity.project.util.Matchers;

public class FailsafeConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-failsafe-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "integration-test", FailsafeConfiguredPlugin::configureIntegrationTest);
    }

    private static void configureIntegrationTest(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            addClasspathInput(inputs, Path.of(context.project().getBuild().getDirectory(), "quarkus-prod-dependencies.txt"));
            // for compatibility with older versions but this is deprecated
            inputs.fileSet("quarkus-dependency-checksums", context.project().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            if (Matchers.directory(context.metadata(), Path.of("integration-tests"))) {
                inputs.fileSet("native-runner", context.project().getBuild().getDirectory(),
                        fs -> fs.include("*-runner").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            }
        });

        context.metadata().outputs(outputs -> {
            if (Matchers.directory(context.metadata(), Path.of("integration-tests", "devtools")) ||
                    Matchers.directory(context.metadata(), Path.of("integration-tests", "gradle")) ||
                    Matchers.directory(context.metadata(), Path.of("integration-tests", "maven"))) {
                outputs.notCacheableBecause("It is too hard to figure out the dependency tree of these modules");
            }
        });
    }
}
