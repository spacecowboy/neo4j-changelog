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
}
