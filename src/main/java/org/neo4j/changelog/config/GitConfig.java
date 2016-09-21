package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GitConfig {

    public static final String DEFAULT_TAG_PATTERN = "(\\d+\\.\\d+.*)";
    private static final List<String> VALID_KEYS = Arrays.asList("dir", "from", "to", "tagpattern");
    private String cloneDir = "";
    private String from = "";
    private String to = "";
    private Pattern tagPattern = Pattern.compile("");

    public static GitConfig from(@Nonnull Map<String, Object> map) {
        validateKeys(map);

        GitConfig gitConfig = new GitConfig();

        gitConfig.cloneDir = map.getOrDefault("dir", "./").toString();
        gitConfig.from = map.getOrDefault("from", "").toString();
        gitConfig.to = map.getOrDefault("to", "").toString();

        if (gitConfig.to.isEmpty()) {
            throw new IllegalArgumentException("Missing 'to' in [git] config");
        }

        gitConfig.tagPattern = Pattern.compile(map.getOrDefault("tagpattern", DEFAULT_TAG_PATTERN).toString());

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
