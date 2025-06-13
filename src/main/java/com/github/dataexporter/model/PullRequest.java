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
 * Model class representing a GitHub Pull Request.
 * Contains comprehensive fields to capture GitHub pull request data from the GraphQL API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {
    
    /**
     * The unique identifier for the pull request (GitHub node ID)
     */
    private String id;
    
    /**
     * The pull request number (repository-specific)
     */
    private Integer number;
    
    /**
     * The title of the pull request
     */
    private String title;
    
    /**
     * The body/description of the pull request (in Markdown)
     */
    private String body;
    
    /**
     * The current state of the pull request (OPEN, CLOSED, MERGED)
     */
    private String state;
    
    /**
     * Whether the pull request is locked
     */
    private boolean locked;
    
    /**
     * The reason the pull request was locked, if it is locked
     */
    private String lockReason;
    
    /**
     * Whether the pull request is a draft
     */
    private boolean isDraft;
    
    /**
     * Whether the pull request has been merged
     */
    private boolean merged;
    
    /**
     * Whether the pull request can be merged (no conflicts)
     */
    private boolean mergeable;
    
    /**
     * The merge state (MERGEABLE, CONFLICTING, UNKNOWN)
     */
    private String mergeableState;
    
    /**
     * The user who created the pull request
     */
    private User author;
    
    /**
     * The repository where this pull request exists
     */
    private Repository repository;
    
    /**
     * Users assigned to this pull request
     */
    private List<User> assignees;
    
    /**
     * Users requested to review this pull request
     */
    private List<User> requestedReviewers;
    
    /**
     * Teams requested to review this pull request
     */
    private List<Team> requestedTeams;
    
    /**
     * Labels attached to this pull request
     */
    private List<Label> labels;
    
    /**
     * The milestone associated with this pull request
     */
    private Milestone milestone;
    
    /**
     * Projects that this pull request belongs to
     */
    private List<Project> projects;
    
    /**
     * Base branch information (target of the pull request)
     */
    private BranchRef baseRef;
    
    /**
     * Head branch information (source of the pull request)
     */
    private BranchRef headRef;
    
    /**
     * Comments on this pull request
     */
    private List<Comment> comments;
    
    /**
     * Total count of comments
     */
    private Integer commentCount;
    
    /**
     * Review comments on this pull request (comments on specific lines of code)
     */
    private List<ReviewComment> reviewComments;
    
    /**
     * Total count of review comments
     */
    private Integer reviewCommentCount;
    
    /**
     * Reviews on this pull request
     */
    private List<Review> reviews;
    
    /**
     * Total count of reviews
     */
    private Integer reviewCount;
    
    /**
     * Commits in this pull request
     */
    private List<Commit> commits;
    
    /**
     * Total count of commits
     */
    private Integer commitCount;
    
    /**
     * Files changed in this pull request
     */
    private List<ChangedFile> files;
    
    /**
     * Total count of files changed
     */
    private Integer changedFileCount;
    
    /**
     * Total number of additions in this pull request
     */
    private Integer additions;
    
    /**
     * Total number of deletions in this pull request
     */
    private Integer deletions;
    
    /**
     * Reactions to this pull request
     */
    private Reactions reactions;
    
    /**
     * Check runs associated with the latest commit on this pull request
     */
    private List<CheckRun> checkRuns;
    
    /**
     * Status checks associated with the latest commit on this pull request
     */
    private List<StatusCheck> statusChecks;
    
    /**
     * Auto-merge settings, if enabled
     */
    private AutoMerge autoMerge;
    
    /**
     * Whether the pull request can be rebased
     */
    private boolean canBeRebased;
    
    /**
     * Whether the pull request can be automatically merged by GitHub
     */
    private boolean canBeAutomaticallyMerged;
    
    /**
     * The method used to merge the pull request, if it was merged
     */
    private String mergeMethod;
    
    /**
     * The commit SHA of the merge commit, if the pull request was merged
     */
    private String mergeCommitSha;
    
    /**
     * Whether the pull request is in a clean state (no conflicts, all checks passing)
     */
    private boolean isClean;
    
    /**
     * When the pull request was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime createdAt;
    
    /**
     * When the pull request was last updated
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime updatedAt;
    
    /**
     * When the pull request was closed, if it is closed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime closedAt;
    
    /**
     * When the pull request was merged, if it is merged
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime mergedAt;
    
    /**
     * The user who closed the pull request, if it is closed
     */
    private User closedBy;
    
    /**
     * The user who merged the pull request, if it is merged
     */
    private User mergedBy;
    
    /**
     * The URL to view the pull request on GitHub
     */
    private String url;
    
    /**
     * Issues linked to this pull request
     */
    private List<IssueRef> linkedIssues;
    
    /**
     * Timeline events for this pull request
     */
    private List<TimelineItem> timeline;
    
    /**
     * User subscriptions to this pull request
     */
    private String subscriptionState;
    
    /**
     * Whether the viewer can subscribe to this pull request
     */
    private boolean viewerCanSubscribe;
    
    /**
     * Whether the viewer can update this pull request
     */
    private boolean viewerCanUpdate;
    
    /**
     * Whether the pull request is a suggested change
     */
    private boolean isSuggestedChange;
    
    /**
     * Whether maintainer modifications are allowed
     */
    private boolean maintainerCanModify;
    
    /**
     * The last time the pull request was edited
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private ZonedDateTime lastEditedAt;
    
    /**
     * Whether the pull request is pinned
     */
    private boolean isPinned;
    
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
     * Nested class representing a Team entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        private String id;
        private String name;
        private String slug;
        private String description;
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
     * Nested class representing a Branch Reference
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchRef {
        private String id;
        private String name;
        private String prefix;
        private Repository repository;
        private Commit target;
    }
    
    /**
     * Nested class representing a Commit entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id;
        private String oid;
        private String message;
        private User author;
        private User committer;
        private ZonedDateTime authoredDate;
        private ZonedDateTime committedDate;
        private String url;
        private List<CheckRun> checkRuns;
        private List<StatusCheck> statusChecks;
    }
    
    /**
     * Nested class representing a Changed File
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChangedFile {
        private String path;
        private Integer additions;
        private Integer deletions;
        private Integer changes;
        private String status; // ADDED, MODIFIED, REMOVED, RENAMED, COPIED, CHANGED, UNCHANGED
        private String previousPath; // For renamed files
        private String patch; // Unified diff
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
     * Nested class representing a Review Comment entity (comment on a specific line of code)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewComment {
        private String id;
        private String body;
        private User author;
        private String path;
        private Integer position;
        private Integer originalPosition;
        private String commitId;
        private String originalCommitId;
        private String diffHunk;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private ZonedDateTime lastEditedAt;
        private String url;
        private Reactions reactions;
    }
    
    /**
     * Nested class representing a Review entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Review {
        private String id;
        private String body;
        private User author;
        private String state; // PENDING, COMMENTED, APPROVED, CHANGES_REQUESTED, DISMISSED
        private String commitId;
        private List<ReviewComment> comments;
        private ZonedDateTime submittedAt;
        private String url;
    }
    
    /**
     * Nested class representing a Check Run entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckRun {
        private String id;
        private String name;
        private String status; // QUEUED, IN_PROGRESS, COMPLETED
        private String conclusion; // SUCCESS, FAILURE, NEUTRAL, CANCELLED, TIMED_OUT, ACTION_REQUIRED, STALE
        private String detailsUrl;
        private String externalId;
        private ZonedDateTime startedAt;
        private ZonedDateTime completedAt;
    }
    
    /**
     * Nested class representing a Status Check entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCheck {
        private String id;
        private String context;
        private String state; // EXPECTED, ERROR, FAILURE, PENDING, SUCCESS
        private String description;
        private String targetUrl;
        private User creator;
        private ZonedDateTime createdAt;
    }
    
    /**
     * Nested class representing Auto-Merge settings
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AutoMerge {
        private boolean enabled;
        private String mergeMethod; // MERGE, SQUASH, REBASE
        private User enabledBy;
        private ZonedDateTime enabledAt;
    }
    
    /**
     * Nested class representing Reactions on a pull request or comment
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
     * Nested class representing a reference to an Issue
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
