package io.quarkus.develocity.project.goals;

import java.util.Map;

import com.gradle.maven.extension.api.cache.MojoMetadataProvider;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.EmptyDirectoryHandling;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.LineEndingHandling;
import com.gradle.maven.extension.api.cache.MojoMetadataProvider.Context.FileSet.NormalizationStrategy;

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

    private static void kapt(MojoMetadataProvider.Context context) {
        context.inputs(inputs -> {
            dependsOnGav(inputs, context);

            inputs.properties("annotationProcessors", "aptMode", "useLightAnalysis",
                    "correctErrorTypes", "mapDiagnosticLocations", "annotationProcessorArgs", "javacOptions",
                    "moduleName", "testModuleName", "jvmTarget", "scriptTemplates", "myIncremental", "javaParameters",
                    "compilerPlugins", "pluginOptions", "multiPlatform", "apiVersion", "args", "experimentalCoroutines",
                    "languageVersion", "module", "testModule");
            inputs.fileSet("classpath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("testClasspath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.CLASSPATH));
            inputs.fileSet("sourceDirs", context.getProject().getCompileSourceRoots(),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                            .lineEndingHandling(LineEndingHandling.NORMALIZE)
                            .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));

            inputs.ignore("jdkHome", "session", "mojoExecution", "nowarn", "project", "output", "testOutput");
        });

        context.nested("annotationProcessorPaths",
                c -> c.inputs(cc -> cc.properties("groupId", "artifactId", "version", "classifier", "type")));

        context.localState(l -> l.files("incrementalCachesRoot"));

        context.outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");
            outputs.directory("kapt", context.getProject().getBuild().getDirectory() + "/generated-sources/kapt/compile");
            outputs.directory("kaptKotlin",
                    context.getProject().getBuild().getDirectory() + "/generated-sources/kaptKotlin/compile");
            outputs.directory("kaptStubs", context.getProject().getBuild().getDirectory() + "/kaptStubs");
        });

        context.getProject()
                .addCompileSourceRoot(context.getProject().getBuild().getDirectory() + "/generated-sources/kapt/compile");
        context.getProject()
                .addCompileSourceRoot(context.getProject().getBuild().getDirectory() + "/generated-sources/kaptKotlin/compile");
    }

    private static void compile(MojoMetadataProvider.Context context) {
        compileCommon(context, false);
    }

    private static void testCompile(MojoMetadataProvider.Context context) {
        compileCommon(context, true);
    }

    private static void compileCommon(MojoMetadataProvider.Context context, boolean test) {
        context.inputs(inputs -> {
            dependsOnGav(inputs, context);

            inputs.properties("compilerPlugins", "pluginOptions", "multiPlatform", "languageVersion", "apiVersion",
                    "experimentalCoroutines", "args", "jvmTarget", "moduleName", "testModuleName", "scriptTemplates",
                    "javaParameters", "myIncremental");
            if (test) {
                inputs.properties("skip");
            }
            inputs.fileSet("classpath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("testClasspath", fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.COMPILE_CLASSPATH));
            inputs.fileSet("sourceDirs", context.getProject().getCompileSourceRoots(),
                    fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                            .lineEndingHandling(LineEndingHandling.NORMALIZE)
                            .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            if (test) {
                inputs.fileSet("defaultSourceDir", context.getProject().getCompileSourceRoots(),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                                .lineEndingHandling(LineEndingHandling.NORMALIZE)
                                .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
                inputs.fileSet("defaultSourceDirs", context.getProject().getCompileSourceRoots(),
                        fileSet -> fileSet.normalizationStrategy(NormalizationStrategy.RELATIVE_PATH)
                                .lineEndingHandling(LineEndingHandling.NORMALIZE)
                                .emptyDirectoryHandling(EmptyDirectoryHandling.IGNORE));
            }

            inputs.ignore("module", "testModule", "mojoExecution", "nowarn", "project", "jdkHome", "session");
        });

        context.localState(l -> l.files("incrementalCachesRoot"));

        context.outputs(outputs -> {
            outputs.cacheable("If the inputs are identical, we should have the same output");
            outputs.directory("output");
            outputs.directory("testOutput");
        });
    }
}
