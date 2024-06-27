package io.quarkus.develocity.project.plugins;

import java.nio.file.Path;
import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;
import io.quarkus.develocity.project.util.Matchers;

public class SurefireConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-surefire-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "test", SurefireConfiguredPlugin::configureTest);
    }

    private static void configureTest(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            inputs.fileSet("quarkus-dependencies", context.getProject().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependencies.txt").normalizationStrategy(NormalizationStrategy.CLASSPATH));
            // for compatibility with older versions but this is deprecated
            inputs.fileSet("quarkus-dependency-checksums", context.getProject().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
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
