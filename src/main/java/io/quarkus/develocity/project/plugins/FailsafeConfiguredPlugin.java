package io.quarkus.develocity.project.plugins;

import java.nio.file.Path;
import java.util.Map;

import com.gradle.maven.extension.api.cache.MojoMetadataProvider;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

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

    private static void configureIntegrationTest(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            inputs.fileSet("dependency-checksums", context.getProject().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            if (Matchers.directory(context, Path.of("integration-tests"))) {
                inputs.fileSet("native-runner", context.getProject().getBuild().getDirectory(),
                        fs -> fs.include("*-runner").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            }
        });

        context.outputs(outputs -> {
            if (Matchers.directory(context, Path.of("integration-tests", "devtools")) ||
                    Matchers.directory(context, Path.of("integration-tests", "gradle")) ||
                    Matchers.directory(context, Path.of("integration-tests", "maven"))) {
                outputs.notCacheableBecause("It is too hard to figure out the dependency tree of these modules");
            }
        });
    }
}
