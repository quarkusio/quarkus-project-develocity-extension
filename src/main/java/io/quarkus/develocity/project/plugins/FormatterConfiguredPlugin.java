package io.quarkus.develocity.project.plugins;

import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.EmptyDirectoryHandling;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

public class FormatterConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "formatter-maven-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "format", FormatterConfiguredPlugin::configureFormat);
    }

    private static void configureFormat(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());

            inputs.properties("includes", "excludes", "compilerSource", "compilerCompliance", "compilerTargetPlatform", "lineEnding", "configFile",
                    "configJsFile", "configHtmlFile", "configXmlFile", "configJsonFile", "configCssFile", "skipFormattingCache",
                    "skipJavaFormatting", "skipJsFormatting", "skipHtmlFormatting", "skipXmlFormatting", "skipJsonFormatting",
                    "skipCssFormatting", "skipFormatting", "useEclipseDefaults", "javaExclusionPattern", "removeTrailingWhitespace", "includeResources");

            inputs.fileSet("sourceDirectory", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                    .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            inputs.fileSet("testSourceDirectory", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                    .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            inputs.fileSet("directories", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                    .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));

            inputs.ignore("project", "targetDirectory", "basedir");
        });

        context.metadata().nested("encoding", c -> c.inputs(inputs -> inputs.properties("displayName")));

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs and dependencies are identical, we should have the same output");

            outputs.directory("cachedir");
        });
    }
}
