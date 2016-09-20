package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.regex.Pattern;

public class GitConfig {

    private String cloneDir = "";
    private String from = "";
    private String to = "";
    private Pattern tagPattern = Pattern.compile("");
    private String includedFrom = "";

    public static GitConfig from(@Nonnull Map<String, Object> map) {
        GitConfig gitConfig = new GitConfig();

        gitConfig.cloneDir = (map.getOrDefault("clone", "./").toString());
        gitConfig.from = (map.getOrDefault("from", "").toString());
        gitConfig.to = (map.get("to").toString());
        gitConfig.tagPattern = Pattern.compile(map.getOrDefault("tagpattern", "(.+)").toString());
        gitConfig.includedFrom = (map.getOrDefault("included_from", "").toString());

        return gitConfig;
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

    @Nonnull
    public String getIncludedFrom() {
        return includedFrom;
    }
}
