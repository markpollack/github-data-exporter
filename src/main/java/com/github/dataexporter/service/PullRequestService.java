package com.github.dataexporter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dataexporter.config.ExportProperties;
import com.github.dataexporter.config.GitHubProperties;
import com.github.dataexporter.model.PullRequest;
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
 * Service for fetching GitHub pull requests using the GraphQL API.
 * Handles pagination and mapping of API responses to domain objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PullRequestService {

    private final HttpGraphQlClient graphQlClient;
    private final GitHubProperties gitHubProperties;
    private final ExportProperties exportProperties;
    private final ObjectMapper objectMapper;

    private static final String PULL_REQUEST_QUERY_FILE = "graphql/pull-request-query.graphql";
    private static final String PULL_REQUEST_STATES_ALL = "[OPEN, CLOSED, MERGED]";

    /**
     * Fetches all pull requests from the configured GitHub repository.
     * Uses pagination to retrieve all pull requests up to the configured maximum.
     *
     * @return List of PullRequest objects representing GitHub pull requests
     */
    public List<PullRequest> fetchAllPullRequests() {
        log.info("Fetching pull requests for repository {}/{}",
                gitHubProperties.getRepository().getOwner(),
                gitHubProperties.getRepository().getName());

        String query = loadQueryFromFile();
        List<PullRequest> allPullRequests = new ArrayList<>();
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

                List<PullRequest> pullRequests = extractPullRequestsFromResponse(response);
                if (pullRequests.isEmpty()) {
                    break;
                }
                
                allPullRequests.addAll(pullRequests);
                totalFetched.addAndGet(pullRequests.size());
                
                JsonNode pageInfo = response.path("data").path("repository").path("pullRequests").path("pageInfo");
                hasNextPage = pageInfo.path("hasNextPage").asBoolean(false);
                cursor = pageInfo.path("endCursor").asText(null);
                
                log.info("Fetched {} pull requests, total: {}, hasNextPage: {}", 
                        pullRequests.size(), totalFetched.get(), hasNextPage);
                
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
                log.error("Error fetching pull requests at cursor {}: {}", cursor, e.getMessage(), e);
                break;
            }
        }

        log.info("Completed fetching pull requests. Total pull requests fetched: {}", allPullRequests.size());
        return allPullRequests;
    }

    /**
     * Loads the GraphQL query from a file on the classpath.
     *
     * @return The query string
     */
    private String loadQueryFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource(PULL_REQUEST_QUERY_FILE);
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load GraphQL query from file: {}", PULL_REQUEST_QUERY_FILE, e);
            throw new RuntimeException("Failed to load GraphQL query", e);
        }
    }

    /**
     * Creates the variables map for the GraphQL query.
     *
     * @param limit  Maximum number of pull requests to fetch in this request
     * @param cursor Pagination cursor (null for first page)
     * @return Map of query variables
     */
    private Map<String, Object> createQueryVariables(int limit, String cursor) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("owner", gitHubProperties.getRepository().getOwner());
        variables.put("name", gitHubProperties.getRepository().getName());
        variables.put("first", limit);
        variables.put("states", Arrays.asList("OPEN", "CLOSED", "MERGED")); // Fetch all pull request states
        
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
     * Extracts PullRequest objects from the GraphQL response.
     *
     * @param response The GraphQL response as JsonNode
     * @return List of PullRequest objects
     */
    private List<PullRequest> extractPullRequestsFromResponse(JsonNode response) {
        List<PullRequest> pullRequests = new ArrayList<>();
        
        try {
            JsonNode prNodes = response.path("repository").path("pullRequests").path("nodes");
            if (prNodes.isMissingNode() || !prNodes.isArray()) {
                log.error("Invalid response format: pull request nodes not found or not an array");
                return pullRequests;
            }
            
            for (JsonNode prNode : prNodes) {
                try {
                    // Convert the pull request node to our PullRequest model
                    PullRequest pullRequest = convertToPullRequest(prNode);
                    pullRequests.add(pullRequest);
                } catch (Exception e) {
                    log.error("Error converting pull request node to PullRequest object: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting pull requests from response: {}", e.getMessage(), e);
        }
        
        return pullRequests;
    }

    /**
     * Converts a JsonNode representing a GitHub pull request to a PullRequest object.
     *
     * @param prNode JsonNode containing pull request data
     * @return PullRequest object
     */
    private PullRequest convertToPullRequest(JsonNode prNode) throws JsonProcessingException {
        // For complex nested structures, using Jackson's tree-to-value conversion
        return objectMapper.treeToValue(prNode, PullRequest.class);
    }
}
