package org.neo4j.changelog;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class ChangeLogTest {

    @Test
    public void simpleChangeLogGeneration() throws Exception {
        ChangeLog cl = simpleChangeLog();

        StringWriter sw = new StringWriter();

        cl.writeTo(sw);

        assertEquals(SIMPLE_CHANGELOG, sw.toString());
    }

    private ChangeLog simpleChangeLog() {
        ChangeLog cl = new ChangeLog(new ArrayList<>(),
                Arrays.asList("Kernel", "Cypher"));

        cl.addToChangeLog(simpleChange("Kernel", "Fix the kernel [#3]"));
        cl.addToChangeLog(simpleChange("Changelog", "Added a changelog [#2]"));
        cl.addToChangeLog(simpleChange("Cypher", "Add a query language [#4]"));
        cl.addToChangeLog(simpleChange("Kernel", "Add a kernel [#1]"));

        return cl;
    }

    private Change simpleChange(@Nonnull String label, @Nonnull String changeText) {
        return new Change() {
            @Nonnull
            @Override
            public List<String> getLabels() {
                return Arrays.asList(label);
            }

            @Nonnull
            @Override
            public String getVersion() {
                return "0.0.0";
            }

            @Override
            public String toString() {
                return changeText;
            }
        };
    }

    private static final String SIMPLE_CHANGELOG =
            "### 1.0\n" +
            "#### Kernel\n" +
            "- Add a kernel [#1]\n" +
            "- Fix the kernel [#3]\n" +
            "#### Cypher\n" +
            "- Add a query language [#4]\n" +
            "#### Misc\n" +
            "- Added a changelog [#2]\n";
}
