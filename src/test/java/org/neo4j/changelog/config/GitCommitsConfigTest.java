package org.neo4j.changelog.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GitCommitsConfigTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();

        GitCommitsConfig c = GitCommitsConfig.from(minSettings);

        assertEquals(Collections.EMPTY_LIST, c.getCommits());
        assertEquals(false, c.getIncludeAuthor());
        assertEquals("", c.getVersionPrefix());
    }

    @Test
    public void testDefault() throws Exception {
        GitCommitsConfig c = new GitCommitsConfig();

        assertEquals(Collections.EMPTY_LIST, c.getCommits());
        assertEquals(false, c.getIncludeAuthor());
        assertEquals("", c.getVersionPrefix());
    }

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bah' in commits file");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bah", "bobo");
        settings.put("to", "abc");

        GitCommitsConfig.from(settings);
    }
}
