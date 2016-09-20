package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Map;

public class GithubConfig {

    private String user = "";
    private String repo = "";
    private String token = "";
    private String requiredLabels = "";
    private String versionPrefix = "";

    public static GithubConfig from(@Nonnull Map<String, Object> map) {
        GithubConfig githubConfig = new GithubConfig();

        githubConfig.user = (map.getOrDefault("user", "").toString());
        githubConfig.repo = (map.getOrDefault("repo", "").toString());
        githubConfig.token = (map.getOrDefault("token", "").toString());
        githubConfig.requiredLabels = (map.getOrDefault("requiredlabels", "").toString());
        githubConfig.versionPrefix = map.getOrDefault("versionprefix", "").toString();

        return githubConfig;
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
