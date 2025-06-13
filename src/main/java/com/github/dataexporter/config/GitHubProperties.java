package com.github.dataexporter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for GitHub API access.
 * Maps to the "github" section in application.yml.
 */
@ConfigurationProperties(prefix = "github")
@Data
@Validated
public class GitHubProperties {

    /**
     * API-related configuration properties.
     */
    private Api api = new Api();

    /**
     * Repository-specific configuration properties.
     */
    private Repository repository = new Repository();

    /**
     * GitHub API configuration properties.
     */
    @Data
    public static class Api {
        /**
         * Personal access token for GitHub API authentication.
         * Should be provided via environment variable for security.
         */
        @NotBlank(message = "GitHub API token must be provided")
        private String token;

        /**
         * GitHub API version to use.
         */
        private String version = "v4";

        /**
         * Rate limit configuration for GitHub API.
         */
        private RateLimit rateLimit = new RateLimit();

        /**
         * Rate limit configuration properties.
         */
        @Data
        public static class RateLimit {
            /**
             * Whether rate limiting is enabled.
             */
            private boolean enabled = true;

            /**
             * Maximum number of requests allowed.
             */
            @Positive
            private int maxRequests = 5000;

            /**
             * Time period in seconds for the rate limit.
             */
            @Positive
            private int perHour = 3600;
        }
    }

    /**
     * GitHub repository configuration properties.
     */
    @Data
    public static class Repository {
        /**
         * Owner (user or organization) of the GitHub repository.
         */
        @NotBlank(message = "Repository owner must be provided")
        private String owner;

        /**
         * Name of the GitHub repository.
         */
        @NotBlank(message = "Repository name must be provided")
        private String name;
    }
}
