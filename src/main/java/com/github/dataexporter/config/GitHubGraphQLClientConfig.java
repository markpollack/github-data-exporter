package com.github.dataexporter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Configuration class for setting up the GitHub GraphQL client.
 * Configures authentication and connection parameters for interacting with GitHub's GraphQL API.
 */
@Configuration
@Slf4j
public class GitHubGraphQLClientConfig {

    @Value("${github.api.token}")
    private String githubToken;

    @Value("${spring.graphql.client.url}")
    private String graphqlUrl;

    @Value("${spring.graphql.client.timeout:30s}")
    private Duration timeout;

    /**
     * Creates a WebClient configured for GitHub GraphQL API access.
     * Includes authentication via personal access token and appropriate headers.
     *
     * @return WebClient instance configured for GitHub API
     */
    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
                .baseUrl(graphqlUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("User-Agent", "GitHub-Data-Exporter")
                .filter(logRequest())
                .build();
    }

    /**
     * Creates an HttpGraphQlClient using the configured WebClient.
     * This client provides GraphQL-specific functionality on top of the WebClient.
     *
     * @param webClient The preconfigured WebClient for GitHub
     * @return HttpGraphQlClient for executing GraphQL operations
     */
    @Bean
    public HttpGraphQlClient githubGraphQlClient(WebClient githubWebClient) {
        return HttpGraphQlClient.builder(githubWebClient)
                .timeout(timeout)
                .build();
    }

    /**
     * Creates a filter function to log outgoing requests for debugging purposes.
     * Only logs at debug level to avoid exposing sensitive information in normal operation.
     *
     * @return ExchangeFilterFunction for logging requests
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                        values.forEach(value -> log.debug("{}={}", name, value));
                    } else {
                        log.debug("{}=<masked>", name);
                    }
                });
            }
            return Mono.just(clientRequest);
        });
    }
}
