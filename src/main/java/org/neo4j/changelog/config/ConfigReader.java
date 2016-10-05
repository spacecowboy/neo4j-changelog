package org.neo4j.changelog.config;

import com.electronwill.toml.Toml;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Deal with config related things
 */
public class ConfigReader {

    public static ProjectConfig parseConfig(@Nonnull String configPath) throws IOException {
        if (configPath.isEmpty()) {
            configPath = "changelog.toml";
        }
        Map<String, Object> toml = readConfig(configPath);
        ProjectConfig config = ProjectConfig.from(toml);

        if (!config.getGitConfig().getCommitsFile().isEmpty()) {
            Map<String, Object> commitToml = readConfig(config.getGitConfig().getCommitsFile());
            config.getGitConfig().setCommitsConfig(GitCommitsConfig.from(commitToml));
        }
        return config;
    }

    @Nonnull
    static Map<String, Object> readConfig(@Nonnull String configPath) throws IOException {
        final InputStream stream;
        if (configPath.startsWith("http://") || configPath.startsWith("https://")) {
            URL configUrl = new URL(configPath);
            stream = configUrl.openStream();
        } else {
            stream = new FileInputStream(configPath);
        }

        try {
            return Toml.read(stream);
        } finally {
            stream.close();
        }
    }
}
