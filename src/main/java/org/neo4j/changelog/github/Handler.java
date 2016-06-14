package org.neo4j.changelog.github;

import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler {
    private static Pattern PAGE_PATTERN = Pattern.compile("page=([0-9]+)>");

    private final GitHubService service;

    public Handler(@Nonnull GitHubService service) {
        this.service = service;
    }

    public @Nonnull List<GitHubService.PR> getPullRequests(@Nonnull String user, @Nonnull String repo) throws IOException {
        List<GitHubService.PR> PRs = new LinkedList<>();

        OptionalInt nextPage = OptionalInt.of(1);
        while (nextPage.isPresent()) {
            Response<List<GitHubService.PR>> response = getPullRequests(user, repo, nextPage.getAsInt());
            PRs.addAll(response.body());
            nextPage = getNextPage(response);
        }

        return PRs;
    }

    Response<List<GitHubService.PR>> getPullRequests(@Nonnull String user, @Nonnull String repo, int page) throws IOException {
        Call<List<GitHubService.PR>> call = service.listPRs(user, repo, page);

        Response<List<GitHubService.PR>> result = call.execute();

        if (!result.isSuccessful()) {
            throw new RuntimeException(result.errorBody().string());
        }

        return result;
    }

    @Nonnull
    private
    OptionalInt getNextPage(@Nonnull Response<List<GitHubService.PR>> result) {
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
