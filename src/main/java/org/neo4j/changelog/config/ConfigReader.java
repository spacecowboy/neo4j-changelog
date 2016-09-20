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
        return ProjectConfig.from(toml);
    }

    @Nonnull
    static Map<String, Object> readConfig(@Nullable String configPath) throws IOException {
        if (configPath == null || configPath.isEmpty()) {
            configPath = "changelog.toml";
        }
        File configFile = new File(configPath);
        if (!configFile.isFile()) {
            return Toml.read("", true);
        }
        return Toml.read(configFile);
    }
}
