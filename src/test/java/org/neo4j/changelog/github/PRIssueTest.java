package org.neo4j.changelog.github;

import org.junit.Test;

import static org.junit.Assert.*;


public class PRIssueTest {

    @Test
    public void getVersionAndChangeTextNoCL() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "body";

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals(pr.title, pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogVersions() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "changelog: 2.2, 2.3  \n";

        assertArrayEquals(new String[] {"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title, pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLVersions() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "cl: 2.2, 2.3  \n";

        assertArrayEquals(new String[] {"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title, pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextDifferentCase() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "CL  : 2.2, 2.3  \n";

        assertArrayEquals(new String[] {"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title, pr.getChangeText());

        pr = new PRIssue();
        pr.title = "";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "cHanGeLoG : 2.2, 2.3  \n";

        assertArrayEquals(new String[] {"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title, pr.getChangeText());

        pr = new PRIssue();
        pr.title = "";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "CHANGELOG: 2.2, 2.3  \n";

        assertArrayEquals(new String[] {"2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals(pr.title, pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLTitle() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "CL: Body text \n";

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Body text", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogTitle() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "changelog: Body text \n";

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Body text", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextTrickyTitle() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "title";
        pr.body = "Blab la\n" +
                "balb lba\n" +
                "CL: Really tricky text: with a colon \n";

        assertTrue(pr.getVersionFilter().isEmpty());
        assertEquals("Really tricky text: with a colon", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBoth() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "";
        pr.body = "";

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = new PRIssue();
        pr.title = "";
        pr.body = "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog: 2.1 ,2.2, 2.3 : My change text follows here\n";

        assertArrayEquals(new String[] {"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("My change text follows here", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextChangeLogBothWithTrickyTitle() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "";
        pr.body = "";

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = new PRIssue();
        pr.title = "";
        pr.body = "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "changelog: 2.1 ,2.2, 2.3 : Really tricky text: with a colon\n";

        assertArrayEquals(new String[] {"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("Really tricky text: with a colon", pr.getChangeText());
    }

    @Test
    public void getVersionAndChangeTextCLBoth() throws Exception {
        PRIssue pr = new PRIssue();
        pr.title = "";
        pr.body = "";

        assertTrue(pr.getVersionFilter().isEmpty());

        pr = new PRIssue();
        pr.title = "";
        pr.body = "Bla bla bla\n" +
                "blala bla\n" +
                "bal\n" +
                "cl: 2.1 ,2.2, 2.3 : My change text follows here\n";

        assertArrayEquals(new String[] {"2.1", "2.2", "2.3"},
                pr.getVersionFilter().toArray());
        assertEquals("My change text follows here", pr.getChangeText());
    }
}
