package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Map;

public class GithubConfig {

    private String user = "";
    private String repo = "";
    private String token = "";
    private String requiredLabel = "";

    // TODO underscore or not?
    public static GithubConfig from(@Nonnull Map<String, Object> map) {
        GithubConfig githubConfig = new GithubConfig();

        githubConfig.user = (map.getOrDefault("user", "").toString());
        githubConfig.repo = (map.getOrDefault("repo", "").toString());
        githubConfig.token = (map.getOrDefault("token", "").toString());
        githubConfig.requiredLabel = (map.getOrDefault("required_label", "").toString());

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
    public String getRequiredLabel() {
        return requiredLabel;
    }
}
