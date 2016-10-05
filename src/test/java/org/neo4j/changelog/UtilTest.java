package org.neo4j.changelog;

import org.junit.Test;

import static org.junit.Assert.*;


public class UtilTest {

    @Test
    public void asSemanticVersion() {
        assertNotNull(Util.asSemanticVersion("1"));
        assertNotNull(Util.asSemanticVersion("1.2"));
        assertNotNull(Util.asSemanticVersion("1.2.3"));
        assertNotNull(Util.asSemanticVersion("1.2.3-M01"));
        assertNotNull(Util.asSemanticVersion("1.2.3-M01.5"));
    }

    @Test
    public void bah() {
        assertTrue(Util.isSemanticVersion("3.1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void asSemanticVersionFail1() {
        Util.asSemanticVersion("1.2.3-M01.5.6");
    }

    @Test(expected = IllegalArgumentException.class)
    public void asSemanticVersionFail2() {
        Util.asSemanticVersion("Bob");
    }
}
