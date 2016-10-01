package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GitConfig {

    public static final String DEFAULT_TAG_PATTERN = "(\\d+\\.\\d+.*)";
    public static final String TAG_PATTERN = "tag_pattern";
    public static final String TO = "to";
    public static final String FROM = "from";
    public static final String DIR = "dir";
    private static final List<String> VALID_KEYS = Arrays.asList(DIR, FROM, TO, TAG_PATTERN);
    private String cloneDir = "./";
    private String from = "";
    private String to = "HEAD";
    private Pattern tagPattern = Pattern.compile(DEFAULT_TAG_PATTERN);

    public static GitConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);

        GitConfig gitConfig = new GitConfig();

        gitConfig.cloneDir = map.getOrDefault(DIR, gitConfig.cloneDir).toString();
        gitConfig.from = map.getOrDefault(FROM, gitConfig.from).toString();
        gitConfig.to = map.getOrDefault(TO, gitConfig.to).toString();
        gitConfig.tagPattern = Pattern.compile(map.getOrDefault(TAG_PATTERN, DEFAULT_TAG_PATTERN).toString());

        return gitConfig;
    }

    private static void validateKeys(Map<String, Object> map) {
        for (String key: map.keySet()) {
            if (!VALID_KEYS.contains(key)) {
                throw new IllegalArgumentException(String.format("Unknown config option '%s' in [git] section", key));
            }
        }
    }

    @Nonnull
    public String getCloneDir() {
        return cloneDir.replaceFirst("^~", System.getProperty("user.home"));
    }

    @Nonnull
    public String getFrom() {
        return from;
    }

    @Nonnull
    public String getTo() {
        return to;
    }

    @Nonnull
    public Pattern getTagPattern() {
        return tagPattern;
    }
}
