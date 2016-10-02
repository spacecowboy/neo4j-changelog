package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GitCommitsConfig {


    public static final String COMMITS = "commits";
    private static final String INCLUDE_AUTHOR = "include_author";
    public static final String VERSION_PREFIX = "version_prefix";
    private static final List<String> VALID_KEYS = Arrays.asList(COMMITS, INCLUDE_AUTHOR, VERSION_PREFIX);
    private ArrayList<GitCommitConfig> commits = new ArrayList<>();
    private boolean includeAuthor = false;
    private String versionPrefix = "";

    public static GitCommitsConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);

        GitCommitsConfig config = new GitCommitsConfig();

        config.versionPrefix = map.getOrDefault(VERSION_PREFIX, config.versionPrefix).toString();
        try {
            config.includeAuthor = (boolean) map.getOrDefault(INCLUDE_AUTHOR, config.includeAuthor);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("'%s' in commits file should be a boolean", INCLUDE_AUTHOR), e);
        }

        if (map.containsKey(COMMITS)) {
            ArrayList<Map<String, Object>> commitList;
            try {
                commitList = (ArrayList<Map<String, Object>>) map.get(COMMITS);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format("'%s' must be a list of maps", COMMITS));
            }

            config.commits.clear();
            for (Map<String, Object> commitEntry: commitList) {
                try {
                    config.commits.add(GitCommitConfig.from(commitEntry));
                } catch (Exception e) {
                    throw new IllegalArgumentException("In a [[" + COMMITS + "]] entry:\n" + e.getMessage(), e);
                }
            }
        }

        return config;
    }

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(String.format("Unknown config option '%s' in commits file", key));
            }
        }
    }

    @Nonnull
    public List<GitCommitConfig> getCommits() {
        return commits;
    }

    public boolean getIncludeAuthor() {
        return includeAuthor;
    }

    @Nonnull
    public String getVersionPrefix() {
        return versionPrefix;
    }
}
