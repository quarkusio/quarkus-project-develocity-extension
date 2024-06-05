package io.quarkus.develocity.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

import com.gradle.develocity.agent.maven.api.DevelocityApi;
import com.gradle.develocity.agent.maven.api.DevelocityListener;
import com.gradle.develocity.agent.maven.api.cache.BuildCacheApi;
import com.gradle.develocity.agent.maven.api.scan.BuildScanPublishing;

import io.quarkus.develocity.project.normalization.Normalization;
import io.quarkus.develocity.project.plugins.CompilerConfiguredPlugin;
import io.quarkus.develocity.project.plugins.EnforcerConfiguredPlugin;
import io.quarkus.develocity.project.plugins.FailsafeConfiguredPlugin;
import io.quarkus.develocity.project.plugins.FormatterConfiguredPlugin;
import io.quarkus.develocity.project.plugins.ImpsortConfiguredPlugin;
import io.quarkus.develocity.project.plugins.KotlinConfiguredPlugin;
import io.quarkus.develocity.project.plugins.QuarkusConfiguredPlugin;
import io.quarkus.develocity.project.plugins.SpotlessConfiguredPlugin;
import io.quarkus.develocity.project.plugins.SurefireConfiguredPlugin;
import io.quarkus.develocity.project.scan.BuildScanMetadata;
import io.quarkus.develocity.project.scan.MavenVersionChecker;

@SuppressWarnings("deprecation")
@Component(role = DevelocityListener.class, hint = "quarkus-project-build-cache", description = "Configures Develocity for the Quarkus project")
public class QuarkusProjectDevelocityConfigurationListener implements DevelocityListener {

    private static final String QUICKLY = "-Dquickly";
    private static final String DASH = "-";

    private static final List<String> NESTED_PROJECTS_PATHS = List.of(
            File.separator + "target" + File.separator + "codestart-test" + File.separator,
            File.separator + "target" + File.separator + "it" + File.separator,
            File.separator + "target" + File.separator + "test-classes" + File.separator,
            File.separator + "target" + File.separator + "test-project" + File.separator);

    @Override
    public void configure(DevelocityApi develocityApi, MavenSession mavenSession) throws Exception {
        if (ignoreProject(mavenSession)) {
            // do not publish a build scan for test builds
            Log.debug("Disabling build scan publication and build cache for nested project: "
                    + mavenSession.getRequest().getBaseDirectory());

            develocityApi.getBuildScan().getPublishing().onlyIf(context -> false);
            develocityApi.getBuildCache().getLocal().setEnabled(false);
            develocityApi.getBuildCache().getRemote().setEnabled(false);

            if (System.getenv("GITHUB_ACTIONS") != null) {
                try {
                    Path storageLocationTmpDir = Files.createTempDirectory(Path.of(System.getenv("RUNNER_TEMP")),
                            "buildScanTmp");
                    Log.debug("Update storage location to " + storageLocationTmpDir);
                    develocityApi.setStorageDirectory(storageLocationTmpDir);
                } catch (IOException e) {
                    Log.error("Temporary storage location directory cannot be created, the Build Scan will be published", e);
                }
            }

            return;
        }

        develocityApi.getBuildScan().publishing(p -> p.onlyIf(BuildScanPublishing.PublishingContext::isAuthenticated));
        BuildScanMetadata.addMetadataToBuildScan(develocityApi.getBuildScan());
        MavenVersionChecker.checkRuntimeMavenVersion(develocityApi.getBuildScan(), mavenSession);

        workaroundQuickly(develocityApi.getBuildCache());

        Normalization.configureNormalization(develocityApi.getBuildCache());

        List<ConfiguredPlugin> configuredGoals = List.of(
                new CompilerConfiguredPlugin(),
                new SurefireConfiguredPlugin(),
                new FailsafeConfiguredPlugin(),
                new EnforcerConfiguredPlugin(),
                //new SourceConfiguredPlugin(),
                new QuarkusConfiguredPlugin(),
                new FormatterConfiguredPlugin(),
                new ImpsortConfiguredPlugin(),
                new KotlinConfiguredPlugin(),
                new SpotlessConfiguredPlugin()
        //new QuarkusExtensionConfiguredPlugin()
        );

        for (ConfiguredPlugin configuredGoal : configuredGoals) {
            configuredGoal.configureBuildCache(develocityApi, mavenSession);
        }
    }

    private static void workaroundQuickly(BuildCacheApi buildCacheApi) {
        String mavenCommandLine = System.getenv("MAVEN_CMD_LINE_ARGS");
        if (mavenCommandLine == null || mavenCommandLine.isBlank()) {
            // let's check if we are using mvnd
            if (System.getProperty("mvnd.home") != null && "true".equals(System.getProperty("quickly"))) {
                // unfortunately, with mvnd, we have no way to inspect the Maven command line so this will have to do
                buildCacheApi.setRequireClean(false);
            }

            return;
        }

        mavenCommandLine = mavenCommandLine.trim();

        String[] segments = mavenCommandLine.split(" ");

        boolean hasQuickly = false;
        boolean hasGoals = false;

        for (String segment : segments) {
            segment = segment.trim();
            if (segment.isEmpty()) {
                continue;
            }
            if (QUICKLY.equals(segment)) {
                hasQuickly = true;
                continue;
            }
            // any option
            if (segment.startsWith(DASH)) {
                continue;
            }
            // when using -T 6/-T 6C as it works
            if (Character.isDigit(segment.charAt(0))) {
                continue;
            }
            // when using -T C6 which seems to work for some versions of Maven (but not mine)
            if (segment.length() > 2 && segment.charAt(0) == 'C' && Character.isDigit(segment.charAt(1))) {
                continue;
            }

            hasGoals = true;
        }

        if (hasQuickly && !hasGoals) {
            buildCacheApi.setRequireClean(false);
        }
    }

    private static boolean ignoreProject(MavenSession mavenSession) {
        if (mavenSession == null || mavenSession.getRequest() == null || mavenSession.getRequest().getBaseDirectory() == null) {
            return false;
        }

        for (String nestedProjectsPath : NESTED_PROJECTS_PATHS) {
            if (mavenSession.getRequest().getBaseDirectory().contains(nestedProjectsPath)) {
                return true;
            }
        }

        return false;
    }
}
