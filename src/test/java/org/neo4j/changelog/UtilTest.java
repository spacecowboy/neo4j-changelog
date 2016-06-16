package org.neo4j.changelog;

import org.junit.Test;

import static org.junit.Assert.*;


public class UtilTest {
    @Test
    public void semanticSortEquality() throws Exception {
        assertEquals(0, Util.SemanticCompare("1", "1"));
        assertEquals(0, Util.SemanticCompare("1.0", "1.0"));
        assertEquals(0, Util.SemanticCompare("1.0.0", "1.0.0"));
        assertEquals(0, Util.SemanticCompare("v1.0.0", "v1.0.0"));
        assertEquals(0, Util.SemanticCompare("v1.0.0", "1.0.0"));
    }

    @Test
    public void semanticSortLess() throws Exception {
        assertEquals(-1, Util.SemanticCompare("1.1", "1.2"));
        assertEquals(-1, Util.SemanticCompare("1.0.0", "1.0.1"));
        assertEquals(-1, Util.SemanticCompare("1.0.0", "1.0.10"));
        assertEquals(-1, Util.SemanticCompare("1.0.9", "1.1.0"));
        assertEquals(-1, Util.SemanticCompare("9.8.7", "10.0.0"));
        assertEquals(-1, Util.SemanticCompare("9.8.7", "9.10.0"));
        assertEquals(-1, Util.SemanticCompare("3.0.0-M08", "3.0.0-M09"));
        assertEquals(-1, Util.SemanticCompare("2.3.9", "3.0.0-M01"));
        assertEquals(-1, Util.SemanticCompare("3.0.0-M03", "3.0.0"));
        assertEquals(-1, Util.SemanticCompare("9.8.7-alpha", "9.8.7"));
        assertEquals(-1, Util.SemanticCompare("9.8.7", "9.10.0-beta.7"));
        assertEquals(-1, Util.SemanticCompare("1.0.0-alpha", "1.0.0-beta"));
        assertEquals(-1, Util.SemanticCompare("1.0.0-alpha", "1.0.0-alpha.1"));
        assertEquals(-1, Util.SemanticCompare("1.0.0-alpha.1", "1.0.0-alpha.10"));
    }

    @Test
    public void isSameMajorMinorVersion() throws Exception {
        assertTrue(Util.isSameMajorMinorVersion("1.1", "1.1"));
        assertTrue(Util.isSameMajorMinorVersion("1.1", "1.1.3"));
        assertTrue(Util.isSameMajorMinorVersion("1.1.2", "1.1"));
        assertTrue(Util.isSameMajorMinorVersion("3.1.0-M01", "3.1.0-M03"));
        assertTrue(Util.isSameMajorMinorVersion("3.1.0", "3.1.0-M03"));

        assertFalse(Util.isSameMajorMinorVersion("1", "1.1"));
        assertFalse(Util.isSameMajorMinorVersion("1.1", "1"));
        assertFalse(Util.isSameMajorMinorVersion("1", "1"));
    }

    @Test
    public void versionLiesBetween() {
        assertTrue(Util.versionLiesBetween("3.0", "2.0", "4.0"));
        assertTrue(Util.versionLiesBetween("3.0.3", "3.0.3", "3.0.3"));
        assertTrue(Util.versionLiesBetween("3.0.3", "3.0.1", "3.0.5"));
        assertTrue(Util.versionLiesBetween("3.1.3", "3.0.3", "3.2.3"));
        assertTrue(Util.versionLiesBetween("3.0.3", "2.0.3", "5.0.3"));


        assertFalse(Util.versionLiesBetween("3.0.3", "3.0.4", "3.0.4"));
        assertFalse(Util.versionLiesBetween("3.0.3", "3.0.4", "3.0.5"));
        assertFalse(Util.versionLiesBetween("3.0.3", "3.0.1", "3.0.2"));

        assertFalse(Util.versionLiesBetween("3.0.3", "3.1.3", "3.2.3"));
        assertFalse(Util.versionLiesBetween("3.2.3", "3.0.3", "3.1.3"));

        assertFalse(Util.versionLiesBetween("3.1.3", "4.1.3", "5.1.3"));
        assertFalse(Util.versionLiesBetween("4.1.3", "2.1.3", "3.1.3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void versionLiesBetweenFromMustBeLessOrEqualThanTo() {
        assertTrue(Util.versionLiesBetween("3.0.3", "3.0.4", "3.0.0"));
    }

    @Test
    public void nonSemanticVersionPlacedLast() {
        assertEquals(1, Util.SemanticCompare("Next Version", "99.99.99"));
        assertEquals(-1, Util.SemanticCompare("99.99.99", "Next Version"));

        assertEquals(1, Util.SemanticCompare("Next Version", "0.0.0"));
        assertEquals(-1, Util.SemanticCompare("0.0.0", "Next Version"));
    }

    @Test
    public void asSemanticVersion() {
        assertNotNull(Util.asSemanticVersion("1"));
        assertNotNull(Util.asSemanticVersion("1.2"));
        assertNotNull(Util.asSemanticVersion("1.2.3"));
        assertNotNull(Util.asSemanticVersion("1.2.3-M01"));
        assertNotNull(Util.asSemanticVersion("1.2.3-M01.5"));
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
