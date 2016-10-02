package org.neo4j.changelog.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GitCommitConfigTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testRequired() throws Exception {
        exception.expectMessage("A commit must specify a 'sha'");
        GitCommitConfig.from(Collections.emptyMap());
    }

    @Test
    public void testDefaults() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();
        minSettings.put("sha", "abc");

        GitCommitConfig c = GitCommitConfig.from(minSettings);

        assertEquals("abc", c.getSha());
        assertEquals("", c.getCategory());
        assertEquals("", c.getText());
        assertEquals(Collections.EMPTY_LIST, c.getVersionFilter());
    }

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bah' in [[commits]] entry");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bah", "bobo");
        settings.put("to", "abc");

        GitCommitConfig.from(settings);
    }
}
