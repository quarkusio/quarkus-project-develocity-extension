package io.quarkus.develocity.project.plugins;

import java.util.Map;
import java.util.stream.Collectors;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

/**
 * TODO discuss this more in depth with Alexey, especially to make sure the output directory is not shared with other plugins.
 */
public class SourceConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "maven-source-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "jar-no-fork", SourceConfiguredPlugin::jarNoFork);
    }

    private static void jarNoFork(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            dependsOnGav(inputs, context);

            inputs.properties("classifier", "includes", "excludes", "useDefaultExcludes",
                    "useDefaultManifestFile", "attach", "excludeResources", "includePom", "finalName", "forceCreation",
                    "skipSource", "outputTimestamp");
            inputs.fileSet("defaultManifestFile", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            inputs.fileSet("resources", context.getProject().getResources().stream().map(r -> r.getDirectory())
                    .collect(Collectors.toList()),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            inputs.fileSet("sources", context.getProject().getCompileSourceRoots(),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            inputs.ignore("project", "jarArchiver", "archive", "outputDirectory", "reactorProjects", "session");
        });

        context.outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");
            outputs.file("source-jar", context.getProject().getBuild().getDirectory() + "/"
                    + context.getProject().getBuild().getFinalName() + "-sources.jar");
        });

        // we should add the source jar as an attached artifact but for now, we can't
    }
}
