package org.neo4j.changelog.git;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;


public class GitHelperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private Repository repo;

    @Before
    public void setup() throws IOException {
        repo = GitHelper.getGit(new File("./")).getRepository();
    }

    @Test
    public void testGetTagShouldFailOutsideRepo() throws Exception {
        File file = tempFolder.newFolder();
        try {
            GitHelper.getVersionTags(file);
            fail("Expected failure");
        } catch (RepositoryNotFoundException e) {
            assertTrue(e.getMessage().contains("repository not found"));
        }
    }

    @Test
    public void testGetTagShouldSucceedInRepo() throws Exception {
        File file = new File("./");
        List<Ref> tags = GitHelper.getVersionTags(file);
        assertTrue(tags.size() > 1);
        // either 1.2.3 or v1.2.3 is ok
        Pattern pattern = Pattern.compile("^v?[\\d\\.]+$");
        tags.stream().allMatch(ref -> pattern.asPredicate().test(ref.getName()));
    }

    @Test
    public void shouldBeAncestor() throws Exception {
        assertTrue(GitHelper.isAncestorOf(repo, "627b3755c221bc4239238941c7fa38663c1e874f",
                "33cd4f62a27d5b67eef82b00be4c7d84df3a2fdd"));
    }

    @Test
    public void shouldBeAncestor2() throws Exception {
        assertTrue(GitHelper.isAncestorOf(repo, "627b3755c221bc4239238941c7fa38663c1e874f",
                "master"));
    }

    @Test
    public void shouldNotBeAncestor() throws Exception {
        assertFalse(GitHelper.isAncestorOf(repo, "737cfe197a30bef5a7f256fce26f518aabe9f6ee",
                "33cd4f62a27d5b67eef82b00be4c7d84df3a2fdd"));
    }

    @Test
    public void testGetCommitFromString() throws Exception {
        assertEquals("2bf464ebf41d5986bc18158ccdef87c5ba080198",
                GitHelper.getCommitFromString(repo, "0.0.3").getName());

        assertEquals("2bf464ebf41d5986bc18158ccdef87c5ba080198",
                GitHelper.getCommitFromString(repo, "v0.0.3").getName());

        assertEquals("a0562e22",
                GitHelper.getCommitFromString(repo, "0.0.2").abbreviate(8).name());

        assertEquals("78211c00",
                GitHelper.getCommitFromString(repo, "0.0.1").abbreviate(8).name());
    }

    @Test
    public void testGetFirstVersion() throws Exception {
        String fallback = "next";

        List<Ref> tags = Arrays.asList(repo.findRef("0.0.1"), repo.findRef("0.0.2"),
                repo.findRef("0.0.3"));

        assertEquals(fallback,
                GitHelper.getFirstVersionOf(repo, "notarealcommit", tags, fallback));

        assertEquals("0.0.1",
                GitHelper.getFirstVersionOf(repo, "8449d265e41e0933731f49ba046af6965a2e6dac", tags, fallback));
        assertEquals("0.0.1",
                GitHelper.getFirstVersionOf(repo, "83018c673c0b85d0bbfb036f582ff4412b5ab173", tags, fallback));
        assertEquals("0.0.1",
                GitHelper.getFirstVersionOf(repo, "78211c0063dc9753212fce110ff3075178226dd2", tags, fallback));

        assertEquals("0.0.2",
                GitHelper.getFirstVersionOf(repo, "a0562e2272d51a423ca7e1189ccaee4cb179a30b", tags, fallback));

        assertEquals("0.0.3",
                GitHelper.getFirstVersionOf(repo, "627b3755c221bc4239238941c7fa38663c1e874f", tags, fallback));
        assertEquals("0.0.3",
                GitHelper.getFirstVersionOf(repo, "e246c65d948ef612cd0d2854d07b6231edcd663c", tags, fallback));
        assertEquals("0.0.3",
                GitHelper.getFirstVersionOf(repo, "2bf464ebf41d5986bc18158ccdef87c5ba080198", tags, fallback));
    }
}
