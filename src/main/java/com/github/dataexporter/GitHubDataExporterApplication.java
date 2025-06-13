package com.github.dataexporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the GitHub Data Exporter application.
 * This application connects to the GitHub GraphQL API to export issues and pull requests data
 * to JSON files for further analysis.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class GitHubDataExporterApplication {

    private static final Logger logger = LoggerFactory.getLogger(GitHubDataExporterApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GitHubDataExporterApplication.class, args);
    }

    /**
     * Command line runner that will be executed when the application starts.
     * This allows the export process to be triggered automatically on application startup.
     * The actual export logic will be implemented in the service classes that will be injected here.
     */
    @Bean
    public CommandLineRunner exportRunner() {
        return args -> {
            logger.info("Starting GitHub data export process...");
            // The export services will be injected and called here in subsequent implementations
            logger.info("GitHub data export process completed.");
        };
    }
}
