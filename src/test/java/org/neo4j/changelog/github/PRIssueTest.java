package org.neo4j.changelog.github;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class PRIssueTest {
    @Test
    public void addLink() throws Exception {
        PRIssue pr = getPrIssue(1243, "    This is the title    ", "   This is the body   ",
                "http://test.com/test");

        // Should have link appended at end
        assertEquals("This is the title [#1243](http://test.com/test)", pr.addLink(pr.title));
    }

    @Test
    public void defaultValuesIfNoOverrides() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                        "balb lba",
                Arrays.asList("kernel", "cypher"));

        assertTrue(pr.getVersionFilter().isEmpty());
        assertArrayEquals(new String[]{"kernel", "cypher"},
                pr.getLabelFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextNoCL() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "body");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: [2.2, 2.3]  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogMessageOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "Message follows");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Message follows" + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogMessageOnNewLineOnlyFirstLineShouldBeUsed() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "Message follows\n" +
                "This piece should be ignored");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Message follows" + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogVersionOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "[2.2, 2.3]");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void changeLogVersionAndMessageOnNewLine() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog:\n" +
                "[2.2, 2.3]\n" +
                "Message follows");

        assertTrue(pr.getLabelFilter().isEmpty());
        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("Message follows" + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersionsWithGHTags() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: [2.2, 2.3]  \n",
                Arrays.asList("kernel", "cypher"));

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertArrayEquals(new String[]{"kernel", "cypher"},
                pr.getLabelFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "cl: [2.2, 2.3  ]\n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersionsNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog [2.2, 2.3]  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersionsNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "cl [2.2, 2.3  ]\n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextDifferentCase() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL[ 2.2, 2.3 ]  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "cHanGeLoG :[ 2.2, 2.3  ]\n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "CHANGELOG[ 2.2, 2.3]  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColon() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColonEmptyBrackets() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL [] Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitleNoColonEmptyBracketsNoSpacing() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL[]Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Really tricky text: with colon [and] brackets \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Really tricky text: with colon [and] brackets [#1](http://test.com/link)", pr.getChangeText());
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

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertArrayEquals(new String[]{"kernel", "cypher"},
                pr.getLabelFilter().toArray());
        assertEquals("My change text follows here [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBothWithTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog [2.1 ,2.2, 2.3]  Really tricky text: with [a] bracket\n");

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("Really tricky text: with [a] bracket [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLBoth() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "cl: [2.1 ,2.2, 2.3]My change text follows here\n");

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("My change text follows here [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void dontCatchCloses() throws Exception {
        PRIssue pr = getPrIssue(1, "pr title", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "closes #99\n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertTrue(pr.getLabelFilter().isEmpty());
        assertEquals("pr title [#1](http://test.com/link)", pr.getChangeText());
    }

    private PRIssue getPrIssue(int number, String title, String body) {
        return getPrIssue(number, title, body, "http://test.com/link");
    }

    private PRIssue getPrIssue(int number, String title, String body, List<String> tags) {
        return getPrIssue(number, title, body, "http://test.com/link", tags);
    }

    private PRIssue getPrIssue(int number, String title, String body, String html_url) {
        return getPrIssue(number, title, body, html_url, new ArrayList<>());
    }

    private PRIssue getPrIssue(int number, String title, String body, String html_url, List<String> tags) {
        return new PRIssue(number, title, body, html_url, "", "", "", tags);
    }
}
