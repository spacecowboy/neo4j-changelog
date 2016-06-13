package org.neo4j.changelog.github;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class GitHubHelperTest {

    @Test
    public void untaggedPRIsNotWorthy() throws Exception {
        assertFalse(GitHubHelper.isChangeLogWorthy(PullRequest()));
    }

    @Test
    public void taggedPRIsWorthy() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(PullRequest("sha", Arrays.asList("changelog", "3.0", "bug"))));
    }

    @Test
    public void taggedPRIsWorthyWithCustomTags() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(PullRequest("sha", Arrays.asList("bah", "hoo")),
                Arrays.asList("meh", "hoo")));
    }

    @Test
    public void prWithNoFilterIsAlwaysIncluded() throws Exception {
        PullRequest pr = PullRequest();

        assertTrue(GitHubHelper.isIncluded(pr, "1.2.3"));
        assertTrue(GitHubHelper.isIncluded(pr, "2.3.4"));
        assertTrue(GitHubHelper.isIncluded(pr, "3.4.5"));
        assertTrue(GitHubHelper.isIncluded(pr, "4.5.6"));
    }

    @Test
    public void prWithFilterIsIncluded() throws Exception {
        PullRequest pr = FilteredPullRequest("1.0", "1.1", "2", "3.4");

        assertTrue(GitHubHelper.isIncluded(pr, "1.0"));
        assertTrue(GitHubHelper.isIncluded(pr, "1.1"));
        assertTrue(GitHubHelper.isIncluded(pr, "2.2"));
        assertTrue(GitHubHelper.isIncluded(pr, "2.3"));
        assertTrue(GitHubHelper.isIncluded(pr, "3.4"));

        assertFalse(GitHubHelper.isIncluded(pr, "3.5"));
        assertFalse(GitHubHelper.isIncluded(pr, "3.6"));
        assertFalse(GitHubHelper.isIncluded(pr, "4.0"));
    }

    private PullRequest FilteredPullRequest(String... versions) {
        return new PullRequest() {
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
            public String getChangeText() {
                return "bah";
            }
        };
    }
}
