package com.github.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.config.GitHubProperties;
import com.github.dataexporter.model.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for fetching GitHub issues using the GraphQL API.
 * Handles pagination and mapping of API responses to domain objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IssueService {

    private final HttpGraphQlClient graphQlClient;
    private final GitHubProperties gitHubProperties;
    private final ExportProperties exportProperties;
    private final ObjectMapper objectMapper;

    private static final String ISSUE_QUERY_FILE = "graphql/issue-query.graphql";
    private static final String ISSUE_STATES_ALL = "[OPEN, CLOSED]";

    /**
     * Fetches all issues from the configured GitHub repository.
     * Uses pagination to retrieve all issues up to the configured maximum.
     *
     * @return List of Issue objects representing GitHub issues
     */
    public List<Issue> fetchAllIssues() {
        log.info("Fetching issues for repository {}/{}",
                gitHubProperties.getRepository().getOwner(),
                gitHubProperties.getRepository().getName());

        String query = loadQueryFromFile();
        List<Issue> allIssues = new ArrayList<>();
        String cursor = null;
        boolean hasNextPage = true;
        AtomicInteger totalFetched = new AtomicInteger(0);
        int maxItems = exportProperties.getPagination().getMaxItems();
        int batchSize = exportProperties.getBatchSize();

        while (hasNextPage && totalFetched.get() < maxItems) {
            int remainingItems = maxItems - totalFetched.get();
            int currentBatchSize = Math.min(batchSize, remainingItems);
            
            Map<String, Object> variables = createQueryVariables(currentBatchSize, cursor);
            
            try {
                JsonNode response = executeGraphQLQuery(query, variables)
                        .block(Duration.ofSeconds(30));
                
                if (response == null) {
                    log.error("Null response received from GitHub API");
                    break;
                }

                List<Issue> issues = extractIssuesFromResponse(response);
                if (issues.isEmpty()) {
                    break;
                }
                
                allIssues.addAll(issues);
                totalFetched.addAndGet(issues.size());
                
                JsonNode pageInfo = response.path("data").path("repository").path("issues").path("pageInfo");
                hasNextPage = pageInfo.path("hasNextPage").asBoolean(false);
                cursor = pageInfo.path("endCursor").asText(null);
                
                log.info("Fetched {} issues, total: {}, hasNextPage: {}", 
                        issues.size(), totalFetched.get(), hasNextPage);
                
                // Respect GitHub's rate limits with a small delay between requests
                if (hasNextPage) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting between API requests", e);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching issues at cursor {}: {}", cursor, e.getMessage(), e);
                break;
            }
        }

        log.info("Completed fetching issues. Total issues fetched: {}", allIssues.size());
        return allIssues;
    }

    /**
     * Loads the GraphQL query from a file on the classpath.
     *
     * @return The query string
     */
    private String loadQueryFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource(ISSUE_QUERY_FILE);
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load GraphQL query from file: {}", ISSUE_QUERY_FILE, e);
            throw new RuntimeException("Failed to load GraphQL query", e);
        }
    }

    /**
     * Creates the variables map for the GraphQL query.
     *
     * @param limit  Maximum number of issues to fetch in this request
     * @param cursor Pagination cursor (null for first page)
     * @return Map of query variables
     */
    private Map<String, Object> createQueryVariables(int limit, String cursor) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("owner", gitHubProperties.getRepository().getOwner());
        variables.put("name", gitHubProperties.getRepository().getName());
        variables.put("first", limit);
        variables.put("states", Arrays.asList("OPEN", "CLOSED")); // Fetch both open and closed issues
        
        // Add orderBy to sort by most recently updated
        Map<String, String> orderBy = new HashMap<>();
        orderBy.put("field", "UPDATED_AT");
        orderBy.put("direction", "DESC");
        variables.put("orderBy", orderBy);
        
        if (cursor != null && !cursor.isEmpty()) {
            variables.put("after", cursor);
        }
        
        return variables;
    }

    /**
     * Executes the GraphQL query with the given variables.
     *
     * @param query     The GraphQL query
     * @param variables The query variables
     * @return Mono of JsonNode containing the response
     */
    private Mono<JsonNode> executeGraphQLQuery(String query, Map<String, Object> variables) {
        return graphQlClient.document(query)
                .variables(variables)
                .retrieve("data")
                .toEntity(JsonNode.class)
                .doOnError(error -> log.error("GraphQL query execution failed: {}", error.getMessage(), error));
    }

    /**
     * Extracts Issue objects from the GraphQL response.
     *
     * @param response The GraphQL response as JsonNode
     * @return List of Issue objects
     */
    private List<Issue> extractIssuesFromResponse(JsonNode response) {
        List<Issue> issues = new ArrayList<>();
        
        try {
            JsonNode issueNodes = response.path("repository").path("issues").path("nodes");
            if (issueNodes.isMissingNode() || !issueNodes.isArray()) {
                log.error("Invalid response format: issues nodes not found or not an array");
                return issues;
            }
            
            for (JsonNode issueNode : issueNodes) {
                try {
                    // Convert the issue node to our Issue model
                    Issue issue = convertToIssue(issueNode);
                    issues.add(issue);
                } catch (Exception e) {
                    log.error("Error converting issue node to Issue object: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting issues from response: {}", e.getMessage(), e);
        }
        
        return issues;
    }

    /**
     * Converts a JsonNode representing a GitHub issue to an Issue object.
     *
     * @param issueNode JsonNode containing issue data
     * @return Issue object
     */
    private Issue convertToIssue(JsonNode issueNode) throws JsonProcessingException {
        // For complex nested structures, using Jackson's tree-to-value conversion
        return objectMapper.treeToValue(issueNode, Issue.class);
    }
}
