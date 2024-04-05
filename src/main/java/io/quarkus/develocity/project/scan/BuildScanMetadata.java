package io.quarkus.develocity.project.scan;

import static io.quarkus.develocity.project.util.Strings.isBlank;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.gradle.develocity.agent.maven.api.scan.BuildScanApi;

import io.quarkus.develocity.project.Log;

public final class BuildScanMetadata {

    private BuildScanMetadata() {
    }

    public static void addMetadataToBuildScan(BuildScanApi buildScanApi) {
        // Add mvn command line
        final String mavenCommandLine = System.getenv("MAVEN_CMD_LINE_ARGS") != null ? "mvn " + System.getenv("MAVEN_CMD_LINE_ARGS") : "";
        if (!isBlank(mavenCommandLine)) {
            buildScanApi.value("Maven command line", mavenCommandLine);
        }

        //Add github action information
        if (System.getenv("GITHUB_ACTIONS") != null) {
            String jobId = System.getenv("GITHUB_JOB");

            buildScanApi.value("gh-job-id", jobId);
            buildScanApi.value("gh-event-name", System.getenv("GITHUB_EVENT_NAME"));
            buildScanApi.value("gh-ref-name", System.getenv("GITHUB_REF_NAME"));
            buildScanApi.value("gh-actor", System.getenv("GITHUB_ACTOR"));
            buildScanApi.value("gh-workflow", System.getenv("GITHUB_WORKFLOW"));
            String jobCustomValues = System.getenv("GE_CUSTOM_VALUES");
            if (!isBlank(jobCustomValues)) {
                for (String jobCustomValue : jobCustomValues.split(",")) {
                    int index = jobCustomValue.indexOf('=');
                    if (index <= 0) {
                        continue;
                    }
                    buildScanApi.value(jobCustomValue.substring(0, index).trim(), jobCustomValue.substring(index + 1).trim());
                }
            }

            List<String> similarBuildsTags = new ArrayList<>();

            buildScanApi.tag(jobId);
            similarBuildsTags.add(jobId);

            buildScanApi.tag(System.getenv("GITHUB_EVENT_NAME"));
            similarBuildsTags.add(System.getenv("GITHUB_EVENT_NAME"));

            buildScanApi.tag(System.getenv("GITHUB_WORKFLOW"));
            similarBuildsTags.add(System.getenv("GITHUB_WORKFLOW"));

            String jobTags = System.getenv("GE_TAGS");
            if (!isBlank(jobTags)) {
                for (String tag : jobTags.split(",")) {
                    buildScanApi.tag(tag.trim());
                    similarBuildsTags.add(tag.trim());
                }
            }

            buildScanApi.link("Workflow run", System.getenv("GITHUB_SERVER_URL") + "/" + System.getenv("GITHUB_REPOSITORY")
                    + "/actions/runs/" + System.getenv("GITHUB_RUN_ID"));

            String prNumber = System.getenv("PULL_REQUEST_NUMBER");
            if (!isBlank(prNumber)) {
                buildScanApi.value("gh-pr", prNumber);
                buildScanApi.tag("pr-" + prNumber);
                similarBuildsTags.add("pr-" + prNumber);

                buildScanApi.link("Pull request",
                        System.getenv("GITHUB_SERVER_URL") + "/" + System.getenv("GITHUB_REPOSITORY") + "/pull/" + prNumber);

                if (!isBlank(System.getenv("GITHUB_BASE_REF"))) {
                    buildScanApi.tag(System.getenv("GITHUB_BASE_REF"));
                }
            }

            similarBuildsTags.add(System.getenv("RUNNER_OS"));

            buildScanApi.link("Similar builds", "https://ge.quarkus.io/scans?search.tags="
                    + URLEncoder.encode(String.join(",", similarBuildsTags), StandardCharsets.UTF_8).replace("+", "%20"));

            buildScanApi.buildScanPublished(publishedBuildScan -> {
                File target = new File("target");
                if (!target.exists()) {
                    target.mkdir();
                }

                try {
                    Path gradleBuildScanUrlFile = Path.of("target/gradle-build-scan-url.txt");
                    if (!Files.exists(gradleBuildScanUrlFile)) {
                        Files.writeString(gradleBuildScanUrlFile, publishedBuildScan.getBuildScanUri().toString());
                    }

                    Files.writeString(Path.of(System.getenv("GITHUB_STEP_SUMMARY")),
                            "\n[Build scan](" + publishedBuildScan.getBuildScanUri() + ")\n<sup>`" + mavenCommandLine + "`</sup>\n\n",
                            StandardOpenOption.APPEND);
                } catch (IOException e) {
                    Log.warn("Unable to write build scan information to files", e);
                }
            });
        }
    }
}
