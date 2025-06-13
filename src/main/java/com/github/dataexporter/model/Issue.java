package com.github.dataexporter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a GitHub Issue.
 * Contains comprehensive fields to capture GitHub issue data from the GraphQL API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    
    /**
     * The unique identifier for the issue (GitHub node ID)
     */
    private String id;
    
    /**
     * The issue number (repository-specific)
     */
    private Integer number;
    
    /**
     * The title of the issue
     */
    private String title;
    
    /**
     * The body/description of the issue (in Markdown)
     */
    private String body;
    
    /**
     * The current state of the issue (OPEN, CLOSED)
     */
    private String state;
    
    /**
     * Whether the issue is locked
     */
    private boolean locked;
    
    /**
     * The reason the issue was locked, if it is locked
     */
    private String lockReason;
    
    /**
     * Whether the issue is a draft
     */
    private boolean isDraft;
    
    /**
     * The user who created the issue
     */
    private User author;
    
    /**
     * The repository where this issue exists
     */
    private Repository repository;
    
    /**
     * Users assigned to this issue
     */
    private List<User> assignees;
    
    /**
     * Labels attached to this issue
     */
    private List<Label> labels;
    
    /**
     * The milestone associated with this issue
     */
    private Milestone milestone;
    
    /**
     * Projects that this issue belongs to
     */
    private List<Project> projects;
    
    /**
     * Comments on this issue
     */
    private List<Comment> comments;
    
    /**
     * Total count of comments
     */
    private Integer commentCount;
    
    /**
     * Reactions to this issue
     */
    private Reactions reactions;
    
    /**
     * When the issue was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime createdAt;
    
    /**
     * When the issue was last updated
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime updatedAt;
    
    /**
     * When the issue was closed, if it is closed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime closedAt;
    
    /**
     * The user who closed the issue, if it is closed
     */
    private User closedBy;
    
    /**
     * The URL to view the issue on GitHub
     */
    private String url;
    
    /**
     * Pull requests associated with this issue
     */
    private List<PullRequestRef> linkedPullRequests;
    
    /**
     * Timeline events for this issue
     */
    private List<TimelineItem> timeline;
    
    /**
     * User subscriptions to this issue
     */
    private String subscriptionState;
    
    /**
     * Whether the viewer can subscribe to this issue
     */
    private boolean viewerCanSubscribe;
    
    /**
     * Whether the viewer can update this issue
     */
    private boolean viewerCanUpdate;
    
    /**
     * Custom metadata fields associated with this issue
     */
    private Map<String, Object> customFields;
    
    /**
     * The last time the issue was edited
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime lastEditedAt;
    
    /**
     * Whether the issue is pinned
     */
    private boolean isPinned;
    
    /**
     * Whether the issue has been marked as a duplicate
     */
    @JsonProperty("isDuplicate")
    private boolean isDuplicate;
    
    /**
     * Reference to the original issue if this is a duplicate
     */
    private IssueRef originalIssue;
    
    /**
     * Nested class representing a simplified User entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String id;
        private String login;
        private String name;
        private String avatarUrl;
        private String url;
    }
    
    /**
     * Nested class representing a simplified Repository entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String id;
        private String name;
        private String nameWithOwner;
        private String url;
    }
    
    /**
     * Nested class representing a Label entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        private String id;
        private String name;
        private String description;
        private String color;
        private boolean isDefault;
    }
    
    /**
     * Nested class representing a Milestone entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Milestone {
        private String id;
        private String title;
        private String description;
        private String state;
        private ZonedDateTime dueOn;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private String url;
    }
    
    /**
     * Nested class representing a Project entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String id;
        private String name;
        private String body;
        private String state;
        private String url;
    }
    
    /**
     * Nested class representing a Comment entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private String body;
        private User author;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private ZonedDateTime lastEditedAt;
        private String url;
        private Reactions reactions;
    }
    
    /**
     * Nested class representing Reactions on an issue or comment
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reactions {
        private int totalCount;
        private int thumbsUp;
        private int thumbsDown;
        private int laugh;
        private int hooray;
        private int confused;
        private int heart;
        private int rocket;
        private int eyes;
    }
    
    /**
     * Nested class representing a reference to a Pull Request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestRef {
        private String id;
        private Integer number;
        private String title;
        private String state;
        private String url;
    }
    
    /**
     * Nested class representing a reference to another Issue
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueRef {
        private String id;
        private Integer number;
        private String title;
        private String state;
        private String url;
    }
    
    /**
     * Nested class representing a Timeline Item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimelineItem {
        private String id;
        private String type;
        private ZonedDateTime createdAt;
        private User actor;
        private Map<String, Object> data;
    }
}
