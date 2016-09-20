package org.neo4j.changelog.github;

import org.neo4j.changelog.Change;
import org.neo4j.changelog.Util;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Miscellaneous utility functions related to GitHub specific things.
 */
public class GitHubHelper {
    private static Pattern PAGE_PATTERN = Pattern.compile("page=([0-9]+)>");

    private final GitHubService service;
    private final String user;
    private final String repo;
    private final String requiredLabel;
    private final String versionPrefix;

    public GitHubHelper(@Nonnull String token, @Nonnull String user, @Nonnull String repo,
                        @Nonnull String requiredLabel, @Nonnull String versionPrefix) {
        service = GitHubService.GetService(token);
        this.user = user;
        this.repo = repo;
        this.requiredLabel = requiredLabel;
        this.versionPrefix = versionPrefix;

        if (!versionPrefix.isEmpty() && !Util.isSemanticVersion(versionPrefix)) {
            throw new IllegalArgumentException("versionprefix is not a semantic version: '" + versionPrefix + "'");
        }
    }

    @Nonnull
    public List<PullRequest> getChangeLogPullRequests() {
        List<GitHubService.Issue> issues = listChangeLogIssues();
        System.out.println("Fetched " + issues.size() + " issues");

        return issues.parallelStream()
                     // Only consider pull requests, not issues
                     .filter(i -> i.pull_request != null)
                     .map(issue -> {
                         GitHubService.PR pr = getPr(issue.number);
                         return new PRIssue(issue, pr);
                     })
                     .filter(pr -> isIncluded(pr, versionPrefix))
                     .collect(Collectors.toList());
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
        List<GitHubService.Issue> issues = new LinkedList<>();

        OptionalInt nextPage = OptionalInt.of(1);
        while (nextPage.isPresent()) {
            //noinspection OptionalGetWithoutIsPresent (while statement is not picked up by intellij)
            Response<List<GitHubService.Issue>> response = listChangeLogIssues(nextPage.getAsInt());
            issues.addAll(response.body());
            nextPage = getNextPage(response);
        }

        return issues;
    }

    @Nonnull
    private Response<List<GitHubService.Issue>> listChangeLogIssues(int page) {
        try {
            Call<List<GitHubService.Issue>> call = service.listChangeLogIssues(user, repo, requiredLabel, page);
            Response<List<GitHubService.Issue>> response = call.execute();
            if (response.isSuccessful()) {
                return response;
            }
            throw new RuntimeException(response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    private OptionalInt getNextPage(@Nonnull Response result) {
        if (result.headers().get("Link") != null) {
            String link = result.headers().get("Link");
            String parsedPage = null;
            for (String part : link.split(",")) {
                for (String piece : part.split(";")) {
                    if ("rel=\"next\"".equals(piece.trim()) && parsedPage != null) {
                        // Previous piece pointed to next
                        return OptionalInt.of(Integer.parseInt(parsedPage));
                    } else if (piece.contains("&page=")) {
                        Matcher match = PAGE_PATTERN.matcher(piece);
                        if (match.find()) {
                            parsedPage = match.group(1);
                        }
                    }
                }
            }
        }
        return OptionalInt.empty();
    }

    public static boolean isChangeLogWorthy(@Nonnull PullRequest pr, @Nonnull String requiredLabel) {
        if (requiredLabel.isEmpty()) {
            return true;
        }
        return isChangeLogWorthy(pr, Arrays.asList(requiredLabel));
    }

    public static <T> boolean isChangeLogWorthy(@Nonnull PullRequest pr, @Nonnull List<String> requiredLabels) {
        return pr.getGitHubTags().stream().anyMatch(requiredLabels::contains);
    }

    public static boolean isIncluded(@Nonnull PullRequest pr, @Nonnull String changelogVersion) {
        // Special case if no filter, then always true
        if (pr.getVersionFilter().isEmpty() || changelogVersion.isEmpty()) {
            return true;
        }

        for (String version: pr.getVersionFilter()) {
            if (changelogVersion.startsWith(version)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static Change convertToChange(@Nonnull PullRequest pr, @Nonnull String version) {
        return new Change() {
            @Override
            public int getSortingNumber() {
                return pr.getNumber();
            }

            @Nonnull
            @Override
            public List<String> getLabels() {
                return pr.getLabelFilter();
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

    public GitHubService getService() {
        return service;
    }
}
