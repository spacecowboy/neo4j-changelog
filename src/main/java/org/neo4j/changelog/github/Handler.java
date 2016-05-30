package org.neo4j.changelog.github;

import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private static Pattern PAGE_PATTERN = Pattern.compile("page=([0-9]+)>");

    private final GitHubService service;

    public Handler(@Nonnull GitHubService service) {
        this.service = service;
    }

    public @Nonnull List<PullRequest> getPullRequests(@Nonnull String user, @Nonnull String repo) throws IOException {
        List<PullRequest> pullRequests = new LinkedList<>();

        OptionalInt nextPage = OptionalInt.of(1);
        while (nextPage.isPresent()) {
            Response<List<PullRequest>> response = getPullRequests(user, repo, nextPage.getAsInt());
            pullRequests.addAll(response.body());
            nextPage = getNextPage(response);
        }

        return pullRequests;
    }

    Response<List<PullRequest>> getPullRequests(@Nonnull String user, @Nonnull String repo, int page) throws IOException {
        Call<List<PullRequest>> call = service.listPullRequests(user, repo, page);

        Response<List<PullRequest>> result = call.execute();

        if (!result.isSuccessful()) {
            throw new RuntimeException(result.errorBody().string());
        }

        return result;
    }

    private
    @Nonnull
    OptionalInt getNextPage(@Nonnull Response<List<PullRequest>> result) {
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
}
