package org.neo4j.changelog.git;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;


public class GitHelperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
}
