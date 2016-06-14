package org.neo4j.changelog;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.StringWriter;
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
        ChangeLog cl = new ChangeLog(Arrays.asList(new Ref() {
            @Override
            public String getName() {
                return "1.0.0";
            }

            @Override
            public boolean isSymbolic() {
                return false;
            }

            @Override
            public Ref getLeaf() {
                return null;
            }

            @Override
            public Ref getTarget() {
                return null;
            }

            @Override
            public ObjectId getObjectId() {
                return null;
            }

            @Override
            public ObjectId getPeeledObjectId() {
                return null;
            }

            @Override
            public boolean isPeeled() {
                return false;
            }

            @Override
            public Storage getStorage() {
                return null;
            }
        }),
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
                return "1.0.0";
            }

            @Override
            public String toString() {
                return changeText;
            }
        };
    }

    private static final String SIMPLE_CHANGELOG =
            "### 1.0.0\n" +
            "#### Kernel\n" +
            "- Add a kernel [#1]\n" +
            "- Fix the kernel [#3]\n" +
            "#### Cypher\n" +
            "- Add a query language [#4]\n" +
            "#### Misc\n" +
            "- Added a changelog [#2]\n";
}
