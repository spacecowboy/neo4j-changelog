package org.neo4j.changelog.config;


import javax.annotation.Nonnull;
import java.util.Map;

public class SubProjectConfig {
    String name = "";
    GitConfig gitConfig;
    GithubConfig githubConfig;

    // TODO underscore or not?
    public static SubProjectConfig from(@Nonnull Map<String, Object> map) {
        SubProjectConfig config = new SubProjectConfig();

        config.name = map.getOrDefault("name", "").toString();
        config.gitConfig = GitConfig.from((Map<String, Object>) map.getOrDefault("git", new GitConfig()));
        config.githubConfig = GithubConfig.from((Map<String, Object>) map.getOrDefault("github", new GithubConfig()));

        return config;
    }

    public String getName() {
        return name;
    }

    public GitConfig getGitConfig() {
        return gitConfig;
    }

    public GithubConfig getGithubConfig() {
        return githubConfig;
    }
}
