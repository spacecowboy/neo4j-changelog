package org.neo4j.changelog.github;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Purpose is to return changelog worthy changes, and what commit they are connected to, as well as any branch filtering.
 */
public class Main {

    final Handler handler;

    public Main(@Nonnull Handler handler) {
        this.handler = handler;
    }

    public @Nonnull List<Change> getChanges(@Nonnull String user, @Nonnull String repo) throws IOException {
        //List<Change> changes = new ArrayList<>();

        List<Issue> issues = handler.getIssues(user, repo);

        HashMap<Integer, Change> changes = new HashMap<>();
        for (Issue issue: issues) {
            change = changes.getOrDefault(issue.number, new Change(issue))
        }

        Map<Integer, Issue> issues = listToMap();
        //List<Issue> issues = handler.getIssues(user, repo);
        //List<PullRequest> pullRequests = handler.getPullRequests(user, repo);

        // All PRs are issues, but not all issues are PRs
        // They should also be sorted in the same way
        //issues.spliterator()

        return changes;
    }

    private Map<Integer, Issue> listToMap(List<Issue> issues, Consumer<Issue>) {
        return null;
    }


}
