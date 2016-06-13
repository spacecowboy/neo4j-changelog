package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PRIssue implements PullRequest {

    public static final Pattern CHANGELOG_PATTERN = Pattern.compile("^(cl|changelog).*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    public static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*\\d+\\.\\d+\\s*$", Pattern.CASE_INSENSITIVE);

    public int number;
    public String title;
    public String body;
    public String html_url;
    public String merged_at;
    public Ref head;
    public Ref base;
    public List<Label> labels;
    private ArrayList<String> versionFilter = null;

    public boolean isMerged() {
        return merged_at != null;
    }

    @Nonnull
    @Override
    public List<String> getGitHubTags() {
        if (labels == null) {
            return new ArrayList<>();
        }
        return labels.stream().map(l -> l.name).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getCommit() {
        return head.sha;
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
                return title.trim();
            }

            List<String> versions = getVersionFilter();

            if (parts.length == 2 && !versions.isEmpty()) {
                return title.trim();
            }

            final int skipCount = versions.isEmpty() ? 1 : 2;

            // Recombine possible split again
            String text = parts[skipCount];
            for (int i = skipCount + 1; i < parts.length; i++) {
                text = String.join(":", text, parts[i]);
            }

            return text.isEmpty() ? title.trim() : text.trim();
        }

        return title.trim();
    }

    public static class Label {
        public String name;
    }

    public static class Ref {
        public String ref;
        public String sha;
    }
}
