package org.neo4j.changelog.github;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Miscellaneous utility functions related to GitHub specific things.
 */
public class GitHubHelper {
    public static boolean isChangeLogWorthy(@Nonnull PullRequest pr) {
        return isChangeLogWorthy(pr, Arrays.asList("changelog"));
    }

    public static <T> boolean isChangeLogWorthy(@Nonnull PullRequest pr, @Nonnull List<String> tagStrings) {
        return pr.getGitHubTags().stream().anyMatch(tagStrings::contains);
    }
}
