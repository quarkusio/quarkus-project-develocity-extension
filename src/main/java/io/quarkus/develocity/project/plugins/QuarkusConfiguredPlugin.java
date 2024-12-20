package io.quarkus.develocity.project.plugins;

import java.util.Map;
import java.util.stream.Collectors;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

/**
 * TODO discuss this more in depth with Alexey, especially to make sure the output directory is not shared with other plugins.
 */
public class QuarkusConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "quarkus-maven-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "build", QuarkusConfiguredPlugin::configureBuild,
                "generate-code", QuarkusConfiguredPlugin::configureGenerateCode,
                "generate-code-tests", QuarkusConfiguredPlugin::configureGenerateCodeTests);
    }

    private static void configureBuild(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            inputs.fileSet("quarkus-dependencies", context.project().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependencies.txt").normalizationStrategy(NormalizationStrategy.CLASSPATH));
            // for compatibility with older versions but this is deprecated
            inputs.fileSet("quarkus-dependency-checksums", context.project().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            });
    }

    private static void configureGenerateCode(GoalMetadataProvider.Context context) {
        configureCommonGeneratedCode(context, false);
    }

    private static void configureGenerateCodeTests(GoalMetadataProvider.Context context) {
        configureCommonGeneratedCode(context, true);
    }

    private static void configureCommonGeneratedCode(GoalMetadataProvider.Context context, boolean test) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());
            dependsOnOs(inputs);
            dependsOnJavaVersion(inputs);

            inputs.properties("skipSourceGeneration", "mode", "properties", "bootstrapId");
            inputs.fileSet("quarkus-dependencies", context.project().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependencies.txt").normalizationStrategy(NormalizationStrategy.CLASSPATH));
            // for compatibility with older versions but this is deprecated
            inputs.fileSet("quarkus-dependency-checksums", context.project().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            inputs.fileSet("resources", context.project().getResources().stream().map(r -> r.getDirectory())
                    .collect(Collectors.toList()),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            if (test) {
                inputs.fileSet("testResources",
                        context.project().getTestResources().stream().map(r -> r.getDirectory())
                                .collect(Collectors.toList()),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            }
            try {
                if (test) {
                    inputs.fileSet("testClassPath", context.project().getTestClasspathElements(),
                            fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
                } else {
                    inputs.fileSet("compileClassPath", context.project().getCompileClasspathElements(),
                            fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable to resolve classpath");
            }

            inputs.ignore("repoSession", "session", "project", "mojoExecution", "buildDir", "finalName", "manifestEntries",
                    "manifestSections", "ignoredEntries", "appArtifact", "closeBootstrappedApp");
        });

        context.metadata().nested("repos", c -> c.inputs(inputs -> inputs.properties("id", "url")));

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs and dependencies are identical, we should have the same output");
            if (test) {
                outputs.directory("generated-test-sources",
                        context.project().getBuild().getDirectory() + "/generated-test-sources");
            } else {
                outputs.directory("generated-sources", context.project().getBuild().getDirectory() + "/generated-sources");
            }
            outputs.directory("proto", context.project().getBuild().getDirectory() + "/proto");
        });

        // we should create these only if we have an output cached but let's create them always...
        if (test) {
            context.project().addTestCompileSourceRoot(context.project().getBuild().getDirectory() + "/generated-test-sources");
        } else {
            context.project().addCompileSourceRoot(context.project().getBuild().getDirectory() + "/generated-sources");
        }
    }
}
