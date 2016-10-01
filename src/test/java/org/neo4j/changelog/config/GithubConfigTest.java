package org.neo4j.changelog.config;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GithubConfigTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bahs' in [github] section");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bahs", "bobo");
        settings.put("user", "abc");
        settings.put("repo", "abc");

        GithubConfig.from(settings);
    }

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();
        minSettings.put("user", "jonas");
        minSettings.put("repo", "git");

        GithubConfig c = GithubConfig.from(minSettings);

        assertEquals("jonas", c.getUser());
        assertEquals("git", c.getRepo());
        assertEquals("", c.getToken());
        assertEquals(false, c.getIncludeAuthor());
    }

    @Test
    public void testSetToken() throws Exception {
        Map minSettings = new HashMap();
        minSettings.put("user", "jonas");
        minSettings.put("repo", "git");

        GithubConfig c = GithubConfig.from(minSettings);

        assertEquals("", c.getToken());
        c.setToken("bob");
        assertEquals("bob", c.getToken());
    }

    @Test
    public void testMissingUser() throws Exception {
        exception.expectMessage("Missing 'user' in [github] config");
        Map minSettings = new HashMap();
        minSettings.put("repo", "git");

        GithubConfig.from(minSettings);
    }

    @Test
    public void testMissingRepo() throws Exception {
        exception.expectMessage("Missing 'repo' in [github] config");
        Map minSettings = new HashMap();
        minSettings.put("user", "jonas");

        GithubConfig.from(minSettings);
    }
}
