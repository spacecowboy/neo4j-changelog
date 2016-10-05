package org.neo4j.changelog.github;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class GitHubHelperTest {

    @Test
    public void prWithNoFilterIsAlwaysIncluded() throws Exception {
        PullRequest pr = PullRequest();

        assertTrue(GitHubHelper.isIncludedInVersion(pr, "1.2.3"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "2.3.4"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "3.4.5"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "4.5.6"));
    }

    @Test
    public void prWithFilterIsIncluded() throws Exception {
        PullRequest pr = FilteredPullRequest("1.0", "1.1", "2", "3.4");

        assertTrue(GitHubHelper.isIncludedInVersion(pr, "1.0"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "1.1"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "2.2"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "2.3"));
        assertTrue(GitHubHelper.isIncludedInVersion(pr, "3.4"));

        assertFalse(GitHubHelper.isIncludedInVersion(pr, "3.5"));
        assertFalse(GitHubHelper.isIncludedInVersion(pr, "3.6"));
        assertFalse(GitHubHelper.isIncludedInVersion(pr, "4.0"));
    }

    private PullRequest FilteredPullRequest(String... versions) {
        return new PullRequest() {
            @Override
            public int getNumber() {
                return 0;
            }

            @Nonnull
            @Override
            public List<String> getGitHubTags() {
                return new ArrayList<>();
            }

            @Nonnull
            @Override
            public String getCommit() {
                return "sha";
            }

            @Nonnull
            @Override
            public List<String> getVersionFilter() {
                return Arrays.asList(versions);
            }

            @Nonnull
            @Override
            public List<String> getLabelFilter() {
                return new ArrayList<>();
            }

            @Nonnull
            @Override
            public String getChangeText() {
                return "bah";
            }
        };
    }

    private PullRequest PullRequest() {
        return PullRequest("sha", new ArrayList<>());
    }

    private PullRequest PullRequest(String val, List<String> tags) {
        return new PullRequest() {
            @Override
            public int getNumber() {
                return 0;
            }

            @Nonnull
            @Override
            public List<String> getGitHubTags() {
                return tags;
            }

            @Nonnull
            @Override
            public String getCommit() {
                return val;
            }

            @Nonnull
            @Override
            public List<String> getVersionFilter() {
                return new ArrayList<>();
            }

            @Nonnull
            @Override
            public List<String> getLabelFilter() {
                return new ArrayList<>();
            }

            @Nonnull
            @Override
            public String getChangeText() {
                return "bah";
            }
        };
    }
}
