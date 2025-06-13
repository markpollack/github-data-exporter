package com.github.dataexporter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.export.JsonExporter;
import com.github.dataexporter.model.Issue;
import com.github.dataexporter.model.PullRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JsonExporterTest {

    @Mock
    private ExportProperties exportProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<File> fileCaptor;

    @Captor
    private ArgumentCaptor<List<Object>> dataCaptor;

    private JsonExporter jsonExporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jsonExporter = new JsonExporter(exportProperties, objectMapper);
    }

    @Test
    void exportIssues_shouldUseCorrectPathAndData() throws IOException {
        // Arrange
        Path issuesPath = tempDir.resolve("issues.json");
        List<Issue> issues = createTestIssues(3);
        
        when(exportProperties.getIssuesFilePath()).thenReturn(issuesPath);
        when(objectMapper.writeValue(any(File.class), any())).thenReturn(null);

        // Act
        Path result = jsonExporter.exportIssues(issues);

        // Assert
        assertEquals(issuesPath, result);
        verify(objectMapper).enable(SerializationFeature.INDENT_OUTPUT);
        verify(objectMapper).writeValue(fileCaptor.capture(), any());
        assertEquals(issuesPath.toFile(), fileCaptor.getValue());
    }

    @Test
    void exportPullRequests_shouldUseCorrectPathAndData() throws IOException {
        // Arrange
        Path pullRequestsPath = tempDir.resolve("pull-requests.json");
        List<PullRequest> pullRequests = createTestPullRequests(2);
        
        when(exportProperties.getPullRequestsFilePath()).thenReturn(pullRequestsPath);
        when(objectMapper.writeValue(any(File.class), any())).thenReturn(null);

        // Act
        Path result = jsonExporter.exportPullRequests(pullRequests);

        // Assert
        assertEquals(pullRequestsPath, result);
        verify(objectMapper).enable(SerializationFeature.INDENT_OUTPUT);
        verify(objectMapper).writeValue(fileCaptor.capture(), any());
        assertEquals(pullRequestsPath.toFile(), fileCaptor.getValue());
    }

    @Test
    void exportToJson_shouldCreateDirectoryIfNotExists() throws IOException {
        // Arrange
        Path nonExistentDir = tempDir.resolve("subdir");
        Path filePath = nonExistentDir.resolve("data.json");
        List<String> data = Arrays.asList("item1", "item2");
        
        // Act
        Path result = jsonExporter.exportToJson(data, filePath);

        // Assert
        assertEquals(filePath, result);
        assertTrue(Files.exists(nonExistentDir));
        verify(objectMapper).enable(SerializationFeature.INDENT_OUTPUT);
        verify(objectMapper).writeValue(eq(filePath.toFile()), eq(data));
    }

    @Test
    void exportToJson_shouldHandleIOException() throws IOException {
        // Arrange
        Path filePath = tempDir.resolve("error.json");
        List<String> data = Arrays.asList("item1", "item2");
        
        doThrow(new IOException("Test IO error")).when(objectMapper).writeValue(any(File.class), any());

        // Act
        Path result = jsonExporter.exportToJson(data, filePath);

        // Assert
        assertNull(result);
        verify(objectMapper).enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    void ensureExportDirectoryExists_shouldCreateDirectoryIfNotExists() {
        // Arrange
        Path nonExistentDir = tempDir.resolve("newdir");
        when(exportProperties.getDirectory()).thenReturn(nonExistentDir.toString());

        // Act
        boolean result = jsonExporter.ensureExportDirectoryExists();

        // Assert
        assertTrue(result);
        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    void ensureExportDirectoryExists_shouldReturnTrueIfDirectoryAlreadyExists() {
        // Arrange
        when(exportProperties.getDirectory()).thenReturn(tempDir.toString());

        // Act
        boolean result = jsonExporter.ensureExportDirectoryExists();

        // Assert
        assertTrue(result);
    }

    @Test
    void getExportDirectory_shouldReturnCorrectPath() {
        // Arrange
        String dirPath = "/some/path";
        when(exportProperties.getDirectory()).thenReturn(dirPath);

        // Act
        Path result = jsonExporter.getExportDirectory();

        // Assert
        assertEquals(Path.of(dirPath), result);
    }

    // Helper methods to create test data
    private List<Issue> createTestIssues(int count) {
        List<Issue> issues = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Issue issue = Issue.builder()
                    .id("issue-" + i)
                    .number(i + 1)
                    .title("Test Issue " + (i + 1))
                    .body("This is a test issue")
                    .state("OPEN")
                    .createdAt(ZonedDateTime.now())
                    .build();
            issues.add(issue);
        }
        return issues;
    }

    private List<PullRequest> createTestPullRequests(int count) {
        List<PullRequest> pullRequests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PullRequest pr = PullRequest.builder()
                    .id("pr-" + i)
                    .number(i + 100)
                    .title("Test PR " + (i + 1))
                    .body("This is a test pull request")
                    .state("OPEN")
                    .createdAt(ZonedDateTime.now())
                    .build();
            pullRequests.add(pr);
        }
        return pullRequests;
    }
}
