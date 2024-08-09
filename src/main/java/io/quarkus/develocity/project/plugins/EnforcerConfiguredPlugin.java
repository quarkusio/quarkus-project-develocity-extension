package io.quarkus.develocity.project.plugins;

import java.util.Map;
import java.util.stream.Collectors;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

public class EnforcerConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-enforcer-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "enforce", EnforcerConfiguredPlugin::configureEnforce);
    }

    private static void configureEnforce(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());
            inputs.properties("skip", "fail", "failFast", "failIfNoRules", "rules", "rulesToExecute", "rulesToSkip",
                    "ignoreCache");
            dependsOnOs(inputs);
            dependsOnJavaVersion(inputs);

            String dependencies = context.project().getArtifacts().stream()
                    .map(a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion() + ":" + a.getClassifier())
                    .sorted()
                    .collect(Collectors.joining("\n"));

            inputs.property("dependencies", dependencies);

            inputs.ignore("project", "mojoExecution", "session");
        });

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs and dependencies are identical, we should have the same output");
        });
    }
}
