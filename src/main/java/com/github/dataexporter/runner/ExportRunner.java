package com.github.dataexporter.runner;

import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.config.GitHubProperties;
import com.github.dataexporter.export.JsonExporter;
import com.github.dataexporter.model.Issue;
import com.github.dataexporter.model.PullRequest;
import com.github.dataexporter.service.IssueService;
import com.github.dataexporter.service.PullRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Command line runner that orchestrates the GitHub data export process.
 * This class is responsible for coordinating the fetching of data from GitHub
 * and exporting it to JSON files.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExportRunner implements CommandLineRunner {

    private final IssueService issueService;
    private final PullRequestService pullRequestService;
    private final JsonExporter jsonExporter;
    private final GitHubProperties gitHubProperties;
    private final ExportProperties exportProperties;
    private final ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        log.info("Starting GitHub data export process");
        log.info("Repository: {}/{}", 
                gitHubProperties.getRepository().getOwner(), 
                gitHubProperties.getRepository().getName());
        log.info("Export directory: {}", exportProperties.getDirectory());

        boolean success = true;
        int exitCode = 0;
        Instant startTime = Instant.now();

        try {
            // Ensure export directory exists
            if (!jsonExporter.ensureExportDirectoryExists()) {
                log.error("Failed to create export directory. Aborting export process.");
                exitWithError(1);
                return;
            }

            // Export issues
            Path issuesPath = exportIssues();
            if (issuesPath == null) {
                success = false;
            }

            // Export pull requests
            Path pullRequestsPath = exportPullRequests();
            if (pullRequestsPath == null) {
                success = false;
            }

            // Report results
            if (success) {
                Duration duration = Duration.between(startTime, Instant.now());
                log.info("GitHub data export completed successfully in {}s", duration.getSeconds());
                log.info("Issues exported to: {}", issuesPath);
                log.info("Pull requests exported to: {}", pullRequestsPath);
            } else {
                log.error("GitHub data export completed with errors");
                exitCode = 1;
            }
        } catch (Exception e) {
            log.error("Unexpected error during GitHub data export: {}", e.getMessage(), e);
            exitWithError(2);
        }

        // Exit application if running as a standalone command-line app
        if (shouldExitApplication(args)) {
            exitWithCode(exitCode);
        }
    }

    /**
     * Exports GitHub issues to a JSON file.
     *
     * @return Path to the exported file, or null if export failed
     */
    private Path exportIssues() {
        try {
            log.info("Fetching issues from GitHub...");
            Instant startTime = Instant.now();
            List<Issue> issues = issueService.fetchAllIssues();
            Duration fetchDuration = Duration.between(startTime, Instant.now());
            log.info("Fetched {} issues in {}s", issues.size(), fetchDuration.getSeconds());

            if (issues.isEmpty()) {
                log.warn("No issues found to export");
                return null;
            }

            log.info("Exporting issues to JSON...");
            startTime = Instant.now();
            Path exportPath = jsonExporter.exportIssues(issues);
            Duration exportDuration = Duration.between(startTime, Instant.now());
            log.info("Exported {} issues to {} in {}s", issues.size(), exportPath, exportDuration.getSeconds());

            return exportPath;
        } catch (Exception e) {
            log.error("Failed to export issues: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Exports GitHub pull requests to a JSON file.
     *
     * @return Path to the exported file, or null if export failed
     */
    private Path exportPullRequests() {
        try {
            log.info("Fetching pull requests from GitHub...");
            Instant startTime = Instant.now();
            List<PullRequest> pullRequests = pullRequestService.fetchAllPullRequests();
            Duration fetchDuration = Duration.between(startTime, Instant.now());
            log.info("Fetched {} pull requests in {}s", pullRequests.size(), fetchDuration.getSeconds());

            if (pullRequests.isEmpty()) {
                log.warn("No pull requests found to export");
                return null;
            }

            log.info("Exporting pull requests to JSON...");
            startTime = Instant.now();
            Path exportPath = jsonExporter.exportPullRequests(pullRequests);
            Duration exportDuration = Duration.between(startTime, Instant.now());
            log.info("Exported {} pull requests to {} in {}s", pullRequests.size(), exportPath, exportDuration.getSeconds());

            return exportPath;
        } catch (Exception e) {
            log.error("Failed to export pull requests: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Determines if the application should exit after completing the export.
     * This allows the runner to be used both as a command-line application and as a component in a larger application.
     *
     * @param args Command line arguments
     * @return true if the application should exit, false otherwise
     */
    private boolean shouldExitApplication(String[] args) {
        // Check if any arguments indicate this is a one-time export operation
        for (String arg : args) {
            if (arg.equals("--export-and-exit")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Exits the application with the specified error code.
     *
     * @param code Exit code
     */
    private void exitWithError(int code) {
        exitWithCode(code);
    }

    /**
     * Exits the application with the specified code.
     *
     * @param code Exit code
     */
    private void exitWithCode(int code) {
        int exitCode = code;
        SpringApplication.exit(applicationContext, (ExitCodeGenerator) () -> exitCode);
    }
}
