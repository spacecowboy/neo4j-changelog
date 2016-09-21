package org.neo4j.changelog.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GitConfigTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();
        minSettings.put("to", "abc");

        GitConfig c = GitConfig.from(minSettings);

        assertEquals("abc", c.getTo());
        assertEquals("", c.getFrom());
        assertEquals("./", c.getCloneDir());
        assertEquals(GitConfig.DEFAULT_TAG_PATTERN, c.getTagPattern().toString());
    }

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bah' in [git] section");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bah", "bobo");
        settings.put("to", "abc");

        GitConfig.from(settings);
    }

    @Test
    public void testMissingTo() throws Exception {
        exception.expectMessage("Missing 'to' in [git] config");
        Map<String, Object> settings = new HashMap<>();
        GitConfig.from(settings);
    }
}
