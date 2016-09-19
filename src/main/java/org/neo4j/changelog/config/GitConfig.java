package org.neo4j.changelog.config;

import javax.annotation.Nonnull;
import java.util.Map;

public class GitConfig {

    private String cloneDir = "";
    private String from = "";
    private String to = "";
    private String tagPrefix = "";
    private String includedFrom = "";

    // TODO underscore or not?
    public static GitConfig from(@Nonnull Map<String, Object> map) {
        GitConfig gitConfig = new GitConfig();

        gitConfig.cloneDir = (map.getOrDefault("clone_dir", "").toString());
        gitConfig.from = (map.getOrDefault("from", "").toString());
        gitConfig.to = (map.getOrDefault("to", "").toString());
        gitConfig.tagPrefix = (map.getOrDefault("tagprefix", "").toString());
        gitConfig.includedFrom = (map.getOrDefault("included_from", "").toString());

        return gitConfig;
    }

    @Nonnull
    public String getCloneDir() {
        return cloneDir;
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
    public String getTagPrefix() {
        return tagPrefix;
    }

    @Nonnull
    public String getIncludedFrom() {
        return includedFrom;
    }
}
