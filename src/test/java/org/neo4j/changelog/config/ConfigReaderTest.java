package org.neo4j.changelog.config;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ConfigReaderTest {
    @Test
    public void readConfigWithUrl() throws Exception {
        Map<String, Object> config =
                ConfigReader.readConfig("https://raw.githubusercontent.com/spacecowboy/NoNonsense-FilePicker/master/commits.toml");

        assertTrue(config.containsKey(GitCommitsConfig.COMMITS));
    }

}
