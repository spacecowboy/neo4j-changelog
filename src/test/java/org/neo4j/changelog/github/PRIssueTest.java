package org.neo4j.changelog.github;

import org.junit.Test;

import java.util.ArrayList;

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
    public void getVersionAndChangeTextNoCL() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "body");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: 2.2, 2.3  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersions() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "cl: 2.2, 2.3  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextDifferentCase() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL  : 2.2, 2.3  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "cHanGeLoG : 2.2, 2.3  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());

        pr = getPrIssue(1, "", "Blab la\n" +
                "balb lba\n" +
                "CHANGELOG: 2.2, 2.3  \n");

        assertArrayEquals(new String[]{"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title + " [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "changelog: Body text \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Body text [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "title", "Blab la\n" +
                "balb lba\n" +
                "CL: Really tricky text: with a colon \n");

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Really tricky text: with a colon [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBoth() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog: 2.1 ,2.2, 2.3 : My change text follows here\n");

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("My change text follows here [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBothWithTrickyTitle() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog: 2.1 ,2.2, 2.3 : Really tricky text: with a colon\n");

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("Really tricky text: with a colon [#1](http://test.com/link)", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLBoth() throws Exception {
        PRIssue pr = getPrIssue(1, "", "");

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = getPrIssue(1, "", "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "cl: 2.1 ,2.2, 2.3 : My change text follows here\n");

        assertArrayEquals(new String[]{"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("My change text follows here [#1](http://test.com/link)", pr.getChangeText());
    }

    private PRIssue getPrIssue(int number, String title, String body) {
        return getPrIssue(number, title, body, "http://test.com/link");
    }

    private PRIssue getPrIssue(int number, String title, String body, String html_url) {
        return new PRIssue(number, title, body, html_url, "", "", "", new ArrayList<>());
    }
}
