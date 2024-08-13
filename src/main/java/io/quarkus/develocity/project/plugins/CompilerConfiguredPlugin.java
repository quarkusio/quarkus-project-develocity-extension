package io.quarkus.develocity.project.plugins;

import java.nio.file.Path;
import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;
import io.quarkus.develocity.project.util.Matchers;

public class CompilerConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-compiler-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of("compile", CompilerConfiguredPlugin::configureCompile);
    }

    private static void configureCompile(GoalMetadataProvider.Context context) {
        if (Matchers.directory(context.metadata(), Path.of("integration-tests"))) {
            context.metadata().inputs(inputs -> {
                inputs.fileSet("specs",
                    context.project().getBasedir() + "/disable-unbind-executions",
                    fs -> fs.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            });
        }

        // for extensions, we need to add skipDocs/quickly as an input as it affects the annotation processor
        if (Matchers.directory(context.metadata(), Path.of("extensions")) ||
                Matchers.directory(context.metadata(), Path.of("test-framework", "jacoco")) ||
                Matchers.directory(context.metadata(), Path.of("core", "runtime")) ||
                Matchers.directory(context.metadata(), Path.of("core", "deployment"))) {

            context.metadata().inputs(inputs -> {
                boolean skipDocs = context.properties().getBoolean("skipDocs") || context.properties().getBoolean("quickly")
                        || context.properties().getBoolean("quickly-ci");
                inputs.property("skipDocs", skipDocs);
            });
        }

        context.metadata().outputs(outputs -> {
            // cache the config doc output of the extension annotation processor
            outputs.directory("quarkusConfigDoc", context.project().getBuild().getDirectory() + "/quarkus-config-doc");
        });
    }
}
