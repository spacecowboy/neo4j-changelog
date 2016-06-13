package org.neo4j.changelog.github;

import org.neo4j.changelog.Change;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Miscellaneous utility functions related to GitHub specific things.
 */
public class GitHubHelper {

    private final GitHubService service;
    private final String user;
    private final String repo;

    public GitHubHelper(@Nonnull String token) {
        service = GitHubService.GetService(token);
        user = "neo4j";
        repo = "neo4j";
    }

    @Nonnull
    public List<PullRequest> getChangeLogPullRequests() {
        List<GitHubService.Issue> issues = listChangeLogIssues();

        return issues.parallelStream()
                     .filter(i -> i.pull_request != null)
                     .map(issue -> {
            GitHubService.PR pr = getPr(issue.number);
            return new PRIssue(issue, pr);
        }).collect(Collectors.toList());
    }

    @Nonnull
    private GitHubService.PR getPr(int number) {
        try {
            Call<GitHubService.PR> call = service.getPR(user, repo, number);
            Response<GitHubService.PR> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            throw new RuntimeException(response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private List<GitHubService.Issue> listChangeLogIssues() {
        // TODO handle pagination
        try {
            Call<List<GitHubService.Issue>> call = service.listChangeLogIssues(user, repo, 1);
            Response<List<GitHubService.Issue>> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            throw new RuntimeException(response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                return pr.getChangeText();
            }
        };
    }
}
