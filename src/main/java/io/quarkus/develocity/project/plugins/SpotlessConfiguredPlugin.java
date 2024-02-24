package io.quarkus.develocity.project.plugins;

import java.util.Map;

import com.gradle.maven.extension.api.cache.MojoMetadataProvider;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.EmptyDirectoryHandling;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.LineEndingHandling;

import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

public class SpotlessConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "spotless-maven-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "apply", SpotlessConfiguredPlugin::configureApply);
    }

    private static void configureApply(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            dependsOnGav(inputs, context);

            inputs.properties("applySkip", "encoding", "checkSkip", "filePatterns", "formats", "goal",
                    "setLicenseHeaderYearsFromGitHistory", "skip", "ratchetFrom");

            inputs.fileSet("baseDir", fileSet -> fileSet
                    .exclude(".idea/*", ".classpath", ".project", ".settings/*", "target/*", ".cache/*", ".factorypath",
                            "*.log")
                    .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE)
                    .lineEndingHandling(LineEndingHandling.NORMALIZE));

            inputs.ignore("repositorySystemSession", "repositories", "project", "lineEndings");

            // Entities used by Spotless with parameters defined in FormatterFactory
            // we can ignore them as our formatting is consistent throughout the project
            inputs.ignore("java", "python", "scala", "groovy", "javascript", "json", "kotlin", "cpp", "antlr4", "sql",
                    "typescript", "yaml", "pom", "markdown");
        });

        context.nested("licenseHeader",
                c -> c.inputs(inputs -> inputs.properties("file", "content", "delimiter", "skipLinesMatching")));
        context.nested("upToDateChecking", c -> c.inputs(inputs -> inputs.properties("enabled", "indexFile")));

        context.outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");

            outputs.directory("buildDir");
        });
    }
}
