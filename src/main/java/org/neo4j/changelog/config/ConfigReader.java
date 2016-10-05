package org.neo4j.changelog.config;

import com.electronwill.toml.Toml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Deal with config related things
 */
public class ConfigReader {

    public static ProjectConfig parseConfig(@Nonnull String configPath) throws IOException {
        Map<String, Object> toml = readConfig(configPath);
        ProjectConfig config = ProjectConfig.from(toml);

        File commitsFile = new File(config.getGitConfig().getCommitsFile());
        if (commitsFile.isFile() && commitsFile.canRead()) {
            Map<String, Object> commitToml = readConfig(commitsFile.getPath());
            config.getGitConfig().setCommitsConfig(GitCommitsConfig.from(commitToml));
        }
        return config;
    }

    @Nonnull
    private static Map<String, Object> readConfig(@Nullable String configPath) throws IOException {
        if (configPath == null || configPath.isEmpty()) {
            configPath = "changelog.toml";
        }
        File configFile = new File(configPath);
        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Could not read '" + configFile.getAbsolutePath() + "'");
        }
        return Toml.read(configFile);
    }
}
