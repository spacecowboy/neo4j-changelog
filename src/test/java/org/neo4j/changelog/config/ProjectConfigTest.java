package org.neo4j.changelog.config;

import org.junit.Test;

import java.util.Map;

import static org.neo4j.changelog.config.ConfigReader.readConfig;

public class ProjectConfigTest {

    @Test
    public void bah() throws Exception {
        Map<String, Object> toml = readConfig("/home/jonas/workspace/neo4j-changelog/changelog.toml");
        ProjectConfig config = ProjectConfig.from(toml);

        for (SubProjectConfig subConfig: config.getSubProjects()) {
            System.out.println(subConfig);
        }
    }
}
