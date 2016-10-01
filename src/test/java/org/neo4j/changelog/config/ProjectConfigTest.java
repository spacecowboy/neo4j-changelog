package org.neo4j.changelog.config;

import com.electronwill.toml.Toml;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ProjectConfigTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testUnknownKeyThrows() throws Exception {
        exception.expectMessage("Unknown config option 'bah'");

        StringBuilder tml = new StringBuilder()
                .append("[bah]\n")
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void testUnknownSubKeyThrows() throws Exception {
        exception.expectMessage("In [subprojects.woo]\nUnknown config option 'bah'");

        StringBuilder tml = new StringBuilder()
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n")
                .append("[subprojects.woo.bah]\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void testMinimum() throws Exception {
        StringBuilder tml = new StringBuilder()
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig c = ProjectConfig.from(Toml.read(tml.toString()));

        assertEquals("jonas", c.getGithubConfig().getUser());
        assertEquals("neo4j", c.getGithubConfig().getRepo());
        assertEquals("abc", c.getGitConfig().getTo());

        assertEquals("", c.getName());
        assertEquals("Unreleased", c.getNextHeader());
        assertEquals("CHANGELOG.md", c.getOutputPath());
        assertEquals(0, c.getCategories().size());
        assertEquals(0, c.getSubProjects().size());
    }

    @Test
    public void missingGitSection() throws Exception {
        exception.expectMessage("Missing [git] section");

        StringBuilder tml = new StringBuilder()
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void gitNotASection() throws Exception {
        exception.expectMessage("Expected 'git' to be a section but found something else");

        StringBuilder tml = new StringBuilder()
                .append("git = 'bob'\n")
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void missingGitHubSection() throws Exception {
        exception.expectMessage("Missing [github] section");

        StringBuilder tml = new StringBuilder()
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void gitHubNotASection() throws Exception {
        exception.expectMessage("Expected 'github' to be a section but found something else");

        StringBuilder tml = new StringBuilder()
                .append("github = 'bob'\n")
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void categoriesNotAList() throws Exception {
        exception.expectMessage("'categories' must be a list of strings");

        StringBuilder tml = new StringBuilder()
                .append("categories = 'justastring'\n")
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void testSubProjectsNotSection() throws Exception {
        exception.expectMessage("Expected 'subprojects' to be a section but found something else");

        StringBuilder tml = new StringBuilder()
                .append("subprojects = 1\n")
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }


    @Test
    public void testEmptySubProject() throws Exception {
        exception.expectMessage("In [subprojects.woho]\nMissing [git] section");

        StringBuilder tml = new StringBuilder()
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("[git]\n")
                .append("to = 'abc'\n")
                .append("[subprojects.woho]\n");

        ProjectConfig.from(Toml.read(tml.toString()));
    }

    @Test
    public void testTokenCarriesThroughToSubProjects() throws Exception {
        StringBuilder tml = new StringBuilder()
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("token = 'supertoken'\n")
                .append("[git]\n")
                .append("to = 'abc'\n")
                .append("[subprojects.woho]\n")
                .append("[subprojects.woho.git]\n")
                .append("to = 'subabc'\n")
                .append("[subprojects.woho.github]\n")
                .append("user = 'subuser'\n")
                .append("repo = 'subrepo'\n");

        ProjectConfig c = ProjectConfig.from(Toml.read(tml.toString()));

        assertEquals("jonas", c.getGithubConfig().getUser());
        assertEquals("neo4j", c.getGithubConfig().getRepo());
        assertEquals("abc", c.getGitConfig().getTo());

        assertEquals(1, c.getSubProjects().size());

        ProjectConfig sub = c.getSubProjects().get(0);

        assertEquals("subabc", sub.getGitConfig().getTo());
        assertEquals("supertoken", sub.getGithubConfig().getToken());
        assertEquals("subuser", sub.getGithubConfig().getUser());
        assertEquals("subrepo", sub.getGithubConfig().getRepo());
    }

    @Test
    public void testAll() throws Exception {
        StringBuilder tml = new StringBuilder()
                .append("name = 'neo4j'\n")
                .append("output = 'somefile'\n")
                .append("nextheader = 'hahaha'\n")
                .append("categories = ['one','two']\n")
                .append("[github]\n")
                .append("user = 'jonas'\n")
                .append("repo = 'neo4j'\n")
                .append("token = 'supertoken'\n")
                .append("[git]\n")
                .append("to = 'abc'\n")
                .append("[subprojects.woho]\n")
                .append("[subprojects.woho.git]\n")
                .append("to = 'subabc'\n")
                .append("[subprojects.woho.github]\n")
                .append("user = 'subuser'\n")
                .append("repo = 'subrepo'\n");

        ProjectConfig c = ProjectConfig.from(Toml.read(tml.toString()));

        assertEquals("jonas", c.getGithubConfig().getUser());
        assertEquals("neo4j", c.getGithubConfig().getRepo());
        assertEquals("abc", c.getGitConfig().getTo());

        assertEquals("neo4j", c.getName());
        assertEquals("hahaha", c.getNextHeader());
        assertEquals("somefile", c.getOutputPath());
        assertArrayEquals(new String[] {"one", "two"},
                c.getCategories().toArray());
        assertEquals(1, c.getSubProjects().size());

        ProjectConfig sub = c.getSubProjects().get(0);

        assertEquals("subabc", sub.getGitConfig().getTo());
        assertEquals("supertoken", sub.getGithubConfig().getToken());
        assertEquals("subuser", sub.getGithubConfig().getUser());
        assertEquals("subrepo", sub.getGithubConfig().getRepo());
    }
}
