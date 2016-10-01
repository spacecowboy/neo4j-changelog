package org.neo4j.changelog.github;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PRIssueTest {
    @Test
    public void addAuthor() throws Exception {
        PRIssue pr = getPrIssue(1243, "    This is the title    ", "   This is the body   ",
                "http://test.com/test", true);

        // Should have author appended at end
        assertEquals("This is the title [\\#1243](http://test.com/test) ([spacecowboy](http://space))",
                pr.addAuthor(pr.addLink(pr.title)));
    }

    @Test
    public void addLink() throws Exception {
        PRIssue pr = getPrIssue(1243, "    This is the title    ", "   This is the body   ",
                "http://test.com/test", false);

        // Should have link appended at end
        assertEquals("This is the title [\\#1243](http://test.com/test)", pr.addLink(pr.title));
    }

    @Test
    public void defaultValuesIfNoOverrides() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                        "balb lba",
                Arrays.asList("kernel", "cypher"), false);

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(Arrays.asList("kernel", "cypher"),
                pr.getLabelFilter());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextNoCL() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "body");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextNoCLWithAuthor() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "body", "http://test.com/link", true);

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link) ([spacecowboy](http://space))", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: [2.2, 2.3]  \n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogMessageOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "Message follows");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Message follows" + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogMessageOnNewLineWithAuthor() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "Message follows",
                "http://test.com/link", true);

        assertTrue(pr.getLabelFilter().isEmpty());
        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Message follows" + " [\\#1](http://test.com/link) ([spacecowboy](http://space))",
                pr.getChangeText());
    }

    @Test
    public void changeLogVersionOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "[2.2, 2.3]");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogVersionAndMessageOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "[2.2, 2.3]\n" +
                "Message follows");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertEquals("Message follows" + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersionsWithGHTags() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: [2.2, 2.3]  \n",
                Arrays.asList("kernel", "cypher"), false);

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertEquals(Arrays.asList("kernel", "cypher"),
                pr.getLabelFilter());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "cl: [2.2, 2.3  ]\n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void bahtest() throws Exception {
        String clFirstLine = "Created new github.labels section";
        String clRest = "   - Moved version_prefix and required to new section\n" +
                "   - exclude: a list of labels which define forbidden labels\n" +
                "   - include: a list of labels which denote labels to use for changelog inclusion\n" +
                "   - exclude_unlabeled: a boolean designating if unlabeled PRs can show up in the change log\n" +
                "   - github.labels.category_map: a section where you can define mappings from github labels to change log categories";
        String clText = clFirstLine + "\n" + clRest;
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: " + clText);

        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(clFirstLine + " [\\#1](http://test.com/link)\n" + clRest, pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersionsNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog [2.2, 2.3]  \n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersionsNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "cl [2.2, 2.3  ]\n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextDifferentCase() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL[ 2.2, 2.3 ]  \n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "cHanGeLoG :[ 2.2, 2.3  ]\n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "CHANGELOG[ 2.2, 2.3]  \n");

        assertEquals(Arrays.asList("2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColonEmptyBrackets() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL [] Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColonEmptyBracketsNoSpacing() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL[]Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Really tricky text: with colon [and] brackets \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Really tricky text: with colon [and] brackets [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBoth() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog: [2.1, kernel ,2.2, cypher, 2.3] My change text follows here\n");

        assertEquals(Arrays.asList("2.1", "2.2", "2.3"),
                pr.getVersionFilter());
        assertEquals(Arrays.asList("kernel", "cypher"),
                pr.getLabelFilter());
        assertEquals("My change text follows here [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBothWithTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog [2.1 ,2.2, 2.3]  Really tricky text: with [a] bracket\n");

        assertEquals(Arrays.asList("2.1", "2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Really tricky text: with [a] bracket [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLBoth() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "cl: [2.1 ,2.2, 2.3]My change text follows here\n");

        assertEquals(Arrays.asList("2.1", "2.2", "2.3"),
                pr.getVersionFilter());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("My change text follows here [\\#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void dontCatchCloses() throws Exception {
        PRIssue pr = getPrIssue(1, "pr title", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "closes #99\n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("pr title [\\#1](http://test.com/link)", pr.getChangeText());
    }

    private PRIssue getPrIssue(int number, String title, String body) {
        return getPrIssue(number, title, body, "http://test.com/link", false);
    }

    private PRIssue getPrIssue(int number, String title, String body, List<String> tags, boolean includeAuthor) {
        return getPrIssue(number, title, body, "http://test.com/link", tags, includeAuthor);
    }

    private PRIssue getPrIssue(int number, String title, String body, String html_url, boolean includeAuthor) {
        return getPrIssue(number, title, body, html_url, Collections.EMPTY_LIST, includeAuthor);
    }

    private PRIssue getPrIssue(int number, String title, String body, String html_url, List<String> tags, boolean includeAuthor) {
        return new PRIssue(number, title, body, html_url, "spacecowboy", "http://space", "", "", "", tags, includeAuthor);
    }
}
