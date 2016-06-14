package org.neo4j.changelog.git;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.changelog.Util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class GitHelperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private GitHelper gitHelper;

    @Before
    public void setup() throws IOException {
        gitHelper = new GitHelper(new File("./"));
    }

    @Test
    public void testGetTagShouldFailOutsideRepo() throws Exception {
        File file = tempFolder.newFolder();
        try {
            new GitHelper(file).getVersionTags();
            fail("Expected failure");
        } catch (RepositoryNotFoundException e) {
            assertTrue(e.getMessage().contains("repository not found"));
        }
    }

    @Test
    public void testGetTagShouldSucceedInRepo() throws Exception {
        List<Ref> tags = gitHelper.getVersionTags();
        assertTrue(tags.size() > 1);
        // either 1.2.3 or v1.2.3 is ok
        Pattern pattern = Pattern.compile("^v?[\\d\\.]+$");
        tags.stream().allMatch(ref -> pattern.asPredicate().test(ref.getName()));
    }

    @Test
    public void testGetVersionTags() throws Exception {
        List<String> tags = gitHelper.getVersionTags("0.0.0", "0.0.3").stream()
                                     .map(Util::getTagName)
                                     .collect(Collectors.toList());
        assertArrayEquals(tags.toArray(),
                new String[]{"0.0.0", "0.0.1", "0.0.2", "0.0.3", "v0.0.3"});
    }

    @Test
    public void testGetVersionTagsSubset() throws Exception {
        List<String> tags = gitHelper.getVersionTags("0.0.0", "0.0.2").stream()
                                     .map(Util::getTagName)
                                     .collect(Collectors.toList());
        assertArrayEquals(tags.toArray(),
                new String[]{"0.0.0", "0.0.1", "0.0.2"});
    }

    @Test
    public void shouldBeAncestor() throws Exception {
        assertTrue(gitHelper.isAncestorOf("627b3755c221bc4239238941c7fa38663c1e874f",
                "33cd4f62a27d5b67eef82b00be4c7d84df3a2fdd"));
    }

    @Test
    public void shouldBeAncestor2() throws Exception {
        assertTrue(gitHelper.isAncestorOf("627b3755c221bc4239238941c7fa38663c1e874f",
                "master"));
    }

    @Test
    public void shouldNotBeAncestor() throws Exception {
        assertFalse(gitHelper.isAncestorOf("737cfe197a30bef5a7f256fce26f518aabe9f6ee",
                "33cd4f62a27d5b67eef82b00be4c7d84df3a2fdd"));
    }

    @Test
    public void testGetCommitFromString() throws Exception {
        assertEquals("2bf464ebf41d5986bc18158ccdef87c5ba080198",
                gitHelper.getCommitFromString("0.0.3").getName());

        assertEquals("2bf464ebf41d5986bc18158ccdef87c5ba080198",
                gitHelper.getCommitFromString("v0.0.3").getName());

        assertEquals("a0562e22",
                gitHelper.getCommitFromString("0.0.2").abbreviate(8).name());

        assertEquals("78211c00",
                gitHelper.getCommitFromString("0.0.1").abbreviate(8).name());
    }

    @Test
    public void testGetFirstVersion() throws Exception {
        String fallback = "next";

        Repository repo = gitHelper.getRepo();

        List<Ref> tags = Arrays.asList(repo.findRef("0.0.1"), repo.findRef("0.0.2"),
                repo.findRef("0.0.3"));

        assertEquals(fallback,
                gitHelper.getFirstVersionOf("notarealcommit", tags, fallback));

        assertEquals("0.0.1",
                gitHelper.getFirstVersionOf("8449d265e41e0933731f49ba046af6965a2e6dac", tags, fallback));
        assertEquals("0.0.1",
                gitHelper.getFirstVersionOf("83018c673c0b85d0bbfb036f582ff4412b5ab173", tags, fallback));
        assertEquals("0.0.1",
                gitHelper.getFirstVersionOf("78211c0063dc9753212fce110ff3075178226dd2", tags, fallback));

        assertEquals("0.0.2",
                gitHelper.getFirstVersionOf("a0562e2272d51a423ca7e1189ccaee4cb179a30b", tags, fallback));

        assertEquals("0.0.3",
                gitHelper.getFirstVersionOf("627b3755c221bc4239238941c7fa38663c1e874f", tags, fallback));
        assertEquals("0.0.3",
                gitHelper.getFirstVersionOf("e246c65d948ef612cd0d2854d07b6231edcd663c", tags, fallback));
        assertEquals("0.0.3",
                gitHelper.getFirstVersionOf("2bf464ebf41d5986bc18158ccdef87c5ba080198", tags, fallback));
    }

    @Test
    public void isVersionTag() throws Exception {
        assertTrue(GitHelper.isVersionTag("1.3"));
        assertTrue(GitHelper.isVersionTag("2.0.0"));
        assertTrue(GitHelper.isVersionTag("3.0.0-M04"));
        assertTrue(GitHelper.isVersionTag("1.8.M07"));
        assertTrue(GitHelper.isVersionTag("2.1.0-RC2"));

        assertFalse(GitHelper.isVersionTag("bob"));
    }

    @Test
    public void shouldFindRoot() throws Exception {
        assertEquals("40d7de280bf5e534d0aba00a101e27ec17f1ed38",
                gitHelper.getRoot("test-A", "test-B").getName());

        assertEquals("40d7de280bf5e534d0aba00a101e27ec17f1ed38",
                gitHelper.getRoot("test-A", "test-C").getName());

        assertEquals("630609cce2b03d73ee6b271c4344beac36cbf01e",
                gitHelper.getRoot("test-B", "test-C").getName());
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotFindRoot1() throws Exception {
        assertEquals("40d7de280bf5e534d0aba00a101e27ec17f1ed38",
                gitHelper.getRoot("test-B", "test-A").getName());
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotFindRoot2() throws Exception {
        assertEquals("630609cce2b03d73ee6b271c4344beac36cbf01e",
                gitHelper.getRoot("test-C", "test-A").getName());
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotFindRoot3() throws Exception {
        assertEquals("630609cce2b03d73ee6b271c4344beac36cbf01e",
                gitHelper.getRoot("test-C", "test-B").getName());
    }
}
