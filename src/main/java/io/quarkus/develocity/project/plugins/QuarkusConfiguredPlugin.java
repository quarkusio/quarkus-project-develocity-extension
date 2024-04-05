package io.quarkus.develocity.project.plugins;

import java.util.Map;
import java.util.stream.Collectors;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

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

    private static void configureBuild(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            inputs.fileSet("dependency-checksums", context.getProject().getBuild().getDirectory(), fs -> fs
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
        });
    }

    private static void configureGenerateCode(MojoMetadataProvider.Context context) {
        configureCommonGeneratedCode(context, false);
    }

    private static void configureGenerateCodeTests(MojoMetadataProvider.Context context) {
        configureCommonGeneratedCode(context, true);
    }

    private static void configureCommonGeneratedCode(MojoMetadataProvider.Context context, boolean test) {
        context.inputs(inputs -> {
            dependsOnGav(inputs, context);
            dependsOnOs(inputs);
            dependsOnJavaVersion(inputs);

            inputs.properties("skipSourceGeneration", "mode", "properties", "bootstrapId");
            inputs.fileSet("dependency-checksums", context.getProject().getBuild().getDirectory(), fileSet -> fileSet
                    .include("quarkus-*-dependency-checksums.txt").normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            inputs.fileSet("resources", context.getProject().getResources().stream().map(r -> r.getDirectory())
                    .collect(Collectors.toList()),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));

            if (test) {
                inputs.fileSet("testResources",
                        context.getProject().getTestResources().stream().map(r -> r.getDirectory())
                                .collect(Collectors.toList()),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH));
            }
            try {
                if (test) {
                    inputs.fileSet("testClassPath", context.getProject().getTestClasspathElements(),
                            fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
                } else {
                    inputs.fileSet("compileClassPath", context.getProject().getCompileClasspathElements(),
                            fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable to resolve classpath");
            }

            inputs.ignore("repoSession", "session", "project", "mojoExecution", "buildDir", "finalName", "manifestEntries",
                    "manifestSections", "ignoredEntries", "appArtifact", "closeBootstrappedApp");
        });

        context.nested("repos", c -> c.inputs(inputs -> inputs.properties("id", "url")));

        context.outputs(outputs -> {
            outputs.cacheable("If the inputs and dependencies are identical, we should have the same output");
            if (test) {
                outputs.directory("generated-test-sources",
                        context.getProject().getBuild().getDirectory() + "/generated-test-sources");
            } else {
                outputs.directory("generated-sources", context.getProject().getBuild().getDirectory() + "/generated-sources");
            }
        });

        // we should create these only if we have an output cached but let's create them always...
        if (test) {
            context.getProject().addTestCompileSourceRoot(context.getProject().getBuild().getDirectory() + "/generated-test-sources");
        } else {
            context.getProject().addCompileSourceRoot(context.getProject().getBuild().getDirectory() + "/generated-sources");
        }
    }
}
