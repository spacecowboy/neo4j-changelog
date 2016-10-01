package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubLabelsConfig {

    public static final String VERSION_PREFIX = "version_prefix";
    public static final String REQUIRED = "required";
    public static final String EXCLUDE = "exclude";
    public static final String INCLUDE = "include";
    public static final String EXCLUDE_UNLABELED = "exclude_unlabeled";
    public static final String CATEGORY_MAP = "category_map";
    private static final List<Object> VALID_KEYS = Arrays.asList(REQUIRED, VERSION_PREFIX, EXCLUDE, INCLUDE,
            EXCLUDE_UNLABELED, CATEGORY_MAP);
    private static final String MUST_BE_STRING_LIST_MSG = "'%s' must be a list of strings";
    private String required = "";
    private String versionPrefix = "";
    private List<String> exclude = new ArrayList<>();
    private List<String> include = new ArrayList<>();
    private Map<String, String> categoryMap = new HashMap<>();
    private boolean excludeUnlabeled = false;

    public GithubLabelsConfig() {
        // Default values
        exclude.addAll(Arrays.asList("question", "duplicate", "invalid", "wontfix"));
        categoryMap.put("bug", "Bug fixes");
        categoryMap.put("enhancement", "Enhancements");
    }

    public static GithubLabelsConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);
        GithubLabelsConfig config = new GithubLabelsConfig();

        config.required = (map.getOrDefault(REQUIRED, "").toString());
        config.versionPrefix = map.getOrDefault(VERSION_PREFIX, "").toString();
        try {
            config.excludeUnlabeled = (Boolean) map.getOrDefault(EXCLUDE_UNLABELED, config.excludeUnlabeled);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("'%s' must be a boolean", EXCLUDE_UNLABELED), e);
        }

        if (map.containsKey(EXCLUDE)) {
            try {
                config.exclude.clear();
                List list = (List) map.get(EXCLUDE);
                for (Object val: list) {
                    config.exclude.add(val.toString());
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format(MUST_BE_STRING_LIST_MSG, EXCLUDE), e);
            }
        }

        if (map.containsKey(INCLUDE)) {
            try {
                config.include.clear();
                List list = (List) map.get(INCLUDE);
                for (Object val: list) {
                    config.include.add(val.toString());
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format(MUST_BE_STRING_LIST_MSG, INCLUDE), e);
            }
        }

        if (map.containsKey(CATEGORY_MAP)) {
            try {
                config.categoryMap.clear();
                Map catMap = (Map) map.get(CATEGORY_MAP);
                for (Object key : catMap.keySet()) {
                    config.categoryMap.put(key.toString(), catMap.get(key).toString());
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format("'%s' must be map of strings to strings",
                        CATEGORY_MAP), e);
            }
        }

        return config;
    }

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(
                        String.format("Unknown config option '%s' in [github.labels] section", key));
            }
        }
    }

    @Nonnull
    public String getRequired() {
        return required;
    }

    @Nonnull
    public String getVersionPrefix() {
        return versionPrefix;
    }

    @Nonnull
    public List<String> getInclude() {
        return include;
    }

    @Nonnull
    public List<String> getExclude() {
        return exclude;
    }

    public boolean getExcludeUnlabeled() {
        return excludeUnlabeled;
    }

    @Nonnull
    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }
}
