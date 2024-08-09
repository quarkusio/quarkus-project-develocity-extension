package io.quarkus.develocity.project.plugins;

import java.util.Map;

import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.EmptyDirectoryHandling;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.LineEndingHandling;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

import io.quarkus.develocity.project.GoalMetadataProvider;
import io.quarkus.develocity.project.SimpleQuarkusConfiguredPlugin;

public class KotlinConfiguredPlugin extends SimpleQuarkusConfiguredPlugin {

    @Override
    protected String getPluginName() {
        return "kotlin-maven-plugin";
    }

    @Override
    protected Map<String, GoalMetadataProvider> getGoalMetadataProviders() {
        return Map.of(
                "compile", KotlinConfiguredPlugin::compile,
                "test-compile", KotlinConfiguredPlugin::testCompile,
                "kapt", KotlinConfiguredPlugin::kapt);
    }

    private static void kapt(GoalMetadataProvider.Context context) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());

            inputs.properties("annotationProcessors", "aptMode", "useLightAnalysis",
                    "correctErrorTypes", "mapDiagnosticLocations", "annotationProcessorArgs", "javacOptions",
                    "moduleName", "testModuleName", "jvmTarget", "scriptTemplates", "myIncremental", "javaParameters",
                    "compilerPlugins", "pluginOptions", "multiPlatform", "apiVersion", "args", "experimentalCoroutines",
                    "languageVersion", "module", "testModule");
            inputs.fileSet("classpath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("testClasspath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.CLASSPATH));
            inputs.fileSet("sourceDirs", context.project().getCompileSourceRoots(),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                            .lineEndingHandling(LineEndingHandling.NORMALIZE)
                            .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));

            inputs.ignore("jdkHome", "session", "mojoExecution", "nowarn", "project", "output", "testOutput");
        });

        context.metadata().nested("annotationProcessorPaths",
                c -> c.inputs(cc -> cc.properties("groupId", "artifactId", "version", "classifier", "type")));

        context.metadata().localState(l -> l.files("incrementalCachesRoot"));

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");
            outputs.directory("kapt", context.project().getBuild().getDirectory() + "/generated-sources/kapt/compile");
            outputs.directory("kaptKotlin",
                    context.project().getBuild().getDirectory() + "/generated-sources/kaptKotlin/compile");
            outputs.directory("kaptStubs", context.project().getBuild().getDirectory() + "/kaptStubs");
        });

        context.project()
                .addCompileSourceRoot(context.project().getBuild().getDirectory() + "/generated-sources/kapt/compile");
        context.project()
                .addCompileSourceRoot(context.project().getBuild().getDirectory() + "/generated-sources/kaptKotlin/compile");
    }

    private static void compile(GoalMetadataProvider.Context context) {
        compileCommon(context, false);
    }

    private static void testCompile(GoalMetadataProvider.Context context) {
        compileCommon(context, true);
    }

    private static void compileCommon(GoalMetadataProvider.Context context, boolean test) {
        context.metadata().inputs(inputs -> {
            dependsOnGav(inputs, context.metadata());

            inputs.properties("compilerPlugins", "pluginOptions", "multiPlatform", "languageVersion", "apiVersion",
                    "experimentalCoroutines", "args", "jvmTarget", "moduleName", "testModuleName", "scriptTemplates",
                    "javaParameters", "myIncremental");
            if (test) {
                inputs.properties("skip");
            }
            inputs.fileSet("classpath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("testClasspath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("sourceDirs", context.project().getCompileSourceRoots(),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                            .lineEndingHandling(LineEndingHandling.NORMALIZE)
                            .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            if (test) {
                inputs.fileSet("defaultSourceDir", context.project().getCompileSourceRoots(),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                                .lineEndingHandling(LineEndingHandling.NORMALIZE)
                                .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
                inputs.fileSet("defaultSourceDirs", context.project().getCompileSourceRoots(),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                                .lineEndingHandling(LineEndingHandling.NORMALIZE)
                                .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            }

            inputs.ignore("module", "testModule", "mojoExecution", "nowarn", "project", "jdkHome", "session");
        });

        context.metadata().localState(l -> l.files("incrementalCachesRoot"));

        context.metadata().outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");
            outputs.directory("output");
            outputs.directory("testOutput");
        });
    }
}
