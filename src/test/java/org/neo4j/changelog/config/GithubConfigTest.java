package org.neo4j.changelog.config;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GithubConfigTest {

    @Test
    public void testMinimum() throws Exception {
        Map minSettings = new HashMap();
        minSettings.put("user", "jonas");
        minSettings.put("repo", "git");

        GithubConfig c = GithubConfig.from(minSettings);

        assertEquals("jonas", c.getUser());
        assertEquals("git", c.getRepo());
        assertEquals("", c.getRequiredLabels());
        assertEquals("", c.getToken());
    }
}
