package io.quarkus.develocity.project.plugins;

import java.nio.file.Path;
import java.util.Map;

import com.gradle.maven.extension.api.cache.MojoMetadataProvider;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;
import io.quarkus.develocity.project.util.Matchers;

public class CompilerConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-compiler-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "compile", CompilerConfiguredPlugin::configureCompile);
    }

    private static void configureCompile(MojoMetadataProvider.Context context) {
        if (Matchers.directory(context, Path.of("integration-tests"))) {
            context.inputs(inputs -> inputs.fileSet("specs",
                    context.getProject().getBasedir() + "/disable-unbind-executions",
                    fs -> fs.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)));
        }

        context.outputs(outputs -> {
            if (Matchers.directory(context, Path.of("extensions")) ||
                    Matchers.directory(context, Path.of("test-framework", "jacoco")) ||
                    Matchers.directory(context, Path.of("core", "runtime")) ||
                    Matchers.directory(context, Path.of("core", "deployment"))) {
                outputs.notCacheableBecause("The extension config doc generation tool shares data across all extensions");
            }
        });
    }
}
