package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.List;

public interface PullRequest {
    int getNumber();
    @Nonnull List<String> getGitHubTags();
    @Nonnull String getCommit();
    @Nonnull List<String> getVersionFilter();
    @Nonnull List<String> getLabelFilter();
    @Nonnull String getChangeText();
}
