package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GitCommitConfig {

    private static final String SHA = "sha";
    private static final String TEXT = "text";
    private static final String VERSION_FILTER = "version_filter";
    private static final String CATEGORY = "category";
    private static final List<String> VALID_KEYS = Arrays.asList(SHA, TEXT, VERSION_FILTER, CATEGORY);
    private String sha;
    private String text = "";
    private String category = "";
    private List<String> versionFilter = new ArrayList<>();

    private GitCommitConfig() {

    }

    public static GitCommitConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);

        GitCommitConfig config = new GitCommitConfig();

        if (!map.containsKey(SHA)) {
            throw new IllegalArgumentException("A commit must specify a '" + SHA + "'");
        }
        config.sha = map.get(SHA).toString();

        config.text = map.getOrDefault(TEXT, config.text).toString();
        config.category = map.getOrDefault(CATEGORY, config.category).toString();

        if (map.containsKey(VERSION_FILTER)) {
            try {
                config.versionFilter.clear();
                config.versionFilter.addAll((List<String>) map.get(VERSION_FILTER));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format("'%s' must be a list of strings", VERSION_FILTER), e);
            }
        }

        return config;
    }

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(String.format("Unknown config option '%s' in [[commits]] entry", key));
            }
        }
    }

    @Nonnull
    public String getSha() {
        return sha;
    }

    @Nonnull
    public String getCategory() {
        return category;
    }

    @Nonnull
    public String getText() {
        return text;
    }

    @Nonnull
    public List<String> getVersionFilter() {
        return versionFilter;
    }
}
