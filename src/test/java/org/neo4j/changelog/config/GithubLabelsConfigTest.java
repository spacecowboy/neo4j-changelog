package org.neo4j.changelog.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GithubLabelsConfigTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bahs' in [github.labels] section");

        Map<String, Object> settings = new HashMap<>();

        settings.put("bahs", "bobo");
        settings.put("user", "abc");
        settings.put("repo", "abc");

        GithubLabelsConfig.from(settings);
    }

    @Test
    public void testMinimum() throws Exception {
        Map<String, Object> minSettings = new HashMap<>();

        GithubLabelsConfig c = GithubLabelsConfig.from(minSettings);

        assertEquals(false, c.getExcludeUnlabeled());
        assertEquals("", c.getVersionPrefix());
        assertEquals("", c.getRequired());
        assertEquals(Arrays.asList("question", "duplicate", "invalid", "wontfix"),
                c.getExclude());
        assertTrue(c.getInclude().isEmpty());

        HashMap<String, String> expectedCatMap = new HashMap<>();
        expectedCatMap.put("bug", "Bug fixes");
        expectedCatMap.put("enhancement", "Enhancements");
        assertEquals(expectedCatMap, c.getCategoryMap());
    }
}
