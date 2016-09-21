package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProjectConfig {
    private static final List<String> VALID_KEYS =
            Arrays.asList("name", "output", "nextheader", "git", "github", "categories", "subprojects");
    private final List<ProjectConfig> subProjects = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();

    private String name = "";
    private String outputPath = "";
    private String nextHeader = "";
    private GitConfig gitConfig;
    private GithubConfig githubConfig;

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(String.format("Unknown config option '%s'", key));
            }
        }
    }

    static ProjectConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);
        ProjectConfig config = new ProjectConfig();

        config.name = map.getOrDefault("name", "").toString();
        config.outputPath = map.getOrDefault("output", "CHANGELOG.md").toString();
        config.nextHeader = map.getOrDefault("nextheader", "Unreleased").toString();

        Map<String, Object> gitSection;
        try {
            gitSection = (Map<String, Object>) map.get("git");
        } catch (ClassCastException e) {
            gitSection = null;
        }
        try {
            config.gitConfig = GitConfig.from(gitSection);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Missing [git] section");
        }

        Map<String, Object> githubSection;
        try {
            githubSection = (Map<String, Object>) map.get("github");
        } catch (ClassCastException e) {
            githubSection = null;
        }
        try {
            config.githubConfig = GithubConfig.from(githubSection);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Missing [github] section");
        }

        if (map.containsKey("categories")) {
            try {
                List list = (List) map.get("categories");
                for (Object cat : list) {
                    config.categories.add(cat.toString());
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("'categories' must be a list of strings");
            }
        }

        if (map.containsKey("subprojects")) {
            Map<String, Object> subMap;

            try {
                subMap = (Map<String, Object>) map.get("subprojects");
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("'subprojects' must be a section");
            }

            for (String key: subMap.keySet()) {
                try {
                    ProjectConfig subConfig = ProjectConfig.from((Map<String, Object>) subMap.get(key));
                    subConfig.githubConfig.setToken(config.getGithubConfig().getToken());
                    config.subProjects.add(subConfig);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("In [subprojects." + key + "]\n" + e.getMessage());
                }
            }
        }

        return config;
    }

    public String getName() {
        return name;
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

    public List<ProjectConfig> getSubProjects() {
        return subProjects;
    }

    public List<String> getCategories() {
        return categories;
    }
}
