package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PRIssue implements PullRequest {

    public static final Pattern CHANGELOG_PATTERN = Pattern.compile("^(cl|changelog).*$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    public static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*\\d+\\.\\d+\\s*$", Pattern.CASE_INSENSITIVE);

    final int number;
    final String title;
    final String body;
    final String html_url;
    final String merged_at;
    final String head;
    final String base;
    final List<String> labels;
    private ArrayList<String> versionFilter = null;

    public PRIssue(int number, String title, String body, String html_url, String merged_at,
                   String head, String base, List<String> labels) {

        this.number = number;
        this.title = title;
        this.body = body;
        this.html_url = html_url;
        this.merged_at = merged_at;
        this.head = head;
        this.base = base;
        this.labels = labels;
    }

    public PRIssue(GitHubService.Issue issue, GitHubService.PR pr) {
        this(pr.number, pr.title, pr.body, pr.html_url, pr.merged_at, pr.head.sha, pr.base.sha,
                issue.labels.stream().map(l -> l.name).collect(Collectors.toList()));
    }

    @Nonnull
    @Override
    public List<String> getGitHubTags() {
        if (labels == null) {
            return new ArrayList<>();
        }
        return labels;
    }

    @Nonnull
    @Override
    public String getCommit() {
        return head;
    }

    @Nonnull
    @Override
    public List<String> getVersionFilter() {
        if (versionFilter != null) {
            return versionFilter;
        }
        versionFilter = new ArrayList<>();

        Matcher matcher = CHANGELOG_PATTERN.matcher(body);
        if (matcher.find()) {
            String cl = matcher.group();
            String[] parts = cl.split(":");

            if (parts.length < 2) {
                return versionFilter;
            }

            String[] versions = parts[1].split(",");
            if (containsVersions(versions)) {
                for (String version: versions) {
                    versionFilter.add(version.trim());
                }
            }
        }
        return versionFilter;
    }

    private boolean containsVersions(@Nonnull String[] versions) {
        return Arrays.stream(versions).allMatch(VERSION_PATTERN.asPredicate());
    }

    @Nonnull
    @Override
    public String getChangeText() {
        Matcher matcher = CHANGELOG_PATTERN.matcher(body);
        if (matcher.find()) {
            String cl = matcher.group();
            String[] parts = cl.split(":");

            if (parts.length < 2) {
                return addLink(title);
            }

            List<String> versions = getVersionFilter();

            if (parts.length == 2 && !versions.isEmpty()) {
                return addLink(title);
            }

            final int skipCount = versions.isEmpty() ? 1 : 2;

            // Recombine possible split again
            String text = parts[skipCount];
            for (int i = skipCount + 1; i < parts.length; i++) {
                text = String.join(":", text, parts[i]);
            }

            return addLink(text.isEmpty() ? title : text);
        }

        return addLink(title);
    }

    String addLink(@Nonnull String text) {
        return String.format("%s [#%d](%s)", text.trim(), number, html_url);
    }
}
