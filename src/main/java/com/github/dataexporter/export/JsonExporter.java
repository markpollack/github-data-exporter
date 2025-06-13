package com.github.dataexporter.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.model.Issue;
import com.github.dataexporter.model.PullRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service responsible for exporting data to JSON files.
 * Handles file creation, directory management, and serialization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JsonExporter {

    private final ExportProperties exportProperties;
    private final ObjectMapper objectMapper;

    /**
     * Exports a list of issues to a JSON file.
     * The file path is determined from the export properties.
     *
     * @param issues List of issues to export
     * @return Path to the exported file, or null if export failed
     */
    public Path exportIssues(List<Issue> issues) {
        Path filePath = exportProperties.getIssuesFilePath();
        log.info("Exporting {} issues to {}", issues.size(), filePath);
        return exportToJson(issues, filePath);
    }

    /**
     * Exports a list of pull requests to a JSON file.
     * The file path is determined from the export properties.
     *
     * @param pullRequests List of pull requests to export
     * @return Path to the exported file, or null if export failed
     */
    public Path exportPullRequests(List<PullRequest> pullRequests) {
        Path filePath = exportProperties.getPullRequestsFilePath();
        log.info("Exporting {} pull requests to {}", pullRequests.size(), filePath);
        return exportToJson(pullRequests, filePath);
    }

    /**
     * Generic method to export any data to a JSON file.
     * Creates the directory if it doesn't exist.
     *
     * @param data     Data to export
     * @param filePath Path where the JSON file should be written
     * @param <T>      Type of the data
     * @return Path to the exported file, or null if export failed
     */
    public <T> Path exportToJson(List<T> data, Path filePath) {
        try {
            // Ensure the directory exists
            Path directory = filePath.getParent();
            if (directory != null && !Files.exists(directory)) {
                log.info("Creating export directory: {}", directory);
                Files.createDirectories(directory);
            }

            // Configure object mapper for pretty printing
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            // Write the data to the file
            objectMapper.writeValue(filePath.toFile(), data);
            
            log.info("Successfully exported data to {}", filePath);
            return filePath;
        } catch (IOException e) {
            log.error("Failed to export data to {}: {}", filePath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the configured export directory path.
     *
     * @return Path to the export directory
     */
    public Path getExportDirectory() {
        return Path.of(exportProperties.getDirectory());
    }

    /**
     * Ensures that the export directory exists, creating it if necessary.
     *
     * @return true if the directory exists or was created successfully, false otherwise
     */
    public boolean ensureExportDirectoryExists() {
        Path directory = getExportDirectory();
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                log.info("Created export directory: {}", directory);
                return true;
            } catch (IOException e) {
                log.error("Failed to create export directory {}: {}", directory, e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
}
