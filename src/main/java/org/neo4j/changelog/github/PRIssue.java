package org.neo4j.changelog.github;


import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PRIssue implements PullRequest {

    public static final Pattern CHANGELOG_PATTERN = Pattern.compile("^(cl|changelog)\\b[\\s:]*(.*)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    public static final Pattern METADATA_PATTERN = Pattern.compile("^\\[(.*?)\\]");
    public static final Pattern VERSION_PATTERN = Pattern.compile("^\\s*\\d+\\.\\d+\\s*$");
    public static final Pattern MESSAGE_PATTERN = Pattern.compile("^(?:\\[.*?\\])?\\s*(.*?)\\s*$", Pattern.DOTALL);

    final int number;
    final String title;
    final String body;
    final String html_url;
    final String merged_at;
    final String head;
    final String base;
    final List<String> labels;
    private ArrayList<String> versionFilter = null;
    private ArrayList<String> labelFilter = null;
    private String changeText = null;

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

    public PRIssue(@Nonnull GitHubService.Issue issue, @Nonnull GitHubService.PR pr,
                   @Nonnull Map<String, String> categoryMap) {
        this(pr.number, pr.title, pr.body, pr.html_url, pr.merged_at, pr.head.sha, pr.base.sha,
                issue.labels.stream()
                            .map(l -> l.name)
                            .map(l -> categoryMap.getOrDefault(l, l))
                            .collect(Collectors.toList()));
    }

    @Override
    public int getNumber() {
        return number;
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
        if (versionFilter == null) {
            parseMetaData();
        }
        return versionFilter;
    }

    private void parseMetaData() {
        versionFilter = new ArrayList<>();
        labelFilter = new ArrayList<>();
        changeText = addLink(title);

        Matcher matcher = CHANGELOG_PATTERN.matcher(body);
        if (matcher.find()) {

            String rest = matcher.group(2);

            // Look for custom message
            Matcher msgMatch = MESSAGE_PATTERN.matcher(rest);

            if (msgMatch.find()) {
                String msg = msgMatch.group(1);
                if (!msg.trim().isEmpty()) {
                    // In case of multiple lines, only use first one
                    changeText = addLink(msg.split("\n")[0]);
                }
            }

            // Look for meta data
            Matcher metaMatch = METADATA_PATTERN.matcher(rest);
            if (metaMatch.find()) {
                String meta = metaMatch.group(1);
                String[] metaParts = meta.split(",");

                for (String metaPart : metaParts) {
                    if (isVersion(metaPart)) {
                        versionFilter.add(metaPart.trim());
                    } else if (!metaPart.trim().isEmpty()) {
                        labelFilter.add(metaPart.trim());
                    }
                }
            }
        }

        if (labelFilter.isEmpty()) {
            labelFilter.addAll(getGitHubTags());
        }
    }

    private boolean isVersion(@Nonnull String meta) {
        return VERSION_PATTERN.asPredicate().test(meta);
    }

    @Nonnull
    @Override
    public String getChangeText() {
        if (changeText == null) {
            parseMetaData();
        }
        return changeText;
    }

    String addLink(@Nonnull String text) {
        return String.format("%s [#%d](%s)", text.trim(), number, html_url);
    }

    @Nonnull
    @Override
    public ArrayList<String> getLabelFilter() {
        if (labelFilter == null) {
            parseMetaData();
        }
        return labelFilter;
    }
}
