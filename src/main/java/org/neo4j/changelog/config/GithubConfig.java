package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GithubConfig {

    private static final List<Object> VALID_KEYS = Arrays.asList("user", "repo", "token", "requiredlabels", "versionprefix");
    private String user = "";
    private String repo = "";
    private String token = "";
    private String requiredLabels = "";
    private String versionPrefix = "";

    public static GithubConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);
        GithubConfig githubConfig = new GithubConfig();

        githubConfig.user = (map.getOrDefault("user", "").toString());
        if (githubConfig.user.isEmpty()) {
            throw new IllegalArgumentException("Missing 'user' in [github] config");
        }

        githubConfig.repo = (map.getOrDefault("repo", "").toString());
        if (githubConfig.repo.isEmpty()) {
            throw new IllegalArgumentException("Missing 'repo' in [github] config");
        }

        githubConfig.token = (map.getOrDefault("token", "").toString());
        githubConfig.requiredLabels = (map.getOrDefault("requiredlabels", "").toString());
        githubConfig.versionPrefix = map.getOrDefault("versionprefix", "").toString();

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

    @Nonnull
    public String getRequiredLabels() {
        return requiredLabels;
    }

    @Nonnull
    public String getVersionPrefix() {
        return versionPrefix;
    }

    public void setToken(@Nonnull String token) {
        this.token = token;
    }
}
