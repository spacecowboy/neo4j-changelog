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
    public void testDefault() throws Exception {
        GithubConfig c = new GithubConfig();

        assertEquals("", c.getUser());
        assertEquals("", c.getRepo());
        assertEquals("", c.getToken());
        assertEquals(false, c.getIncludeAuthor());
    }

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();

        GithubConfig c = GithubConfig.from(minSettings);

        assertEquals("", c.getUser());
        assertEquals("", c.getRepo());
        assertEquals("", c.getToken());
        assertEquals(false, c.getIncludeAuthor());
    }

    @Test
    public void testSetToken() throws Exception {
        Map minSettings = new HashMap();

        GithubConfig c = GithubConfig.from(minSettings);

        assertEquals("", c.getToken());
        c.setToken("bob");
        assertEquals("bob", c.getToken());
    }
}
