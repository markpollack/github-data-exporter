package com.github.dataexporter.controller;

import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.export.JsonExporter;
import com.github.dataexporter.model.Issue;
import com.github.dataexporter.model.PullRequest;
import com.github.dataexporter.service.IssueService;
import com.github.dataexporter.service.PullRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * REST controller providing endpoints for manually triggering GitHub data exports
 * and checking export status.
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final IssueService issueService;
    private final PullRequestService pullRequestService;
    private final JsonExporter jsonExporter;
    private final ExportProperties exportProperties;

    // Track export status
    private final AtomicBoolean exportInProgress = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, ExportStatus> exportStatuses = new ConcurrentHashMap<>();

    /**
     * Class representing the status of an export operation.
     */
    private static class ExportStatus {
        private final String id;
        private final String type;
        private final Instant startTime;
        private Instant endTime;
        private String status;
        private String filePath;
        private int itemCount;
        private String errorMessage;

        public ExportStatus(String id, String type) {
            this.id = id;
            this.type = type;
            this.startTime = Instant.now();
            this.status = "IN_PROGRESS";
        }

        public void complete(int itemCount, String filePath) {
            this.endTime = Instant.now();
            this.status = "COMPLETED";
            this.itemCount = itemCount;
            this.filePath = filePath;
        }

        public void fail(String errorMessage) {
            this.endTime = Instant.now();
            this.status = "FAILED";
            this.errorMessage = errorMessage;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("type", type);
            map.put("status", status);
            map.put("startTime", startTime.toString());
            
            if (endTime != null) {
                map.put("endTime", endTime.toString());
                map.put("durationSeconds", Duration.between(startTime, endTime).getSeconds());
            }
            
            if (itemCount > 0) {
                map.put("itemCount", itemCount);
            }
            
            if (filePath != null) {
                map.put("filePath", filePath);
            }
            
            if (errorMessage != null) {
                map.put("errorMessage", errorMessage);
            }
            
            return map;
        }
    }

    /**
     * Endpoint to export both issues and pull requests.
     *
     * @return Response with export status information
     */
    @PostMapping
    public ResponseEntity<?> exportAll() {
        if (exportInProgress.getAndSet(true)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Export already in progress"));
        }

        try {
            // Ensure export directory exists
            if (!jsonExporter.ensureExportDirectoryExists()) {
                exportInProgress.set(false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to create export directory"));
            }

            // Generate unique IDs for each export
            String issuesExportId = "issues-" + Instant.now().toEpochMilli();
            String prsExportId = "prs-" + Instant.now().toEpochMilli();

            // Create and store export status objects
            ExportStatus issuesStatus = new ExportStatus(issuesExportId, "issues");
            ExportStatus prsStatus = new ExportStatus(prsExportId, "pull_requests");
            exportStatuses.put(issuesExportId, issuesStatus);
            exportStatuses.put(prsExportId, prsStatus);

            // Start async export tasks
            CompletableFuture<Void> issuesFuture = CompletableFuture.runAsync(() -> exportIssues(issuesStatus));
            CompletableFuture<Void> prsFuture = CompletableFuture.runAsync(() -> exportPullRequests(prsStatus));

            // When both exports are done, reset the in-progress flag
            CompletableFuture.allOf(issuesFuture, prsFuture)
                    .thenRun(() -> exportInProgress.set(false));

            // Return the export IDs for status tracking
            return ResponseEntity.accepted().body(Map.of(
                    "message", "Export started",
                    "issuesExportId", issuesExportId,
                    "pullRequestsExportId", prsExportId
            ));
        } catch (Exception e) {
            exportInProgress.set(false);
            log.error("Failed to start export: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start export: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to export only GitHub issues.
     *
     * @return Response with export status information
     */
    @PostMapping("/issues")
    public ResponseEntity<?> exportIssuesOnly() {
        if (exportInProgress.getAndSet(true)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Export already in progress"));
        }

        try {
            // Ensure export directory exists
            if (!jsonExporter.ensureExportDirectoryExists()) {
                exportInProgress.set(false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to create export directory"));
            }

            // Generate unique ID for this export
            String exportId = "issues-" + Instant.now().toEpochMilli();

            // Create and store export status object
            ExportStatus status = new ExportStatus(exportId, "issues");
            exportStatuses.put(exportId, status);

            // Start async export task
            CompletableFuture.runAsync(() -> {
                try {
                    exportIssues(status);
                } finally {
                    exportInProgress.set(false);
                }
            });

            // Return the export ID for status tracking
            return ResponseEntity.accepted().body(Map.of(
                    "message", "Issues export started",
                    "exportId", exportId
            ));
        } catch (Exception e) {
            exportInProgress.set(false);
            log.error("Failed to start issues export: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start issues export: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to export only GitHub pull requests.
     *
     * @return Response with export status information
     */
    @PostMapping("/pull-requests")
    public ResponseEntity<?> exportPullRequestsOnly() {
        if (exportInProgress.getAndSet(true)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Export already in progress"));
        }

        try {
            // Ensure export directory exists
            if (!jsonExporter.ensureExportDirectoryExists()) {
                exportInProgress.set(false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to create export directory"));
            }

            // Generate unique ID for this export
            String exportId = "prs-" + Instant.now().toEpochMilli();

            // Create and store export status object
            ExportStatus status = new ExportStatus(exportId, "pull_requests");
            exportStatuses.put(exportId, status);

            // Start async export task
            CompletableFuture.runAsync(() -> {
                try {
                    exportPullRequests(status);
                } finally {
                    exportInProgress.set(false);
                }
            });

            // Return the export ID for status tracking
            return ResponseEntity.accepted().body(Map.of(
                    "message", "Pull requests export started",
                    "exportId", exportId
            ));
        } catch (Exception e) {
            exportInProgress.set(false);
            log.error("Failed to start pull requests export: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start pull requests export: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to check the status of an export operation.
     *
     * @param exportId ID of the export operation to check
     * @return Response with export status information
     */
    @GetMapping("/status/{exportId}")
    public ResponseEntity<?> getExportStatus(@PathVariable String exportId) {
        ExportStatus status = exportStatuses.get(exportId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status.toMap());
    }

    /**
     * Endpoint to check the status of all export operations.
     *
     * @return Response with all export status information
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAllExportStatuses() {
        Map<String, Object> allStatuses = new HashMap<>();
        exportStatuses.forEach((id, status) -> allStatuses.put(id, status.toMap()));
        return ResponseEntity.ok(Map.of(
                "exportInProgress", exportInProgress.get(),
                "exports", allStatuses
        ));
    }

    /**
     * Exports GitHub issues and updates the export status.
     *
     * @param status Export status object to update
     */
    private void exportIssues(ExportStatus status) {
        try {
            log.info("Starting issues export (ID: {})", status.id);
            
            // Fetch issues from GitHub
            List<Issue> issues = issueService.fetchAllIssues();
            
            if (issues.isEmpty()) {
                status.fail("No issues found to export");
                log.warn("No issues found to export (ID: {})", status.id);
                return;
            }
            
            // Export issues to JSON file
            Path exportPath = jsonExporter.exportIssues(issues);
            
            if (exportPath == null) {
                status.fail("Failed to write issues to file");
                log.error("Failed to write issues to file (ID: {})", status.id);
                return;
            }
            
            // Update export status
            status.complete(issues.size(), exportPath.toString());
            log.info("Issues export completed successfully (ID: {}): {} issues exported to {}", 
                    status.id, issues.size(), exportPath);
        } catch (Exception e) {
            status.fail(e.getMessage());
            log.error("Error during issues export (ID: {}): {}", status.id, e.getMessage(), e);
        }
    }

    /**
     * Exports GitHub pull requests and updates the export status.
     *
     * @param status Export status object to update
     */
    private void exportPullRequests(ExportStatus status) {
        try {
            log.info("Starting pull requests export (ID: {})", status.id);
            
            // Fetch pull requests from GitHub
            List<PullRequest> pullRequests = pullRequestService.fetchAllPullRequests();
            
            if (pullRequests.isEmpty()) {
                status.fail("No pull requests found to export");
                log.warn("No pull requests found to export (ID: {})", status.id);
                return;
            }
            
            // Export pull requests to JSON file
            Path exportPath = jsonExporter.exportPullRequests(pullRequests);
            
            if (exportPath == null) {
                status.fail("Failed to write pull requests to file");
                log.error("Failed to write pull requests to file (ID: {})", status.id);
                return;
            }
            
            // Update export status
            status.complete(pullRequests.size(), exportPath.toString());
            log.info("Pull requests export completed successfully (ID: {}): {} pull requests exported to {}", 
                    status.id, pullRequests.size(), exportPath);
        } catch (Exception e) {
            status.fail(e.getMessage());
            log.error("Error during pull requests export (ID: {}): {}", status.id, e.getMessage(), e);
        }
    }
}
