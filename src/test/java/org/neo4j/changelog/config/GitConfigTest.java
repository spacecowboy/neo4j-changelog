package org.neo4j.changelog.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GitConfigTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();

        GitConfig c = GitConfig.from(minSettings);

        assertEquals("HEAD", c.getTo());
        assertEquals("", c.getFrom());
        assertEquals("./", c.getCloneDir());
        assertEquals("", c.getCommitsFile());
        assertEquals(GitConfig.DEFAULT_TAG_PATTERN, c.getTagPattern().toString());
        assertEquals(Collections.EMPTY_LIST, c.getCommitsConfig().getCommits());
    }

    @Test
    public void testDefault() throws Exception {
        GitConfig c = new GitConfig();

        assertEquals("HEAD", c.getTo());
        assertEquals("", c.getFrom());
        assertEquals("./", c.getCloneDir());
        assertEquals("", c.getCommitsFile());
        assertEquals(GitConfig.DEFAULT_TAG_PATTERN, c.getTagPattern().toString());
        assertEquals(Collections.EMPTY_LIST, c.getCommitsConfig().getCommits());
    }

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bah' in [git] section");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bah", "bobo");
        settings.put("to", "abc");

        GitConfig.from(settings);
    }
}
