package org.neo4j.changelog.github;

import javax.annotation.Nonnull;

/**
 * Combination of Issue and PR
 */
public class Change {
    public Issue issue;
    public PullRequest pullRequest;

    public Change(@Nonnull Issue issue, @Nonnull PullRequest pullRequest) {
        this.issue = issue;
        this.pullRequest = pullRequest;
    }
}
