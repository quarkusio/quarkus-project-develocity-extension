package io.quarkus.develocity.project.plugins;

import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

/**
 * Unfortunately, this won't work because we are overwriting an existing file and we cannot cache this output.
 * To make it work, we would have to avoid using the same filename for quarkus-extension.yaml.
 *
 * TODO discuss this more in depth with Alexey.
 */
public class QuarkusExtensionConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "quarkus-extension-maven-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "extension-descriptor", QuarkusExtensionConfiguredPlugin::configureExtensionDescriptor);
    }

    private static void configureExtensionDescriptor(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());
            inputs.properties("deployment", "excludedArtifacts",
                    "parentFirstArtifacts", "runnerParentFirstArtifacts", "lesserPriorityArtifacts", "skipExtensionValidation",
                    "ignoreNotDetectedQuarkusCoreVersion", "conditionalDependencies", "dependencyCondition", "skipCodestartValidation",
                    "minimumJavaVersion");
            inputs.fileSet("extensionFile", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            inputs.property("skipExtensionValidation", context.properties().getBoolean("skipExtensionValidation"));
            inputs.property("skipCodestartValidation", context.properties().getBoolean("skipCodestartValidation"));

            inputs.ignore("session", "repoSession", "outputDirectory", "project");
        });

        context.metadata().nested("repos", c -> c.inputs(inputs -> inputs.properties("id", "url")));
        context.metadata().nested("removedResources", c -> c.inputs(inputs -> inputs.properties("key", "resources")));
        context.metadata().nested("capabilities", c -> {
            c.nested("requires", cc -> cc.inputs(inputs -> inputs.properties("name", "onlyIf", "onlyIfNot")));
            c.nested("provides", cc -> cc.inputs(inputs -> inputs.properties("name", "onlyIf", "onlyIfNot")));
        });

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs and dependencies are identical, we should have the same output");
            outputs.file("quarkus-extension.yaml", context.project().getBuild().getOutputDirectory() + "/META-INF/quarkus-extension.yaml");
            outputs.file("quarkus-extension.properties", context.project().getBuild().getOutputDirectory() + "/META-INF/quarkus-extension.properties");
        });
    }
}
