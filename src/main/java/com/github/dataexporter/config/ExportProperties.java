package com.github.dataexporter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Configuration properties for data export functionality.
 * Maps to the "export" section in application.yml.
 */
@ConfigurationProperties(prefix = "export")
@Data
@Validated
public class ExportProperties {

    /**
     * Directory where exported files will be saved.
     * Defaults to "./exports" if not specified.
     */
    @NotBlank(message = "Export directory must be provided")
    private String directory = "./exports";

    /**
     * Configuration for export file names.
     */
    private Files files = new Files();

    /**
     * Number of items to fetch in each GraphQL request.
     */
    @Positive(message = "Batch size must be positive")
    private int batchSize = 100;

    /**
     * Pagination configuration for data fetching.
     */
    private Pagination pagination = new Pagination();

    /**
     * File names configuration for different export types.
     */
    @Data
    public static class Files {
        /**
         * Filename for exported issues data.
         */
        @NotBlank(message = "Issues filename must be provided")
        private String issues = "issues.json";

        /**
         * Filename for exported pull requests data.
         */
        @NotBlank(message = "Pull requests filename must be provided")
        private String pullRequests = "pull-requests.json";
    }

    /**
     * Pagination configuration for data fetching.
     */
    @Data
    public static class Pagination {
        /**
         * Whether pagination is enabled.
         */
        private boolean enabled = true;

        /**
         * Maximum number of items to fetch in total.
         */
        @Positive(message = "Maximum items must be positive")
        private int maxItems = 1000;
    }

    /**
     * Get the full path for the issues export file.
     * 
     * @return Path to the issues export file
     */
    public Path getIssuesFilePath() {
        return Path.of(directory, files.getIssues());
    }

    /**
     * Get the full path for the pull requests export file.
     * 
     * @return Path to the pull requests export file
     */
    public Path getPullRequestsFilePath() {
        return Path.of(directory, files.getPullRequests());
    }
}
