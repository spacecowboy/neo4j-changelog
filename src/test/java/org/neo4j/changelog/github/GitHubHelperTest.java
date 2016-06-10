package org.neo4j.changelog.github;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class GitHubHelperTest {

    private PullRequest DummyPullRequest(String val, List<String> tags) {
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
        };
    }

    @Test
    public void untaggedPRIsNotWorthy() throws Exception {
        assertFalse(GitHubHelper.isChangeLogWorthy(DummyPullRequest("sha", new ArrayList<>())));
    }

    @Test
    public void taggedPRIsWorthy() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(DummyPullRequest("sha", Arrays.asList("changelog", "3.0", "bug"))));
    }

    @Test
    public void taggedPRIsWorthyWithCustomTags() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(DummyPullRequest("sha", Arrays.asList("bah", "hoo")),
                Arrays.asList("meh", "hoo")));
    }
}
