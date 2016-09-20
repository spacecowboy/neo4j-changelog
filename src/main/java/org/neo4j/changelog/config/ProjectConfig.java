package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectConfig {
    private final List<SubProjectConfig> subProjects = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();

    private String outputPath = "";
    private String nextHeader = "";
    private GitConfig gitConfig;
    private GithubConfig githubConfig;

    // TODO underscore or not?
    static ProjectConfig from(@Nonnull Map<String, Object> map) {
        ProjectConfig config = new ProjectConfig();

        config.outputPath = map.getOrDefault("output", "").toString();
        config.nextHeader = map.getOrDefault("nextheader", "").toString();

        config.gitConfig = GitConfig.from((Map<String, Object>) map.getOrDefault("git", new GitConfig()));
        config.githubConfig = GithubConfig.from((Map<String, Object>) map.getOrDefault("github", new GithubConfig()));

        if (map.containsKey("categories")) {
            List list = (List) map.get("categories");
            for (Object cat: list) {
                config.categories.add(cat.toString());
            }
        }

        if (map.containsKey("subprojects")) {
            Map<String, Object> subMap = (Map<String, Object>) map.get("subprojects");

            for (String key: subMap.keySet()) {
                SubProjectConfig subConfig = SubProjectConfig.from((Map<String, Object>) subMap.get(key));
                subConfig.githubConfig.setToken(config.getGithubConfig().getToken());
                config.subProjects.add(subConfig);
            }
        }

        return config;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getNextHeader() {
        return nextHeader;
    }

    public GitConfig getGitConfig() {
        return gitConfig;
    }

    public GithubConfig getGithubConfig() {
        return githubConfig;
    }

    public List<SubProjectConfig> getSubProjects() {
        return subProjects;
    }

    public List<String> getCategories() {
        return categories;
    }
}
