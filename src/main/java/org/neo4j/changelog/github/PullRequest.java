package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.List;

public interface PullRequest {
    @Nonnull List<String> getGitHubTags();
    @Nonnull String getCommit();
    @Nonnull List<String> getVersionFilter();
    @Nonnull String getChangeText();
}
