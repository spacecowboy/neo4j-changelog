package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GithubConfig {

    public static final String USER = "user";
    public static final String REPO = "repo";
    public static final String TOKEN = "token";
    public static final String LABELS = "labels";
    private static final String INCLUDE_AUTHOR = "include_author";
    private static final List<Object> VALID_KEYS = Arrays.asList(USER, REPO, TOKEN, INCLUDE_AUTHOR, LABELS);
    private String user = "";
    private String repo = "";
    private String token = "";
    private boolean includeAuthor = false;
    private GithubLabelsConfig labels = new GithubLabelsConfig();

    public static GithubConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);
        GithubConfig githubConfig = new GithubConfig();

        githubConfig.user = (map.getOrDefault(USER, "").toString());
        if (githubConfig.user.isEmpty()) {
            throw new IllegalArgumentException("Missing 'user' in [github] config");
        }

        githubConfig.repo = (map.getOrDefault(REPO, "").toString());
        if (githubConfig.repo.isEmpty()) {
            throw new IllegalArgumentException("Missing 'repo' in [github] config");
        }

        githubConfig.token = (map.getOrDefault(TOKEN, "").toString());

        try {
            githubConfig.includeAuthor = (boolean) map.getOrDefault(INCLUDE_AUTHOR, githubConfig.includeAuthor);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("'%s' in [github] should be a boolean", INCLUDE_AUTHOR), e);
        }

        if (map.containsKey(LABELS)) {
            try {
                Map labelMap = (Map<String, Object>) map.get(LABELS);
                githubConfig.labels = GithubLabelsConfig.from(labelMap);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        String.format("Expected '%s' to be a section but found something else", LABELS), e);
            }
        }

        return githubConfig;
    }

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(String.format("Unknown config option '%s' in [github] section", key));
            }
        }
    }

    @Nonnull
    public String getUser() {
        return user;
    }

    @Nonnull
    public String getRepo() {
        return repo;
    }

    @Nonnull
    public String getToken() {
        return token;
    }

    public void setToken(@Nonnull String token) {
        this.token = token;
    }

    @Nonnull
    public GithubLabelsConfig getLabels() {
        return labels;
    }

    public boolean getIncludeAuthor() {
        return includeAuthor;
    }

    public boolean hasUserAndRepo() {
        return !user.isEmpty() && !repo.isEmpty();
    }
}
