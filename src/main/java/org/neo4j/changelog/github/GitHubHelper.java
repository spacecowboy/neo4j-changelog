package org.neo4j.changelog.github;

import org.eclipse.jgit.lib.Ref;
import org.neo4j.changelog.Change;

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

    public static boolean isIncluded(@Nonnull PullRequest pr, @Nonnull String nextVersion) {
        // Special case if no filter, then always true
        if (pr.getVersionFilter().isEmpty()) {
            return true;
        }

        for (String version: pr.getVersionFilter()) {
            if (nextVersion.startsWith(version)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static Change convertToChange(@Nonnull PullRequest pr, @Nonnull String version) {
        return new Change() {
            @Nonnull
            @Override
            public List<String> getLabels() {
                return pr.getGitHubTags();
            }

            @Nonnull
            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public String toString() {
                // TODO check for CL message, otherwise do title
                return "TITLE GOES HERE!";
            }
        };
    }
}
