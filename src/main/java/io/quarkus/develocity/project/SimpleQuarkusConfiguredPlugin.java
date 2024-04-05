package io.quarkus.develocity.project;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import com.gradle.develocity.agent.maven.api.DevelocityApi;
import com.gradle.develocity.agent.maven.api.cache.MojoMetadataProvider;
import com.gradle.develocity.agent.maven.api.cache.NormalizationProvider;

public abstract class SimpleQuarkusConfiguredPlugin implements ConfiguredPlugin {

    @Override
    public void configureBuildCache(DevelocityApi develocityApi, MavenSession mavenSession) {
        develocityApi.getBuildCache().registerMojoMetadataProvider(context -> {
            context.withPlugin(getPluginName(), () -> {
                if (!isBuildCacheEnabled(context.getProject())) {
                    Log.debug(getPluginName(), "Build cache is disabled.");
                    return;
                }

                Map<String, GoalMetadataProvider> goalMetadataProviders = Collections.unmodifiableMap(getGoalMetadataProviders());

                Log.debug(getPluginName(), "Build cache is enabled. Configuring metadata providers.");
                Log.debug(getPluginName(), "Configuring metadata for goals: " + goalMetadataProviders.keySet());

                for (Entry<String, GoalMetadataProvider> goalMetadataProviderEntry : goalMetadataProviders.entrySet()) {
                    if (goalMetadataProviderEntry.getKey().equalsIgnoreCase(context.getMojoExecution().getGoal())) {
                        goalMetadataProviderEntry.getValue().configure(context);
                    }
                }
            });
        });
    }

    protected abstract String getPluginName();

    protected boolean isBuildCacheEnabled(MavenProject project) {
        return true;
    }

    protected abstract Map<String, GoalMetadataProvider> getGoalMetadataProviders();

    protected static void dependsOnGav(MojoMetadataProvider.Context.Inputs inputs, MojoMetadataProvider.Context context) {
        inputs.property("_internal_gav", context.getProject().getGroupId() + ":" + context.getProject().getArtifactId() + ":" + context.getProject().getVersion());
    }


    protected static void dependsOnOs(MojoMetadataProvider.Context.Inputs inputs) {
        inputs.property("_internal_osName", System.getProperty("os.name"))
            .property("_internal_osVersion", System.getProperty("os.version"))
            .property("_internal_osArch", System.getProperty("os.arch"));
    }

    protected static void dependsOnJavaVersion(MojoMetadataProvider.Context.Inputs inputs) {
        inputs.property("_internal_javaVersion", System.getProperty("java.version"));
    }

    protected void addClasspathInput(MojoMetadataProvider.Context context, MojoMetadataProvider.Context.Inputs inputs) {
        try {
            List<String> compileClasspathElements = context.getProject().getCompileClasspathElements();
            inputs.fileSet("quarkusCompileClasspath", compileClasspathElements, fileSet -> fileSet.normalizationStrategy(MojoMetadataProvider.Context.FileSet.NormalizationStrategy.CLASSPATH));
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalStateException("Classpath can't be resolved");
        }
    }

    @FunctionalInterface
    public interface PluginNormalizationProvider {

        void configure(NormalizationProvider.Context context);
    }

    @FunctionalInterface
    public interface GoalMetadataProvider {

        void configure(MojoMetadataProvider.Context context);
    }
}
