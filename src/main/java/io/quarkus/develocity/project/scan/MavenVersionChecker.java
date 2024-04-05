package io.quarkus.develocity.project.scan;

import static io.quarkus.develocity.project.util.Strings.isBlank;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.gradle.develocity.agent.maven.api.scan.BuildScanApi;

import io.quarkus.develocity.project.Log;

public final class MavenVersionChecker {

    private static final Pattern MAVEN_WRAPPER_VERSION_PATTERN = Pattern.compile(".*/apache-maven-(.*)-bin\\.zip");

    private MavenVersionChecker() {
    }

    public static void checkRuntimeMavenVersion(BuildScanApi buildScanApi, MavenSession mavenSession) {
        // Check runtime Maven version and Maven Wrapper version are aligned
        RuntimeInformation runtimeInfo;
        try {
            runtimeInfo = (RuntimeInformation) mavenSession.lookup(RuntimeInformation.class.getName());
        } catch (ComponentLookupException e) {
            return;
        }

        if (runtimeInfo == null) {
            return;
        }

        String runtimeMavenVersion = runtimeInfo.getMavenVersion();
        Properties mavenWrapperProperties = new Properties();
        Path mavenWrapperPropertiesPath = Path.of(".mvn/wrapper/maven-wrapper.properties");
        if (Files.isReadable(mavenWrapperPropertiesPath)) {
            try (Reader reader = Files.newBufferedReader(mavenWrapperPropertiesPath)) {
                mavenWrapperProperties.load(reader);
                // assuming the wrapper properties contains:
                // distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/VERSION/apache-maven-VERSION-bin.zip
                Matcher matcher = MAVEN_WRAPPER_VERSION_PATTERN.matcher(mavenWrapperProperties.getProperty("distributionUrl"));
                if (matcher.matches()) {
                    String wrapperMavenVersion = matcher.group(1);
                    if (!isBlank(runtimeMavenVersion) && !isBlank(wrapperMavenVersion)
                            && !wrapperMavenVersion.equals(runtimeMavenVersion)) {
                        Log.warn("Maven Wrapper is configured with a different version (" + wrapperMavenVersion
                                + ") than the runtime version (" + runtimeMavenVersion
                                + "). This will negatively impact build consistency and build caching.");
                        buildScanApi.tag("misaligned-maven-version");
                        buildScanApi.value("wrapper-maven-version", wrapperMavenVersion);
                    }
                }
            } catch (Exception e) {
                Log.warn("Unable to determine Maven wrapper version", e);
            }
        }
    }
}
