package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.List;

public interface PullRequest {
    @Nonnull String getBaseBranch();
    @Nonnull List<String> getGitHubTags();
}
