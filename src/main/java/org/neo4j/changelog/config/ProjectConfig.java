package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProjectConfig {
    public static final String NAME = "name";
    public static final String OUTPUT = "output";
    public static final String NEXTHEADER = "nextheader";
    public static final String GIT = "git";
    public static final String GITHUB = "github";
    public static final String CATEGORIES = "categories";
    public static final String SUBPROJECTS = "subprojects";
    private static final List<String> VALID_KEYS =
            Arrays.asList(NAME, OUTPUT, NEXTHEADER, GIT, GITHUB, CATEGORIES, SUBPROJECTS);
    private static final String MISSING_SECTION_MSG = "Missing [%s] section";
    private static final String EXPECTED_SECTION_MSG = "Expected '%s' to be a section but found something else";
    private final List<ProjectConfig> subProjects = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();

    private String name = "";
    private String outputPath = "";
    private String nextHeader = "";
    private GitConfig gitConfig;
    private GithubConfig githubConfig;

    public ProjectConfig() {
        // Default values
        categories.addAll(Arrays.asList("Bug fixes", "Enchancements"));
    }

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

        config.name = map.getOrDefault(NAME, "").toString();
        config.outputPath = map.getOrDefault(OUTPUT, "CHANGELOG.md").toString();
        config.nextHeader = map.getOrDefault(NEXTHEADER, "Unreleased").toString();

        Map<String, Object> gitSection;
        try {
            gitSection = (Map<String, Object>) map.get(GIT);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXPECTED_SECTION_MSG, GIT), e);
        }
        try {
            config.gitConfig = GitConfig.from(gitSection);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(String.format(MISSING_SECTION_MSG, GIT));
        }

        Map<String, Object> githubSection;
        try {
            githubSection = (Map<String, Object>) map.get(GITHUB);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(EXPECTED_SECTION_MSG, GITHUB), e);
        }
        try {
            config.githubConfig = GithubConfig.from(githubSection);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(String.format(MISSING_SECTION_MSG, GITHUB));
        }

        if (map.containsKey(CATEGORIES)) {
            try {
                config.categories.clear();
                List list = (List) map.get(CATEGORIES);
                for (Object cat : list) {
                    config.categories.add(cat.toString());
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format("'%s' must be a list of strings", CATEGORIES), e);
            }
        }

        if (map.containsKey(SUBPROJECTS)) {
            Map<String, Object> subMap;

            try {
                subMap = (Map<String, Object>) map.get(SUBPROJECTS);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        String.format(EXPECTED_SECTION_MSG, SUBPROJECTS));
            }

            for (String key: subMap.keySet()) {
                try {
                    ProjectConfig subConfig = ProjectConfig.from((Map<String, Object>) subMap.get(key));
                    subConfig.githubConfig.setToken(config.getGithubConfig().getToken());
                    config.subProjects.add(subConfig);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("In [" + SUBPROJECTS + "." + key + "]\n" + e.getMessage());
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
