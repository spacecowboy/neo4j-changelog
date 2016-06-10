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
        assertFalse(GitHubHelper.isChangeLogWorthy(new PullRequest() {
            @Nonnull
            @Override
            public String getBaseBranch() {
                return "1.0";
            }

            @Nonnull
            @Override
            public List<String> getGitHubTags() {
                return new ArrayList<>();
            }
        }));
    }

    @Test
    public void taggedPRIsWorthy() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(new PullRequest() {
            @Nonnull
            @Override
            public String getBaseBranch() {
                return "1.0";
            }

            @Nonnull
            @Override
            public List<String> getGitHubTags() {
                return Arrays.asList("changelog", "3.0", "bug");
            }
        }));
    }

    @Test
    public void taggedPRIsWorthyWithCustomTags() throws Exception {
        assertTrue(GitHubHelper.isChangeLogWorthy(new PullRequest() {
            @Nonnull
            @Override
            public String getBaseBranch() {
                return "1.0";
            }

            @Nonnull
            @Override
            public List<String> getGitHubTags() {
                return Arrays.asList("bah", "hoo");
            }
        }, Arrays.asList("meh", "hoo")));
    }
}
